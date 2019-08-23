package aws.sample.cognito.command;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.concurrent.Callable;

@Component @Slf4j
@CommandLine.Command(
        name = "signUp"
)
public class CognitoSignUpCommand implements Callable<Integer> {

    @CommandLine.Option(required = true, names = {"-u", "--username"})
    private String username;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true, required = true)
    char[] password;

    @Value("app.aws.cognito.pool-id")
    private String poolId;

    @Value("app.aws.cognito.client-id")
    private String clientId;

    @Value("app.aws.cognito.region")
    private String region;


    @Override
    public Integer call() throws Exception {
        log.info("tryng creating user {} .." , username);

        CognitoIdentityProviderClient cognitoIdentityProviderClient = null;
        try {

            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(username)
                    .password(password.toString())
                    .userAttributes(AttributeType.builder().name("email").value("caccamo.antonio@gmail.com").build())
                    .build()
            ;


            cognitoIdentityProviderClient =
                    CognitoIdentityProviderClient.builder()
                            .region(Region.EU_WEST_1)
                            .credentialsProvider( AnonymousCredentialsProvider.create())
                            .build()
            ;

            SignUpResponse response = cognitoIdentityProviderClient.signUp(signUpRequest);

            log.info("response.sdkHttpResponse().statusCode() : {} - response.sdkHttpResponse().statusText() {}",
                    response.sdkHttpResponse().statusCode(),
                    response.sdkHttpResponse().statusText().isPresent() ? response.sdkHttpResponse().statusText().get() : ""
            );
            return response.sdkHttpResponse().isSuccessful() == true ?  0 : 1 ;
        } catch (Exception e) {
            log.error("error occurred", e);
            return  -1;
        }
        finally {
            if ( cognitoIdentityProviderClient != null)
                cognitoIdentityProviderClient.close();
        }


    }
}
