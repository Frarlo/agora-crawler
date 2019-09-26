package me.ferlo.crawler.course;

import com.google.inject.assistedinject.Assisted;

public interface CourseFactory {
    Course create(@Assisted("title") String title,
                  @Assisted("href") String href);
}
