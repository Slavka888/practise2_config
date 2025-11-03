import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class FileUtils {

    public static Map<String, List<String>> parseTestRepository(String filePath) throws IOException {
        Map<String, List<String>> repository = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Пропускаем пустые строки и комментарии
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // Формат: PACKAGE: DEP1, DEP2, DEP3
                if (!line.contains(":")) {
                    System.err.println("Warning: Invalid format at line " + lineNumber + ": " + line);
                    continue;
                }

                String[] parts = line.split(":", 2);
                String packageName = parts[0].trim();

                // Валидация имени пакета (большие латинские буквы)
                if (!packageName.matches("[A-Z]+")) {
                    System.err.println("Warning: Package name must be uppercase letters at line " + lineNumber);
                    continue;
                }

                List<String> dependencies = new ArrayList<>();

                if (parts.length == 2 && !parts[1].trim().isEmpty()) {
                    String[] deps = parts[1].split(",");
                    for (String dep : deps) {
                        String cleanDep = dep.trim();
                        if (!cleanDep.isEmpty()) {
                            if (!cleanDep.matches("[A-Z]+")) {
                                System.err.println("Warning: Dependency name must be uppercase letters: " + cleanDep);
                                continue;
                            }
                            dependencies.add(cleanDep);
                        }
                    }
                }

                repository.put(packageName, dependencies);
            }
        }

        return repository;
    }
}