package shariff.cli;

import com.fasterxml.jackson.databind.JsonNode;
import io.ballerina.cli.BLauncherCmd;

import java.io.*;
import java.util.List;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "shariff",
        description = "Generate SARIF report for a given ballerina file"
)
public class ShariffCommand implements BLauncherCmd {
    private final PrintStream printStream;
    @Option(
            names = {"--help", "-h", "?"},
            usageHelp = true
    )
    private boolean helpFlag;

    @Parameters(
            description = "Ballerina file"
    )
    private List<String> argList;

    public ShariffCommand() {
        this.printStream = System.out;
    }

    public ShariffCommand(PrintStream printStream) {
        this.printStream = printStream;
    }

    @Override
    public void execute(){
        // if bal sheriff --help is passed
        if (this.helpFlag) {
            StringBuilder builder = new StringBuilder();
            builder.append("Sample tool for bal tool testing\n\n");
            builder.append("bal shariff \n\n");
            builder.append("  <balFileName.bal>\n");
            builder.append("    generates SARIF report for a ballerina project\n\n");
            this.printStream.println(builder);
            return;
        }

        printStream.println("shariff command 1.0.0 is executing\n");

        // ================
        // Initial Checkups
        // ================
        // if an invalid argument is passed to the bal shariff command
        if (this.argList == null || this.argList.size() != 1) {
            this.printStream.println("Invalid number of arguments recieved!\n try bal shariff --help for more information.");
            return;
        }

        // retrieve the user passed argument
        String userFile = this.argList.get(0); // userFile

        // check if the user passed file is a ballerina file or not
        String[] userFileExtension = userFile.split("\\.(?=[^\\.]+$)"); // [userFile, bal]
        if((userFileExtension.length != 2) || !userFileExtension[1].equals("bal")){
            this.printStream.println("Invalid file format received!\n file format should be of type '.bal'");
            return;
        }

        // check if such ballerina file exists in the working directory
        File tempFile = new File("./" + userFile);
        if(!tempFile.exists()){
            this.printStream.println("No such file exists!\n please check the file name and then re run the command");
            return;
        }

        // =====================
        // Building the jar file
        // =====================
        // create a process that builds the ballerina project in the working directory
        ProcessBuilder processBuilder = new ProcessBuilder();

        // for now the command is supported for the windows platform
        // The ballerina file should be inside a ballerina project for the build process to work
        processBuilder.command("cmd", "/c", "bal", "build");

        // Start the build process
        try{
            Process process = processBuilder.start();
            this.printStream.println(userFile + " is being built...");

            // read the output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                this.printStream.println(line);
            }
        }catch (Exception e){
            this.printStream.println("Error!, unable to execute the ballerina file");
            this.printStream.println(e);
        }

        // =======================
        // Extracting the jar file
        // =======================
        // get the list of files in the target/bin folder of the package
        File[] listOfFiles = new File("./target/bin").listFiles();

        // get the jar file from the list of Files
        String jarFile = listOfFiles[0].getName();

        // Extract the jarFile
        processBuilder.command(
            "cmd", "/c", "cd", "./target/bin", "&&",
            "jar", "xf", jarFile);
        
        // start the extraction process
        try{
            Process process = processBuilder.start();
            this.printStream.println("Extracting " + jarFile + "...");

            // read the output of the process
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                this.printStream.println(line);
            }
        }catch(Exception e){
                this.printStream.println("Error!, unable to extract the jar file");
                this.printStream.println(e);
            }

        // ======================
        // Moving the Syntax Tree
        // ======================
        // move the syntax-tree.json file in the syntax-tree folder of the extracted jar folder to the working directory
        processBuilder.command(
            "cmd", "/c", "cd", "./target/bin/syntax-tree", "&&",
            "move", "./syntax-tree.json", "../../../");
        
        // start the moving process
        try{
            Process process = processBuilder.start();
            this.printStream.println("Moving syntax-tree.json...");
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                this.printStream.println(line);
            }
        }catch(Exception e){
                this.printStream.println("Error!, unable to move the syntax-tree.json file");
                this.printStream.println(e);
        }

        // ==================================================================
        // Using the syntax tree by a scanner to perform static code analysis
        // ==================================================================
        // pass the syntax tree json and convert it to nodes
        SyntaxTreeScanner stScanner = new SyntaxTreeScanner(new File("./syntax-tree.json"));

        try {
            this.printStream.println("Starting the static code analysis for the generated syntax tree...");
            JsonNode stNode = stScanner.scanTree();

            // display the syntax tree is prettier format in the console
            this.printStream.println(stNode.toPrettyString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "shariff";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Tool for generating SARIF report for provided ballerina file\n\n");
        out.append("bal shariff [args]\n\n");
        out.append("--args--\n");
        out.append("  <balFileName.bal>\n");
        out.append("    generates a SARIF report\n\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("Tool for generating SARIF report for provided ballerina file");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
