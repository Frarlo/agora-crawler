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
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public class AssignmentActivity extends Activity {

    private final HttpClientService httpClientService;

    private final String desc;
    private final List<Resource> resources;
    private final List<Resource> submissions;

    @Inject AssignmentActivity(@Authenticated HttpClientService httpClientService,
                               @Assisted("name") String name,
                               @Assisted("href") String href,
                               @Assisted("type") String type,
                               @Assisted("indent") int indent,
                               @Assisted("desc") String desc,
                               @Assisted("resources") List<Resource> resources,
                               @Assisted("submissions") List<Resource> submissions) {
        super(name, href, type, indent);

        this.httpClientService = httpClientService;

        this.desc = desc;
        this.resources = resources;
        this.submissions = submissions;
    }

    public String getDesc() {
        return desc;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public List<Resource> getSubmissions() {
        return submissions;
    }

    public void writeInFolder(File folder) {

        final String[] descToSave = { desc != null ? desc : "" };
        final BiConsumer<Resource, Boolean> downloadAndSave = (toSave, isSubmission) -> {

            String relativePath = (isSubmission ? "consegne" +  File.separator : "") + toSave.getPath().trim();
            descToSave[0] = descToSave[0].replaceAll(Pattern.quote(toSave.getHref()), toSave.getPath());

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

        resources.stream()
                .filter(r -> r.getHref().contains("agora.ismonnet"))
                .forEach(r -> downloadAndSave.accept(r, false));
        submissions.forEach(r -> downloadAndSave.accept(r, true));

        if(!descToSave[0].equals("")) {
            File file = new File(folder, "consegna.html");

            try (PrintStream out = new PrintStream(new FileOutputStream(file))) {
                out.print(descToSave[0]);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public String toString() {
        return "Assignment{" +
                "desc='" + desc + '\'' +
                ", resources=" + resources +
                ", submissions=" + submissions +
                "} " + super.toString();
    }
}
