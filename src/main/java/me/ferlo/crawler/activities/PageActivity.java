package me.ferlo.crawler.activities;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import me.ferlo.client.Authenticated;
import me.ferlo.client.HttpClientService;
import me.ferlo.crawler.Resource;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.regex.Pattern;

public class PageActivity extends Activity {

    private final HttpClientService httpClientService;

    private final String content;
    private final List<Resource> resources;

    @Inject PageActivity(@Authenticated HttpClientService httpClientService,
                         @Assisted("name") String name,
                         @Assisted("href") String href,
                         @Assisted("type") String type,
                         @Assisted("indent") int indent,
                         @Assisted("content") String content,
                         @Assisted("resources") List<Resource> resources) {
        super(name, href, type, indent);

        this.httpClientService = httpClientService;

        this.content = content;
        this.resources = resources;
    }

    public String getContent() {
        return content;
    }

    public List<Resource> getResources() {
        return resources;
    }

    @Override
    public void writeInFolder(File folder) {

        final String[] contentToSave = { content != null ? content : "" };
        resources.stream()
                .filter(r -> r.getHref().contains("agora.ismonnet"))
                .forEach(toSave -> {

                    String relativePath = toSave.getPath().trim();
                    contentToSave[0] = contentToSave[0].replaceAll(Pattern.quote(toSave.getHref()), toSave.getPath());

                    File dest = new File(folder, relativePath);

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
                });

        if(!contentToSave[0].equals("")) {
            File file = new File(folder, "pagina.html");

            try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                out.print(contentToSave[0]);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public String toString() {
        return "PageActivity{" +
                "content='" + content + '\'' +
                ", resources=" + resources +
                "} " + super.toString();
    }
}
