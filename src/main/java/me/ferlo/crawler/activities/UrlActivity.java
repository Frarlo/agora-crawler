package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.Resource;

import java.util.List;

public class UrlActivity extends PageActivity {

    @Inject UrlActivity(@Authenticated HttpClientService httpClientService,
                        @Assisted("name") String name,
                        @Assisted("href") String href,
                        @Assisted("type") String type,
                        @Assisted("indent") int indent,
                        @Assisted("content") String content,
                        @Assisted("resources") List<Resource> resources) {
        super(httpClientService, name, href, type, indent, content, resources);
    }

    @Override
    public String toString() {
        return "UrlActivity{} " + super.toString();
    }
}
