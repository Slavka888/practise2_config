import java.util.*;

public class DependencyGraph {
    private Map<String, List<String>> adjacencyList;
    private Set<String> allPackages;
    private boolean hasCycles;

    public DependencyGraph() {
        this.adjacencyList = new HashMap<>();
        this.allPackages = new HashSet<>();
        this.hasCycles = false;
    }

    public void addPackage(String packageName) {
        allPackages.add(packageName);
        if (!adjacencyList.containsKey(packageName)) {
            adjacencyList.put(packageName, new ArrayList<>());
        }
    }

    public void addDependency(String from, String to) {
        addPackage(from);
        addPackage(to);

        List<String> deps = adjacencyList.get(from);
        if (!deps.contains(to)) {
            deps.add(to);
        }
    }

    public List<String> getDirectDependencies(String packageName) {
        return new ArrayList<>(adjacencyList.getOrDefault(packageName, new ArrayList<>()));
    }

    public Set<String> getAllPackages() {
        return new HashSet<>(allPackages);
    }

    public Map<String, List<String>> getAdjacencyList() {
        Map<String, List<String>> copy = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        return copy;
    }

    public boolean hasCycles() {
        return hasCycles;
    }

    public void setHasCycles(boolean hasCycles) {
        this.hasCycles = hasCycles;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DependencyGraph{\n");
        sb.append("  packages: ").append(allPackages.size()).append("\n");
        sb.append("  hasCycles: ").append(hasCycles).append("\n");
        sb.append("  adjacencyList:\n");
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}