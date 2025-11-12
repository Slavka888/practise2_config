import java.io.*;
import java.util.*;

public class GraphvizGenerator {

    public String generateDot(DependencyGraph graph) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Dependencies {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=box];\n\n");

        // Узлы
        for (String pkg : graph.getAllPackages()) {
            sb.append("  \"").append(escape(pkg)).append("\";\n");
        }
        sb.append("\n");

        // Рёбра: package -> dependency
        Map<String, List<String>> adj = graph.getAdjacencyList();
        for (Map.Entry<String, List<String>> e : adj.entrySet()) {
            String from = e.getKey();
            for (String to : e.getValue()) {
                sb.append("  \"").append(escape(from)).append("\" -> \"").append(escape(to)).append("\";\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    public void saveDotFile(String content, String filename) throws IOException {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(filename))) {
            w.write(content);
        }
    }

    public void generatePDF(String dotFile, String outputPdf) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("dot", "-Tpdf", dotFile, "-o", outputPdf);
        pb.redirectErrorStream(true);
        Process p;
        try {
            
            p = pb.start();
        } catch (IOException ioe) {
            throw new Exception("Failed to start 'dot' process. Is Graphviz installed and in PATH?");
        }

        StringBuilder output = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exit = p.waitFor();
        if (exit != 0) {
            throw new Exception("Graphviz 'dot' exited with code " + exit + ". Output:\n" + output.toString());
        }
    }

    private String escape(String s) {
        return s.replace("\"", "\\\"");
    }
}