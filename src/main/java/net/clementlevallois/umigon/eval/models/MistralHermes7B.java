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
public class MistralHermes7B implements ModelInterface {

    private final HttpClient httpClient;

    private final Task task = Task.SENTIMENT;

    private final String API_KEY;

    public MistralHermes7B() {
        this.httpClient = HttpClient.newHttpClient();
        API_KEY = Controller.HUGGINGFACE_API_KEY;
    }

    @Override
    public String getName() {
        return "OpenHermes-2-Mistral-7B-basic-prompt";
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
    public String sendApiCall(AnnotatedDocument annotatedDocument) {
//        URI uri = UrlBuilder
//                .empty()
//                .withScheme("https")
//                .withHost("xgm12cmncnuvjkcp.us-east-1.aws.endpoints.huggingface.cloud")
//                //                .withPath("models/teknium/OpenHermes-2-Mistral-7B")
//                .toUri();

        
        throw new UnsupportedOperationException("This API is not yet configured");

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
