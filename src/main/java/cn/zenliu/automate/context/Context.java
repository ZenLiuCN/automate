package cn.zenliu.automate.context;

import cn.zenliu.automate.action.Action;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.typesafe.config.ConfigFactory;
import lombok.SneakyThrows;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface Context extends AutoCloseable {

    Logger log();

    Map<String, AutoCloseable> closable();

    ConcurrentLinkedQueue<AutoCloseable> closableQueue();

    @Override
    default void close() throws Exception {
        var q = closableQueue();
        var c = q.poll();
        while (c != null) {
            if (c instanceof Playwright) continue;
            if (c instanceof Browser) continue;
            c.close();
            c = q.poll();
        }
    }

    Map<String, Object> vars();

    /**
     * put a context variable
     *
     * @param name  the name
     * @param value the value
     * @return success (false if already exists)
     */
    default boolean put(String name, Object value) {
        var m = vars();
        if (m.containsKey(name)) return false;
        m.put(name, value);
        if (value instanceof AutoCloseable a) {
            closable().put(name, a);
            closableQueue().add(a);
        }
        return true;
    }

    /**
     * remove variable, if variable value is an AutoCloseable also close it.
     *
     * @param name the variable name
     * @return true if success
     */
    @SneakyThrows
    default boolean invalidate(String name) {
        var m = vars();
        if (m.containsKey(name)) {
            var v = m.remove(name);
            if (v instanceof AutoCloseable a) {
                closable().remove(name);
                closableQueue().removeIf(x -> x == a);
                a.close();
            }
            return true;
        }
        return false;
    }

    /**
     * @param name the variable name
     * @return value or empty
     */
    default Optional<Object> var(String name) {
        return Optional.ofNullable(vars().get(name));
    }

    /**
     * fetch a typed context variable
     *
     * @param name the context var
     * @param type the class
     * @return Optional
     */
    default <T> Optional<T> var(String name, Class<T> type) {
        return var(name).map(x -> type.isInstance(x) ? type.cast(x) : null);
    }

    default <T> T require(String name, Class<T> type) {
        return var(name, type).orElseThrow(() -> new IllegalStateException("missing required '" + name + "' of " + type));
    }

    default void require(String name) {
        if (!vars().containsKey(name)) {
            throw new IllegalStateException(name + " required, but not exists.");
        }
    }

    default void requireNot(String name) {
        if (vars().containsKey(name)) {
            throw new IllegalStateException(name + " already exists.");
        }
    }


    record context(
            Logger log,
            Map<String, AutoCloseable> closable,
            ConcurrentLinkedQueue<AutoCloseable> closableQueue,
            Map<String, Object> vars
    ) implements Context {
        context(Logger log) {
            this(log, new ConcurrentHashMap<>(), new ConcurrentLinkedQueue<>(), new ConcurrentHashMap<>());
        }

        record ActionIter(
                List<Conf> define,
                List<Action<?>> src,
                Logger log,
                int[] p
        ) implements Iterator<Action<?>> {
            ActionIter(List<Conf> define, Logger log) {
                this(define, new ArrayList<>(define.size()), log, new int[]{0});
            }

            @Override
            public boolean hasNext() {
                return p[0] < define.size();
            }

            @Override
            public Action<?> next() {
                var def = define.get(p[0]);
                var act = Action.parseConf(def, log);
                p[0]++;
                src.add(act);
                return act;
            }
        }

        public Action<?> parseFile(String file) {
            if (log.isTraceEnabled()) log.trace("parse case file: {} ", file);
            var fx = new File(file);
            var name = fx.getName().transform(x -> x.substring(0, x.length() - 5));
            var f = Conf.of(ConfigFactory.parseFile(fx).resolve());
            name = f.string("name").orElse(name);
            var action = new ArrayList<Action<?>>();
            var vars = new HashMap<String, Object>();
            f.objects("init").ifPresent(o -> o.forEach(c -> action.add(Action.parseConf(c, log))));
            f.object("vars").ifPresent(v -> v.keys(null).ifPresent(keys -> keys.forEach(key -> vars.put(key, v.getAnyRef(key)))));
            var actions = f.objects("actions").orElseThrow(() -> new IllegalArgumentException("missing required actions"));
            if (actions.isEmpty()) throw new IllegalArgumentException("actions should not be empty");
            actions.forEach(c -> action.add(Action.parseConf(c, log)));
            return new Case(file, name, action, vars);
        }

        record Case(
                String file,
                String name,
                List<Action<?>> actions,
                Map<String, Object> vars
        ) implements Action<Case> {

            @Override
            public String action() {
                return name;
            }

            @Override
            public void execute(Context ctx) {
                var log = ctx.log();
                if (ctx.log().isTraceEnabled()) {
                    log.trace("execute case {}", this);
                }
                ctx.vars().putAll(vars);
                for (var act : actions) {
                    if (ctx.log().isTraceEnabled()) {
                        log.trace("execute case {} action {}", name, act.action());
                    }
                    var err = act.run(ctx);
                    if (err.isPresent()) {
                        var ex = err.get();
                        throw new RuntimeException("execute '" + name + "." + act.action() + "' failed: " + ex.getMessage(), ex);
                    }
                }
            }
        }

        public Iterable<Action<?>> use(Conf global) {
            if (log.isTraceEnabled()) log.trace("apply global config");
            var init = global.objects("init").orElse(null);
            if (log.isTraceEnabled() && init == null) {
                log.trace("initialize not exists");
            }
            global.object("vars")
                    .ifPresent(x -> x.keys(null).ifPresent(keys -> keys
                            .forEach(key -> {
                                var v = x.getAnyRef(key);
                                if (log.isTraceEnabled()) {
                                    log.trace("register variable {} = {}({})", key, v, v.getClass().getSimpleName());
                                }
                                put(key, v);
                            })));
            var iter = init == null ? null : new ActionIter(init, log);
            return iter == null ? List.of() : () -> iter;
        }

        @SuppressWarnings("unchecked")
        public Iterable<Action<?>> files(String[] scripts) {
            if (scripts == null || scripts.length == 0) {
                log.error("no script defined");
                return List.of();
            }
            if (log.isTraceEnabled()) log.trace("parse scripts {}", (Object) scripts);
            return (Iterable<Action<?>>) (Iterable<?>) Arrays
                    .stream(scripts)
                    .map(this::parseFile)
                    .toList();
        }

        @SuppressWarnings("unchecked")
        @SneakyThrows
        public Iterable<Action<?>> walk(String path) {
            if (log.isTraceEnabled()) log.trace("parse cases folder {}", path);
            try (var s = Files.walk(Paths.get(path))) {
                return (Iterable<Action<?>>) (Iterable<?>) s
                        .filter(Files::isRegularFile)
                        .filter(x -> x.toFile().getName().endsWith(".conf"))
                        .map(x -> parseFile(x.toString()))
                        .toList();
            }
        }
    }
}
