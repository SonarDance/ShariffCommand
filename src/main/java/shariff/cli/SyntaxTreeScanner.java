package shariff.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

public class SyntaxTreeScanner {
    // File to get the syntax tree from
    private File syntaxTreeFile;

    // variables to populate when the syntax tree is scanned
    private String engineId;
    private String filePath;

    

    // set of issues to be utilized for static code analysis via in SonarCloud generic issue data
    private ArrayList<JsonNode> issues = new ArrayList<>();

    public SyntaxTreeScanner(File syntaxTreeFile){
        this.syntaxTreeFile = syntaxTreeFile;
    }

    public JsonNode scanTree() throws IOException{
        // Read the syntax tree file into a string
        String syntaxTreeString = new String(Files.readAllBytes(syntaxTreeFile.toPath()));

        // Create an object mapper using Jackson library
        ObjectMapper stMapper = new ObjectMapper();

        // Parse the syntax tree string into a JSON object
        JsonNode stNode = stMapper.readTree(syntaxTreeString);

        // populate the engineId and filePath variables
        engineId = stNode.get("ballerinaVersion").textValue();
        // get the filename from the user input received via the bal shariff cli
        filePath = "main.bal"; //

        // Return the JSON object node for further actions
        return stNode;
    }

    // ============================================================
    // Create Diagnostic to support SonarCloud Generic Issue format
    // ============================================================
    public JsonNode generateSonarGenericIssueData(JsonNode stNode, String projectName, String sourceFileName){
        // get the modules object in the syntax tree
        JsonNode stModulesNode = stNode.get("modules");

        // get the projectName object in the modules object
        JsonNode stProjectNode = stModulesNode.get(projectName);

        // get the documents object in the projectName object
        JsonNode stDocumentsNode = stProjectNode.get("documents");

        // get the main.bal object in the documents object
        JsonNode stMainBalObject = stDocumentsNode.get(sourceFileName);

        // get the syntaxTree object in the sourceFileName object
        JsonNode syntaxTreeNode = stMainBalObject.get("syntaxTree");

        // get the members object in the syntaxTree Object
        JsonNode members = syntaxTreeNode.get("members");

        // Iterate through objects in the "members" array
        for (JsonNode memberNode : members) {
            // ===============================================
            // Check for Diagnostics inside of function bodies
            // ===============================================
            // Check if "functionBody" object exists
            if (memberNode.has("functionBody")) {
                JsonNode functionBody = memberNode.get("functionBody");

                // Check if "statements" array exists and is not empty
                if (functionBody.has("statements") && functionBody.get("statements").isArray()) {
                    JsonNode statementsArray = functionBody.get("statements");

                    // iterate through each object inside the statements array
                    JsonNode lastPositionNode = null;
                    for(JsonNode statementObject : statementsArray){
                        // get the last position node
                        if(statementObject.get("position") != null){
                            lastPositionNode = statementObject.get("position");
                        }

                        // Iterate through each object inside the main object
                        for (JsonNode subObject : statementObject) {
                            // Check if "diagnostics" array exists and is not empty
                            if (subObject.has("diagnostics") && subObject.get("diagnostics").isArray()) {
                                // Extract the diagnostics
                                JsonNode diagnosticsArray = subObject.get("diagnostics");

                                // ================================================
                                // SonarCloud Report Format Generation happens here
                                // ================================================
                                // Check if the diagnostics array contains one or more objects
                                if (diagnosticsArray.size() > 0) {
                                    // Create an object mapper to create a new object
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    // Create an ObjectNode for the aggregated JSON
                                    ObjectNode aggregatedJson = objectMapper.createObjectNode();
                                    // Create the primaryLocation object
                                    ObjectNode primaryLocation = objectMapper.createObjectNode();

                                    // Set the individual fields in the aggregated JSON
                                    aggregatedJson.put("engineId", engineId);
                                    for(JsonNode diagnosticsArrayNode : diagnosticsArray){
                                        aggregatedJson.put("ruleId", diagnosticsArrayNode.get("diagnosticInfo").get("code"));
                                        aggregatedJson.put("severity", "INFO");
                                        aggregatedJson.put("type", "CODE_SMELL");
                                        aggregatedJson.put("effortMinutes", 0);
                                        primaryLocation.put("message", diagnosticsArrayNode.get("message"));
                                    }
                                    primaryLocation.put("filePath", filePath);

                                    // Create the textRange object
                                    ObjectNode textRange = objectMapper.createObjectNode();
                                    textRange.put("startLine", lastPositionNode.get("startLine"));
                                    textRange.put("endLine", lastPositionNode.get("endLine"));
                                    textRange.put("startColumn", lastPositionNode.get("startColumn"));
                                    textRange.put("endColumn", lastPositionNode.get("endColumn"));

                                    primaryLocation.set("textRange", textRange);

                                    // Set the primaryLocation object in the aggregated JSON
                                    aggregatedJson.set("primaryLocation", primaryLocation);

                                    // put the Issue to the issues array
                                    issues.add(aggregatedJson);
                                }
                            }
                        }
                    }
                }
            }
        }

         // Create an issues Json Java Object to hold array of issues
        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode issuesJsonObject = objectMapper.createObjectNode();

        // create an issuesArray
        ArrayNode issuesArray = objectMapper.createArrayNode();
        for(JsonNode issue : issues){
            issuesArray.add(issue);
        }

        // populate the issues json object with the array
        issuesJsonObject.put("issues", issuesArray);

        // return the generated report node
        return issuesJsonObject;
    }
}