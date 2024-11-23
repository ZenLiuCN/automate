package cn.zenliu.automate.context;

import cn.zenliu.automate.notation.ConfReader;
import cn.zenliu.automate.notation.Reader;
import com.typesafe.config.*;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.Optional;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface Conf extends Config {
    static Conf of(Config c) {
        return new conf(c);
    }

    record conf(Config c) implements Conf {
    }

    //region Delegate
    @Override
    default ConfigObject root() {
        return c().root();
    }

    @Override
    default ConfigOrigin origin() {
        return c().origin();
    }

    @Override
    default Config withFallback(ConfigMergeable configMergeable) {
        return c().withFallback(configMergeable);
    }

    @Override
    default Config resolve() {
        return c().resolve();
    }

    @Override
    default Config resolve(ConfigResolveOptions configResolveOptions) {
        return c().resolve(configResolveOptions);
    }

    @Override
    default boolean isResolved() {
        return c().isResolved();
    }

    @Override
    default Config resolveWith(Config config) {
        return c().resolveWith(config);
    }

    @Override
    default Config resolveWith(Config config, ConfigResolveOptions configResolveOptions) {
        return c().resolveWith(config, configResolveOptions);
    }

    @Override
    default void checkValid(Config config, String... strings) {
        c().checkValid(config, strings);
    }

    @Override
    default boolean hasPath(String s) {
        return c().hasPath(s);
    }

    @Override
    default boolean hasPathOrNull(String s) {
        return c().hasPathOrNull(s);
    }

    @Override
    default boolean isEmpty() {
        return c().isEmpty();
    }

    @Override
    default Set<Map.Entry<String, ConfigValue>> entrySet() {
        return c().entrySet();
    }

    @Override
    default boolean getIsNull(String s) {
        return c().getIsNull(s);
    }

    @Override
    default boolean getBoolean(String s) {
        return c().getBoolean(s);
    }

    @Override
    default Number getNumber(String s) {
        return c().getNumber(s);
    }

    @Override
    default int getInt(String s) {
        return c().getInt(s);
    }

    @Override
    default long getLong(String s) {
        return c().getLong(s);
    }

    @Override
    default double getDouble(String s) {
        return c().getDouble(s);
    }

    @Override
    default String getString(String s) {
        return c().getString(s);
    }

    @Override
    default <T extends Enum<T>> T getEnum(Class<T> aClass, String s) {
        return c().getEnum(aClass, s);
    }

    @Override
    default ConfigObject getObject(String s) {
        return c().getObject(s);
    }

    @Override
    default Config getConfig(String s) {
        return c().getConfig(s);
    }

    @Override
    default Object getAnyRef(String s) {
        return c().getAnyRef(s);
    }

    @Override
    default ConfigValue getValue(String s) {
        return c().getValue(s);
    }

    @Override
    default Long getBytes(String s) {
        return c().getBytes(s);
    }

    @Override
    default ConfigMemorySize getMemorySize(String s) {
        return c().getMemorySize(s);
    }

    @Override
    @Deprecated
    default Long getMilliseconds(String s) {
        return c().getMilliseconds(s);
    }

    @Override
    @Deprecated
    default Long getNanoseconds(String s) {
        return c().getNanoseconds(s);
    }

    @Override
    default long getDuration(String s, TimeUnit timeUnit) {
        return c().getDuration(s, timeUnit);
    }

    @Override
    default Duration getDuration(String s) {
        return c().getDuration(s);
    }

    @Override
    default Period getPeriod(String s) {
        return c().getPeriod(s);
    }

    @Override
    default TemporalAmount getTemporal(String s) {
        return c().getTemporal(s);
    }

    @Override
    default ConfigList getList(String s) {
        return c().getList(s);
    }

    @Override
    default List<Boolean> getBooleanList(String s) {
        return c().getBooleanList(s);
    }

    @Override
    default List<Number> getNumberList(String s) {
        return c().getNumberList(s);
    }

    @Override
    default List<Integer> getIntList(String s) {
        return c().getIntList(s);
    }

    @Override
    default List<Long> getLongList(String s) {
        return c().getLongList(s);
    }

    @Override
    default List<Double> getDoubleList(String s) {
        return c().getDoubleList(s);
    }

    @Override
    default List<String> getStringList(String s) {
        return c().getStringList(s);
    }

    @Override
    default <T extends Enum<T>> List<T> getEnumList(Class<T> aClass, String s) {
        return c().getEnumList(aClass, s);
    }

    @Override
    default List<? extends ConfigObject> getObjectList(String s) {
        return c().getObjectList(s);
    }

    @Override
    default List<? extends Config> getConfigList(String s) {
        return c().getConfigList(s);
    }

    @Override
    default List<? extends Object> getAnyRefList(String s) {
        return c().getAnyRefList(s);
    }

    @Override
    default List<Long> getBytesList(String s) {
        return c().getBytesList(s);
    }

    @Override
    default List<ConfigMemorySize> getMemorySizeList(String s) {
        return c().getMemorySizeList(s);
    }

    @Override
    @Deprecated
    default List<Long> getMillisecondsList(String s) {
        return c().getMillisecondsList(s);
    }

    @Override
    @Deprecated
    default List<Long> getNanosecondsList(String s) {
        return c().getNanosecondsList(s);
    }

    @Override
    default List<Long> getDurationList(String s, TimeUnit timeUnit) {
        return c().getDurationList(s, timeUnit);
    }

    @Override
    default List<Duration> getDurationList(String s) {
        return c().getDurationList(s);
    }

    @Override
    default Config withOnlyPath(String s) {
        return c().withOnlyPath(s);
    }

    @Override
    default Config withoutPath(String s) {
        return c().withoutPath(s);
    }

    @Override
    default Config atPath(String s) {
        return c().atPath(s);
    }

    @Override
    default Config atKey(String s) {
        return c().atKey(s);
    }

    @Override
    default Config withValue(String s, ConfigValue configValue) {
        return c().withValue(s, configValue);
    }
    //endregion

    Config c();

    default <T> Optional<T> maybe(String path, BiFunction<Config, String, T> fn) {
        var c = c();
        return c.hasPath(path) ? Optional.of(fn.apply(c, path)) : Optional.empty();
    }

    default <T> T require(String path, BiFunction<Config, String, T> fn, @Nullable Supplier<? extends RuntimeException> ex) {
        var c = c();
        if (c.hasPath(path)) {
            return fn.apply(c, path);
        }
        if (ex == null) throw new IllegalArgumentException("missing value at '" + path + "'");
        throw ex.get();
    }

    default <T> T require(String path, BiFunction<Config, String, T> fn, @Nullable String ex) {
        var c = c();
        if (c.hasPath(path)) {
            return fn.apply(c, path);
        }
        if (ex == null) throw new IllegalArgumentException("missing value at '" + path + "'");
        throw new IllegalArgumentException(ex);
    }

    default String rString(String path) {
        return require(path, Config::getString, (String) null);
    }

    default Map<String, String> stringMap(String path) {
        var o = getObject(path);
        var m = new LinkedHashMap<String, String>();
        var c = o.toConfig();
        Conf cf = () -> c;
        for (String k : o.keySet()) {
            m.put(k, cf.rString(k));
        }
        return m;
    }

    static <T> Reader<T> required(String path, BiFunction<Conf, String, T> fn) {
        return c -> {
            if (c.hasPath(path)) return fn.apply(c, path);
            throw new IllegalArgumentException("missing value of " + path);
        };
    }

    static <T> Reader<T> maybe(String path, BiFunction<Conf, String, T> fn, T def) {
        return c -> Optional.ofNullable(c.hasPath(path) ? fn.apply(c, path) : null).orElse(def);
    }

    static Reader<Long> readLong(String path, boolean req) {
        return req
                ? required(path, Config::getLong)
                : maybe(path, Config::getLong, null);
    }

    static Reader<Integer> readInteger(String path, boolean req) {
        return req
                ? required(path, Config::getInt)
                : maybe(path, Config::getInt, null);
    }

    static Reader<String> readString(String path, boolean req) {
        return req
                ? required(path, Config::getString)
                : maybe(path, Config::getString, null);
    }

    static Reader<Boolean> readBoolean(String path, boolean req) {
        return req
                ? required(path, Config::getBoolean)
                : maybe(path, Config::getBoolean, null);
    }

    static Reader<Map<String, String>> stringMap(String path, boolean req) {
        return req
                ? required(path, Conf::stringMap)
                : maybe(path, Conf::stringMap, null);
    }

    ConfReader<Map<String, String>> MaybeStringMap = (c, p) -> stringMap(p, false).apply(c);
}
