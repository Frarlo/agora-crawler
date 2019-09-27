package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.download.DownloadService;

import java.nio.file.Path;
import java.util.Map;

public class UrlActivity extends PageActivity {

    @Inject UrlActivity(DownloadService downloadService,
                        @Assisted("baseData") BaseActivityData baseData,
                        @Assisted("content") String content,
                        @Assisted("resources") Map<Path, String> resources) {
        super(downloadService, baseData, content, resources);
    }

    @Override
    public String toString() {
        return "UrlActivity{} " + super.toString();
    }
}
