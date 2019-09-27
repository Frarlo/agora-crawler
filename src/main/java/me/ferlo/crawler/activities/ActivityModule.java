package me.ferlo.crawler.activities;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import com.google.inject.name.Names;

public class ActivityModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();

        install(new FactoryModuleBuilder()
                .implement(Key.get(Activity.class, Names.named("unsupported")), UnsupportedActivity.class)
                .implement(Key.get(Activity.class, Names.named("assignment")), AssignmentActivity.class)
                .implement(Key.get(Activity.class, Names.named("folder")), FolderActivity.class)
                .implement(Key.get(Activity.class, Names.named("resource")), ResourceActivity.class)
                .implement(Key.get(Activity.class, Names.named("page")), PageActivity.class)
                .implement(Key.get(Activity.class, Names.named("url")), UrlActivity.class)
                .implement(Key.get(Activity.class, Names.named("quiz")), QuizActivity.class)
                .build(ActivityFactory.class));
    }
}
