package aws.sample.cognito;

import io.micronaut.configuration.picocli.PicocliRunner;

import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "my-cognito", description = "...",
        subcommands={   CognitoSignUpCommand.class,
                        CognitoSignInCommand.class
        },
        mixinStandardHelpOptions = true) @Slf4j
public class CognitoCommand implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        log.info("args : {}", Arrays.asList(args));
        PicocliRunner.call( CognitoCommand.class, args);
    }

    public Integer call() {
        // business logic here

        if (verbose) {
            System.out.println("Hi!");
        }

        return 0;
    }
}
