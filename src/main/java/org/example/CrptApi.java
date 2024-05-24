package org.example;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;

public class CrptApi {
    private final String API_URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";
    private final int TIMEOUT_SECONDS = 10;

    private final TimeUnit timeUnit;
    private final int requestLimit;
    private final Lock lock = new ReentrantLock();
    private final AtomicInteger requestCount = new AtomicInteger(0);
    private long lastResetTime = System.currentTimeMillis();

    private final HttpClient httpClient = HttpClients.createDefault();
    private final Gson gson = new Gson();

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.timeUnit = timeUnit;
        this.requestLimit = requestLimit;
    }

    public void createDocument(Object document, String signature) {
        lock.lock();
        try {
            resetIfNecessary();
            waitForQuota();
            sendRequest(document, signature);
            requestCount.incrementAndGet();
        } finally {
            lock.unlock();
        }
    }

    private void resetIfNecessary() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastResetTime;
        long timeLimitMillis = timeUnit.toMillis(1);

        if (elapsedTime > timeLimitMillis) {
            lastResetTime = currentTime;
            requestCount.set(0);
        }
    }

    private void waitForQuota() {
        while (requestCount.get() >= requestLimit) {
            try {
                Thread.sleep(100); // wait for a short time before checking again
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while waiting for quota");
            }
            resetIfNecessary();
        }
    }

    private void sendRequest(Object document, String signature) {
        try {
            HttpPost request = new HttpPost(API_URL);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(TIMEOUT_SECONDS * 1000)
                    .setConnectionRequestTimeout(TIMEOUT_SECONDS * 1000)
                    .setSocketTimeout(TIMEOUT_SECONDS * 1000)
                    .build();
            request.setConfig(requestConfig);

            String json = gson.toJson(document);
            request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

            // Here you can add headers, such as authorization header with the signature

            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                throw new RuntimeException("HTTP error code: " + statusCode);
            }

            HttpEntity entity = response.getEntity();
            // Handle response entity if needed

        } catch (Exception e) {
            throw new RuntimeException("Error sending request", e);
        }
    }
}

