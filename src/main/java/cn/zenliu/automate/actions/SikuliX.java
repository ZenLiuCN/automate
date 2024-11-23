package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface SikuliX {
    String ScreenPrefix = "Screen::";

    @AutoService(Action.class)
    @Info("initialize a SikuliX screen.")
    @Slf4j
    record Screen(
            @Info(value = "screen name for use by other actions. automatic prefix with '" + ScreenPrefix + "'")
            String name,
            @Info(value = "screen index(negative for primary, otherwise is the specific screen)", optional = true)
            Integer id
    ) implements Action<Screen> {


        public Screen() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            var name = ScreenPrefix + this.name;
            ctx.requireNot(name);
            log.trace("initialize screen " + name);
            var c = id == null ? new org.sikuli.script.Screen() : new org.sikuli.script.Screen(id);
            ctx.put(name, c);
        }
    }

}
