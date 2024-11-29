package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;
import lombok.SneakyThrows;

/**
 * @author Zen.Liu
 * @since 2024-11-23
 */
public interface SikuliX {
    String ScreenPrefix = "Screen::";
    String MatchPrefix = "Match::";

    @AutoService(Action.class)
    @Info("initialize a SikuliX screen.")
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
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("initialize screen " + name);
            var c = id == null ? new org.sikuli.script.Screen() : new org.sikuli.script.Screen(id);
            ctx.put(name, c);
        }
    }

    @AutoService(Action.class)
    @Info("find match on screen by pattern or text.")
    record Match(
            @Info(value = "screen name to use, which will prefixed by '" + MatchPrefix + "'.")
            String screen,
            @Info(value = "pattern image file or text to match")
            String pattern,
            @Info(value = "store match as name")
            String name,
            @Info(value = "max wait seconds", optional = true)
            Double await
    ) implements Action<Match> {

        public Match() {
            this(null, null, null, null);
        }

        @SneakyThrows
        @Override
        public void execute(Context ctx) {
            var sc = ctx.require(ScreenPrefix + this.screen, org.sikuli.script.Screen.class);
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("match pattern {} on screen {}", pattern, this.screen);
            if (await != null)
                sc.setAutoWaitTimeout(await);
            var match = sc.find(pattern);
            ctx.put(this.name, MatchPrefix + match);
        }
    }

    @AutoService(Action.class)
    @Info("highlight a screen matched region.")
    record Highlight(
            @Info(value = "match name to use")
            String match,
            @Info(value = "color to use (HEX RGB start with #), default red", optional = true)
            String color,
            @Info(value = "seconds to highlight", optional = true)
            Double sec
    ) implements Action<Highlight> {

        public Highlight() {
            this(null, null, null);
        }

        @SneakyThrows
        @Override
        public void execute(Context ctx) {
            var m = ctx.require(MatchPrefix + match, org.sikuli.script.Match.class);
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("highlight match {} on screen", match);
            if (sec != null && color != null) m.highlight(sec, color);
            else if (sec != null) m.highlight(sec);
            else if (color != null) m.highlight(color);
            else m.highlight();
        }
    }

    @AutoService(Action.class)
    @Info("highlight a screen matched region, should manual off by action 'highlightOff'.")
    record HighlightOn(
            @Info(value = "match name to use")
            String match,
            @Info(value = "color to use (HEX RGB start with #), default red", optional = true)
            String color
    ) implements Action<HighlightOn> {

        public HighlightOn() {
            this(null, null);
        }

        @SneakyThrows
        @Override
        public void execute(Context ctx) {
            var m = ctx.require(MatchPrefix + match, org.sikuli.script.Match.class);
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("highlight on for match {} on screen", match);

            if (color != null) m.highlightOn(color);
            m.highlightOn();
        }
    }

    @AutoService(Action.class)
    @Info("highlight off for a screen matched region.")
    record HighlightOff(
            @Info(value = "match name to use")
            String match
    ) implements Action<HighlightOff> {

        public HighlightOff() {
            this(null);
        }

        @SneakyThrows
        @Override
        public void execute(Context ctx) {
            var m = ctx.require(MatchPrefix + match, org.sikuli.script.Match.class);
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("highlight off for match {} on screen", match);
            m.highlightOff();
        }
    }

    @AutoService(Action.class)
    @Info("click match on screen.")
    record Click(
            @Info(value = "match name to use")
            String match,
            @Info(value = "x offset", optional = true)
            Integer x,
            @Info(value = "y offset", optional = true)
            Integer y
    ) implements Action<Click> {

        public Click() {
            this(null, null, null);
        }

        @SneakyThrows
        @Override
        public void execute(Context ctx) {
            var m = ctx.require(MatchPrefix + match, org.sikuli.script.Match.class);
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("click match {} on screen", match);
            if (x != null || y != null) {
                m.offset(x == null ? 0 : x, y == null ? 0 : y).click();
            } else {
                m.click();
            }
        }
    }

    @AutoService(Action.class)
    @Info("click and parse text to screen by match.")
    record Parse(
            @Info(value = "match name to use")
            String match,
            @Info(value = "text name to parse")
            String text,
            @Info(value = "x offset of click location, default zero.", optional = true)
            Integer x,
            @Info(value = "y offset of click location, default zero.", optional = true)
            Integer y
    ) implements Action<Parse> {

        public Parse() {
            this(null, null, null, null);
        }

        @SneakyThrows
        @Override
        public void execute(Context ctx) {
            var m = ctx.require(MatchPrefix + match, org.sikuli.script.Match.class);
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("parse {} to match {} on screen", text, match);
            if (x != null || y != null) {
                if (m.offset(x == null ? 0 : x, y == null ? 0 : y).paste(text) <= 0)
                    throw new IllegalStateException("parse failed");
            } else {
                if (m.paste(text) <= 0)
                    throw new IllegalStateException("parse failed");
            }

        }
    }

    @AutoService(Action.class)
    @Info("expect pattern or text pattern show on screen.")
    record Expect(
            @Info(value = "screen name to use, which will prefixed by '" + MatchPrefix + "'.")
            String screen,
            @Info(value = "pattern image file or text to match")
            String pattern,
            @Info(value = "match name to store, when name absent will not stored.", optional = true)
            String name,
            @Info(value = "timeout in seconds")
            double timeout
    ) implements Action<Expect> {

        public Expect() {
            this(null, null, null, -1);
        }

        @SneakyThrows
        @Override
        public void execute(Context ctx) {
            var sc = ctx.require(ScreenPrefix + this.screen, org.sikuli.script.Screen.class);
            var log = ctx.log();
            if (log.isTraceEnabled())
                log.trace("match pattern {} on screen {}", pattern, this.screen);
            var match = sc.exists(pattern, timeout);
            if (match == null) throw new IllegalStateException("pattern not found");
            if (name != null)
                ctx.put(name, MatchPrefix + match);
        }
    }
}
