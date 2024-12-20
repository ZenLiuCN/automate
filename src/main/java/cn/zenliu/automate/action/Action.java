package cn.zenliu.automate.action;

import cn.zenliu.automate.context.Conf;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.ConfReader;
import cn.zenliu.automate.notation.Info;
import cn.zenliu.automate.notation.Reader;
import org.slf4j.Logger;

import java.lang.reflect.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

/**
 * SPI actions.<br/>
 * 1. must have a public none-arguments constructor.<br/>
 * 2. must have a public secondary construct to create new instance,
 * {@link cn.zenliu.automate.notation.Info} should annotate on this constructor. <br/>
 * 3. must have only two constructor. <br/>
 * 4. must have compiled with '-parameters'. <br/>
 *
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface Action {

    static Action parseConf(Conf def) {
        var act = def.string("action").orElseThrow(() -> new IllegalArgumentException("missing action value"));
        return Objects.requireNonNull(ACTIONS.get(act), () -> "not exists action '" + act + "'").make(def);
    }

    void execute(Context ctx, Logger log);

    default Optional<Exception> run(Context ctx) {
        var log = ctx.log();
        var trace = log.isTraceEnabled();
        Set<String> keys = null;
        if (trace) {
            keys = new HashSet<>(ctx.vars().keySet());
        }
        try {
            if (trace) {
                log.trace("will execute {}", action());
            }
            execute(ctx, log);
            return Optional.empty();
        } catch (Exception ex) {
            log.error("execute {}", action(), ex);
            return Optional.of(ex);
        } finally {
            if (trace) {
                var fk = keys;
                ctx.vars().forEach((k, v) -> {
                    if (!fk.contains(k)) log.trace("write {}: {}", k, v);
                });
                log.trace("{} done", action());
            }
        }
    }

    static String camel(String pascal) {
        return pascal.substring(0, 1).toLowerCase() + pascal.substring(1);
    }

    /**
     * unique action identity. fetch by class name and convert to lower camel-case.
     */
    default String action() {
        return camel(this.getClass().getSimpleName());
    }

    default String category() {
        return Optional.of(this.getClass().getDeclaringClass()).map(Class::getSimpleName).orElse("");
    }

    /**
     * reflect fetch usage by notations
     */
    default String usage() {
        var act = action();
        return INFO.computeIfAbsent(act, a -> Action.buildInfo(a, this.getClass()));
    }

    static String buildInfo(String name, Class<?> c) {
        var it = c.getAnnotation(Info.class);
        var fac = Arrays.stream(c.getConstructors())
                .max(Comparator.comparingInt(Executable::getParameterCount))
                .orElseThrow();
        var b = new StringBuilder();
        b.append("\n");
        if (it != null) b.append("//").append(it.value()).append('\n');
        b.append(name).append("{\n");
        var n = -1;
        var annos = fac.getParameterAnnotations();
        var find = ((IntFunction<Info>) i -> (Info) Arrays.stream(annos[i]).filter(x -> x instanceof Info).findFirst().orElse(null));
        var types = fac.getAnnotatedParameterTypes();
        for (var p : fac.getParameters()) {
            n++;
            if (n > 0) b.append("\n");
            var tx = types[n].getType();
            var tt = p.getType();
            var t = types[n].getAnnotation(Info.class);
            if (t == null) t = find.apply(n);
            if (t != null) {
                var u = 0;
                if (!t.value().isBlank()) {
                    b.append("\t//").append(t.value());
                    u++;
                }
                if (u > 0) b.append("\n");
            }
            b.append("\t").append(p.getName()).append(":").append(
                    tt.getTypeParameters().length == 0 ? tt.getSimpleName() : parseGeneric(tx)
            );
            if (t != null) {
                if (t.optional()) b.append('?');
                if (t.values().length > 0) {
                    b.append("\t//");
                    var x = -1;
                    for (String value : t.values()) {
                        x++;
                        if (x > 0) b.append('|');
                        b.append(value);
                    }
                }
            }
        }
        return b.append("\n}\n\n").toString();
    }

    static String parseGeneric(Type tx) {
        if (tx instanceof GenericArrayType x) {
            return parseGeneric(x.getGenericComponentType()) + "[]";
        } else if (tx instanceof ParameterizedType x) {
            return simpleName(x.getRawType().getTypeName()) + "<" + Arrays.stream(x.getActualTypeArguments()).sequential()
                    .map(v -> simpleName(v.getTypeName()))
                    .collect(Collectors.joining(",")) + ">";
        }
        return simpleName(tx.getTypeName());
    }

    static String simpleName(String typeName) {
        return typeName.contains("$") ? typeName.substring(typeName.lastIndexOf("$") + 1) :
                typeName.substring(typeName.lastIndexOf('.') + 1);
    }

    /**
     * construct a new instance of current action
     *
     * @param c configuration value
     */

    default Action make(Conf c) {
        var fn = FAC.computeIfAbsent(action(), a -> buildFactory(a, this.getClass()));
        return fn.apply(c);
    }

    static Function<Conf, Action> buildFactory(String name, Class<?> c) {
        var fac = Arrays.stream(c.getConstructors())
                .max(Comparator.comparingInt(Executable::getParameterCount))
                .orElseThrow();
        var n = fac.getParameterCount();
        var ann = fac.getParameterAnnotations();
        var find = ((IntFunction<Info>) i -> (Info) Arrays.stream(ann[i]).filter(x -> x instanceof Info).findFirst().orElse(null));
        var rs = new Reader<?>[n];
        var i = -1;
        for (var p : fac.getParameters()) {
            i++;
            int fi = i;
            rs[i] = READERS.computeIfAbsent(new Arg(p.getName(), p.getType(), c), arg -> buildReader(arg, fi, fac, find));
        }
        return f -> {
            var a = new Object[n];
            var x = -1;
            for (var r : rs) {
                x++;
                a[x] = r.apply(f);
            }
            try {
                return (Action) fac.newInstance(a);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        };
    }

    static Reader<?> buildReader(Arg arg, int i, Constructor<?> fac, IntFunction<Info> find) {
        var p = fac.getAnnotatedParameterTypes()[i];
        var a = p.getAnnotation(Info.class);
        if (a == null) a = find.apply(i);
        var name = arg.name;
        if (a != null && a.read() != Void.class) {
            var c = a.read();
            var n = a.from();
            try {
                var f = c.getField(n);
                if (Modifier.isStatic(f.getModifiers())) {
                    var v = f.get(null);
                    return ((ConfReader<?>) v).asReader(name);
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        var req = a != null && !a.optional();
        var t = arg.type;
        if (t.isAssignableFrom(Long.class)) return Conf.readLong(name, req);
        if (t.isAssignableFrom(long.class)) return Conf.readLong(name, true);

        if (t.isAssignableFrom(Integer.class)) return Conf.readInteger(name, req);
        if (t.isAssignableFrom(int.class)) return Conf.readInteger(name, true);

        if (t.isAssignableFrom(String.class)) return Conf.readString(name, req);

        if (t.isAssignableFrom(Boolean.class)) return Conf.readBoolean(name, req);
        if (t.isAssignableFrom(boolean.class)) return Conf.readBoolean(name, true);

        if (t.isAssignableFrom(Double.class)) return Conf.readDouble(name, req);
        if (t.isAssignableFrom(double.class)) return Conf.readDouble(name, true);

        if (t.isAssignableFrom(Float.class)) return Conf.readFloat(name, req);
        if (t.isAssignableFrom(float.class)) return Conf.readFloat(name, true);

        if (t.isAssignableFrom(Duration.class)) return Conf.readDuration(name, req);
        throw new IllegalArgumentException("unsupported type " + t + ", try define user reader");
    }


    record Arg(String name, Class<?> type, Class<?> owner) {
    }

    Map<Arg, Reader<?>> READERS = new ConcurrentHashMap<>();
    Map<String, String> INFO = new ConcurrentHashMap<>();
    Map<String, Function<Conf, Action>> FAC = new ConcurrentHashMap<>();

    Map<String, Action> ACTIONS = ServiceLoader.load(Action.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toMap(Action::action, Function.identity()));
}
