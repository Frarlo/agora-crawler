package me.ferlo.crawler;

import me.ferlo.crawler.category.Category;
import me.ferlo.crawler.course.Course;

import java.util.List;

public interface CrawlerService {
    List<Course> fetchCourses();
    List<Category> fetchCategories(String courseHref);
}
