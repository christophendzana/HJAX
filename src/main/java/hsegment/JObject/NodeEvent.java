/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package APIXPath;

import java.util.List;

/**
 *
 * @author FIDELE
 */
public class NodeEvent{    
    
    public static final int SIMPLE_NODE = 0;
    public static final int NODE_WITH_PREDICATE = 1;
    public static final int NODE_WITH_FUNCTION = 2;

    private String nodeName;
    private String predicate;
    private String functionName;
    private List<String> functionArgs;

    //   0 = / (enfant direct) | 1 = // (descendant)
    private Integer depth;

    private int eventType;

    /**
     * Constructeur pour un nœud simple.
     *
     * @param nodeName Nom du nœud
     * @param depth    0 pour "/", 1 pour "//"
     */
    public NodeEvent(String nodeName, Integer depth) {
        this.nodeName = nodeName;
        this.depth = depth;
        this.eventType = SIMPLE_NODE;
    }

    /**
     * Constructeur pour un nœud avec prédicat.
     *
     * @param nodeName Nom du nœud
     * @param predicate Contenu du prédicat
     * @param depth 0 pour "/", 1 pour "//"
     */
    public NodeEvent(String nodeName, String predicate, Integer depth) {
        this.nodeName = nodeName;
        this.predicate = predicate;
        this.depth = depth;
        this.eventType = NODE_WITH_PREDICATE;
    }

    /**
     * Constructeur pour un nœud avec fonction.
     *
     * @param nodeName Nom du nœud
     * @param functionName Nom de la fonction
     * @param args Arguments de la fonction
     * @param depth 0 pour "/", 1 pour "//"
     */
    public NodeEvent(String nodeName, String functionName, List<String> args, Integer depth) {
        this.nodeName = nodeName;
        this.functionName = functionName;
        this.functionArgs = args;
        this.depth = depth;
        this.eventType = NODE_WITH_FUNCTION;
    }

    // Getters
    public String getNodeName() {
        return nodeName;
    }

    public String getPredicate() {
        return predicate;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getFunctionArgs() {
        return functionArgs;
    }

    public Integer getDepth() {
        return depth;
    }

    public int getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NodeEvent{node='").append(nodeName).append('\'');
        sb.append(", depth=").append(depth == 0 ? "/" : "//");

        switch (eventType) {
            case NODE_WITH_PREDICATE:
                sb.append(", predicate='").append(predicate).append('\'');
                break;
            case NODE_WITH_FUNCTION:
                sb.append(", function='").append(functionName)
                  .append("', args=").append(functionArgs);
                break;
        }
        sb.append('}');
        return sb.toString();
    }
    
    
}
