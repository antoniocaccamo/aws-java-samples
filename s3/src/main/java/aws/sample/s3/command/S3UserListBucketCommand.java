package aws.sample.s3.command;

import aws.sample.helpers.CognitoHelper;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.model.Credentials;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import javax.inject.Inject;
import java.util.concurrent.Callable;

@Command(name = "user-ls") @Slf4j
public class S3UserListBucketCommand  implements Callable<Integer> {

    @Option(required = true, names = {"-u", "--username"})
    private String username;

    @Option(names = {"-p", "--password"}, description = "enter password", interactive = true, required = true)
    private String password;

    @Value("${app.aws.s3.bucket.region}")
    private String region;

    @Inject
    private CognitoHelper cognitoHelper;


    @Override
    public Integer call() throws Exception {
        log.info("trying access for user : {}", username);
        try {
            Credentials credentials = cognitoHelper.signIn(username, password);
            GetIdResponse idResponse = cognitoHelper.getLastGetIdResponse();
            log.info("got temporary credential");
            try (
                        S3Client s3Client = S3Client.builder()
                            .region(Region.of(region))
                            .credentialsProvider(
                                    StaticCredentialsProvider.create(
                                            AwsSessionCredentials.create(
                                                    credentials.accessKeyId(),
                                                    credentials.secretKey(),
                                                    credentials.sessionToken()
                                            )
                                    )
                            )
                            .build()
            ) {
                log.info("list buckets :");
                ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
                ListBucketsResponse listBucketsResponse = s3Client.listBuckets(listBucketsRequest);
                listBucketsResponse.buckets().stream().forEach(x -> log.info("\t{}", x.name()));
                return 0;
            } catch (Exception e) {
                log.error("error occurred : {}", e.getMessage());
                return 2;
            }
        } catch (Exception e) {
            log.error("error occurred : {}", e.getMessage());
            return 1;
        }
    }
}
