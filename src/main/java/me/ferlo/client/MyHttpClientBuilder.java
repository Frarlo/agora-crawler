package me.ferlo.client;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class MyHttpClientBuilder extends HttpClientBuilder {

    private final Set<Function<CloseableHttpClient, CloseableHttpClient>> listeners = new LinkedHashSet<>();

    @Override
    public CloseableHttpClient build() {
        final AtomicReference<CloseableHttpClient> client = new AtomicReference<>(super.build());
        listeners.forEach(l -> client.getAndUpdate(l::apply));
        return client.get();
    }

    public void addOnBuildListener(Function<CloseableHttpClient, CloseableHttpClient> listener) {
        listeners.add(listener);
    }

    public void removeOnBuildListener(Function<CloseableHttpClient, CloseableHttpClient> listener) {
        listeners.remove(listener);
    }
}
