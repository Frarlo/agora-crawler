package me.ferlo.cookie;

import org.apache.http.client.CookieStore;

public interface CookieService {
    CookieStore getCookies();

    String getSessKey();
}
