package de.srh_dr.mediamanagementtoolmmt.util;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;


import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ImageManager {
    //using "io.github.cdimascio.dotenv.java" to load .env-files
    private static final Dotenv dotenv = Dotenv.load();

    private final String serverUrl = dotenv.get("IMAGE_SERVER_URL");
    private final String serverPhp = dotenv.get("IMAGE_SERVER_HANDLER");
    private final String token = dotenv.get("IMAGE_UPLOAD_TOKEN");
    private final String imageFolder = dotenv.get("IMAGE_FOLDER");
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // UPLOAD
    public boolean uploadImage(File file, String generatedName) throws IOException {
        String url = serverUrl + serverPhp;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(url);
            post.setHeader("Authorization", "Bearer " + token);

            HttpEntity entity = MultipartEntityBuilder.create()
                    .addTextBody("filename", generatedName)
                    .addBinaryBody("image", file, ContentType.APPLICATION_OCTET_STREAM, file.getName())
                    .build();

            post.setEntity(entity);

            return client.execute(post, response ->
                    response.getCode() == 200 && EntityUtils.toString(response.getEntity()).contains("success")
            );
        }
    }

    // DELETE
    public void deleteImage(String filename){
        if(filename == null || filename.isEmpty()){return;}

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + serverPhp + "?action=delete&filename=" + filename))
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.noBody()) //empty POST to trigger delete on the server
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if(response.statusCode() != 200){
                        System.err.println("Delete failed: " + response.body());
                    }
                });
    }

    // Helper
    public String getFullImageUrl(String filename){
        return serverUrl + imageFolder + filename;
    }
}
