package aws.sample.s3.command;

import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

import static picocli.CommandLine.Command;

/**
 * @author antoniocaccamo on 22/08/2019.
 */
@Component
@Command( name = "s3",
          subcommands = {
                  S3ListBucketCommand.class
          }
)
public class S3Command implements Callable<Integer>{

    @Override
    public Integer call() throws Exception {
        return 0;
    }
}