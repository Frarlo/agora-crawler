package me.ferlo.crawler.parser;

import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.BaseActivityData;

public interface ActivityParserService {
    Activity parse(BaseActivityData baseData);
}
