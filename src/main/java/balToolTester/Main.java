package balToolTester;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.ParserFactory;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Module;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.tools.text.LineRange;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import shariff.cli.SyntaxTreeScanner;

// Imports relevant to getting the Ballerina Semantic Model
import io.ballerina.projects.PackageCompilation;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.compiler.syntax.tree.SyntaxTree;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class Main {
    public static void main(String[] args)  throws IOException {
        // ====================
        // Tool Testing Methods
        // ====================
        // testJacksonSyntaxTree();
        // showTokens();
        semanticAPIOperations();
    }

    public static void semanticAPIOperations(){
        // get the source file
        File sourceFile = new File("./shariff-tester/main.bal");

        // Receive absolute path of the ballerina source file
        // Path filePath = Path.of(sourceFile.getAbsolutePath());
        Path filePath = Path.of(sourceFile.getPath());

        // Load the ballerina file
        Project project = ProjectLoader.loadProject(filePath);

        // get the document ID by considering if the project structure is relevant to Ballerina
        DocumentId documentId = project.documentId(filePath);
        if (project.kind().equals(ProjectKind.BUILD_PROJECT)) {
            documentId = project.documentId(filePath);
        } else {
            // If project structure is different go to the next document
            Module currentModule = project.currentPackage().getDefaultModule();
            Iterator<DocumentId> documentIterator = currentModule.documentIds().iterator();

            // block is used to prevent crashing
            try{
            documentId = documentIterator.next();
            }catch (NoSuchElementException exception){
                System.out.println("Error: " + exception);
            }
        }

        // Compile the Ballerina source code file
        PackageCompilation compilation = project.currentPackage().getCompilation();

        // Retrieve the BLangPackage Node
        BLangPackage bLangPackage = compilation.defaultModuleBLangPackage();
        System.out.println("Rules start from here");

        // To retrieve the semantic model
        // SemanticModel semanticModel = compilation.getSemanticModel(documentId.moduleId());

        // ========================
        // Creating One simple Rule
        // ========================
        // This rule should be able to determine all functions who's parameters are greater than 7
        // and report them with the accurate location information

        // Obtain all functions from the syntax tree
        List<BLangFunction> functions = bLangPackage.getFunctions();

        // Only run the rule if the functions are not empty
        if(!functions.isEmpty()){
            functions.forEach(bLangFunction -> {
                // Only trigger the check if there are parameters and the count is greater than 7
                if(!bLangFunction.getParameters().isEmpty() && bLangFunction.getParameters().size() > 7){
                    System.out.println("Display: error number");
                    System.out.println("Message: Too many parameters in method");
                    System.out.println("Issue spotted in the range of:");

                    // Get position information where the issue has occured
                    LineRange issueLocation = bLangFunction.getPosition().lineRange();
                    System.out.println("Start line: " + issueLocation.startLine().line());
                    System.out.println("Start offset: " + issueLocation.startLine().offset());
                    System.out.println("End line: " + issueLocation.endLine().line());
                    System.out.println("End offset: " + issueLocation.endLine().offset());
                    System.out.println();
                }
            });
        }



        // Retrieve the semantic model of the compiled source code
        // SemanticModel semanticModel = compilation.getSemanticModel(documentId.moduleId());
    }

    public static void showTokens() throws IOException{

        // get the relevant file
        File sourceCodeFile = new File("./shariff-tester/main.bal");

        // get the file code in string format
        String sourceCodeString = new String(Files.readAllBytes(sourceCodeFile.toPath()));

        // Create a Syntax Tree of the source file
        BallerinaParser parseFile = ParserFactory.getParser(sourceCodeString);

        STNode sourceFileSyntaxTree = parseFile.parse();
        for(STNode token : sourceFileSyntaxTree.tokens()){
            System.out.println(token.toString());
        }

        // SLang test: get a specific token from a set of tokens which match the word record
        System.out.println("TOKENS with 'record' keyword");
        for(STNode token : sourceFileSyntaxTree.tokens()){
            if(token.kind.equals(SyntaxKind.RECORD_KEYWORD)){
                System.out.println(token);
            }
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
