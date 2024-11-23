package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static cn.zenliu.automate.actions.Chrome.KEY;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
@AutoService(Action.class)
@Info("initialize a Chrome browser. playwright required. Unique named as " + KEY + ".")
@Slf4j
public record Chrome(
        @Info(value = "CDP url", optional = true)
        String cdp
) implements Action<Chrome> {
    public static final String KEY = "CHROME";

    public Chrome() {
        this(null);
    }

    @Override
    public Optional<Exception> execute(Context ctx) {
        if (!ctx.vars().containsKey(Playwright.KEY))
            return Optional.of(new IllegalStateException("Playwright not initialized"));
        if (!ctx.vars().containsKey(KEY))
            return Optional.of(new IllegalStateException("Chrome already initialized"));
        log.trace("initialize chrome");
        var p = ctx.var(Playwright.KEY, com.microsoft.playwright.Playwright.class).orElseThrow(() -> new IllegalStateException("missing playwright"));
        var c = p.chromium().connectOverCDP(cdp);
        ctx.put(KEY, c);
        return Optional.empty();
    }
}
