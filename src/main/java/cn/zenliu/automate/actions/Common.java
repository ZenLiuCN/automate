package cn.zenliu.automate.actions;

import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Conf;
import cn.zenliu.automate.context.Context;
import cn.zenliu.automate.notation.Info;
import com.google.auto.service.AutoService;

/**
 * @author Zen.Liu
 * @since 2024-11-29
 */
@SuppressWarnings("unused")
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
    ) implements Action {

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
    ) implements Action {

        public Remove() {
            this(null);
        }

        @Override
        public void execute(Context ctx) {
            ctx.invalidate(name);
        }
    }

    @AutoService(Action.class)
    @Info("check value present and not null in context, store result as name.")
    record ExistCheck(
            @Info(value = "variable name to check")
            String var,
            @Info(value = "boolean result to store")
            String name
    ) implements Action {

        public ExistCheck() {
            this(null, null);
        }

        @Override
        public void execute(Context ctx) {
            if (!ctx.vars().containsKey(name)) {
                ctx.put(name, false);
                return;
            }
            if (ctx.var(name).isEmpty()) {
                ctx.put(name, false);
                return;
            }
            ctx.put(name, true);

        }
    }

    @AutoService(Action.class)
    @Info("require value present and not null in context")
    record RequireExists(
            @Info(value = "variable name")
            String name,
            @Info(value = "error message")
            String message
    ) implements Action {

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
    ) implements Action {

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
    ) implements Action {

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
    ) implements Action {

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

    @AutoService(Action.class)
    @Info("check a text value matches target in context, store result in contex as name")
    record TextCheck(
            @Info(value = "variable name")
            String var,
            @Info(value = "result variable name")
            String name,
            @Info(value = "match value")
            String value,
            @Info(value = "error message")
            String message,
            @Info(value = "value is regex pattern", optional = true)
            Boolean regex
    ) implements Action {

        public TextCheck() {
            this(null, null, null, null, null);
        }

        @Override
        public void execute(Context ctx) {
            ctx.put(name, ctx.var(var, String.class).filter(x -> regex != null ? x.matches(value) : x.equals(value)).isEmpty());
        }
    }

    @AutoService(Action.class)
    @Info("condition action for a boolean variable")
    record IfElse(
            @Info(value = "a boolean variable name")
            String var,
            @Info(value = "when true execute")
            Action whenTrue,
            @Info(value = "when false execute")
            Action whenFalse
    ) implements Action {
        @Override
        public IfElse make(Conf c) {
            var var = c.string(this.var).orElseThrow(() -> new IllegalArgumentException("var required but missing"));
            var ot = c.object("whenTrue").orElseThrow(() -> new IllegalArgumentException("whenTrue required but missing"));
            var of = c.object("whenFalse").orElseThrow(() -> new IllegalArgumentException("whenFalse required but missing"));
            var at = Action.parseConf(ot);
            var af = Action.parseConf(of);
            return new IfElse(var, at, af);
        }

        public IfElse() {
            this(null, null, null);
        }

        @Override
        public void execute(Context ctx) {
            if (ctx.require(var, Boolean.class)) {
                whenTrue.execute(ctx);
            } else {
                whenFalse.execute(ctx);
            }
        }
    }
}
