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
import jakarta.json.JsonWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
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
public class Thesis_Titan implements ModelInterface {

    private final HttpClient httpClient;

    private final Task task = Task.FACTUALITY;
    
    private final String API_KEY;

    public Thesis_Titan() {
        this.httpClient = HttpClient.newHttpClient();
        API_KEY = Controller.HUGGINGFACE_API_KEY;
    }

    @Override
    public String getName() {
        return "Thesis_Titan";
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getPaperWebLink() {
        return "https://ceur-ws.org/Vol-3497/paper-020.pdf";
    }

    @Override
    public String getAPIWebLink() {
        return "https://huggingface.co/GroNLP/mdebertav3-subjectivity-english";
    }
    
    @Override
    public Boolean areConcurrentAPICallsPossible() {
        return Boolean.TRUE;
    }    

    @Override
    public Sentiment extractSentimentLabelFromAPiResponse(String response) {
        throw new UnsupportedOperationException("This API does not return a label for sentiment");
    }

    @Override
    public Factuality extractFactualityLabelFromAPiResponse(String response) {
        if (response.startsWith("[")) {
            return getLabelOnObjectivityFromJsonArray(response);
        } else {
            return getLabelOnObjectivityFromJsonObject(response);
        }
    }

    @Override
    public String sendApiCall(AnnotatedDocument annotatedDocument) {
        URI uri = UrlBuilder
                .empty()
                .withScheme("https")
                .withHost("c463vy3xx8s1dx18.us-east-1.aws.endpoints.huggingface.cloud")
                //                .withPath("models/GroNLP/mdebertav3-subjectivity-english")
                .toUri();

        JsonObjectBuilder overallObject = Json.createObjectBuilder();
        overallObject.add("inputs", annotatedDocument.getText());
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
                    System.out.println("-----------");
                    Thread.sleep(Duration.ofSeconds(1));
                } else {
                    // adding a pause between calls for the Hugging Face API
                    Thread.sleep(Duration.ofMillis(sleepBetweenRequests).toMillis());
                    System.out.print("*");
                }
            } catch (IOException | InterruptedException ex) {
                System.out.println("");
                System.out.println("internet connexion probably broken for GroNLP: check it");
                try {
                    Thread.sleep(Duration.ofSeconds(3));
                } catch (InterruptedException ex1) {
                    Logger.getLogger(Thesis_Titan.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        }
        return response.body();
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
            } else {
                return Factuality.NOT_SET;
            }
        } else {
            JsonObject responseObject = array.getJsonObject(0);
            String label = responseObject.getString("label");
            float score = responseObject.getJsonNumber("score").bigDecimalValue().floatValue();
            if (label.equals("LABEL_0") && score > 0.5) {
                return Factuality.OBJ;
            } else if (label.equals("LABEL_1") && score > 0.5) {
                return Factuality.SUBJ;
            } else {
                return Factuality.NOT_SET;
            }
        }
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
}
