package me.ferlo.crawler.course;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class Course {

    private final String title;
    private final String href;

    @Inject Course(@Assisted("title") String title,
                   @Assisted("href") String href) {
        this.title = title;
        this.href = href;
    }

    public String getTitle() {
        return title;
    }

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "Course{" +
                "title='" + title + '\'' +
                ", href='" + href + '\'' +
                '}';
    }
}
