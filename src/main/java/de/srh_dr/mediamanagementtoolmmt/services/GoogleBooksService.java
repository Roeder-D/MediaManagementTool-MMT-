package de.srh_dr.mediamanagementtoolmmt.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.srh_dr.mediamanagementtoolmmt.dto.ApiSource;
import de.srh_dr.mediamanagementtoolmmt.dto.ExternalMediaSearchResult;
import de.srh_dr.mediamanagementtoolmmt.model.Artist;
import de.srh_dr.mediamanagementtoolmmt.model.Language;
import de.srh_dr.mediamanagementtoolmmt.model.Publisher;
import de.srh_dr.mediamanagementtoolmmt.util.ConfigManager;
import io.github.cdimascio.dotenv.Dotenv;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GoogleBooksService implements BookLookupService{
    private static final Logger LOGGER = Logger.getLogger(GoogleBooksService.class.getName());

    private static final Dotenv dotenv = Dotenv.load();
    private static final String apiToken = dotenv.get("GOOGLE_BOOKS_API_TOKEN");
    private static final String baseUrl = dotenv.get("GOOGLE_BOOKS_BASE_URL");

    //Fetch data
    @Override
    public List<ExternalMediaSearchResult> searchByTitle(String title) {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = baseUrl + "?q=" + encodedTitle + "&key=" + apiToken;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            ClassicHttpRequest request = ClassicRequestBuilder.get(url).build();

            String jsonResponse = httpClient.execute(request, response ->
                    EntityUtils.toString(response.getEntity())
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            int totalItems = root.get("totalItems").asInt(0);
            if(totalItems == 0){return List.of();}

            List<ExternalMediaSearchResult> results = new ArrayList<>();
            JsonNode itemsNode = root.path("items");

            if(itemsNode.isArray()){
                for(JsonNode item : root.path("items")) {
                    results.add(mapItemToSearchResult(item));
                }
            }
            return results;
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Error executing or parsing Google Books title lookup for: " + title, e);
            return null;
        }
    }

    @Override
    public ExternalMediaSearchResult searchByIsbn(String isbn) {
        String url = baseUrl + "?q=isbn:" + isbn + "&key=" + apiToken;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.get(url).build();

            String jsonResponse = httpClient.execute(request, response ->
                EntityUtils.toString(response.getEntity())
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            int totalItems = root.path("totalItems").asInt(0);
            if(totalItems == 0){return null;}
            //get first item (can only hit one item)
            JsonNode firstItem = root.path("items").path(0);
            //extract data
            return mapItemToSearchResult(firstItem);

        }catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Error executing or parsing Google Books ISBN lookup for: " + isbn, e);
            return null;
        }
    }

    //Unused/Should not be reached!
    @Override
    public ExternalMediaSearchResult fetchDetails(String remoteId) {
        LOGGER.log(Level.SEVERE,"--Broken Path-- Tried to fetch Google Books details for: " + remoteId + " --Broken Path--");
        return null;
    }

    //helper for mapping JSON into program class
    private ExternalMediaSearchResult mapItemToSearchResult(JsonNode itemNode){
        String remoteId = itemNode.path("id").asText("");

        JsonNode volumeInfo = itemNode.path("volumeInfo");

        String mainTitle = volumeInfo.path("title").asText("");
        String subtitle = volumeInfo.path("subtitle").asText("");
        String publisherName = volumeInfo.path("publisher").asText("Unknown Publisher");
        String releaseDate = volumeInfo.path("publishedDate").asText("");
        String description = volumeInfo.path("description").asText("Unknown Description");
        String languageCode = volumeInfo.path("language").asText("en");
        String imageUrl = volumeInfo.path("imageLinks").path("thumbnail").asText("");

        Publisher publisher = new Publisher(0, publisherName, true);

        List<Artist> artists = new ArrayList<>();
        JsonNode authorsNode = volumeInfo.path("authors");
        if(authorsNode.isArray()){
            for(JsonNode author : authorsNode){
                String name = author.asText();
                String[] splitName = name.split(" ");
                StringBuilder firstName = new StringBuilder();
                for(int i = 0; i < splitName.length - 1; i++){
                    firstName.append(splitName[i]);
                }
                String lastName = splitName[splitName.length - 1];
                artists.add(new Artist(0, firstName.toString(), lastName, "", "", true));
            }
        }

        String languageName;
        String appLanguage = ConfigManager.getAppLanguage();
        if(appLanguage.equals("default")){
            languageName = Locale.forLanguageTag(languageCode).getDisplayLanguage();
        }else{
            Locale locale = Locale.forLanguageTag(appLanguage);
            languageName = Locale.forLanguageTag(languageCode).getDisplayLanguage(locale);
        }

        List<Language> languages = new ArrayList<>();
        languages.add(new Language(0, languageName, true));

        String finalTitle = mainTitle;
        if(!subtitle.isEmpty()){
            finalTitle += " - " + subtitle;
        }

        // use ISBN as identifier unless it's not in the industry identifiers
        String finalRemoteId = remoteId;
        JsonNode identifiers = volumeInfo.path("industryIdentifiers");
        if(identifiers.isArray() && !identifiers.isEmpty()){
            for(JsonNode idNode : identifiers){
                if("ISBN_13".equals(idNode.path("type").asText())){
                    finalRemoteId = idNode.path("identifier").asText();
                    break;
                }
            }
        }

        return new ExternalMediaSearchResult(
                finalTitle,
                publisher,
                releaseDate,
                description,
                imageUrl,
                artists,
                languages,
                finalRemoteId,
                ApiSource.GOOGLE_BOOKS
        );
    }
}
