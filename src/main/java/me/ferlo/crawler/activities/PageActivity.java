package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.download.DownloadService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageActivity extends ResourceActivity {

    protected String content;

    @Inject PageActivity(DownloadService downloadService,
                         @Assisted("baseData") BaseActivityData baseData,
                         @Assisted("content") String content,
                         @Assisted("resources") Map<Path, String> resources) {
        super(downloadService, baseData, resources);

        this.content = content;
        // Clear and re-add cause the content is set after this action
        // gets done in the super constructor
        this.resources.clear();
        resources.forEach(this::addResource);
    }

    public String getContent() {
        return content;
    }

    @Override
    public Path addResource(Path path, String url) {
        path = super.addResource(path, url);
        if(content != null)
            content = content.replaceAll(Pattern.quote(url), Matcher.quoteReplacement(path.toString()));
        return path;
    }

    @Override
    public void writeInFolder(Path folder) {
        this.downloadResources(folder);
        this.saveContent(folder.resolve("pagina.html"));
    }

    protected void saveContent(Path destination) {
        if(content.equals(""))
            return;

        try {
            // Not sure which Charset I should use, I tried UTF-8 and it was all fucked up
            Files.write(destination, content.getBytes(Charset.defaultCharset()), StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public String toString() {
        return "PageActivity{" +
                "content='" + content + '\'' +
                "} " + super.toString();
    }
}
