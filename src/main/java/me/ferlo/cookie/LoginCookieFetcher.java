package me.ferlo.cookie;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.ferlo.client.HttpClientService;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class LoginCookieFetcher implements CookieService {

    private final HttpClientService httpClientService;
    private final String username;
    private final String password;

    private CookieStore cookie;

    @Inject LoginCookieFetcher(HttpClientService httpClientService,
                                @Username String username,
                                @Password String password) {
        this.httpClientService = httpClientService;
        this.username = username;
        this.password = password;

        cookie = null;
    }

    @Override
    public CookieStore getCookies() {
        if(cookie == null)
            cookie = fetchCookie();
        return cookie;
    }

    private CookieStore fetchCookie() {

        final CookieStore cookieStore = new BasicCookieStore();

        final HttpClientBuilder clientBuilder = httpClientService.makeHttpClientBuilder();
        clientBuilder.setDefaultCookieStore(cookieStore);

        try(CloseableHttpClient client = clientBuilder.build()) {

            System.out.println("Fetching login form");
            HttpGet sessionRequest = new HttpGet("https://agora.ismonnet.it/agora/login/index.php");

            final String loginToken;
            try(CloseableHttpResponse response = client.execute(sessionRequest)) {
                System.out.println("Logging in");
                final Pattern loginTokenRegex = Pattern.compile("<input " +
                        "type=\"hidden\" " +
                        "name=\"logintoken\" " +
                        "value=\"(?<value>.*)\">");

                final String html = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                                .lines()
                                .collect(Collectors.joining("\n"));
                final Matcher matcher = loginTokenRegex.matcher(html);
                if(!matcher.find())
                    throw new AssertionError("Couldn't find loginToken form value");

                loginToken = matcher.group("value");
            }

            final List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("anchor", ""));
            params.add(new BasicNameValuePair("logintoken", loginToken));
            params.add(new BasicNameValuePair("username", username));
            params.add(new BasicNameValuePair("password", password));

            HttpPost request = new HttpPost("https://agora.ismonnet.it/agora/login/index.php");
            request.setEntity(new UrlEncodedFormEntity(params));

            try(CloseableHttpResponse response = client.execute(request)) {
                return cookieStore;
            }

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
