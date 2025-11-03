import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PyPIClient {
    private static final String PYPI_API_URL = "https://pypi.org/pypi/%s/json";

    public List<String> getDependencies(String packageName) throws Exception {
        String urlString = String.format(PYPI_API_URL, packageName);
        System.out.println("  Requesting: " + urlString);

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0");

        int responseCode = conn.getResponseCode();
        System.out.println("  Response code: " + responseCode);

        if (responseCode != 200) {
            throw new Exception("Package not found: " + packageName);
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = in.readLine()) != null) {
            response.append(line).append("\n");
        }
        in.close();

        return simpleParseJson(response.toString());
    }

    private List<String> simpleParseJson(String json) {
        Set<String> deps = new HashSet<>();

        // Ищем requires_dist
        int idx = json.indexOf("\"requires_dist\"");
        if (idx == -1) {
            System.out.println("  No 'requires_dist' found");
            return new ArrayList<>();
        }

        // Находим начало массива [
        int start = json.indexOf("[", idx);
        if (start == -1) {
            System.out.println("  'requires_dist' is null or not array");
            return new ArrayList<>();
        }

        // Находим конец массива ]
        int end = findMatchingBracket(json, start);
        if (end == -1) {
            return new ArrayList<>();
        }

        String content = json.substring(start + 1, end);

        // Разбиваем по кавычкам
        String[] parts = content.split("\"");

        for (int i = 1; i < parts.length; i += 2) {
            String dep = parts[i].trim();

            // Пропускаем условные зависимости
            if (dep.contains("extra ==") || dep.contains("python_version")) {
                continue;
            }

            // Извлекаем имя пакета
            String pkgName = extractName(dep);
            if (pkgName != null && !pkgName.isEmpty()) {
                deps.add(pkgName);
                System.out.println("    Found: " + pkgName);
            }
        }

        return new ArrayList<>(deps);
    }

    //Находит закрывающую скобку
    private int findMatchingBracket(String str, int start) {
        int depth = 1;
        for (int i = start + 1; i < str.length(); i++) {
            if (str.charAt(i) == '[') depth++;
            if (str.charAt(i) == ']') depth--;
            if (depth == 0) return i;
        }
        return -1;
    }

    // Извлекает имя пакета из строки
    private String extractName(String dep) {
        // Убираем всё после пробела, скобки или оператора
        for (int i = 0; i < dep.length(); i++) {
            char c = dep.charAt(i);
            if (c == ' ' || c == '(' || c == '[' || c == '<' || c == '>' || c == '=' || c == '!') {
                return dep.substring(0, i).trim();
            }
        }
        return dep.trim();
    }

    public String cleanPackageName(String packageName) {
        return extractName(packageName);
    }
}