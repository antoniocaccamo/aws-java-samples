package aws.sample.cognito;

import aws.sample.cognito.helpers.CognitoHelper;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import software.amazon.awssdk.services.cognitoidentity.model.Credentials;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@Command( name = "signIn") @Slf4j
public class SignInCommand implements Callable<Integer> {

    @Value("${app.aws.cognito.pool-id}")
    private String poolId;

    @Value("${app.aws.cognito.client-id}")
    private String clientId;

    @Value("${app.aws.cognito.region}")
    private String region;

    @Value("${app.aws.cognito.fed-pool-id}")
    private String fedPoolId;

    @Option(required = true, names = {"-u", "--username"})
    private String username;

    @Option(names = {"-p", "--password"}, description = "enter password", interactive = true, required = true)
    private String password;

    @Inject
    private CognitoHelper cognitoHelper;

    @Override
    public Integer call() {

        log.info("trying authenticate user {} ..", username);

        Credentials credentials = cognitoHelper.signIn(username, password);

        return credentials != null ? Integer.valueOf(0) : Integer.valueOf(1);
    }
}
