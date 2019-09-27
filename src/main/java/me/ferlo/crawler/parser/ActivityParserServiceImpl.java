package me.ferlo.crawler.parser;

import com.google.inject.Inject;
import com.google.inject.Injector;
import me.ferlo.crawler.activities.Activity;
import me.ferlo.crawler.activities.BaseActivityData;
import me.ferlo.crawler.parser.impl.*;
import me.ferlo.crawler.parser.impl.FolderParser;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class ActivityParserServiceImpl implements ActivityParserService {

    private final Map<String, ActivityParserService> typeToParser;
    private final UnsupportedParser unsupportedParser;

    @Inject ActivityParserServiceImpl(Injector injector) {
        // Make the map case insensitive cause why not
        final Map<String, ActivityParserService> temp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        temp.put("resource", injector.getInstance(ResourceParser.class));
        temp.put("page", injector.getInstance(PageParser.class));
        temp.put("url", injector.getInstance(UrlParser.class));
        temp.put("quiz", injector.getInstance(QuizParser.class));
        temp.put("assign", injector.getInstance(AssignmentParser.class));
        temp.put("folder", injector.getInstance(FolderParser.class));

        this.typeToParser = Collections.unmodifiableMap(temp);
        this.unsupportedParser = injector.getInstance(UnsupportedParser.class);
    }

    @Override
    public Activity parse(BaseActivityData baseData) {
        return typeToParser.getOrDefault(baseData.getType(), unsupportedParser).parse(baseData);
    }
}
