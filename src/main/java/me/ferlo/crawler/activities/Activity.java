package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.io.File;

public abstract class Activity {

    private final String name;
    private final String href;
    private final String type;
    private final int indent;

    @Inject Activity(@Assisted("name") String name,
                     @Assisted("href") String href,
                     @Assisted("type") String type,
                     @Assisted("indent") int indent) {
        this.name = name;
        this.href = href;
        this.type = type;
        this.indent = indent;
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

    public abstract void writeInFolder(File folder);

    @Override
    public String toString() {
        return "Activity{" +
                "name='" + name + '\'' +
                ", href='" + href + '\'' +
                ", type='" + type + '\'' +
                ", indent=" + indent +
                '}';
    }
}
