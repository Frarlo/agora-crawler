package me.ferlo.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.ferlo.cookie.CookieService;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

@Singleton
public class AuthenticatedHttpClient implements HttpClientService {

    private final HttpClientService baseClientService;
    private final CookieService cookieService;

    @Inject
    AuthenticatedHttpClient(HttpClientService baseClientService,
                            CookieService cookieService) {
        this.baseClientService = baseClientService;
        this.cookieService = cookieService;
    }

    @Override
    public HttpClientBuilder makeHttpClientBuilder() {
        HttpClientBuilder client = baseClientService.makeHttpClientBuilder();
        client.setDefaultCookieStore(cookieService.getCookies());
        return client;
    }

    @Override
    public CloseableHttpClient makeHttpClient() {
        return makeHttpClientBuilder().build();
    }
}
