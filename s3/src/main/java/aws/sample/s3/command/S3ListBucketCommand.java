package aws.sample.s3.command;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;

import javax.inject.Singleton;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

/**
 * @author antoniocaccamo on 22/08/2019.
 */

@Singleton
@Command(name = "list", aliases = {"ls"}) @Slf4j
public class S3ListBucketCommand implements Callable<Integer>{

    @Override
    public Integer call() throws Exception{
    log.info("creating S3 client..");
        try (S3Client s3 = S3Client.builder().build()){
            log.info("list buckets..");
            ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
            ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
            listBucketsResponse.buckets().stream().forEach(x -> System.out.println(x.name()));
        }catch (Exception e){
            log.error("error occurred", e);
        }
        return 0;
    }
}