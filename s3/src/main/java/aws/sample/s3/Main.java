package aws.sample.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
/**
 * @author antoniocaccamo on 22/08/2019.
 */

@SpringBootApplication @Slf4j
public class Main implements CommandLineRunner{

    public static void main(String[] args) {
        log.info(">>> application started <<<");
        SpringApplication.run(Main.class, args);
        log.info(">>> application ended   <<<");
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("creating S3 client..");
        try (S3Client s3 = S3Client.builder().build()){
            log.info("list buckets..");
            ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
            ListBucketsResponse listBucketsResponse = s3.listBuckets(listBucketsRequest);
            listBucketsResponse.buckets().stream().forEach(x -> System.out.println(x.name()));
        }catch (Exception e){
            log.error("error occurred", e);
        }
    }

}