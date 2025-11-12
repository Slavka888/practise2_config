
import java.util.*;

public class DependencyResolver {
    private Config config;
    private PyPIClient pypiClient;
    private static final int MAX_DEPTH = 10; // Защита от слишком глубоких графов

    public DependencyResolver(Config config) {
        this.config = config;
        this.pypiClient = new PyPIClient();
    }

    public DependencyGraph buildDependencyGraph() throws Exception {
        if (config.isTestMode()) {
            System.out.println("Using test mode - loading from file: " + config.getRepositoryUrl());
            return buildFromTestFile(config.getRepositoryUrl());
        } else {
            System.out.println("Using PyPI mode - fetching from internet");
            return buildFromPyPI();
        }
    }

    private DependencyGraph buildFromPyPI() throws Exception {
        DependencyGraph graph = new DependencyGraph();

        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        Map<String, Integer> depth = new HashMap<>();

        String rootPackage = config.getPackageName();
        queue.offer(rootPackage);
        depth.put(rootPackage, 0);
        graph.addPackage(rootPackage);

        System.out.println("Starting BFS from: " + rootPackage);
        int processed = 0;

        // BFS без рекурсии
        while (!queue.isEmpty()) {
            String currentPackage = queue.poll();
            int currentDepth = depth.get(currentPackage);

            if (visited.contains(currentPackage)) {
                // Уже посещали - возможен цикл
                continue;
            }

            if (currentDepth >= MAX_DEPTH) {
                System.out.println("  Max depth reached for: " + currentPackage);
                continue;
            }

            visited.add(currentPackage);
            processed++;

            System.out.println("  [" + processed + "] Processing: " + currentPackage + " (depth: " + currentDepth + ")");

            try {
                List<String> dependencies = pypiClient.getDependencies(currentPackage);
                System.out.println("    Found " + dependencies.size() + " dependencies");

                for (String dep : dependencies) {
                    String cleanDep = pypiClient.cleanPackageName(dep);

                    graph.addDependency(currentPackage, cleanDep);

                    // Проверка на цикл
                    if (visited.contains(cleanDep)) {
                        System.out.println("   Cycle detected: " + cleanDep);
                        graph.setHasCycles(true);
                    } else if (!depth.containsKey(cleanDep)) {
                        queue.offer(cleanDep);
                        depth.put(cleanDep, currentDepth + 1);
                    }
                }

                // Небольшая задержка чтобы не перегружать PyPI
                Thread.sleep(100);

            } catch (Exception e) {
                System.out.println("    Warning: Could not fetch dependencies for " + currentPackage + ": " + e.getMessage());
            }
        }

        System.out.println("BFS completed. Processed " + processed + " packages.");

        return graph;
    }

    private DependencyGraph buildFromTestFile(String filePath) throws Exception {
        DependencyGraph graph = new DependencyGraph();
        Map<String, List<String>> testData = FileUtils.parseTestRepository(filePath);

        System.out.println("Loaded test repository with " + testData.size() + " packages");

        for (Map.Entry<String, List<String>> entry : testData.entrySet()) {
            String pkg = entry.getKey();
            graph.addPackage(pkg);

            for (String dep : entry.getValue()) {
                graph.addDependency(pkg, dep);
                System.out.println("  " + pkg + " -> " + dep);
            }
        }

        // Обнаружение циклов
        detectCycles(graph);

        return graph;
    }

    private void detectCycles(DependencyGraph graph) {
        Set<String> visited = new HashSet<>();
        Set<String> recStack = new HashSet<>();

        for (String pkg : graph.getAllPackages()) {
            if (hasCycleDFS(pkg, graph, visited, recStack)) {
                System.out.println(" Cycle detected in graph!");
                graph.setHasCycles(true);
                return;
            }
        }

        System.out.println(" No cycles detected");
    }

    private boolean hasCycleDFS(String node, DependencyGraph graph, Set<String> visited, Set<String> recStack) {
        if (recStack.contains(node)) {
            return true; // Цикл найден
        }
        if (visited.contains(node)) {
            return false; // Уже проверяли
        }

        visited.add(node);
        recStack.add(node);

        for (String neighbor : graph.getDirectDependencies(node)) {
            if (hasCycleDFS(neighbor, graph, visited, recStack)) {
                return true;
            }
        }

        recStack.remove(node);
        return false;
    }
}