import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class ConfigParser {

    public Config parse(String filePath) throws Exception {
        File configFile = new File(filePath);
        if (!configFile.exists()) {
            throw new IllegalArgumentException("Config file not found: " + filePath);
        }

        Config config = new Config();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(configFile);
            doc.getDocumentElement().normalize();

            Element root = doc.getDocumentElement();

            // Чтение параметров
            config.setPackageName(getElementText(root, "package_name"));
            config.setRepositoryUrl(getElementText(root, "repository_url"));
            config.setTestMode(Boolean.parseBoolean(getElementText(root, "test_mode")));
            config.setOutputFile(getElementText(root, "output_file"));

            config.validate();

        } catch (Exception e) {
            throw new Exception("Error parsing config file: " + e.getMessage(), e);
        }

        return config;
    }

    private String getElementText(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }
}