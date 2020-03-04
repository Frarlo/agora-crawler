package me.ferlo.client;

import com.google.inject.Singleton;
import me.ferlo.cookie.LoginCookieFetcher;
import org.apache.http.impl.client.CloseableHttpClient;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

@Singleton
public class LetsEncryptHttpClient implements HttpClientService {

    @Override
    public MyHttpClientBuilder makeHttpClientBuilder() {
        final MyHttpClientBuilder clientBuilder = new MyHttpClientBuilder();
        clientBuilder.setSSLContext(getLetsEncryptSSLCtx());
        return clientBuilder;
    }

    @Override
    public CloseableHttpClient makeHttpClient() {
        return makeHttpClientBuilder().build();
    }

    private SSLContext getLetsEncryptSSLCtx() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            Path ksPath = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
            keyStore.load(Files.newInputStream(ksPath), "changeit".toCharArray());

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream caInput = LoginCookieFetcher.class.getClassLoader().getResourceAsStream("DSTRootCAX3.der")) {
                Certificate crt = cf.generateCertificate(caInput);
                keyStore.setCertificateEntry("DSTRootCAX3", crt);
            }

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLContext.setDefault(sslContext);

            return sslContext;
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
