import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

            System.out.println("\nDirect Dependencies of " + config.getPackageName() + ":");
            List<String> directDeps = graph.getDirectDependencies(config.getPackageName());
            if (directDeps.isEmpty()) {
                System.out.println("  (no dependencies)");
            } else {
                for (String dep : directDeps) {
                    System.out.println("  - " + dep);
                }
            }

            //Этап 4. Дополнительные операции
            List<String> loadOrder = graph.getLoadOrder();

            // Печать простым списком
            System.out.println("Load order (dependencies first):");
            for (i = 0; i < loadOrder.size(); i++) {
                System.out.printf(" %2d. %s\n", i + 1, loadOrder.get(i));
            }
            System.out.println();

            // Простая валидация порядка
            Map<String, Integer> pos = new HashMap<>();
            for (i = 0; i < loadOrder.size(); i++) {
                pos.put(loadOrder.get(i), i);
            }

            boolean ok = true;
            for (String pkg : graph.getAllPackages()) {
                for (String dep : graph.getDirectDependencies(pkg)) {
                    Integer pPkg = pos.get(pkg);
                    Integer pDep = pos.get(dep);
                    if (pPkg == null || pDep == null) continue;
                    if (pDep > pPkg) {
                        System.out.println("Invalid order: " + pkg + " depends on " + dep + " but comes before it.");
                        ok = false;
                    }
                }
            }
            if (ok) System.out.println("Load order valid.");
            else System.out.println("Load order invalid or cycles present.");

            if (graph.hasCycles()) System.out.println("Graph contains cycles!");

            //5
            // Сгенерировать DOT
            GraphvizGenerator gen = new GraphvizGenerator();
            String dot = gen.generateDot(graph);

            // Базовое имя выходного файла (без расширения)
            String out = config.getOutputFile();
            String base = out;
            if (base.toLowerCase().endsWith(".png") || base.toLowerCase().endsWith(".svg") || base.toLowerCase().endsWith(".pdf") || base.toLowerCase().endsWith(".dot")) {
                int idx = base.lastIndexOf('.');
                base = base.substring(0, idx);
            }
            String dotFile = base + ".dot";
            String pdfFile = base + ".pdf";

            gen.saveDotFile(dot, dotFile);
            System.out.println("DOT file saved: " + dotFile);

            // Попытаться автоматически сгенерировать PDF через 'dot'
            try {
                gen.generatePDF(dotFile, pdfFile);
                System.out.println("PDF file saved: " + pdfFile);
            } catch (Exception e) {
                System.out.println("Could not generate PDF automatically: " + e.getMessage());
                System.out.println("You can generate PDF manually with Graphviz:");
                System.out.println("  dot -Tpdf " + dotFile + " -o " + pdfFile);
            }


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}