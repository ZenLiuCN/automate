package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import com.microsoft.playwright.Browser;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static cn.zenliu.automate.actions.Chrome.KEY;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
@AutoService(Action.class)
@Info("open a chrome browser page.chrome required.")
@Slf4j
public record Page(
        @Info(value = "unique page name for other actions to use, automatic prefix with '" + PREFIX+"'")
        String name,
        @Info(value = "page url", optional = true)
        String url
) implements Action<Page> {
    public static final String PREFIX = "Page::";

    public Page() {
        this(null, null);
    }

    @Override
    public Optional<Exception> execute(Context ctx) {
        var name = PREFIX + this.name;
        if (!ctx.vars().containsKey(KEY))
            return Optional.of(new IllegalStateException("chrome not initialized"));
        if (ctx.vars().containsKey(name))
            return Optional.of(new IllegalStateException("page already initialized"));
        log.trace("initialize page " + name);
        var p = ctx.var(KEY, com.microsoft.playwright.Browser.class).orElseThrow(() -> new IllegalStateException("missing browser"));
        var c = p.newPage(new Browser.NewPageOptions());
        if (this.url != null && !this.url.isBlank()) c.navigate(url);
        ctx.put(name, c);
        return Optional.empty();
    }
}
