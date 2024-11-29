package cn.zenliu.automate;


import cn.zenliu.automate.action.Action;
import cn.zenliu.automate.context.Conf;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Option;

/**
 * @author Zen.Liu
 * @since 2024-11-28
 */
@Command(name = "auto", mixinStandardHelpOptions = true, version = "1.0",
        description = "Automate executor.")
public class Cli implements Callable<Integer> {
    public static final Logger LOG = LoggerFactory.getLogger("automate");
    @Option(names = {"-c", "--conf"}, description = "global configuration file path (HOCON format).default global.conf.", defaultValue = "global.conf")
    private String conf;
    @Option(names = {"-h", "--help"}, description = "show help and supported actions.")
    private boolean help;
    @Parameters(paramLabel = "script file", description = {"specific script to execute.can be empty to use global configuration's cases path."})
    private String[] scripts;

    @Override
    public Integer call() throws Exception {
        if (help) {
            doHelp();
        } else if (conf == null || conf.isBlank() || !Paths.get(conf).toFile().exists()) {
            System.out.println("Fatal Error: Missing global configuration file!");
            doHelp();
        } else {
            Conf.execute(Conf.of(ConfigFactory.parseFile(new File(conf)).resolve()), LOG, scripts);
        }
        return 0;
    }

    private void doHelp() {
        CommandLine.usage(this, System.out);
        System.out.println("Supported Actions:");
        System.out.println("------------------");
        var last = new String[]{""};
        Action.ACTIONS.values().stream().sorted(Comparator.comparing(Action::category))
                .forEach((a) -> {
                    if (last[0].isBlank() || !last[0].equals(a.category())) {
                        System.out.println("====================");
                        System.out.println(a.category());
                        last[0] = a.category();
                    }
                    System.out.println(a.usage());
                });
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Cli()).execute(args));
    }
}
