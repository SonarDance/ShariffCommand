package shariff.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.cli.BLauncherCmd;

import java.io.*;
import java.util.List;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(
        name = "shariff",
        description = "Generate Syntax Tree based reports for a given ballerina source file"
)
public class ShariffCommand implements BLauncherCmd {
    // CMD Launcher Attributes
    private final PrintStream printStream;
    @Option(
            names = {"--help", "-h", "?"},
            usageHelp = true
    )
    private boolean helpFlag;

    @Option(
            names = {"--generate-generic-report"}
    )
    private boolean genericsIssueDataFlag;

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

    // FEATURE Methods
    public String checkFile(){
        // ================
        // Initial Checkups
        // ================
        // if an invalid argument is passed to the bal shariff command
        if (this.argList == null || this.argList.size() != 1) {
            this.printStream.println("Invalid number of arguments recieved!\n try bal shariff --help for more information.");
            return "";
        }

        // retrieve the user passed argument
        String userFile = this.argList.get(0); // userFile

        // check if the user passed file is a ballerina file or not
        String[] userFileExtension = userFile.split("\\.(?=[^\\.]+$)"); // [userFile, bal]
        if((userFileExtension.length != 2) || !userFileExtension[1].equals("bal")){
            this.printStream.println("Invalid file format received!\n file format should be of type '.bal'");
            return "";
        }

        // check if such ballerina file exists in the working directory
        File tempFile = new File("./" + userFile);
        if(!tempFile.exists()){
            this.printStream.println("No such file exists!\n please check the file name and then re run the command");
            return "";
        }

        // return name of user file if it exists
        return userFile;
    }

    public void generateGenericIssuesReport(String userFile){
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

        // =============================================================
        // Generating a generic issues JSON file through the syntax tree
        // =============================================================
        // retrieve the syntax tree file, the project executes from the ShariffCommand directory itself
        File syntaxTreeFile = new File("./syntax-tree.json");

        // pass the file to the syntax tree scanner
        SyntaxTreeScanner syntaxTree = new SyntaxTreeScanner(syntaxTreeFile);

        // use the syntax tree scanner to convert the JSON file to Java Objects using Jackson
        JsonNode stNode = null;
        try {
            this.printStream.println("Onboarding syntax-tree.json to memory...");
            stNode = syntaxTree.scanTree();
        } catch (IOException e) {
            this.printStream.println("Error!, unable to move the syntax-tree.json file");
            this.printStream.println(e);
        }

        // get the diagnostics of the syntax tree as a Json java object
        // receive the project name through the jar file
        String projectName = jarFile.split("\\.(?=[^\\.]+$)")[0]; // [jarfile_name, .jar]
        JsonNode sonarGenericIssuesNode = syntaxTree.generateSonarGenericIssueData(stNode, projectName, userFile);

        // save the generic issue data as a json file
        try {
            this.printStream.println("Generating Sonar Generic Issue report...");
            // Convert JsonNode to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonAsString = objectMapper.writeValueAsString(sonarGenericIssuesNode);

            // Specify the file path where you want to save the JSON file
            String filePath = "./ballerina-sonar-generic-report.json";

            // Write the JSON string to a file
            File jsonFile = new File(filePath);
            objectMapper.writeValue(jsonFile, sonarGenericIssuesNode);

            this.printStream.println("Ballerina Sonar Generic Report created successfully!");
        } catch (IOException e) {
            this.printStream.println("Error!, unable to Create a generic report!");
            this.printStream.println(e);
        }

        // Print the generated report data in the console
        this.printStream.println(sonarGenericIssuesNode.toPrettyString());
    }

    // MAIN method
    @Override
    public void execute(){
        // if bal shariff --help is passed
        if (this.helpFlag) {
            StringBuilder builder = new StringBuilder();
            builder.append("Tool for generating reports based off of the syntax tree of a ballerina source file\n\n");
            builder.append("bal shariff <ballerina-file-name> <command>\n\n");
            builder.append("--ballerina-file-name--\n");
            builder.append("  i.e: balFileName.bal\n");
            builder.append("--command--\n");
            builder.append("  --generate-generic-report\n");
            builder.append("  --generate-Slang-ast [BETA]\n");
            builder.append("  --generate-sarif-report [NOT IMPLEMENTED]\n");
            builder.append("i.e: bal shariff main.bal --generate-generic-report\n");
            this.printStream.println(builder);
            return;
        }

        printStream.println("shariff command 1.0.0 is executing\n");

        // check for user provided file in directory
        String userFile = checkFile();
        if(userFile.equals("")){
            return;
        }

        // if --generate-generic-issues is passed
        if(this.genericsIssueDataFlag){
            generateGenericIssuesReport(userFile);
        }
    }

    // INFO Methods
    @Override
    public String getName() {
        return "shariff";
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append("Tool for generating reports based off of the syntax tree of a ballerina source file\n\n");
        out.append("bal shariff <ballerina-file-name> <command>\n\n");
        out.append("--ballerina-file-name--\n");
        out.append("  i.e: balFileName.bal\n");
        out.append("--command--\n");
        out.append("  --generate-generic-report\n");
        out.append("  --generate-Slang-ast [BETA]\n");
        out.append("  --generate-sarif-report [NOT IMPLEMENTED]\n");
        out.append("    generate reports based on ST of a ballerina file\n\n");
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("Tool for generating reports based off of the syntax tree of a ballerina source file");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }
}
