package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;

/**
 * @author Zen.Liu
 * @since 2024-11-29
 */
public interface Common {

    /**
     * @author Zen.Liu
     * @since 2024-11-23
     */
    @AutoService(Action.class)
    @Info("show context variable.")
    record ShowVar(
            @Info(value = "variable name")
            String name,
            @Info(value = "type of variable", optional = true, values = {"1:playwright", "2:chrome", "3:page", "4:screen"})
            Integer type
    ) implements Action<ShowVar> {

        public ShowVar() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            var log = ctx.log();
            if (type == null && name.isBlank()) {
                log.info("{}", ctx);
            } else if (type == null) {
                log.info("variable {}: {}", name, ctx.var(name));
            } else switch (type) {
                case 1 -> log.info("variable {}:{} ", Playwrights.PLAYWRIGHT, ctx.var(Playwrights.PLAYWRIGHT));
                case 2 -> log.info("variable {}:{} ", Playwrights.BROWSER, ctx.var(Playwrights.BROWSER));
                case 3 -> log.info("variable {}:{} ", name, ctx.var(Playwrights.PagePrefix + name));
                case 4 -> log.info("variable {}:{} ", name, ctx.var(SikuliX.ScreenPrefix + name));
                default -> log.error("unsupported type {}", type);
            }
        }
    }

    @AutoService(Action.class)
    @Info("remove value in context")
    record Remove(
            @Info(value = "variable name")
            String name
    ) implements Action<Remove> {

        public Remove() {
            this(null);
        }

        @Override
        public void execute(Context ctx) {
            ctx.invalidate(name);
        }
    }

    @AutoService(Action.class)
    @Info("require value present and not null in context")
    record RequireExists(
            @Info(value = "variable name")
            String name,
            @Info(value = "error message")
            String message
    ) implements Action<RequireExists> {

        public RequireExists() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            if (!ctx.vars().containsKey(name)) {
                throw new IllegalStateException(message);
            }
            if (ctx.var(name).isEmpty()) {
                throw new IllegalStateException(message);
            }
        }
    }

    @AutoService(Action.class)
    @Info("require a boolean value is true in context")
    record RequireTrue(
            @Info(value = "variable name")
            String name,
            @Info(value = "error message")
            String message
    ) implements Action<RequireTrue> {

        public RequireTrue() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            if (ctx.var(name, Boolean.class).filter(x -> x).isEmpty()) {
                throw new IllegalStateException(message);
            }
        }
    }

    @AutoService(Action.class)
    @Info("require a boolean value is false in context")
    record RequireFalse(
            @Info(value = "variable name")
            String name,
            @Info(value = "error message")
            String message
    ) implements Action<RequireFalse> {

        public RequireFalse() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            if (ctx.var(name, Boolean.class).filter(x -> !x).isEmpty()) {
                throw new IllegalStateException(message);
            }
        }
    }

    @AutoService(Action.class)
    @Info("require a text value matches target in context")
    record TextMatch(
            @Info(value = "variable name")
            String name,
            @Info(value = "match value")
            String value,
            @Info(value = "error message")
            String message,
            @Info(value = "value is regex pattern", optional = true)
            Boolean regex
    ) implements Action<TextMatch> {

        public TextMatch() {
            this(null, null, null, null);
        }

        @Override
        public void execute(Context ctx) {
            if (ctx.var(name, String.class).filter(x -> regex != null ? x.matches(value) : x.equals(value)).isEmpty()) {
                throw new IllegalStateException(message);
            }
        }
    }
}
