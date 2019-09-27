package me.ferlo.crawler;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import me.ferlo.crawler.activities.ActivityModule;
import me.ferlo.crawler.category.Category;
import me.ferlo.crawler.category.CategoryFactory;
import me.ferlo.crawler.course.Course;
import me.ferlo.crawler.course.CourseFactory;
import me.ferlo.crawler.download.AuthenticatedDownloadService;
import me.ferlo.crawler.download.DownloadService;
import me.ferlo.crawler.parser.ActivityParserModule;

public class CrawlerModule extends AbstractModule {
    @Override
    protected void configure() {
        super.configure();

        install(new ActivityModule());
        install(new ActivityParserModule());

        bindConstant().annotatedWith(Domain.class).to("agora.ismonnet");

        bind(DownloadService.class).to(AuthenticatedDownloadService.class);
        bind(CrawlerService.class).to(Crawler.class);

        install(new FactoryModuleBuilder()
                .implement(Course.class, Course.class)
                .build(CourseFactory.class));
        install(new FactoryModuleBuilder()
                .implement(Category.class, Category.class)
                .build(CategoryFactory.class));
    }
}
