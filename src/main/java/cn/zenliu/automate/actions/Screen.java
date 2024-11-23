package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
@AutoService(Action.class)
@Info("initialize a Sikulix screen.")
@Slf4j
public record Screen(
        @Info(value = "screen name for use by other actions. automatic prefix with '" + PREFIX+"'")
        String name,
        @Info(value = "screen index(negative for primary, otherwise is the specific screen)", optional = true)
        Integer id
) implements Action<Screen> {
    public static final String PREFIX = "Screen::";

    public Screen() {
        this(null, null);
    }

    @Override
    public Optional<Exception> execute(Context ctx) {
        var name = PREFIX + this.name;
        if (!ctx.vars().containsKey(name))
            return Optional.of(new IllegalStateException("already initialized"));
        log.trace("initialize screen " + name);
        var c = id == null ? new org.sikuli.script.Screen() : new org.sikuli.script.Screen(id);
        ctx.put(name, c);
        return Optional.empty();
    }
}
