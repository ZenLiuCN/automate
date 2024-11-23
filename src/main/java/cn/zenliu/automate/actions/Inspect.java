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
@AutoService(Action.class)
@Info("context inspect action.")
@Slf4j
public record Inspect(
        @Info(value = "variable name")
        String name,
        @Info(value = "type of variable", optional = true, values = {"1:playwright", "2:chrome", "3:page", "4:screen"})
        Integer type
) implements Action<Inspect> {

    public Inspect() {
        this(null, null);
    }

    @Override
    public void execute(Context ctx) {
        if (type == null && name.isBlank()) {
            log.info("context {}", ctx);
        } else if (type == null) {
            log.info("var {}: {}", name, ctx.var(name));
        } else switch (type) {
            case 1 -> log.info("var {}:{} ", name, ctx.var(Playwrights.PLAYWRIGHT));
            case 2 -> log.info("var {}:{} ", name, ctx.var(Playwrights.BROWSER));
            case 3 -> log.info("var {}:{} ", name, ctx.var(Playwrights.PagePrefix + name));
            case 4 -> log.info("var {}:{} ", name, ctx.var(SikuliX.ScreenPrefix + name));
            default -> log.error("unsupported type {}", type);
        }
    }
}
