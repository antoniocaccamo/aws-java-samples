package mn.cli.cognito;

import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import mn.cli.cognito.helpers.AuthenticationHelper;
import mn.cli.cognito.helpers.CognitoJWTParser;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityResponse;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.services.cognitosync.CognitoSyncClient;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command( name = "signIn") @Slf4j @Singleton
public class SignInCommand implements Callable<String> {

    @Value("${app.aws.cognito.pool-id}")
    private String poolId; //= "eu-west-1_gVkypexZ5";

    @Value("${app.aws.cognito.client-id}")
    private String clientId; //= "1242pkpdpd6omkldubs41qo18j";

    @Value("${app.aws.cognito.region}")
    private String region;

    @Value("${app.aws.cognito.fed-pool-id}")
    private String fedPoolId;

    @Option(required = true, names = {"-u", "--username"})
    private String username;

    @Inject
    private AuthenticationHelper authenticationHelper;

    @Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true, required = true)
    private String password;

    @Override
    public String call() {

        String authResult = StringUtils.EMPTY;

        log.info("poolId {} clientId {}", poolId, clientId);

        log.info("tryng creating user {} ..", username);

        CognitoIdentityProviderClient cognitoIdentityProviderClient = null;
        try {
            Map<String, String> map = new HashMap<>();
            map.put("USERNAME", username);
           // map.put("PASSWORD", password.toString());
            map.put("SRP_A", authenticationHelper.getA().toString(16));

            InitiateAuthRequest authRequest = InitiateAuthRequest
                    .builder()
                    .clientId(clientId)
                    .authFlow(AuthFlowType.USER_SRP_AUTH)
                    .authParameters(map)
                    .build();


            cognitoIdentityProviderClient =
                    CognitoIdentityProviderClient.builder()
                            .region(Region.EU_WEST_1)
                            .credentialsProvider(AnonymousCredentialsProvider.create())
                            .build()
            ;

            InitiateAuthResponse response = cognitoIdentityProviderClient.initiateAuth(authRequest);

            log.info("response.sdkHttpResponse().statusCode() : {} - response.sdkHttpResponse().statusText() {}- response.challengeNameAsString() {}",
                    response.sdkHttpResponse().statusCode(),
                    response.sdkHttpResponse().statusText().isPresent() ? response.sdkHttpResponse().statusText().get() : ""

                    , response.challengeNameAsString()
            );
            // challenge
            if (ChallengeNameType.PASSWORD_VERIFIER.equals(response.challengeName())) {
                RespondToAuthChallengeRequest challengeRequest =
                        authenticationHelper.getRespondToAuthChallengeRequest(response, password);
                RespondToAuthChallengeResponse challengeResponse = cognitoIdentityProviderClient.respondToAuthChallenge(challengeRequest);
                //System.out.println(result);
                log.info("result.authenticationResult().idToken() {}", CognitoJWTParser.getPayload(challengeResponse.authenticationResult().idToken()));
                String idToken = challengeResponse.authenticationResult().idToken();
                JSONObject payload = CognitoJWTParser.getPayload(idToken);
                String provider = payload.get("iss").toString().replace("https://", "");
                // get temp credential

                GetCredentialsForIdentityRequest identityRequest;
                CognitoIdentityClient cognitoIdentityClient =
                        CognitoIdentityClient
                            .builder()
                                .credentialsProvider(AnonymousCredentialsProvider.create())
                                .region(Region.of(region))
                            .build()
                        ;

                GetIdRequest idRequest = GetIdRequest.builder()
                        .identityPoolId(fedPoolId)
                        .logins()
                        .build()
                        ;

                GetIdResponse idResponse = cognitoIdentityClient.getId(idRequest);

                GetCredentialsForIdentityRequest credentialsForIdentityRequest = GetCredentialsForIdentityRequest.builder()
                        .identityId(idResponse.identityId())
                        .logins()
                        .build()
                        ;

                GetCredentialsForIdentityResponse credentialsForIdentityResponse =
                        cognitoIdentityClient.getCredentialsForIdentity(credentialsForIdentityRequest);

                credentialsForIdentityResponse
            }
        } catch (Exception e) {
            log.error("error occurred", e);
            //return  -1;
        } finally {
            if (cognitoIdentityProviderClient != null)
                cognitoIdentityProviderClient.close();
        }
        return authResult;
    }
}
