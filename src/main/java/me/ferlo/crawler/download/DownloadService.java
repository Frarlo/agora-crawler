package me.ferlo.crawler.download;

import org.apache.http.client.methods.HttpUriRequest;

import java.nio.file.Path;

public interface DownloadService {

    String getFileName(String url);

    String followRedirects(String url);

    void download(String url, Path destination);

    String fetch(String url);

    String fetch(HttpUriRequest request);
}
