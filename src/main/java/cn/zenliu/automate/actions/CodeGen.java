package cn.zenliu.automate.actions;

import com.microsoft.playwright.Page;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * @author Zen.Liu
 * @since 2024-12-01
 */
public interface CodeGen {
    static String cap(String v) {
        return Character.toUpperCase(v.charAt(0)) + v.substring(1);
    }

    static Tuple2<String, String> opt(Class<?> v) {
        Seq.of(v.getFields())
                .forEach(System.out::println);
        return null;
    }

    /// WIP
    public static void main(String[] args) {
        var c = Page.class;
        Seq.of(c.getMethods())
                .filter(x -> x.getParameterCount() >= 1 &&
                             !(
                                     x.getName().startsWith("on") ||
                                     x.getName().startsWith("wait") ||
                                     x.getName().startsWith("off")
                             ))
                .grouped(Method::getName)
                .map(x -> x.map2(s -> s.sorted(Comparator.comparing(v -> v.getParameterCount())).reverse().findFirst().orElseThrow()))
                .map(v -> v.v2)
                .filter(x -> Seq.of(x.getParameterTypes()).findFirst(y -> y.isAssignableFrom(Object.class)).isEmpty())
                .map(v -> {
                    var p = v.getParameterTypes();
                    var lp = p[p.length - 1];
                    var opt = lp.getSimpleName().endsWith("Options");
                    var voids = v.getReturnType() == Void.TYPE;
                    if (voids && !opt) {
                        return """
                                @AutoService(Action.class)
                                @Info("fill value to a stored input element on page.")
                                record Page%1$s(
                                        @Info(value = "element name to use, automatic prefix with '" + ElementPrefix + "'")
                                        String ele,
                                        @Info(value = "text value")
                                        String text,
                                        @Info(value = "timeout of action, default 30s.", optional = true)
                                        Double timeout
                                ) implements Action {
                                    public Page%1$s() {
                                        this(null,
                                                null,
                                                null
                                        );
                                    }
                                                    
                                    @Override
                                    public void execute(Context ctx) {
                                        ctx.mustExists(BROWSER);
                                        var p = ctx.require(ElementPrefix + ele, ElementHandle.class);
                                        var log = ctx.log();
                                        if (log.isTraceEnabled()) log.trace("fetch element {} ", ele);
                                        var opt = new ElementHandle.FillOptions();
                                        if (timeout != null) opt.setTimeout(timeout);
                                        p.fill(text, opt);
                                    }
                                }
                                """.formatted(cap(v.getName()));
                    }
                    return "";
                })
                .forEach(u -> System.out.println(u));

    }
}
