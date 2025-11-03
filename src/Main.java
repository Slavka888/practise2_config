import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            //Этап 1: Чтение конфигурации
            String configPath = args.length > 0 ? args[0] : "config.xml";
            ConfigParser parser = new ConfigParser();
            Config config = parser.parse(configPath);

            //Вывод параметров конфигурации
            System.out.println("Configuration parameters:");
            System.out.println("package_name = " + config.getPackageName());
            System.out.println("repository_url = " + config.getRepositoryUrl());
            System.out.println("test_mode = " + config.isTestMode());
            System.out.println("output_file = " + config.getOutputFile());
            System.out.println();

            // Этап 2: Получение прямых зависимостей
            if (config.isTestMode()) {
                System.out.println("Test mode is enabled");
            } else {
                System.out.println("Fetching dependencies from PyPI...");
                PyPIClient client = new PyPIClient();

                List<String> dependencies = client.getDependencies(config.getPackageName());

                System.out.println("Total direct dependencies: " + dependencies.size());
            }

            // Этап 3: Построение графа зависимостей с BFS
            System.out.println("Building dependency graph using BFS...");
            DependencyResolver resolver = new DependencyResolver(config);
            DependencyGraph graph = resolver.buildDependencyGraph();

            System.out.println("\nGraph Statistics");
            System.out.println("Total packages: " + graph.getAllPackages().size());
            System.out.println("Has cycles: " + (graph.hasCycles() ? "YES" : "NO"));

            System.out.println("\nAll Packages in Graph");
            int i = 1;
            for (String pkg : graph.getAllPackages()) {
                List<String> deps = graph.getDirectDependencies(pkg);
                System.out.println(i++ + ". " + pkg + " -> " + deps.size() + " dependencies");
            }

            System.out.println("\nDirect Dependencies of " + config.getPackageName() + " ===");
            List<String> directDeps = graph.getDirectDependencies(config.getPackageName());
            if (directDeps.isEmpty()) {
                System.out.println("  (no dependencies)");
            } else {
                for (String dep : directDeps) {
                    System.out.println("  - " + dep);
                }
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}