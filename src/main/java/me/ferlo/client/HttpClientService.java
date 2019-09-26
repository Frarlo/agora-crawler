package me.ferlo.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public interface HttpClientService {
    HttpClientBuilder makeHttpClientBuilder();
    CloseableHttpClient makeHttpClient();
}
