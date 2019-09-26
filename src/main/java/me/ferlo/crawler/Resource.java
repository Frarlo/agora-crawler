package me.ferlo.crawler;

public class Resource {

    private final String path;
    private final String href;

    public Resource(String path, String href) {
        this.path = path;
        this.href = href;
    }

    public String getPath() {
        return path;
    }

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "path='" + path + '\'' +
                ", href='" + href + '\'' +
                '}';
    }
}
