package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Conf;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import com.microsoft.playwright.Browser;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface Playwrights {
    String BROWSER = "Browser";
    String PLAYWRIGHT = "Playwright";
    String PagePrefix = "Page::";

    @AutoService(Action.class)
    @Info("connect to a chrome browser. playwright required. Unique named as " + BROWSER + ".")
    @Slf4j
    record Chrome(
            @Info(value = "CDP url", optional = true)
            String cdp,
            @Info(value = "ws endpoint", optional = true)
            String ws
    ) implements Action<Chrome> {
        public Chrome() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            ctx.require(PLAYWRIGHT);
            ctx.requireNot(BROWSER);
            log.trace("initialize chrome browser");
            var p = ctx.require(PLAYWRIGHT, com.microsoft.playwright.Playwright.class);
            var c = cdp != null && !cdp.isBlank() ?
                    p.chromium().connectOverCDP(cdp)
                    : ws != null && !ws.isBlank() ? p.chromium().connect(ws)
                    : null;
            if (c == null) throw new IllegalArgumentException("at least one of CDP or ws required");
            ctx.put(BROWSER, c);

        }
    }

    @AutoService(Action.class)
    @Info("initialize playwright. Unique named as " + PLAYWRIGHT + ".")
    @Slf4j
    record Playwright(
            @Info(value = "context property", optional = true, read = Conf.class, from = "MaybeStringMap")
            Map<String, String> property
    ) implements Action<Playwright> {


        public Playwright() {
            this(null);
        }

        @Override
        public void execute(Context ctx) {
            ctx.requireNot(PLAYWRIGHT);
            log.trace("initialize playwright");
            com.microsoft.playwright.Playwright p;
            if (property != null && !property.isEmpty()) {
                p = com.microsoft.playwright.Playwright.create(new com.microsoft.playwright.Playwright.CreateOptions().setEnv(property));
            } else {
                p = com.microsoft.playwright.Playwright.create();
            }
            ctx.put(PLAYWRIGHT, p);
        }
    }

    @AutoService(Action.class)
    @Info("open a browser page. any of browser required.")
    @Slf4j
    record Open(
            @Info(value = "unique page name for other actions to use, automatic prefix with '" + PagePrefix + "'")
            String name,
            @Info(value = "page url", optional = true)
            String url
    ) implements Action<Open> {

        public Open() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            var name = PagePrefix + this.name;
            ctx.require(BROWSER);
            ctx.requireNot(name);
            log.trace("open page {} ", name);
            var p = ctx.require(BROWSER, Browser.class);
            var c = p.newPage(new Browser.NewPageOptions());
            if (this.url != null && !this.url.isBlank()) c.navigate(url);
            ctx.put(name, c);
        }
    }

    @AutoService(Action.class)
    @Info("fetch a exists browser page. any of browser required.")
    @Slf4j
    record Page(
            @Info(value = "unique page name for other actions to use, automatic prefix with '" + PagePrefix + "'")
            String name,
            @Info(value = "context index, default use first context", optional = true)
            Integer context,
            @Info(value = "page index")
            int page
    ) implements Action<Open> {

        public Page() {
            this(null, -1, -1);
        }

        @Override
        public void execute(Context ctx) {
            var name = PagePrefix + this.name;
            ctx.require(BROWSER);
            ctx.requireNot(name);
            log.trace("fetch page {} ", name);
            var p = ctx.require(BROWSER, Browser.class);
            var cx = p.contexts().get(context == null ? 0 : context);
            var c = cx.pages().get(page);
            ctx.put(name, c);
        }
    }

}
