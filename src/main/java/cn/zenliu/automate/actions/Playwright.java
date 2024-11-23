package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Conf;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Optional;

import static cn.zenliu.automate.actions.Playwright.KEY;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
@AutoService(Action.class)
@Info("initialize a playwright. Unique named as " + KEY + ".")
@Slf4j
public record Playwright(
        @Info(value = "context property", optional = true, read = Conf.class, from = "MaybeStringMap")
        Map<String, String> property
) implements Action<Playwright> {
    public static final String KEY = "PLAYWRIGHT";

    public Playwright() {
        this(null);
    }

    @Override
    public Optional<Exception> execute(Context ctx) {
        if (ctx.vars().containsKey(KEY))
            return Optional.of(new IllegalStateException("playwright already initialized"));
        log.trace("initialize playwright");
        if (property != null && !property.isEmpty()) {
            var p = com.microsoft.playwright.Playwright.create(new com.microsoft.playwright.Playwright.CreateOptions().setEnv(property));
            ctx.put(KEY, p);
        } else {
            var p = com.microsoft.playwright.Playwright.create();
            ctx.put(KEY, p);
        }
        return Optional.empty();
    }
}
