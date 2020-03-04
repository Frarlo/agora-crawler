package me.ferlo.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.ferlo.cookie.CookieService;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.net.URISyntaxException;

@Singleton
public class AuthenticatedHttpClient implements HttpClientService {

    private final HttpClientService baseClientService;
    private final CookieService cookieService;

    @Inject AuthenticatedHttpClient(HttpClientService baseClientService,
                                    CookieService cookieService) {
        this.baseClientService = baseClientService;
        this.cookieService = cookieService;
    }

    @Override
    public MyHttpClientBuilder makeHttpClientBuilder() {
        final MyHttpClientBuilder builder = baseClientService.makeHttpClientBuilder();
        builder.setDefaultCookieStore(cookieService.getCookies());
        builder.addOnBuildListener(client -> new WrappedCloseableHttpClient(client, cookieService.getSessKey()));
        return builder;
    }

    @Override
    public CloseableHttpClient makeHttpClient() {
        return makeHttpClientBuilder().build();
    }

    private static class WrappedCloseableHttpClient extends CloseableHttpClient {

        private final CloseableHttpClient wrapped;
        private final String sesskey;

        public WrappedCloseableHttpClient(CloseableHttpClient wrapped, String sesskey) {
            this.wrapped = wrapped;
            this.sesskey = sesskey;
        }

        @Override
        public CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
            return wrapped.execute(target, editRequest(request), context);
        }

        @Override
        public CloseableHttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException {
            return wrapped.execute(target, editRequest(request), context);
        }

        @Override
        public CloseableHttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException {
            return wrapped.execute(editRequest(request), context);
        }

        @Override
        public CloseableHttpResponse execute(HttpUriRequest request) throws IOException {
            return wrapped.execute(editRequest(request));
        }

        @Override
        public CloseableHttpResponse execute(HttpHost target, HttpRequest request) throws IOException {
            return wrapped.execute(target, editRequest(request));
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
            return wrapped.execute(editRequest(request), responseHandler);
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
            return wrapped.execute(editRequest(request), responseHandler, context);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
            return wrapped.execute(target, editRequest(request), responseHandler);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
            return wrapped.execute(target, editRequest(request), responseHandler, context);
        }

        public <T> T editRequest(T request) {

            if(!(request instanceof HttpRequestBase))
                return request;

            final HttpRequestBase req = (HttpRequestBase) request;

            try {
                final URIBuilder uriBuilder = new URIBuilder(req.getURI());
                final boolean hasSessKey = uriBuilder.getQueryParams().stream()
                        .map(NameValuePair::getName)
                        .anyMatch(name -> name.equals("sesskey"));

                if(hasSessKey)
                    req.setURI(uriBuilder
                            .setParameter("sesskey", sesskey)
                            .build());
            } catch (URISyntaxException e) {
                // Ignored, couldn't replace
            }

            return request;
        }

        @Override
        @Deprecated
        public HttpParams getParams() {
            return wrapped.getParams();
        }

        @Override
        @Deprecated
        public ClientConnectionManager getConnectionManager() {
            return wrapped.getConnectionManager();
        }

        @Override
        public void close() throws IOException {
            wrapped.close();
        }
    }
}
