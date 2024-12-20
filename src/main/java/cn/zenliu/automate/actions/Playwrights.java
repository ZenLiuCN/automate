package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Conf;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
@SuppressWarnings("unused")
public interface Playwrights {
    String CONTEXT = "context";
    String BROWSER = "Browser";
    String PLAYWRIGHT = "Playwright";
    String PagePrefix = "Page::";
    String ElementPrefix = "Element::";
    String LocatorPrefix = "Locator::";

    @AutoService(Action.class)
    @Info("connect to a chrome browser. playwright required. Unique named as " + BROWSER + ".")
    record Chrome(
            @Info(value = "CDP url", optional = true)
            String cdp,
            @Info(value = "ws endpoint", optional = true)
            String ws
    ) implements Action {
        public Chrome() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(PLAYWRIGHT);
            ctx.mustNotExists(BROWSER);
            if (log.isTraceEnabled()) log.trace("initialize chrome browser");
            var p = ctx.require(PLAYWRIGHT, com.microsoft.playwright.Playwright.class);
            var c = cdp != null && !cdp.isBlank() ?
                    p.chromium().connectOverCDP(cdp)
                    : ws != null && !ws.isBlank() ? p.chromium().connect(ws)
                    : null;
            if (c == null) throw new IllegalArgumentException("at least one of CDP or ws required");
            ctx.put(BROWSER, c);

        }
    }

