package aws.sample.helpers;

import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.*;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
@Singleton
@Slf4j
public class CognitoHelper {

    @Value("${app.aws.cognito.pool-id}")
    private String poolId; //= "eu-west-1_gVkypexZ5";

    @Value("${app.aws.cognito.client-id}")
    private String clientId; //= "1242pkpdpd6omkldubs41qo18j";

    @Value("${app.aws.cognito.region}")
    private String region;

    @Value("${app.aws.cognito.fed-pool-id}")
    private String fedPoolId;

    @Inject
    private AuthenticationHelper authenticationHelper;

    /**
     *
     * @param username
     * @param password
     * @param name
     * @param email
     * @return
     */
    public boolean signUp(String username, String password, String name, String email) {

        String result = StringUtils.EMPTY;

        log.info("poolId {} clientId {}", poolId, clientId);

        log.info("trying creating user {} .." , username);

        CognitoIdentityProviderClient cognitoIdentityProviderClient = null;
        try {

            SignUpRequest signUpRequest = SignUpRequest.builder()
                    .clientId(clientId)
                    .username(username)
                    .password(password)
                    .userAttributes(
                            AttributeType.builder()
                                    .name("name").value(name)
                                    .build(),
                            AttributeType.builder()
                                    .name("email").value(email)
                                    .build()
                    )
                    .build();


            cognitoIdentityProviderClient =
                    CognitoIdentityProviderClient.builder()
                            .region(Region.EU_WEST_1)
                            .credentialsProvider(AnonymousCredentialsProvider.create())
                            .build()
            ;

            SignUpResponse response = cognitoIdentityProviderClient.signUp(signUpRequest);

            log.info("response.sdkHttpResponse().statusCode() : {} - response.sdkHttpResponse().statusText() {}",
                    response.sdkHttpResponse().statusCode(),
                    response.sdkHttpResponse().statusText().isPresent() ? response.sdkHttpResponse().statusText().get() : ""
            );

            return response.sdkHttpResponse().isSuccessful();
        }
        finally {
            if ( cognitoIdentityProviderClient != null)
                cognitoIdentityProviderClient.close();
        }
    }


    /**
     *
     * @param username
     * @param password
     * @return
     */
    public Credentials signIn(String username, String password ) {

        log.warn("poolId {} clientId {} region {} fedPoolId {}", poolId, clientId, region, fedPoolId);

        CognitoIdentityProviderClient cognitoIdentityProviderClient = null;        try {
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
                            .region(Region.of(region))
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
            if ( ! ChallengeNameType.PASSWORD_VERIFIER.equals(response.challengeName())) {
                return null;
            }
            RespondToAuthChallengeRequest challengeRequest =
                    authenticationHelper.getRespondToAuthChallengeRequest(response, password);
            RespondToAuthChallengeResponse challengeResponse = cognitoIdentityProviderClient.respondToAuthChallenge(challengeRequest);
            //System.out.println(result);

            String idToken = challengeResponse.authenticationResult().idToken();
            JSONObject payload = CognitoJWTParser.getPayload(idToken);
            log.info("authentication payload {}", payload);

            String providerId = payload.get("iss").toString().replace("https://", "");
            log.info("providerId : {}", providerId);

            // get temp credential

            GetCredentialsForIdentityRequest identityRequest;
            CognitoIdentityClient cognitoIdentityClient =
                    CognitoIdentityClient
                            .builder()
                            .credentialsProvider(AnonymousCredentialsProvider.create())
                            .region(Region.of(region))
                            .build();

            Map<String, String> loginMap = new HashMap<>();
            loginMap.put(providerId, idToken);

            log.info("loginMap : {}", loginMap);

            GetIdRequest idRequest = GetIdRequest.builder()
                    //.accountId(username)
                    .identityPoolId(fedPoolId)
                    .logins(loginMap)
                    .build();

            GetIdResponse idResponse = cognitoIdentityClient.getId(idRequest);

            GetCredentialsForIdentityRequest credentialsForIdentityRequest = GetCredentialsForIdentityRequest.builder()
                    .identityId(idResponse.identityId())
                    .logins(loginMap)
                    .build();

            GetCredentialsForIdentityResponse credentialsForIdentityResponse =
                    cognitoIdentityClient.getCredentialsForIdentity(credentialsForIdentityRequest);


            log.info("temp credentials : {}", credentialsForIdentityResponse.credentials());


            return credentialsForIdentityResponse.credentials();

        } finally {
            if (cognitoIdentityProviderClient != null)
                cognitoIdentityProviderClient.close();
        }

    }

}
