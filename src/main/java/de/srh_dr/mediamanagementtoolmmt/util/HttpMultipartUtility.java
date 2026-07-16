package de.srh_dr.mediamanagementtoolmmt.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class HttpMultipartUtility {
    // HTTP requires Carriage Return + Line Feed for newlines
    private static final String LINE_FEED = "\r\n";
    private final String boundary = "===" + System.currentTimeMillis() + "===";

    // This output stream will act as a buffer to hold all the bytes
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    public String getBoundary() {
        return boundary;
    }

    public void addFormField(String name, String value) throws IOException {

        String builder = "--" + boundary + LINE_FEED +
                "Content-Disposition: form-data; name=\"" + name + "\"" + LINE_FEED +
                LINE_FEED +
                value + LINE_FEED;

        outputStream.write(builder.getBytes(StandardCharsets.UTF_8));
    }

    public void addFilePart(String formFieldName, File uploadFile) throws IOException {
        String header = "--" + boundary + LINE_FEED +
                "Content-Disposition: form-data; name=\"" + formFieldName + "\"; filename=\"" + uploadFile.getName() + "\"" + LINE_FEED +
                "Content-Type: application/octet-stream" + LINE_FEED +
                LINE_FEED;

        outputStream.write(header.getBytes(StandardCharsets.UTF_8));

        byte[] fileBytes = java.nio.file.Files.readAllBytes(uploadFile.toPath());
        outputStream.write(fileBytes);

        outputStream.write(LINE_FEED.getBytes(StandardCharsets.UTF_8));
    }

    public byte[] finishMultipart() throws IOException {
        String builder = "--" + boundary + "--";
        outputStream.write(builder.getBytes(StandardCharsets.UTF_8));

        return outputStream.toByteArray();
    }
}
