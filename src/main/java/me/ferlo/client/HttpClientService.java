package me.ferlo.client;

import org.apache.http.impl.client.CloseableHttpClient;

public interface HttpClientService {
    MyHttpClientBuilder makeHttpClientBuilder();
    CloseableHttpClient makeHttpClient();
}
