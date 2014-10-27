package com.liveperson.infra.akka.actorx.ui;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Amit Tal
 * @since 10/26/2014
 */
public class ActorXCastGraphBuilder {

    public static final String VERTICES = "Vertices:";
    public static final String EDGES = "Edges:";

    private Map<String, Map<String, Set<String>>> castConnectionList;
    private Graph<String, String> graph;

    public ActorXCastGraphBuilder() {
        this.castConnectionList = new HashMap<String, Map<String, Set<String>>>();
        this.graph = new SparseMultigraph<String, String>();
    }

    public void initFromString(String input) {

        final String SEPARATOR = "###";
        int index = input.indexOf(SEPARATOR);

        String vertices = input.substring(0, index).trim();
        String edges = input.substring(index + SEPARATOR.length() + 1).trim();

        vertices = vertices.substring(VERTICES.length());
        String[] actorNames = vertices.split(",");
        for (String actorName : actorNames) {
            graph.addVertex(actorName);
            castConnectionList.put(actorName, new HashMap<String, Set<String>>());
        }

        edges = edges.substring(EDGES.length());
        String[] edgesStrings = edges.split(",");
        for (String edgeString : edgesStrings) {
            String[] edge = edgeString.split("\\|");
            String messageClass = edge[0];
            String fromActor = edge[1];
            String toActor = edge[2];
            Map<String, Set<String>> actorConnections = castConnectionList.get(fromActor);
            if (!(actorConnections.containsKey(toActor))) {
                actorConnections.put(toActor, new HashSet<String>());
            }
            Set<String> messages = actorConnections.get(toActor);
            if (messages.isEmpty()) {
                graph.addEdge(fromActor + Main.DELIMITER + toActor, fromActor, toActor, EdgeType.DIRECTED);
            }
            messages.add(messageClass);
        }
    }

    public Map<String, Map<String, Set<String>>> getCastConnectionList() {
        return castConnectionList;
    }

    public Graph<String, String> getGraph() {
        return graph;
    }
}
