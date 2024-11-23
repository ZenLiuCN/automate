package cn.zenliu.automate.notation;

import cn.zenliu.automate.context.Conf;

import java.util.function.Function;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface Reader<T> extends Function<Conf, T> {
    T apply(Conf c);
}
