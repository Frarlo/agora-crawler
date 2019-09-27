package me.ferlo.crawler.activities;

import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.download.DownloadService;

import javax.inject.Inject;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class FolderActivity extends ResourceActivity {

    @Inject FolderActivity(DownloadService downloadService,
                           @Assisted("baseData") BaseActivityData baseData,
                           @Assisted("docs") Map<Path, String> docs) {
        super(downloadService, baseData, docs);
    }

    @Override
    public Path addResource(Path path, String url) {
        return super.addResource(Paths.get("documenti").resolve(path), url);
    }

    @Override
    public void writeInFolder(Path folder) {
        downloadResources(folder);
    }

    @Override
    public String toString() {
        return "FolderActivity{} " + super.toString();
    }
}
