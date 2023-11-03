/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.models;

import io.mikael.urlbuilder.UrlBuilder;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.umigon.eval.controller.Controller;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Factuality;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.datamodel.Task;

/**
 *
 * @author LEVALLOIS
 */
public class Umigon implements ModelInterface {

    private final HttpClient httpClient;

    private final Task task = Task.FACTUALITY_AND_SENTIMENT;

    boolean local = Boolean.TRUE;

    private final String API_KEY;
    
    public Umigon() {
        this.httpClient = HttpClient.newHttpClient();
        this.API_KEY = Controller.UMIGON_API_KEY;
    }

    @Override
    public String getName() {
        return "umigon";
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getPaperWebLink() {
        return "https://aclanthology.org/S13-2068/no";
    }

    @Override
    public String getAPIWebLink() {
        return "https://nocodefunctions.com/umigon/sentiment_analysis_tool.html";
    }

    
    @Override
    public Boolean areConcurrentAPICallsPossible() {
        return Boolean.TRUE;
    }    

    @Override
    public Sentiment extractSentimentLabelFromAPiResponse(String response) {
        return getLabelOnSentimentFromJson(response);
    }

    @Override
    public Factuality extractFactualityLabelFromAPiResponse(String response) {
        return getLabelOnFactualityFromJson(response);
    }

    @Override
    public String sendApiCall(AnnotatedDocument annotatedDocument) {
        URI uri = null;
        UrlBuilder urlBuilder = UrlBuilder
                .empty()
                .withPath("api/sentimentForAText")
                .addParameter("text-lang", "en")
                .addParameter("owner", API_KEY) // this param is not . If you don't have it, calls are throttled to 40 per seconds max.
                .addParameter("output-format", "json")
                .addParameter("explanation-lang", "en");

        if (local) {
            urlBuilder = urlBuilder
                    .withScheme("http")
                    .withHost("localhost")
                    .withPort(7002);

        } else {
            urlBuilder = urlBuilder
                    .withScheme("https")
                    .withHost("nocodefunctions.com");
        }
        uri = urlBuilder.toUri();

        HttpResponse<String> response = null;
        int waitloop = 1;
        while (response == null || response.statusCode() == 503) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(uri)
                        .POST(BodyPublishers.ofString(annotatedDocument.getText()))
                        .build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 503) {
                    // model still loading, let's wait
                    Thread.sleep(Duration.ofSeconds(1));
                    System.out.println("waiting for the model to load... (" + waitloop++ + ")");
                } else {
                    System.out.print("*");
                    if (API_KEY == null){
                        // without an API key for Umigon, calls get throttled to 50 max per second. This pause gives ample time.
                        Thread.sleep(Duration.ofMillis(30));
                    }

                }
            } catch (IOException | InterruptedException ex) {
                System.out.println("internet connexion probably broken for Umigon API: check it");
                try {
                    Thread.sleep(Duration.ofSeconds(3));
                } catch (InterruptedException ex1) {
                    Logger.getLogger(Thesis_Titan.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return response.body();
    }

    public Factuality getLabelOnFactualityFromJson(String response) {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject jo = jsonReader.readObject();
        if (jo.getString("sentiment").contains("neutral")) {
            return Factuality.OBJ;
        } else {
            return Factuality.SUBJ;
        }
    }

    public Sentiment getLabelOnSentimentFromJson(String response) {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject jo = jsonReader.readObject();
        String label = jo.getString("sentiment");
        switch (label) {
            case "neutral feeling" -> {
                return Sentiment.NEUTRAL;
            }
            case "negative feeling" -> {
                return Sentiment.NEGATIVE;
            }
            case "positive feeling" -> {
                return Sentiment.POSITIVE;
            }
            default -> {
                return Sentiment.NOT_SET;
            }
        }
    }
}
