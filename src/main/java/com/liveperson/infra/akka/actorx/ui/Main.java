package com.liveperson.infra.akka.actorx.ui;

import edu.uci.ics.jung.graph.Graph;

import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Amit Tal
 * @since 10/26/2014
 */
public class Main {

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String DELIMITER = "#@#";

    public static void main(String[] args) throws Exception {

        String input = null;
        if (args == null || args.length == 0) {
            printUsageAndExit();
        }
        else if (args.length == 1) {
            input = args[0];
        }
        else if (args.length == 2 && args[0].equals("-f")) {
            input = readFromFile(args[1]);
        }
        else {
            printUsageAndExit();
        }

        ActorXCastGraphBuilder actorXCastGraphBuilder = new ActorXCastGraphBuilder();
        actorXCastGraphBuilder.initFromString(input);

        DisplayGraph.setCastConnectionList(actorXCastGraphBuilder.getCastConnectionList());
        DisplayGraph.setGraph(actorXCastGraphBuilder.getGraph());
        JPanel jp = DisplayGraph.getGraphPanel();
        JFrame jf = new JFrame();
        jf.getContentPane().add(jp);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
    }

    private static String readFromFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    private static void printUsageAndExit() {
        System.out.println("USAGE: java -jar akka-actor-x-ui-0.1-SNAPSHOT.one-jar.jar \"<INPUT STRING>\"");
        System.out.println("USAGE: java -jar akka-actor-x-ui-0.1-SNAPSHOT.one-jar.jar -f \"<PATH TO FILE>\"");
        System.exit(0);
    }
}
