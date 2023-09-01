package shariff.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class SyntaxTreeScanner {
    private File syntaxTreeFile;

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

        // Return the JSON object node for further actions
        return stNode;
    }
}
