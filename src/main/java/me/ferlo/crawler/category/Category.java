package me.ferlo.crawler.category;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.activities.Activity;

import java.util.List;

public class Category {

    private final String name;
    private final List<Activity> activities;

    @Inject Category(@Assisted String name,
                     @Assisted List<Activity> activities) {
        this.name = name;
        this.activities = activities;
    }

    public String getName() {
        return name;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    @Override
    public String toString() {
        return "Category{" +
                "name='" + name + '\'' +
                ", activities=" + activities +
                '}';
    }
}
