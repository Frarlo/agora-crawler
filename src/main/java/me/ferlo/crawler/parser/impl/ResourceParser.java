package me.ferlo.crawler.parser.impl;

import com.google.inject.Inject;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.ActivityFactory;
import me.ferlo.crawler.activities.BaseActivityData;
import me.ferlo.crawler.parser.ActivityParserService;

public class ResourceParser implements ActivityParserService {

    private final ActivityFactory factory;

    @Inject ResourceParser(ActivityFactory factory) {
        this.factory = factory;
    }

    @Override
    public Activity parse(BaseActivityData baseData) {
        return factory.createResource(baseData);
    }
}
