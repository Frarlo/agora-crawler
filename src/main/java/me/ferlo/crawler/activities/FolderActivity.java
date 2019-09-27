package me.ferlo.crawler.activities;

import com.google.inject.assistedinject.Assisted;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.Resource;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.inject.Inject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.BiConsumer;

public class FolderActivity extends Activity {

    private final HttpClientService httpClientService;
    private final List<Resource> docs;

    @Inject FolderActivity(@Authenticated HttpClientService httpClientService,
                   @Assisted("name") String name,
                   @Assisted("href") String href,
                   @Assisted("type") String type,
                   @Assisted("indent") int indent,
                   @Assisted("folder") List<Resource> docs) {
        super(name, href, type, indent);
        this.httpClientService=httpClientService;
        this.docs=docs;
    }

    @Override
    public void writeInFolder(File folder) {
        final BiConsumer<Resource, Boolean> downloadAndSave = (toSave, isSubmission) -> {

            String relativePath = (isSubmission ? "documenti" +  File.separator : "") + toSave.getPath().trim();

            File dest = new File(folder, relativePath);

            // Fix per il Pozzi che fa cose strane con i link
            // Too lazy to make it properly
            int n = 0;
            while (dest.exists())
                dest = new File(folder, (n++) + "_" + relativePath);

            if(!dest.getParentFile().exists())
                System.out.println("Creating dir " + dest.getParentFile());
            if(!dest.getParentFile().exists() && !dest.getParentFile().mkdirs())
                throw new AssertionError("Couldn't create parent directories " + dest.getParentFile() + " for " + dest);

            System.out.println("Downloading file " + toSave.getHref());
            try (CloseableHttpClient client = httpClientService.makeHttpClient()) {

                HttpGet request = new HttpGet(toSave.getHref());
                try(CloseableHttpResponse response = client.execute(request)) {
                    if(response.getStatusLine().getStatusCode() != 200)
                        throw new IOException(response.getStatusLine().toString());

                    Files.copy(response.getEntity().getContent(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

            } catch (IOException e) {
                // Don't rethrow cause it tries to download an unknown host at one point (http://map.norsecorp.com)
                System.err.println("Couldn't download file " + toSave.getHref());
                e.printStackTrace();
            }
        };

        docs.stream()
                .filter(r -> r.getHref().contains("agora.ismonnet"))
                .forEach(r -> downloadAndSave.accept(r, false));


    }



    @Override
    public String toString() {
        return "Folder{" +
                ", docs=" + docs +
                "} " + super.toString();
    }
}
