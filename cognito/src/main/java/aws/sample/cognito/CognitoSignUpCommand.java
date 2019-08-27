package aws.sample.cognito;

import aws.sample.helpers.CognitoHelper;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.Callable;

@Command( name = "signUp") @Slf4j @Singleton
public class CognitoSignUpCommand implements Callable<Integer>{

    @Value("${app.aws.cognito.pool-id}")
    private String poolId;

    @Value("${app.aws.cognito.client-id}")
    private String clientId;

    @Value("${app.aws.cognito.region}")
    private String region;

    @Option(required = true, names = {"-u", "--username"})
    private String username;

    @Option(required = true, names = {"-n", "--name"})
    private String name;

    @Option(required = true, names = {"-e", "--email"})
    private String email;

    @Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true, required = true)
    private String password;

    @Inject
    private CognitoHelper cognitoHelper;

    @Override
    public Integer call() {


        boolean result = cognitoHelper.signUp(username, password, name, email);

        if (result)
            return Integer.valueOf(0);
        return Integer.valueOf(1);
    }
    
}