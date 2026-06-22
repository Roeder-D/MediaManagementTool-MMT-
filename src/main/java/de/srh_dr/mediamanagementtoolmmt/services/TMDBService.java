package de.srh_dr.mediamanagementtoolmmt.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class TMDBService implements ExternalMediaService{
    Logger LOGGER = Logger.getLogger(TMDBService.class.getName());

    private static final Dotenv dotenv = Dotenv.load();
    private static final String apiToken = dotenv.get("TMDB_API_TOKEN");
    private static final String baseUrl = dotenv.get("TMDB_BASE_URL", "https://api.themoviedb.org/3");
    private static  final String imageBaseUrl = dotenv.get("TMDB_IMAGE_BASE_URL");


    @Override
    public List<ExternalMediaSearchResult> searchByTitle(String title) {
        String encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String url = baseUrl + "/search/movie?query=" + encodedTitle;

        try(CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ClassicHttpRequest request = ClassicRequestBuilder.get(url)
                    .setHeader("Authorization", "Bearer " + apiToken)
                    .setHeader("accept", "application/json")
                    .build();

            String jsonResponse = httpClient.execute(request, response ->
                    EntityUtils.toString(response.getEntity())
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            int totalResults = root.path("total_results").asInt(0);
            if (totalResults == 0) {
                return List.of();
            }

            List<ExternalMediaSearchResult> results = new ArrayList<>();
            JsonNode itemsNode = root.path("results");

            if (itemsNode.isArray()) {
                for (JsonNode item : itemsNode) {
                    results.add(mapItemToSearchResult(item));
                }
            }
            return results;
        }catch (Exception e){
            LOGGER.log(Level.SEVERE,"Error executing or parsing TMDB title lookup for: " + title, e);
            return null;
        }
    }

    @Override
    public ExternalMediaSearchResult searchByIsbn(String isbn) {
        LOGGER.log(Level.SEVERE,"--BROKEN PATH-- Tried to search ISBN via TMDB --BROKEN PATH--");
        return null;
    }

    @Override
    public ExternalMediaSearchResult fetchDetails(String remoteId) {
        String url = baseUrl + "/movie/" + remoteId + "?append_to_response=credits";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()){
            ClassicHttpRequest request = ClassicRequestBuilder.get(url)
                    .setHeader("Authorization", "Bearer " + apiToken)
                    .setHeader("accept", "application/json")
                    .build();

            String jsonResponse = httpClient.execute(request, response ->
                    EntityUtils.toString(response.getEntity())
            );

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);

            String mainTitle = root.path("title").asText("");
            String releaseDate = root.path("release_date").asText();
            String description = root.path("overview").asText("");
            String posterPath = root.path("poster_path").asText("");
            String imageUrl = posterPath.isEmpty() || posterPath.equalsIgnoreCase("null") ? "" : imageBaseUrl + posterPath;

            Publisher publisher = new Publisher(0, "Unknown Producer", true);
            JsonNode productionCompanies =  root.path("production_companies");
            if (productionCompanies.isArray() && !productionCompanies.isEmpty()){
                publisher = new Publisher(0, productionCompanies.get(0).path("name").asText("Unknown Producer"), true);
            }

            List<Language> languages = new ArrayList<>();
            String languageCode = root.path("original_language").asText("en");
            String appLanguage = ConfigManager.getAppLanguage();
            String languageName = appLanguage.equals("default") ? Locale.forLanguageTag(languageCode).getDisplayLanguage() : Locale.forLanguageTag(appLanguage).getDisplayLanguage();
            languages.add(new Language(0, languageName, true));

            List<Artist> artists = new ArrayList<>();
            JsonNode credits = root.path("credits");

            JsonNode crew = credits.path("crew");
            if (crew.isArray()) {
                for (JsonNode member : crew) {
                    if (member.path("job").asText("").equalsIgnoreCase("Director")) {
                        String[] splitName = member.get("name").asText("").split(" ", 2);
                        String firstName = splitName.length > 1 ? splitName[0] : "";
                        String lastName = splitName.length > 1 ? splitName[1] : splitName[0];
                        artists.add(new Artist(0, firstName, lastName, "", "", true));
                        break; // Just one director
                    }
                }
            }

            JsonNode cast = credits.path("cast");
            if(cast.isArray()){
                for(int i=0; i < Math.min(cast.size(), 3); i++){
                    JsonNode actor = cast.get(i);
                    String[] splitName = actor.get("name").asText("").split(" ", 2);
                    String firstName = splitName.length > 1 ? splitName[0] : "";
                    String lastName = splitName.length > 1 ? splitName[1] : splitName[0];
                    artists.add(new Artist(0, firstName, lastName, "", "", true));
                }
            }

            return new ExternalMediaSearchResult(
                    mainTitle,
                    publisher,
                    releaseDate,
                    description,
                    imageUrl,
                    artists,
                    languages,
                    remoteId
            );
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE,"Error fetching TMDB details for: " + remoteId);
            return null;
        }
    }

    //helper
    private ExternalMediaSearchResult mapItemToSearchResult(JsonNode item) {
        String remoteId = item.get("id").asText("");
        String mainTitle = item.get("title").asText("");
        String releaseDate = item.get("release_date").asText("");
        String description = item.get("overview").asText("");

        String posterPath = item.path("poster_path").asText("");
        String imageUrl = posterPath.isEmpty() || posterPath.equalsIgnoreCase("null") ? "" : imageBaseUrl + posterPath;

        //TMDB doesn't provide the producer in standard requests
        Publisher defaultPublisher = new Publisher(0, "TMDB-Default", true);

        List<Language> languages = new ArrayList<>();
        String languageCode = item.path("original_language").asText("en");

        String languageName;
        String appLanguage = ConfigManager.getAppLanguage();
        if(appLanguage.equals("default")){
            languageName = Locale.forLanguageTag(languageCode).getDisplayLanguage();
        }else{
            Locale locale = Locale.forLanguageTag(appLanguage);
            languageName = Locale.forLanguageTag(languageCode).getDisplayLanguage(locale);
        }
        languages.add(new Language(0, languageName, true));

        //TMDB doesn't provide the artists in standard requests
        List<Artist> artists = new ArrayList<>();

        return new  ExternalMediaSearchResult(
                mainTitle,
                defaultPublisher,
                releaseDate,
                description,
                imageUrl,
                artists,
                languages,
                remoteId
        );
    }
}
