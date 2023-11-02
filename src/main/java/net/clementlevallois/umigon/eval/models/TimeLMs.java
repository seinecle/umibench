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
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Iterator;
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
public class TimeLMs implements ModelInterface {

    private final HttpClient httpClient;

    private final Task task = Task.FACTUALITY_AND_SENTIMENT;

    private final String API_KEY;
    
    
    public TimeLMs() {
        this.httpClient = HttpClient.newHttpClient();
        API_KEY = Controller.HUGGINGFACE_API_KEY;
    }

    @Override
    public String getName() {
        return "TimeLMs";
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getPaperWebLink() {
        return "https://arxiv.org/abs/2202.03829";
    }

    @Override
    public String getAPIWebLink() {
        return "https://huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest";
    }

    @Override
    public String sendApiCall(AnnotatedDocument annotatedDocument) {
        URI uri = UrlBuilder
                .empty()
                .withScheme("https")
                .withHost("km49e5ysuccbhkg3.eu-west-1.aws.endpoints.huggingface.cloud")
                //                .withPath("models/cardiffnlp/twitter-roberta-base-sentiment-latest")
                .toUri();

        // this model accepts inputs of max length 511
        // see https://huggingface.co/cardiffnlp/twitter-roberta-base-sentiment-latest/discussions/2
        String input = annotatedDocument.getText();
        int maxInputSize = Math.min(input.length(), 510);

        input = input.substring(0, maxInputSize);

        JsonObjectBuilder overallObject = Json.createObjectBuilder();
        overallObject.add("inputs", input);
        overallObject.add("use_cache", false);
        String jsonString = overallObject.build().toString();

        HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.ofString(jsonString);
        HttpResponse<String> response = null;
        int waitloop = 1;
        HttpRequest request;
        int sleepBetweenRequests = 1;
        while (response == null || response.statusCode() != 200) {
            try {
                request = HttpRequest.newBuilder()
                        .POST(bodyPublisher)
                        .header("Authorization", "Bearer "+ API_KEY)
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
                    System.out.println("text was: " + annotatedDocument.getText());
                    System.out.println("-----------");
                    Thread.sleep(Duration.ofSeconds(1));
                } else {
                    // adding a pause between calls for the Hugging Face API
                    Thread.sleep(Duration.ofMillis(sleepBetweenRequests).toMillis());
                    System.out.print("*");
                }
            } catch (IOException | InterruptedException ex) {
                System.out.println("");
                System.out.println("internet connexion probably broken on Twitter Roberta: check it");
                try {
                    Thread.sleep(Duration.ofSeconds(3));
                } catch (InterruptedException ex1) {
                    Logger.getLogger(TimeLMs.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return response.body();
    }

    @Override
    public Sentiment extractSentimentLabelFromAPiResponse(String response) {
        return getLabelOnSentimentFromJsonArray(response);
    }

    @Override
    public Factuality extractFactualityLabelFromAPiResponse(String response) {
        return getLabelOnFactualityFromJson(response);
    }

    public Factuality getLabelOnFactualityFromJson(String response) {
        if (response.startsWith("[")) {
            return getLabelOnObjectivityFromJsonArray(response);
        } else {
            return getLabelOnObjectivityFromJsonObject(response);
        }
    }

    private Factuality getLabelOnObjectivityFromJsonArray(String response) {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonArray array = jsonReader.readArray();
        JsonObject firstLabelResponse;
        JsonObject secondLabelResponse;
        float scoreLabel0 = -1;
        float scoreLabel1 = -1;
        if (array.get(0).getValueType().equals(ValueType.ARRAY)) {
            JsonArray responseArray = array.getJsonArray(0);
            firstLabelResponse = responseArray.getJsonObject(0);
            secondLabelResponse = responseArray.getJsonObject(1);
            if (secondLabelResponse.getString("label").equals("LABEL_0")) {
                scoreLabel0 = secondLabelResponse.getJsonNumber("score").bigDecimalValue().floatValue();
            } else {
                scoreLabel1 = secondLabelResponse.getJsonNumber("score").bigDecimalValue().floatValue();
            }
            if (firstLabelResponse.getString("label").equals("LABEL_0")) {
                scoreLabel0 = firstLabelResponse.getJsonNumber("score").bigDecimalValue().floatValue();
            } else {
                scoreLabel1 = firstLabelResponse.getJsonNumber("score").bigDecimalValue().floatValue();
            }
            if (scoreLabel0 >= 0 && scoreLabel1 >= 0) {
                if (scoreLabel0 > scoreLabel1) {
                    return Factuality.OBJ;
                } else {
                    return Factuality.SUBJ;
                }
            }
        } else {
            JsonObject responseObject = array.getJsonObject(0);
            String label = responseObject.getString("label");
            float score = responseObject.getJsonNumber("score").bigDecimalValue().floatValue();
            if (label.equals("neutral") && score > 0.5) {
                return Factuality.OBJ;
            } else {
                return Factuality.SUBJ;
            }

        }
        return null;
    }

    private Factuality getLabelOnObjectivityFromJsonObject(String response) {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonObject jsonObject = jsonReader.readObject();
        JsonArray responseArray = jsonObject.getJsonArray("response");
        Iterator<JsonValue> iteratorOnResponse = responseArray.iterator();
        float scoreLabel0 = -1;
        float scoreLabel1 = -1;
        while (iteratorOnResponse.hasNext()) {
            JsonArray nextLabelResponse = iteratorOnResponse.next().asJsonArray();
            JsonObject jsonObjectLabel = nextLabelResponse.getJsonObject(0);
            if (jsonObjectLabel.containsKey("LABEL_0")) {
                scoreLabel0 = jsonObjectLabel.getJsonNumber("LABEL_0").bigDecimalValue().floatValue();
            }
            if (jsonObjectLabel.containsKey("LABEL_1")) {
                scoreLabel1 = jsonObjectLabel.getJsonNumber("LABEL_1").bigDecimalValue().floatValue();
            }
        }
        if (scoreLabel0 >= 0 && scoreLabel1 >= 0) {
            if (scoreLabel0 > scoreLabel1) {
                return Factuality.OBJ;
            } else {
                return Factuality.SUBJ;
            }
        } else {
            System.out.println("error in scores with response " + response);
            return null;
        }
    }

    public Sentiment getLabelOnSentimentFromJsonArray(String response) {
        JsonReader jsonReader = Json.createReader(new StringReader(response));
        JsonArray array = jsonReader.readArray();
        JsonObject responseObject = array.getJsonObject(0);
        String label = responseObject.getString("label");
        if (label.equalsIgnoreCase("negative")) {
            return Sentiment.NEGATIVE;
        } else if (label.equalsIgnoreCase("positive")) {
            return Sentiment.POSITIVE;
        } else if (label.equalsIgnoreCase("neutral")) {
            return Sentiment.NEUTRAL;
        } else {
            return Sentiment.NOT_SET;
        }
    }
}
