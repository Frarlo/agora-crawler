package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.download.DownloadService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class AssignmentActivity extends PageActivity {

    @Inject AssignmentActivity(DownloadService downloadService,
                               @Assisted("baseData") BaseActivityData baseData,
                               @Assisted("desc") String desc,
                               @Assisted("resources") Map<Path, String> resources,
                               @Assisted("submissions") Map<Path, String> submissions) {
        super(downloadService, baseData, desc, resources);

        submissions.forEach(this::addSubmission);
    }

    public Path addSubmission(Path path, String url) {
        return this.addResource(
                Paths.get("consegne").resolve(path),
                url
        );
    }

    @Override
    public void writeInFolder(Path folder) {
        this.downloadResources(folder);
        this.saveContent(folder.resolve("consegna.html"));
    }

    @Override
    public String toString() {
        return "Assignment{} " + super.toString();
    }
}
