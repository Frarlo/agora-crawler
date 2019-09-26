package me.ferlo.client;

import com.google.inject.AbstractModule;

public class HttpClientModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();

        bind(HttpClientService.class).to(LetsEncryptHttpClient.class);
        bind(HttpClientService.class)
                .annotatedWith(Authenticated.class)
                .to(LoggedInHttpClient.class);
    }
}
