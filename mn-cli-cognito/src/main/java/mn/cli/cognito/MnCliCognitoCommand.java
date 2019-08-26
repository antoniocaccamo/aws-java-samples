package mn.cli.cognito;

import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;

import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "mn-cli-cognito", description = "...",
        subcommands={   SignUpCommand.class,
                        SignInCommand.class
        },
        mixinStandardHelpOptions = true) @Slf4j
public class MnCliCognitoCommand implements Callable<String> {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        log.info("args : {}", Arrays.asList(args));
        PicocliRunner.call( MnCliCognitoCommand.class, args);
    }

    public String call() {
        // business logic here

        if (verbose) {
            System.out.println("Hi!");
        }

        return StringUtils.EMPTY;
    }
}
