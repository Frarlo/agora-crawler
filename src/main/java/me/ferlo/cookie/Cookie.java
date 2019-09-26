package me.ferlo.cookie;

import com.google.inject.Singleton;
import org.apache.http.client.CookieStore;

@Singleton
public class Cookie implements CookieService {

    private final CookieStore cookie;

    public Cookie(CookieStore cookie) {
        this.cookie = cookie;
    }

    @Override
    public CookieStore getCookies() {
        return cookie;
    }
}
