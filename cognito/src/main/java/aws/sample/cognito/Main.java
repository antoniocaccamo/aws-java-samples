package aws.sample.cognito;

import aws.sample.cognito.command.CognitoCommand;
import aws.sample.cognito.command.CognitoSignUpCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import picocli.CommandLine;

@SpringBootApplication @Slf4j
public class Main implements CommandLineRunner, ExitCodeGenerator {

    private final CommandLine.IFactory factory;
    private final CognitoCommand cognitoCommand;
    private final CognitoSignUpCommand createUserCommand;
    private int exitCode;

    public static void main(String[] args) {
        log.info(">>> application started <<<");
        //SpringApplication.run(Main.class, args);
        System.exit(SpringApplication.exit(SpringApplication.run(Main.class, args)));
        log.info(">>> application ended   <<<");
    }

    public Main(CommandLine.IFactory factory, CognitoCommand cognitoCommand, CognitoSignUpCommand createUserCommand) {
        this.factory = factory;
        this.cognitoCommand = cognitoCommand;
        this.createUserCommand = createUserCommand;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("args : {}", args);
        CommandLine cl =  new CommandLine(cognitoCommand, factory);
//        cl.addSubcommand( "create", createUserCommand);
        exitCode = cl.execute(args);
    }


    @Override
    public int getExitCode() {
        return exitCode;
    }
}