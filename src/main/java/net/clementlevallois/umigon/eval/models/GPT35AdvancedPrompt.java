/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.models;

import io.mikael.urlbuilder.UrlBuilder;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.io.IOException;
import java.io.StringReader;
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
public class GPT35AdvancedPrompt implements ModelInterface {

    private final HttpClient httpClient;

    private final Task task = Task.SENTIMENT;

    private final String API_KEY;

    public GPT35AdvancedPrompt() {
        this.httpClient = HttpClient.newHttpClient();
        this.API_KEY = Controller.OPENAI_API_KEY;
    }

    @Override
    public String getName() {
        return "gpt-3.5-turbo-advanced-prompt";
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getPaperWebLink() {
        return "https://openai.com/blog/gpt-3-5-turbo-fine-tuning-and-api-updates";
    }

    @Override
    public String getAPIWebLink() {
        return "https://api.openai.com/v1/chat/completions";
    }

    @Override
    public Boolean areConcurrentAPICallsPossible() {
        return Boolean.FALSE;
    }

    @Override
    public Sentiment extractSentimentLabelFromAPiResponse(String response) {
        return getLabelOnSentimentFromJson(response);
    }

    @Override
    public Factuality extractFactualityLabelFromAPiResponse(String response) {
        throw new UnsupportedOperationException("This API does not return a label for factuality - so far");
    }

    @Override
    public String sendApiCall(AnnotatedDocument annotatedDocument) {

        String contentRoleSystem = """
                                   You are a the equivalent of a human annotator in a data labelling task. The task consists in labelling the sentiment of a text provided by the user. When annotating, be especially attentive to these 3 recommendations:
                                   1. you should annotate the sentiment expressed by the author of the text, not the sentiment expressed by a person cited in the text.
                                   2. a sentiment is expressed when the text reflects personal feelings, tastes, or opinions.
                                   3. a factual, even when it has strong positive or negative prior associations (such as "war" or "happyness"), is not a sentiment.
                                   
                                   The label should be a single word: "positive", "negative" or "neutral".
                                   """;
        String contentRoleUser = "The text to label: \n\n" + annotatedDocument.getText();

        JsonObjectBuilder overallObject = Json.createObjectBuilder();
        overallObject.add("model", "gpt-3.5-turbo");
        JsonArrayBuilder messages = Json.createArrayBuilder();
        JsonObjectBuilder messageSystemObject = Json.createObjectBuilder();
        messageSystemObject.add("role", "system");
        messageSystemObject.add("content", contentRoleSystem);
        JsonObjectBuilder messageUserObject = Json.createObjectBuilder();
        messageUserObject.add("role", "user");
        messageUserObject.add("content", contentRoleUser);
        messages.add(messageSystemObject);
        messages.add(messageUserObject);
        overallObject.add("messages", messages);
        JsonObject build = overallObject.build();
        String jsonString = build.toString();

        URI uri = UrlBuilder
                .empty()
                .withScheme("https")
                .withHost("api.openai.com")
                .withPath("v1/chat/completions").toUri();

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);
        HttpResponse<String> response = null;
        int waitloop = 1;
        HttpRequest request;
        int sleepBetweenRequests = 150;
        while (response == null || response.statusCode() != 200) {
            try {
                request = HttpRequest.newBuilder()
                        .POST(bodyPublisher)
                        .header("Authorization", "Bearer " + API_KEY)
                        .header("Content-Type", "application/json")
                        .uri(uri)
                        .build();
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != 200) {
                    System.out.println("");
                    System.out.println("ERROR: ");
                    System.out.println(response.body());
                    System.out.println("-----------");
                    Thread.sleep(Duration.ofSeconds(1));
                } else {
                    // adding a pause between calls corresponding to Tier 1 usage limits
                    // which are 500 requests per minute (RPM) and 40k tokens per minute for GPT-3.5
                    Thread.sleep(Duration.ofMillis(sleepBetweenRequests).toMillis());
                    System.out.print("*");
                }
            } catch (IOException | InterruptedException ex) {
                System.out.println("");
                System.out.println("internet connexion probably broken for GPT: check it");
                try {
                    Thread.sleep(Duration.ofSeconds(3));
                } catch (InterruptedException ex1) {
                    Logger.getLogger(GPT35AdvancedPrompt.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return response.body();
    }

    public Sentiment getLabelOnSentimentFromJson(String response) {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject jo = jsonReader.readObject();
        JsonArray choices = jo.getJsonArray("choices");
        JsonObject firstChoice = choices.getJsonObject(0);
        JsonObject message = firstChoice.getJsonObject("message");
        String label = message.getString("content").toLowerCase();
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
