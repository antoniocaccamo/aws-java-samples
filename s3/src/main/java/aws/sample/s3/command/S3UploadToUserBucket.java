package aws.sample.s3.command;

import aws.sample.helpers.CognitoHelper;
import io.micronaut.context.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.model.Credentials;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import javax.inject.Inject;
import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "uploadUser") @Slf4j
public class S3UploadToUserBucket implements Callable<Integer> {

    @Option(required = true, names = {"-u", "--username"})
    private String username;

    @Option(names = {"-p", "--password"}, description = "enter password", interactive = true, required = true)
    private String password;

    @Option(names = {"-f", "--file"}, description = "file to upload", required = true)
    private File file;

    @Value("${app.aws.s3.bucket.name}")
    private String s3BucketName;

    @Value("${app.aws.s3.bucket.prefix}")
    private String s3BucketPrefix;

    @Value("${app.aws.s3.bucket.region}")
    private String region;

    @Inject
    private CognitoHelper cognitoHelper;


    @Override
    public Integer call() throws Exception {

        log.info("trying access for user : {}", username);


        Credentials credentials = cognitoHelper.signIn(username, password);

        GetIdResponse idResponse = cognitoHelper.getLastGetIdResponse();

        log.info("got temporary credential");
        try (
            S3Client s3Client =S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(
                            StaticCredentialsProvider.create( AwsBasicCredentials.create(credentials.accessKeyId(), credentials.secretKey()))
                    )
                    .build();
        ){
            String key = new StringBuffer(s3BucketPrefix).append("/")
                            .append(idResponse.identityId()).append("/")
                            .append(file.getName())
                            .toString();
            log.info("uploading file [{}] to bucket [{}] with key [{}] ...", file.getAbsolutePath(), s3BucketName, key);
//            CreateBucketResponse createBucketResponse= s3Client.createBucket(CreateBucketRequest
//                    .builder()
//                    .bucket(bucketName)
//                    .build()
//            );
//            if ( createBucketResponse.sdkHttpResponse().isSuccessful() ) {
                PutObjectRequest putObjectRequest =
                        PutObjectRequest.builder()
                                .bucket(s3BucketName)
                                .key(key)
                                .build();
                PutObjectResponse putObjectResponse = s3Client.putObject(putObjectRequest, file.toPath());
                log.info( "putObjectResponse : {}", putObjectResponse.sdkHttpResponse() );
//            } else {
//                log.error("can't create bucket {}", bucketName);
//            }
        } catch (Exception e){
            log.error("error occurred : {}", e.getMessage());
        }



        return 0;
    }
}
