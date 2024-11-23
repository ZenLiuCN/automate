package cn.zenliu.automate;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Conf;
import com.typesafe.config.ConfigFactory;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public class Launcher {
    public static void main(String[] args) {
        Action.ACTIONS.forEach((name, a) -> {
            System.out.println(name);
            System.out.println(a.action());
            System.out.println(a.usage());
        });
        var c = ConfigFactory.parseString("""
                property{
                    a:"1"
                }
                """);
        Conf cx = () -> c;
        System.out.println(Action.ACTIONS.get("playwright").make(cx));
    }
}
