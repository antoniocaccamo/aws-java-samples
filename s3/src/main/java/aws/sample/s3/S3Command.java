package aws.sample.s3;

import aws.sample.s3.command.S3ListBucketCommand;
import aws.sample.s3.command.S3UploadToUserBucket;
import io.micronaut.configuration.picocli.PicocliRunner;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Arrays;
import java.util.concurrent.Callable;

@Command(name = "my-s3", description = "...",
        subcommands={
            S3ListBucketCommand.class
        ,   S3UploadToUserBucket.class

        },
        mixinStandardHelpOptions = true) @Slf4j
public class S3Command implements Callable<Integer> {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        log.info("args : {}", Arrays.asList(args));
        PicocliRunner.call( S3Command.class, args);
    }

    public Integer call() {
        // business logic here

        if (verbose) {
            System.out.println("Hi!");
        }

        return 0;
    }
}
