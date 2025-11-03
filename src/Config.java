public class Config {
    private String packageName;
    private String repositoryUrl;
    private boolean testMode;
    private String outputFile;

    public Config() {
        // Значения по умолчанию
        this.packageName = "";
        this.repositoryUrl = "";
        this.testMode = false;
        this.outputFile = "dependency_graph.png";
    }

    // Геттеры и сеттеры
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        if (packageName == null || packageName.trim().isEmpty()) {
            throw new IllegalArgumentException("Package name cannot be empty");
        }
        this.packageName = packageName.trim();
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public void setRepositoryUrl(String repositoryUrl) {
        if (repositoryUrl == null || repositoryUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Repository URL cannot be empty");
        }
        this.repositoryUrl = repositoryUrl.trim();
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        if (outputFile == null || outputFile.trim().isEmpty()) {
            throw new IllegalArgumentException("Output file name cannot be empty");
        }
        this.outputFile = outputFile.trim();
    }

    public void validate() {
        if (packageName.isEmpty()) {
            throw new IllegalStateException("Package name must be configured");
        }
        if (repositoryUrl.isEmpty()) {
            throw new IllegalStateException("Repository URL must be configured");
        }
        if (outputFile.isEmpty()) {
            throw new IllegalStateException("Output file must be configured");
        }
    }
}