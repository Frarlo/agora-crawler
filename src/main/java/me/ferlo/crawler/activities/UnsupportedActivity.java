package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.download.DownloadService;

import java.nio.file.Path;
import java.util.Collections;

public class UnsupportedActivity extends PageActivity {

    @Inject UnsupportedActivity(DownloadService downloadService,
                                @Assisted("baseData") BaseActivityData baseData) {
        super(downloadService, baseData, "", Collections.emptyMap());
        this.content = "'" + getType() + "' type is not supported";
    }

    @Override
    public void writeInFolder(Path folder) {
        saveContent(folder.resolve("unsupported.html"));
    }

    @Override
    public String toString() {
        return "UnsupportedActivity{} " + super.toString();
    }
}
