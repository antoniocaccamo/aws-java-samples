package mn.cli.cognito;

import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

import javax.inject.Singleton;
import java.util.concurrent.Callable;

@Command( name = "signUp") @Slf4j @Singleton
public class SignUpCommand implements Callable<String>{

    @Value("${app.aws.cognito.pool-id}")
    private String poolId; //= "eu-west-1_gVkypexZ5";

    @Value("${app.aws.cognito.client-id}")
    private String clientId; //= "1242pkpdpd6omkldubs41qo18j";

    @Value("${app.aws.cognito.region}")
    private String region;

    @Option(required = true, names = {"-u", "--username"})
    private String username;

    @Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true, required = true)
    private String password;

    @Override
    public String call() {

        String result = StringUtils.EMPTY;

        log.info("poolId {} clientId {}", poolId, clientId);

        log.info("tryng creating user {} .." , username);

        CognitoIdentityProviderClient cognitoIdentityProviderClient = null;
        try {

            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(username)
                    .password(password)
                    .userAttributes(
                            AttributeType.builder()
                                    .name("name").value("antonio caccamo")
                                    .build(),
                            AttributeType.builder()
                                    .name("email").value("antonio.caccamo@outlook.com")
                                    .build()
                    )
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
            //return response.sdkHttpResponse().isSuccessful() == true ?  0 : 1 ;
            result = String.valueOf(response.sdkHttpResponse().isSuccessful());
        } catch (Exception e) {
            log.error("error occurred", e);
            //return  -1;
        }
        finally {
            if ( cognitoIdentityProviderClient != null)
                cognitoIdentityProviderClient.close();
        }
        return result;
    }
    
}