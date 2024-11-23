package cn.zenliu.automate.notation;

import cn.zenliu.automate.context.Conf;

import java.util.function.BiFunction;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface ConfReader<T> extends BiFunction<Conf, String, T> {
    default Reader<T> asReader(String path) {
        return c -> apply(c, path);
    }
}
