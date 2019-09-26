import com.google.inject.Guice;
import me.ferlo.CommandLine;
import me.ferlo.CommandLineModule;
import me.ferlo.client.HttpClientModule;
import me.ferlo.cookie.CookieModule;
import me.ferlo.crawler.CrawlerModule;

public class Main {
    public static void main(String[] args) {
        Guice.createInjector(new CommandLineModule(), new HttpClientModule(), new CookieModule(), new CrawlerModule())
                .getInstance(CommandLine.class)
                .start();
    }
}
