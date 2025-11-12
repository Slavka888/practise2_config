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

    public List<String> getLoadOrder() {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String pkg : allPackages) {
            inDegree.put(pkg, adjacencyList.getOrDefault(pkg, Collections.emptyList()).size());
        }

        Map<String, List<String>> reverse = new HashMap<>();
        for (String pkg : allPackages) reverse.put(pkg, new ArrayList<>());
        for (Map.Entry<String, List<String>> e : adjacencyList.entrySet()) {
            for (String dep : e.getValue()) {
                reverse.computeIfAbsent(dep, k -> new ArrayList<>()).add(e.getKey());
            }
        }

        Queue<String> q = new ArrayDeque<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) q.offer(e.getKey());
        }

        List<String> order = new ArrayList<>();
        while (!q.isEmpty()) {
            String cur = q.poll();
            order.add(cur);
            for (String dependent : reverse.getOrDefault(cur, Collections.emptyList())) {
                inDegree.put(dependent, inDegree.get(dependent) - 1);
                if (inDegree.get(dependent) == 0) q.offer(dependent);
            }
        }

        if (order.size() != allPackages.size()) {
            hasCycles = true;
            // добавить оставшиеся в конце (чтобы вернуть полный набор)
            for (String pkg : allPackages) {
                if (!order.contains(pkg)) order.add(pkg);
            }
        } else {
            hasCycles = false;
        }

        return order;
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