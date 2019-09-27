package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.download.DownloadService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class QuizActivity extends PageActivity {

    @Inject QuizActivity(DownloadService downloadService,
                         @Assisted("baseData") BaseActivityData baseData,
                         @Assisted("quizHtml") String quizHtml,
                         @Assisted("resources") Map<Path, String> resources) {
        super(downloadService, baseData, quizHtml, resources);
    }

    @Override
    public Path addResource(Path path, String url) {
        return super.addResource(Paths.get("res").resolve(path), url);
    }

    @Override
    public void writeInFolder(Path folder) {
        downloadResources(folder);
        saveContent(folder.resolve("revisione.html"));
    }

    @Override
    public String toString() {
        return "QuizActivity{} " + super.toString();
    }
}
