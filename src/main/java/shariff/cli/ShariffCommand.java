package shariff.cli;

import io.ballerina.cli.BLauncherCmd;
import java.io.PrintStream;
import java.util.List;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "shariff",
        description = "POC sub tool"
)
public class ShariffCommand implements BLauncherCmd {
    private final PrintStream printStream;
    @Option(
            names = {"--help", "-h", "?"},
            usageHelp = true
    )
    private boolean helpFlag;

    @Parameters(
            description = "User name"
    )
    private List<String> argList;

    public ShariffCommand() {
        this.printStream = System.out;
    }

    public ShariffCommand(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void execute() {
        if (this.helpFlag) {
            StringBuilder builder = new StringBuilder();
            builder.append("Sample tool for bal tool testing\n\n");
            builder.append("bal shariff [args]\n\n");
            builder.append("--args--\n");
            builder.append("  <name>\n");
            builder.append("    greets with a hello <name>\n\n");
            this.printStream.println(builder);
            return;
        }
        printStream.println("shariff command 1.0.0 is executing\n");
        if (this.argList == null || this.argList.size() != 1) {
            this.printStream.println("Invalid number of arguments recieved!\n try bal scanner --help for more information.");
            return;
        }
        printStream.println("Hello " + this.argList.get(0) + "!");
    }

    @Override
    public String getName() {
        return "shariff";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Sample tool for bal tool testing\n\n");
        out.append("bal shariff [args]\n\n");
        out.append("--args--\n");
        out.append("  <name>\n");
        out.append("    greets with a hello <name>\n\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("A sample tool built for testing bal tool functionality");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
