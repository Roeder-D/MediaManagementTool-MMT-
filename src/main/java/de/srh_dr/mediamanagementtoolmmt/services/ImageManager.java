package de.srh_dr.mediamanagementtoolmmt.services;

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
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageManager {
    private static final Logger LOGGER = Logger.getLogger(ImageManager.class.getName());
    //using "io.github.cdimascio.dotenv.java" to load .env-files
    private static final Dotenv dotenv = Dotenv.load();

    private final String serverUrl = dotenv.get("IMAGE_SERVER_URL");
    private final String serverPhp = dotenv.get("IMAGE_SERVER_HANDLER");
    private final String token = dotenv.get("IMAGE_UPLOAD_TOKEN");
    private final String imageFolder = dotenv.get("IMAGE_FOLDER");
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // UPLOAD
    public boolean uploadImage(File file, String generatedName){
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
        }catch (IOException e){
            LOGGER.log(Level.SEVERE, "Error uploading image", e);
            return false;
        }
    }

    public boolean uploadImageFromUrl(String remoteUrl, String generatedName){
        if (remoteUrl == null || remoteUrl.isEmpty()) {
            LOGGER.log(Level.WARNING, "Remote URL is empty, skipping image upload.");
            return false;
        }

        Path tempFile = null;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(remoteUrl))
                    .GET()
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());

            if (response.statusCode() != 200) {
                LOGGER.log(Level.WARNING, "Failed to download image from URL: " + remoteUrl + " : " + response.statusCode());
                return false;
            }

            String contentType = response.headers().firstValue("Content-Type").orElse("image/jpeg");

            String extension = ".jpg";
            if (contentType.contains("png")) {
                extension = ".png";
            } else if (contentType.contains("webp")) {
                extension = ".webp";
            }

            tempFile = Files.createTempFile("mmt_download_", extension);

            try (InputStream is = response.body()) {
                Files.copy(is, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }

            String correctedFileName = generatedName;
            if(!correctedFileName.toLowerCase().endsWith(extension)){
                int lastDotIndex = correctedFileName.lastIndexOf(".");
                if(lastDotIndex > 0){
                    correctedFileName = correctedFileName.substring(0, lastDotIndex);
                }
                correctedFileName += extension;
            }

            File fileToUpload = tempFile.toFile();
            return uploadImage(fileToUpload, correctedFileName);

        }catch(Exception e){
            LOGGER.log(Level.WARNING, "Error streaming remote image from URL: " + remoteUrl, e);
            return false;
        }finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, "Warning: Could not delete temporary file: " + tempFile, e);
                }
            }
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
                        LOGGER.log(Level.WARNING, "Delete failed: " + response.body());
                    }
                });
    }

    // Helper
    public String getFullImageUrl(String filename){
        return serverUrl + imageFolder + filename;
    }
}
