/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.models;

import io.mikael.urlbuilder.UrlBuilder;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
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
public class Mistral7BHermesAdvancedPrompt implements ModelInterface {

    private final HttpClient httpClient;

    private final Task task = Task.FACTUALITY_AND_SENTIMENT;

    private final String API_KEY;

    public Mistral7BHermesAdvancedPrompt() {
        this.httpClient = HttpClient.newHttpClient();
        API_KEY = Controller.HUGGINGFACE_API_KEY;
    }

    @Override
    public String getName() {
        return "OpenHermes-2-Mistral-7B-advanced-prompt";
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getPaperWebLink() {
        return "https://huggingface.co/teknium/OpenHermes-2-Mistral-7B";
    }

    @Override
    public String getAPIWebLink() {
        return "https://huggingface.co/teknium/OpenHermes-2-Mistral-7B";
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
        URI uri = UrlBuilder
                .empty()
                .withScheme("https")
                .withHost("zb7s4fpu9b8a4hqs.us-east-1.aws.endpoints.huggingface.cloud")
                //                .withPath("models/teknium/OpenHermes-2-Mistral-7B")
                .toUri();

        String input = """
        This is a textbook about natural language processing (NLP). Sentiment analysis is a classic task that we detail in this chapter. The annotation for sentiment consists in labelling a text with one of these three labels: \"positive\", \"negative\" or \"neutral\".
        For example, the text "I am very happy that she could come" will be labelled as "positive".
        It is important to note that the quality of the labelling depends on the strict following of these instructions:
                       - the annotator should use a single word for the label of the sentiment, without further comment. The word should be "neutral", "positive" or "negative".
                       - the annotator identifies a sentiment when the text reflects personal feelings, tastes, or opinions.
                       - the annotator should label the sentiment expressed by the author of the text, not the sentiment expressed by a person cited in the text.
                       - the annotator should be careful that a factual, even when it has strong positive or negative prior associations (such as "war" or "happyness"), is not a sentiment.
        
        The following examples will illustrate this lesson:
                       
        Example 1:
        - The text to label for sentiment: "I love chocolate"
        - The label: positive
                       
        Example 2:
       - The text to label for sentiment: "She says she loves chocolate"
       - The label: neutral
                                                      
        Example 3:
        - The text to label for sentiment: \"""";
        input = input + annotatedDocument.getText() + "\"\n- The label: ";

        JsonObjectBuilder overallObject = Json.createObjectBuilder();
        overallObject.add("inputs", input);
        overallObject.add("use_cache", false);
        StringWriter sw = new StringWriter(128);
        try (JsonWriter jw = Json.createWriter(sw)) {
            jw.write(overallObject.build());
        }
        String jsonString = sw.toString();

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);
        HttpResponse<String> response = null;
        int waitloop = 1;
        HttpRequest request;
        int sleepBetweenRequests = 20;
        while (response == null || response.statusCode() != 200) {
            try {
                request = HttpRequest.newBuilder()
                        .POST(bodyPublisher)
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .uri(uri)
                        .build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 503) {
                    // model still loading, let's wait
                    Thread.sleep(Duration.ofMillis(1000 + sleepBetweenRequests));
                    System.out.println("");
                    System.out.println("waiting for the model to load... (" + waitloop++ + ")");
                    sleepBetweenRequests = sleepBetweenRequests + 20;
                } else if (response.statusCode() != 200) {
                    System.out.println("");
                    System.out.println("ERROR: ");
                    System.out.println(response.body());
                    System.out.println("-----------");
                    Thread.sleep(Duration.ofSeconds(1));
                } else {
                    // adding a pause between calls for the Hugging Face API
                    Thread.sleep(Duration.ofMillis(sleepBetweenRequests).toMillis());
                    System.out.print("*");
                }
            } catch (IOException | InterruptedException ex) {
                System.out.println("");
                System.out.println("internet connexion probably broken for Mistral Hermes 7B: check it");
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
        JsonArray ja = jsonReader.readArray();
        JsonObject generatedTextObject = ja.getJsonObject(0);
        String generatedText = generatedTextObject.getString("generated_text");
        int maxLength = Math.min(generatedText.length() -1, 25);
        String label = generatedText.substring(0, maxLength).toLowerCase();
        if (label.contains("positive") || label.contains("negative")) {
            return Factuality.SUBJ;
        } else if (label.contains("neutral")) {
            return Factuality.OBJ;
        } else {
            System.out.println("weird response: ");
            System.out.println(response);
            return Factuality.NOT_SET;
        }
    }

    public Sentiment getLabelOnSentimentFromJson(String response) {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonArray ja = jsonReader.readArray();
        JsonObject generatedTextObject = ja.getJsonObject(0);
        String generatedText = generatedTextObject.getString("generated_text");
        int maxLength = Math.min(generatedText.length() -1, 25);
        String label = generatedText.substring(0, maxLength).toLowerCase();
        if (label.contains("positive")) {
            return Sentiment.POSITIVE;
        } else if (label.contains("negative")) {
            return Sentiment.NEGATIVE;
        } else if (label.contains("neutral")) {
            return Sentiment.NEUTRAL;
        } else {
            System.out.println("weird response: ");
            System.out.println(response);
            return Sentiment.NOT_SET;
        }
    }

}
