package aws.sample.s3;

import aws.sample.s3.command.S3Command;
import aws.sample.s3.command.S3ListBucketCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;

/**
 * @author antoniocaccamo on 22/08/2019.
 */

@SpringBootApplication @Slf4j
public class Main implements CommandLineRunner, ExitCodeGenerator {

    private final CommandLine.IFactory factory;
    private final S3Command s3Command;
    private int exitCode;

    public static void main(String[] args) {
        log.info(">>> application started <<<");
        //SpringApplication.run(Main.class, args);
        System.exit(SpringApplication.exit(SpringApplication.run(Main.class, args)));
        log.info(">>> application ended   <<<");
    }

    public Main(CommandLine.IFactory factory, S3Command s3Command, S3ListBucketCommand s3ListBucketCommand) {
        this.factory = factory;
        this.s3Command = s3Command;
    }

    @Override
    public int getExitCode() {
        return exitCode;
    }

//    @Override
//    public void run(String... args) throws Exception {
//        log.info("args : {}", args);
////        CommandLine commandLine = new CommandLine(s3Command);
////        commandLine.parseWithHandler(new CommandLine.RunLast(), strings);
//        exitCode = new CommandLine(s3Command, factory).execute(args);
//    }

    @Override
    public void run(String... args) throws Exception {
        CommandLine cl =  new CommandLine(s3Command, factory);
        exitCode = cl.execute(args);
    }

}