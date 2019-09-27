package me.ferlo.crawler.parser;

import com.google.inject.AbstractModule;

public class ActivityParserModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();

        bind(ActivityParserService.class).to(ActivityParserServiceImpl.class);
    }
}
