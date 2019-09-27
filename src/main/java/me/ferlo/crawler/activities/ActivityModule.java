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
                .implement(Key.get(Activity.class, Names.named("unsupported_activity")), UnsupportedActivity.class)
                .implement(Key.get(Activity.class, Names.named("resource_activity")), ResourceActivity.class)
                .implement(Key.get(Activity.class, Names.named("page_activity")), PageActivity.class)
                .implement(Key.get(Activity.class, Names.named("url_activity")), UrlActivity.class)
                .implement(Key.get(Activity.class, Names.named("quiz_activity")), QuizActivity.class)
                .implement(Key.get(Activity.class, Names.named("assignment_activity")), AssignmentActivity.class)
                .implement(Key.get(Activity.class, Names.named("folder_activity")), FolderActivity.class)
                .build(ActivityFactory.class));
    }
}
