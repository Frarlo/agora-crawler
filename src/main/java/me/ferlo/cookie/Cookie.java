package me.ferlo.cookie;

import com.google.inject.Singleton;
import org.apache.http.client.CookieStore;

@Singleton
public class Cookie implements CookieService {

    private final CookieStore cookie;
    private final String sessKey;

    public Cookie(CookieStore cookie, String sessKey) {
        this.cookie = cookie;
        this.sessKey = sessKey;
    }

    @Override
    public CookieStore getCookies() {
        return cookie;
    }

    @Override
    public String getSessKey() {
        return sessKey;
    }
}
