package aws.sample.cognito;

import io.micronaut.configuration.picocli.PicocliRunner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "mn-cli-cognito", description = "...",
        subcommands={   SignUpCommand.class,
                        SignInCommand.class
        },
        mixinStandardHelpOptions = true) @Slf4j
public class MnCliCognitoCommand implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        log.info("args : {}", Arrays.asList(args));
        PicocliRunner.call( MnCliCognitoCommand.class, args);
    }

    public Integer call() {
        // business logic here

        if (verbose) {
            System.out.println("Hi!");
        }

        return 0;
    }
}
