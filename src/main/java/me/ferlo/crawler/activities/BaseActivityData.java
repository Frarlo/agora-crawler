package me.ferlo.crawler.activities;

public class BaseActivityData {

    private final String name;
    private final String href;
    private final String type;
    private final int indent;

    public BaseActivityData(String name, String href, String type, int indent) {
        this.name = name;
        this.href = href;
        this.type = type;
        this.indent = indent;
    }

    BaseActivityData(BaseActivityData data) {
        this(data.name, data.href, data.type, data.indent);
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

    public String getType() {
        return type;
    }

    public int getIndent() {
        return indent;
    }

    @Override
    public String toString() {
        return "BaseActivityData{" +
                "name='" + name + '\'' +
                ", href='" + href + '\'' +
                ", type='" + type + '\'' +
                ", indent=" + indent +
                '}';
    }
}
