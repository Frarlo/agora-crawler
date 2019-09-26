package me.ferlo.cookie;

import com.google.inject.AbstractModule;

public class CookieModule extends AbstractModule {

    @Override
    protected void configure() {
        super.configure();

        bindConstant().annotatedWith(Username.class).to("ferlin_francesco");
        bindConstant().annotatedWith(Password.class).to("Password.2073");
        bind(CookieService.class).to(LoginCookieFetcher.class);
    }
}
