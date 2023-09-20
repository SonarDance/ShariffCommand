package balToolTester;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.ParserFactory;
import io.ballerina.compiler.internal.parser.tree.STNode;
import shariff.cli.SyntaxTreeScanner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Main {
    public static void main(String[] args)  throws IOException {
        System.out.println("Welcome to bal shariff tool tester!");


        // ====================
        // Tool Testing Methods
        // ====================
//        testJacksonSyntaxTree();
        showTokens();
    }

    public static void showTokens() throws IOException{

        // get the relevant file
        File sourceCodeFile = new File("./shariff-tester/main.bal");

        // get the file code in string format
        String sourceCodeString = new String(Files.readAllBytes(sourceCodeFile.toPath()));

        // Create a Syntax Tree of the source file
        BallerinaParser parseFile = ParserFactory.getParser(sourceCodeString);
        STNode sourceFileSyntaxtTree = parseFile.parse();

        // Print all tokens individually
        for(STNode tokenNode : sourceFileSyntaxtTree.tokens()){
            System.out.println("TOKEN: " + tokenNode);
        }
    }

    // Method to test modifying a syntax tree according to the form we require
    public static void testJacksonSyntaxTree()  throws IOException {
        // obtain the syntax tree file, the project executes from the ShariffCommand directory itself
        File syntaxTreeFile = new File("./shariff-tester/syntax-tree.json");

        // pass the file to be held in the class
        SyntaxTreeScanner syntaxTree = new SyntaxTreeScanner(syntaxTreeFile);

        // use the class passed to the file to create a syntax tree representation as a java object
        JsonNode stNode = syntaxTree.scanTree();

        // get the diagnostics of the syntax tree as a Json java object
        String projectName = "shariff_tester";
        String sourceFileName = "main.bal";
        JsonNode sonarGenericIssuesNode = syntaxTree.generateSonarGenericIssueData(stNode, projectName, sourceFileName);

        // save the generic issue data as a json file
        try {
            // Convert JsonNode to JSON string
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonAsString = objectMapper.writeValueAsString(sonarGenericIssuesNode);

            // Specify the file path where you want to save the JSON file
            String filePath = "./shariff-tester/ballerina-sonar-generic-report.json";

            // Write the JSON string to a file
            File jsonFile = new File(filePath);
            objectMapper.writeValue(jsonFile, sonarGenericIssuesNode);

            System.out.println("JSON saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // print the results of the diagnostics object in the console
        System.out.println(sonarGenericIssuesNode.toPrettyString());
    }
}
