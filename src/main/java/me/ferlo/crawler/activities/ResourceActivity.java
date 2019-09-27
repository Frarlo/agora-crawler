package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.crawler.download.DownloadService;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ResourceActivity extends Activity {

    protected final DownloadService downloadService;

    protected final Map<Path, String> resources = new HashMap<>();

    ResourceActivity(DownloadService downloadService,
                     @Assisted("baseData") BaseActivityData baseData,
                     @Assisted("resources") Map<Path, String> resources) {
        super(baseData);

        this.downloadService = downloadService;
        resources.forEach(this::addResource);
    }

    @Inject ResourceActivity(DownloadService downloadService,
                             @Assisted("baseData") BaseActivityData baseData) {
        super(baseData);

        this.downloadService = downloadService;
        addResource(Paths.get(downloadService.getFileName(getHref())), getHref());
    }

    public Path addResource(Path path, String url) {

        Path parent = path.getParent();
        Path newPath = path;

        // Fix per il Pozzi che fa cose strane con i link
        // Too lazy to make it properly
        int n = 0;
        while (resources.containsKey(path)) {
            if(parent != null)
                newPath = parent.resolve((n++) + path.getFileName().toString());
            else
                newPath = Paths.get((n++) + path.getFileName().toString());
        }

        resources.put(path, url);
        return newPath;
    }

    protected void downloadResources(Path folder) {
        resources.forEach((path, url) -> {
            try {
                final Path destination = folder.resolve(path);

                if(!Files.exists(destination.getParent())) {
                    System.out.println("Creating dir " + destination.getParent());
                    Files.createDirectories(destination.getParent());
                }

                try {
                    downloadService.download(url, destination);
                } catch (UncheckedIOException ex) {
                    // Don't rethrow cause it tries to download an unknown host at one point (http://map.norsecorp.com)
                    System.err.println("Couldn't download file " + url);
                    ex.printStackTrace();
                }

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    @Override
    public void writeInFolder(Path folder) {
        downloadResources(folder);
    }

    @Override
    public String toString() {
        return "ResourceActivity{" +
                "downloadService=" + downloadService +
                ", resources=" + resources +
                "} " + super.toString();
    }
}
