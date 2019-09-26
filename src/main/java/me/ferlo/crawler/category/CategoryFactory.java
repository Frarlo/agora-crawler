package me.ferlo.crawler.category;

import me.ferlo.crawler.activities.Activity;

import java.util.List;

public interface CategoryFactory {
    Category create(String name, List<Activity> activities);
}