/*  @AutoService(Action.class)
    @Info("create new browser context. playwright required. Unique named as " + CONTEXT + ".")
    record ContextCreate(
            @Info(value = "simulation touch", optional = true)
            Boolean touch,
            @Info(value = "simulation mobile", optional = true)
            Boolean mobile,
            @Info(value = "simulation device scale", optional = true)
            Double scale,
            @Info(value = "simulation view port width", optional = true)
            Integer vwWidth,
            @Info(value = "simulation view port height", optional = true)
            Integer vwHeight,
            @Info(value = "window width", optional = true)
            Integer width,
            @Info(value = "window height", optional = true)
            Integer height,
            @Info(value = "timeout", optional = true)
            Double timeout
    ) implements Action {
        public ContextCreate() {
            this(null, null, null, null,null, null, null, null);
        }

        @Override
        public void execute(Context ctx ,Logger log) {
            ctx.mustExists(PLAYWRIGHT);
            ctx.mustExists(BROWSER);
            ctx.mustNotExists(CONTEXT);
            if (log.isTraceEnabled()) log.trace("initialize browser context");
            var p = ctx.require(BROWSER, Browser.class);
            var opt = new Browser.NewContextOptions();
            if (touch != null) opt.setHasTouch(touch);
            if (mobile != null) opt.setIsMobile(mobile);
            if (scale != null) opt.setDeviceScaleFactor(scale);
            if (vwWidth != null && vwHeight != null) opt.setViewportSize(vwWidth, vwHeight);
            else if (vwWidth != null || vwHeight != null)
                throw new IllegalStateException("both vwHeight and vwWidth should not null");
            if (width != null && height != null) opt.setScreenSize(width, height);
            else if (width != null || height != null) {
                throw new IllegalStateException("viewport width and height must both not null");
            }
            if(timeout!=null) opt.timeout
            var cx = p.newContext(opt);
            ctx.put(CONTEXT, cx);

        }
    }*/

    @AutoService(Action.class)
    @Info("initialize playwright. Unique named as " + PLAYWRIGHT + ".")
    record Playwright(
            @Info(value = "context property", optional = true, read = Conf.class, from = "MaybeStringMap")
            Map<String, String> property
    ) implements Action {


        public Playwright() {
            this(null);
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustNotExists(PLAYWRIGHT);
            if (log.isTraceEnabled()) log.trace("initialize playwright");
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
    record PageOpen(
            @Info(value = "unique page name for other actions to use, automatic prefix with '" + PagePrefix + "'")
            String name,
            @Info(value = "page url", optional = true)
            String url,
            @Info(value = "simulation user agent", optional = true)
            String ua,
            @Info(value = "dark mode", optional = true)
            Boolean dark,
            @Info(value = "simulation touch", optional = true)
            Boolean touch,
            @Info(value = "simulation mobile", optional = true)
            Boolean mobile,
            @Info(value = "simulation device scale", optional = true)
            Double scale,
            @Info(value = "simulation view port width", optional = true)
            Integer vwWidth,
            @Info(value = "simulation view port height", optional = true)
            Integer vwHeight,
            @Info(value = "window width", optional = true)
            Integer width,
            @Info(value = "window height", optional = true)
            Integer height,
            @Info(value = "default timeout for operation on page", optional = true)
            Duration timeout
    ) implements Action {

        public PageOpen() {
            this(null, null, null, null, null, null, null, null, null, null, null, null);
        }

        @Override
        public void execute(Context ctx, Logger log) {
            var name = PagePrefix + this.name;
            ctx.mustExists(BROWSER);
            ctx.mustNotExists(name);
            if (log.isTraceEnabled()) log.trace("open page {} ", name);
            var p = ctx.require(BROWSER, Browser.class);
            var opt = new Browser.NewPageOptions();
            if (dark != null) opt.setColorScheme(dark ? ColorScheme.DARK : ColorScheme.LIGHT);
            if (mobile != null) opt.setIsMobile(mobile);
            if (touch != null) opt.setHasTouch(touch);
            if (scale != null) opt.setDeviceScaleFactor(scale);
            if (ua != null && !ua.isBlank()) opt.setUserAgent(ua);
            if (vwWidth != null && vwHeight != null) opt.setViewportSize(vwWidth, vwHeight);
            else if (vwWidth != null || vwHeight != null) {
                throw new IllegalStateException("viewport width and height must both not null");
            }
            if (width != null && height != null) opt.setScreenSize(width, height);
            else if (width != null || height != null) {
                throw new IllegalStateException("viewport width and height must both not null");
            }
            var c = p.newPage(opt);
            if (this.url != null && !this.url.isBlank()) c.navigate(url);
            if (timeout != null) c.setDefaultTimeout(timeout.toMillis());
            ctx.put(name, c);
        }
    }

    @AutoService(Action.class)
    @Info("close a browser page. An exists page required.")
    record PageClose(
            @Info(value = "unique page name to operate, automatic prefix with '" + PagePrefix + "'")
            String page
    ) implements Action {

        public PageClose() {
            this(null);
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            ctx.mustExists(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("close page {} ", page);
            ctx.invalidate(PagePrefix + page);
        }
    }

    @AutoService(Action.class)
    @Info("navigate page to url. An exists page is required.")
    record PageNav(
            @Info(value = "unique page name , automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "url to open")
            String url,
            @Info(value = "store response as provide name for later use", optional = true)
            String response,
            @Info(value = "timeout in milliseconds", optional = true)
            Double timeout
    ) implements Action {

        public PageNav() {
            this(null, null, null, null);
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("navigate page {} to {} ", page, url);
            var res = timeout != null ? p.navigate(url, new Page.NavigateOptions().setTimeout(timeout)) : p.navigate(url);
            if (response != null && !response.isBlank()) ctx.put(response, res);
        }
    }

    @AutoService(Action.class)
    @Info("fetch a exists browser page. any of browser required.")
    record PagePick(
            @Info(value = "unique page name for other actions to use, automatic prefix with '" + PagePrefix + "'")
            String name,
            @Info(value = "browser context index, default use first context", optional = true)
            Integer context,
            @Info(value = "page index")
            int page
    ) implements Action {

        public PagePick() {
            this(null, -1, -1);
        }

        @Override
        public void execute(Context ctx, Logger log) {
            var name = PagePrefix + this.name;
            ctx.mustExists(BROWSER);
            ctx.mustNotExists(name);
            if (log.isTraceEnabled()) log.trace("fetch page {} ", name);
            var p = ctx.require(BROWSER, Browser.class);
            var cx = p.contexts().get(context == null ? 0 : context);
            var c = cx.pages().get(page);
            ctx.put(name, c);
        }
    }

    @AutoService(Action.class)
    @Info("fetch a exists browser page. any of browser required.")
    record PageUrl(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "var name to store page url")
            String name


    ) implements Action {

        public PageUrl() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("fetch page {} url ", page);
            if (!ctx.put(name, p.url())) {
                throw new IllegalStateException("argument '" + name + "' already exists:  " + ctx);
            }
        }
    }

    @AutoService(Action.class)
    @Info("click on page element.")
    record PageClick(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "selector")
            String selector,
            @Info(value = "strict for only one element found", optional = true)
            Boolean strict,
            @Info(value = "button to click", optional = true, values = {
                    "LEFT: left mouse button",
                    "RIGHT: right mouse button",
                    "MIDDLE: middle mouse button",
            })
            String button,
            @Info(value = "times to click", optional = true)
            Integer times,
            @Info(value = "delay ms of button down and button up,default is zero", optional = true)
            Double delay,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public PageClick() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("click {} on page {}", selector, page);
            var opt = new Page.ClickOptions();
            if (strict != null) opt.strict = strict;
            if (button != null) opt.button = MouseButton.valueOf(button);
            if (times != null) opt.setClickCount(times);
            if (timeout != null) opt.setTimeout(timeout);
            if (delay != null) opt.setDelay(delay);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.click(selector, opt);
        }
    }

    @AutoService(Action.class)
    @Info("double click on page element. same as click with two times.")
    record PageDbClick(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "selector")
            String selector,
            @Info(value = "strict for only one element found", optional = true)
            Boolean strict,
            @Info(value = "button to click", optional = true, values = {
                    "LEFT: left mouse button",
                    "RIGHT: right mouse button",
                    "MIDDLE: middle mouse button",
            })
            String button,
            @Info(value = "delay ms of button down and button up,default is zero", optional = true)
            Double delay,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {

        public PageDbClick() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("dbclick {} on page {}", selector, page);
            var opt = new Page.DblclickOptions();
            if (strict != null) opt.strict = strict;
            if (button != null) opt.button = MouseButton.valueOf(button);
            if (timeout != null) opt.setTimeout(timeout);
            if (delay != null) opt.setDelay(delay);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.dblclick(selector, opt);
        }
    }

    @AutoService(Action.class)
    @Info("check on page element (click center of an element,eg check-box).")
    record PageCheck(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "selector")
            String selector,
            @Info(value = "strict for only one element found", optional = true)
            Boolean strict,
            @Info(value = "x position relative to top left corner ", optional = true)
            Double x,
            @Info(value = "y position relative to top left corner ", optional = true)
            Double y,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout
    ) implements Action {

        public PageCheck() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("check {} on page {} ", selector, page);
            var opt = new Page.CheckOptions();
            if (strict != null) opt.strict = strict;
            if (timeout != null) opt.timeout = timeout;
            if (x != null || y != null) opt.position = new Position(x == null ? 0 : x, y == null ? 0 : y);
            p.check(selector, opt);
        }
    }

    @AutoService(Action.class)
    @Info("drag source element and drop on target.")
    record PageDragDrop(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "selector of source")
            String src,
            @Info(value = "selector of target")
            String tar,
            @Info(value = "strict for only one element found", optional = true)
            Boolean strict,
            @Info(value = "source x position relative to top left corner ", optional = true)
            Double sx,
            @Info(value = "source y position relative to top left corner ", optional = true)
            Double sy,
            @Info(value = "target x position relative to top left corner ", optional = true)
            Double tx,
            @Info(value = "target y position relative to top left corner ", optional = true)
            Double ty,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout
    ) implements Action {
        public PageDragDrop() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("drag {} and drop {} on page {}", src, tar, page);
            var opt = new Page.DragAndDropOptions();
            if (strict != null) opt.strict = strict;
            if (timeout != null) opt.timeout = timeout;
            if (sx != null || sy != null) opt.sourcePosition = new Position(sx == null ? 0 : sx, sy == null ? 0 : sy);
            if (tx != null || ty != null) opt.targetPosition = new Position(tx == null ? 0 : tx, ty == null ? 0 : ty);
            p.dragAndDrop(src, tar, opt);
        }
    }

    @AutoService(Action.class)
    @Info("select and store an element on page by select query.")
    record PageQuery(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "selector of element")
            String selector,
            @Info(value = "name of element to store in context, which will prefix with '" + ElementPrefix + "'")
            String name,
            @Info(value = "strict for only one element found", optional = true)
            Boolean strict,
            @Info(value = "wait state", optional = true, values = {
                    "ATTACHED: element attached",
                    "DETACHED: element detached",
                    "VISIBLE: element visible",
                    "HIDDEN: element hidden",
            })
            String state,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout
    ) implements Action {
        public PageQuery() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("select {} on page {} ", selector, page);
            var opt = new Page.WaitForSelectorOptions();
            if (strict != null) opt.strict = strict;
            if (timeout != null) opt.timeout = timeout;
            if (state != null && !state.isBlank()) opt.setState(WaitForSelectorState.valueOf(state));
            var ele = p.waitForSelector(selector, opt);
            if (ele != null) ctx.put(ElementPrefix + name, ele);
        }
    }

    @AutoService(Action.class)
    @Info("select and store a locator on page by label.")
    record LocateRole(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "aria role", values = {
                    "ALERT",
                    "ALERTDIALOG",
                    "APPLICATION",
                    "ARTICLE",
                    "BANNER",
                    "BLOCKQUOTE",
                    "BUTTON",
                    "CAPTION",
                    "CELL",
                    "CHECKBOX",
                    "CODE",
                    "COLUMNHEADER",
                    "COMBOBOX",
                    "COMPLEMENTARY",
                    "CONTENTINFO",
                    "DEFINITION",
                    "DELETION",
                    "DIALOG",
                    "DIRECTORY",
                    "DOCUMENT",
                    "EMPHASIS",
                    "FEED",
                    "FIGURE",
                    "FORM",
                    "GENERIC",
                    "GRID",
                    "GRIDCELL",
                    "GROUP",
                    "HEADING",
                    "IMG",
                    "INSERTION",
                    "LINK",
                    "LIST",
                    "LISTBOX",
                    "LISTITEM",
                    "LOG",
                    "MAIN",
                    "MARQUEE",
                    "MATH",
                    "METER",
                    "MENU",
                    "MENUBAR",
                    "MENUITEM",
                    "MENUITEMCHECKBOX",
                    "MENUITEMRADIO",
                    "NAVIGATION",
                    "NONE",
                    "NOTE",
                    "OPTION",
                    "PARAGRAPH",
                    "PRESENTATION",
                    "PROGRESSBAR",
                    "RADIO",
                    "RADIOGROUP",
                    "REGION",
                    "ROW",
                    "ROWGROUP",
                    "ROWHEADER",
                    "SCROLLBAR",
                    "SEARCH",
                    "SEARCHBOX",
                    "SEPARATOR",
                    "SLIDER",
                    "SPINBUTTON",
                    "STATUS",
                    "STRONG",
                    "SUBSCRIPT",
                    "SUPERSCRIPT",
                    "SWITCH",
                    "TAB",
                    "TABLE",
                    "TABLIST",
                    "TABPANEL",
                    "TERM",
                    "TEXTBOX",
                    "TIME",
                    "TIMER",
                    "TOOLBAR",
                    "TOOLTIP",
                    "TREE",
                    "TREEGRID",
                    "TREEITEM"
            })
            String role,
            @Info(value = "name of locator to store in context, which will prefix with '" + LocatorPrefix + "'")
            String name,
            @Info(value = "text of element", optional = true)
            String text,
            @Info(value = "place holder of element", optional = true)
            String placeHolder,
            @Info(value = "exact match", optional = true)
            Boolean exact
    ) implements Action {
        public LocateRole() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("select by role {} on page {} ", role, page);
            var opt = new Page.GetByRoleOptions();
            if (exact != null) opt.exact = exact;
            var ele = p.getByRole(AriaRole.valueOf(role), opt);
            if (exact == null || !exact) {
                if (text != null && !text.isBlank()) {
                    ele = ele.filter(new Locator.FilterOptions().setHasText(text));
                }
                if (placeHolder != null && !placeHolder.isBlank()) {
                    ele = ele.filter(new Locator.FilterOptions().setHas(p.getByPlaceholder(placeHolder)));
                }
            }
            if (ele != null) ctx.put(LocatorPrefix + name, ele);
        }
    }

    @AutoService(Action.class)
    @Info("select and store a locator on page by label.")
    record LocateLabel(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "pattern of element label")
            String pattern,
            @Info(value = "name of locator to store in context, which will prefix with '" + LocatorPrefix + "'")
            String name,
            @Info(value = "exact match", optional = true)
            Boolean exact
    ) implements Action {
        public LocateLabel() {
            this(null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("select by label {} on page {} ", pattern, page);
            var opt = new Page.GetByLabelOptions();
            if (exact != null) opt.exact = exact;
            var ele = p.getByLabel(pattern, opt);
            if (ele != null) ctx.put(LocatorPrefix + name, ele);
        }
    }

    @AutoService(Action.class)
    @Info("select and store a locator on page by text.")
    record LocateText(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "pattern of element label")
            String pattern,
            @Info(value = "name of locator to store in context, which will prefix with '" + LocatorPrefix + "'")
            String name,
            @Info(value = "html ariaRole to pick, use when exact is false", optional = true)
            String ariaRole,
            @Info(value = "html aria name to pick, use when exact is false and  aria role is provided.", optional = true)
            String ariaName,
            @Info(value = "exact match", optional = true)
            Boolean exact
    ) implements Action {
        public LocateText() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("select by text {} on page {} ", pattern, page);
            var opt = new Page.GetByTextOptions();
            if (exact != null) opt.exact = exact;
            var ele = p.getByText(pattern, opt);
            if ((exact == null || !exact)) {
                if (ariaRole != null && !ariaRole.isBlank()) {
                    if (ariaName != null && !ariaName.isBlank()) {
                        ele = ele.filter(new Locator.FilterOptions()
                                .setHas(p.getByRole(AriaRole.valueOf(ariaRole.toUpperCase()), new Page.GetByRoleOptions().setName(ariaName))));
                    } else {
                        ele = ele.filter(new Locator.FilterOptions()
                                .setHas(p.getByRole(AriaRole.valueOf(ariaRole.toUpperCase()))));
                    }
                }
            }
            if (ele != null) ctx.put(LocatorPrefix + name, ele);
        }
    }

    @AutoService(Action.class)
    @Info("select and store a locator on page by placeholder.")
    record LocatePlaceholder(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "value of element placeholder")
            String pattern,
            @Info(value = "name of locator to store in context, which will prefix with '" + LocatorPrefix + "'")
            String name,
            @Info(value = "html ariaRole to pick, use when exact is false", optional = true)
            String ariaRole,
            @Info(value = "html aria name to pick, use when exact is false and  aria role is provided.", optional = true)
            String ariaName,
            @Info(value = "exact match", optional = true)
            Boolean exact
    ) implements Action {
        public LocatePlaceholder() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("select by text {} on page {} ", pattern, page);
            var opt = new Page.GetByPlaceholderOptions();
            if (exact != null) opt.exact = exact;
            var ele = p.getByPlaceholder(pattern, opt);
            if ((exact == null || !exact)) {
                if (ariaRole != null && !ariaRole.isBlank()) {
                    if (ariaName != null && !ariaName.isBlank()) {
                        ele = ele.filter(new Locator.FilterOptions()
                                .setHas(p.getByRole(AriaRole.valueOf(ariaRole.toUpperCase()), new Page.GetByRoleOptions().setName(ariaName))));
                    } else {
                        ele = ele.filter(new Locator.FilterOptions()
                                .setHas(p.getByRole(AriaRole.valueOf(ariaRole.toUpperCase()))));
                    }
                }
            }
            if (ele != null) ctx.put(LocatorPrefix + name, ele);
        }
    }

    @AutoService(Action.class)
    @Info("select and store a locator on page by text.")
    record LocateTitle(
            @Info(value = "page name to use, automatic prefix with '" + PagePrefix + "'")
            String page,
            @Info(value = "pattern of element title")
            String pattern,
            @Info(value = "name of locator to store in context, which will prefix with '" + LocatorPrefix + "'")
            String name,
            @Info(value = "exact match", optional = true)
            Boolean exact
    ) implements Action {
        public LocateTitle() {
            this(null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(PagePrefix + page, Page.class);
            if (log.isTraceEnabled()) log.trace("select by label {} on page {} ", pattern, page);
            var opt = new Page.GetByTitleOptions();
            if (exact != null) opt.exact = exact;
            var ele = p.getByTitle(pattern, opt);
            if (ele != null) ctx.put(LocatorPrefix + name, ele);
        }
    }

    @AutoService(Action.class)
    @Info("click a stored locate on page.")
    record LocateClick(
            @Info(value = "locate name to use, automatic prefix with '" + LocatorPrefix + "'")
            String locate,
            @Info(value = "button to click", optional = true, values = {
                    "LEFT: left mouse button",
                    "RIGHT: right mouse button",
                    "MIDDLE: middle mouse button",
            })
            String button,
            @Info(value = "times to click", optional = true)
            Integer times,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "delay ms of button down and button up,default is zero", optional = true)
            Double delay,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public LocateClick() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(LocatorPrefix + locate, Locator.class);
            if (log.isTraceEnabled()) log.trace("click locator {} ", locate);
            var opt = new Locator.ClickOptions();
            if (button != null) opt.button = MouseButton.valueOf(button);
            if (times != null) opt.setClickCount(times);
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            if (delay != null) opt.setDelay(delay);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.click(opt);
        }
    }

    @AutoService(Action.class)
    @Info("tap a stored locate on page.")
    record LocateTap(
            @Info(value = "locate name to use, automatic prefix with '" + LocatorPrefix + "'")
            String locate,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public LocateTap() {
            this(null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(LocatorPrefix + locate, Locator.class);
            if (log.isTraceEnabled()) log.trace("click locator {} ", locate);
            var opt = new Locator.TapOptions();
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.tap(opt);
        }
    }

    @AutoService(Action.class)
    @Info("double click a stored locator on page.")
    record LocateDbClick(
            @Info(value = "locator name to use, automatic prefix with '" + LocatorPrefix + "'")
            String locate,
            @Info(value = "button to click", optional = true, values = {
                    "LEFT: left mouse button",
                    "RIGHT: right mouse button",
                    "MIDDLE: middle mouse button",
            })
            String button,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "delay ms of button down and button up,default is zero", optional = true)
            Double delay,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public LocateDbClick() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(LocatorPrefix + locate, Locator.class);
            if (log.isTraceEnabled()) log.trace("dbclick locator {} ", locate);
            var opt = new Locator.DblclickOptions();
            if (button != null) opt.button = MouseButton.valueOf(button);
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            if (delay != null) opt.setDelay(delay);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.dblclick(opt);
        }
    }

    @AutoService(Action.class)
    @Info("check a stored locator on page.")
    record LocateCheck(
            @Info(value = "locator name to use, automatic prefix with '" + LocatorPrefix + "'")
            String locate,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout
    ) implements Action {
        public LocateCheck() {
            this(null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(LocatorPrefix + locate, Locator.class);
            if (log.isTraceEnabled()) log.trace("check locate {} ", locate);
            var opt = new Locator.CheckOptions();
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            p.check(opt);
        }
    }

    @AutoService(Action.class)
    @Info("hover on a stored locator on page.")
    record LocateHover(
            @Info(value = "locator name to use, automatic prefix with '" + LocatorPrefix + "'")
            String locate,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public LocateHover() {
            this(null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(LocatorPrefix + locate, Locator.class);
            if (log.isTraceEnabled()) log.trace("hover element {} ", locate);
            var opt = new Locator.HoverOptions();
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.hover(opt);
        }
    }

    @AutoService(Action.class)
    @Info("fill value to a stored input locator on page.")
    record LocateFill(
            @Info(value = "locate name to use, automatic prefix with '" + LocatorPrefix + "'")
            String locate,
            @Info(value = "text value")
            String text,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout
    ) implements Action {
        public LocateFill() {
            this(null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(LocatorPrefix + locate, Locator.class);
            if (log.isTraceEnabled()) log.trace("fill locate {} ", locate);
            var opt = new Locator.FillOptions();
            if (timeout != null) opt.setTimeout(timeout);
            p.fill(text, opt);
        }
    }


    @AutoService(Action.class)
    @Info("click a stored element on page.")
    record ElClick(
            @Info(value = "element name to use, automatic prefix with '" + ElementPrefix + "'")
            String ele,
            @Info(value = "button to click", optional = true, values = {
                    "LEFT: left mouse button",
                    "RIGHT: right mouse button",
                    "MIDDLE: middle mouse button",
            })
            String button,
            @Info(value = "times to click", optional = true)
            Integer times,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "delay ms of button down and button up,default is zero", optional = true)
            Double delay,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public ElClick() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(ElementPrefix + ele, ElementHandle.class);
            if (log.isTraceEnabled()) log.trace("fetch element {} ", ele);
            var opt = new ElementHandle.ClickOptions();
            if (button != null) opt.button = MouseButton.valueOf(button);
            if (times != null) opt.setClickCount(times);
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            if (delay != null) opt.setDelay(delay);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.click(opt);
        }
    }

    @AutoService(Action.class)
    @Info("double click a stored element on page.")
    record ElDbClick(
            @Info(value = "element name to use, automatic prefix with '" + ElementPrefix + "'")
            String ele,
            @Info(value = "button to click", optional = true, values = {
                    "LEFT: left mouse button",
                    "RIGHT: right mouse button",
                    "MIDDLE: middle mouse button",
            })
            String button,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "delay ms of button down and button up,default is zero", optional = true)
            Double delay,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public ElDbClick() {
            this(null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(ElementPrefix + ele, ElementHandle.class);
            if (log.isTraceEnabled()) log.trace("fetch element {} ", ele);
            var opt = new ElementHandle.DblclickOptions();
            if (button != null) opt.button = MouseButton.valueOf(button);
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            if (delay != null) opt.setDelay(delay);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.dblclick(opt);
        }
    }

    @AutoService(Action.class)
    @Info("check a stored element on page.")
    record ElCheck(
            @Info(value = "element name to use, automatic prefix with '" + ElementPrefix + "'")
            String ele,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout
    ) implements Action {
        public ElCheck() {
            this(null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(ElementPrefix + ele, ElementHandle.class);
            if (log.isTraceEnabled()) log.trace("fetch element {} ", ele);
            var opt = new ElementHandle.CheckOptions();
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            p.check(opt);
        }
    }

    @AutoService(Action.class)
    @Info("hover on a stored element on page.")
    record ElHover(
            @Info(value = "element name to use, automatic prefix with '" + ElementPrefix + "'")
            String ele,
            @Info(value = "x position relative to top left", optional = true)
            Double x,
            @Info(value = "y position relative to top left", optional = true)
            Double y,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout,
            @Info(value = "keyboard modifier", optional = true, values = {
                    "ALT: ALT key",
                    "CONTROL: CTRL key",
                    "CONTROLORMETA: CTRL key | META/WIN key",
                    "META: META/WIN key",
                    "SHIFT: SHIFT key",
            }, read = Conf.class, from = "MaybeSetString")
            Set<String> modifier
    ) implements Action {
        public ElHover() {
            this(null,
                    null,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(ElementPrefix + ele, ElementHandle.class);
            if (log.isTraceEnabled()) log.trace("fetch element {} ", ele);
            var opt = new ElementHandle.HoverOptions();
            if (x != null || y != null) opt.setPosition(x == null ? 0 : x, y == null ? 0 : y);
            if (timeout != null) opt.setTimeout(timeout);
            if (modifier != null && !modifier.isEmpty())
                opt.setModifiers(modifier.stream().map(KeyboardModifier::valueOf).toList());
            p.hover(opt);
        }
    }

    @AutoService(Action.class)
    @Info("fill value to a stored input element on page.")
    record ElFill(
            @Info(value = "element name to use, automatic prefix with '" + ElementPrefix + "'")
            String ele,
            @Info(value = "text value")
            String text,
            @Info(value = "timeout of action, default 30s.", optional = true)
            Double timeout
    ) implements Action {
        public ElFill() {
            this(null,
                    null,
                    null
            );
        }

        @Override
        public void execute(Context ctx, Logger log) {
            ctx.mustExists(BROWSER);
            var p = ctx.require(ElementPrefix + ele, ElementHandle.class);
            if (log.isTraceEnabled()) log.trace("fetch element {} ", ele);
            var opt = new ElementHandle.FillOptions();
            if (timeout != null) opt.setTimeout(timeout);
            p.fill(text, opt);
        }
    }


}
