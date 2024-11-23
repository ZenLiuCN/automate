package cn.zenliu.automate.context;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface Context extends AutoCloseable {
    Logger LOG = LoggerFactory.getLogger(Context.class);

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

    record context(Map<String, AutoCloseable> closable, ConcurrentLinkedQueue<AutoCloseable> closableQueue,
                   Map<String, Object> vars) implements Context {
        context() {
            this(new ConcurrentHashMap<>(), new ConcurrentLinkedQueue<>(), new ConcurrentHashMap<>());
        }
    }
}
