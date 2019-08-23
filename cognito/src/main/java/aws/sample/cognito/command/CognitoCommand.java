package aws.sample.cognito.command;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.util.concurrent.Callable;

/**
 * @author antoniocaccamo on 22/08/2019.
 */

@Component @Slf4j
@CommandLine.Command( name = "cognito",
        subcommands = {
                CognitoSignUpCommand.class
        }
)
public class CognitoCommand implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {

        return 0;
    }
}
