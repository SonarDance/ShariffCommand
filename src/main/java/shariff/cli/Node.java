package shariff.cli;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;
    private String type;
    private List<Node> children;

    public Node(String name, String type) {
        this.name = name;
        this.type = type;
        this.children = new ArrayList<>();
    }

    public String getName(){
        return name;
    }

    public String getType(){
        return type;
    }

    public List<Node> getChildren(){
        return children;
    }
}
