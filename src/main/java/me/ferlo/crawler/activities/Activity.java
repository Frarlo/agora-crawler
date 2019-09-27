package me.ferlo.crawler.activities;

import com.google.inject.Inject;

import java.nio.file.Path;

public abstract class Activity extends BaseActivityData {

    @Inject Activity(BaseActivityData baseData) {
        super(baseData);
    }

    public abstract void writeInFolder(Path folder);

    @Override
    public String toString() {
        return "Activity{" +
                "name='" + getName() + '\'' +
                ", href='" + getHref() + '\'' +
                ", type='" + getType() + '\'' +
                ", indent=" + getIndent() +
                '}';
    }
}
