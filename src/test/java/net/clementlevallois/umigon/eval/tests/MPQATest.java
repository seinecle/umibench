/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.tests;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Annotation;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.datasets.MPQA;
import org.junit.jupiter.api.Test;

/**
 *
 * @author LEVALLOIS
 */
public class MPQATest {

    public void writeMPQAToFile() throws IOException {
        MPQA mpqa = new MPQA();
        Map<String, AnnotatedDocument> data = mpqa.read();
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, AnnotatedDocument> entry : data.entrySet()) {
            sb.append(entry.getKey());
            sb.append(" # ");
            sb.append(entry.getValue().getText());
            sb.append("\n");
        }
        Files.writeString(Path.of("mpqa-entries.txt"), sb.toString());
    }

    @Test
    public void writePredictedLabelsforMistral() throws IOException {
        Map<String, AnnotatedDocument> predictedLabels = new ConcurrentHashMap();

        List<String> allLines = Files.readAllLines(Path.of("mpqa", "results", "mpqa-entries.txt"));
        for (String line : allLines) {
            int indexOfDelimiter = line.indexOf("#");
            String uuid = line.substring(0, indexOfDelimiter).trim();
            String text = line.substring(indexOfDelimiter).trim();
            AnnotatedDocument doc = new AnnotatedDocument(uuid, text);
            predictedLabels.put(uuid, doc);
        }

        JsonbConfig jsonbConfig = new JsonbConfig();
        jsonbConfig.withFormatting(Boolean.TRUE);
        Jsonb jsonb = JsonbBuilder.create(jsonbConfig);

        List<String> lines = Files.readAllLines(Path.of("mpqa/results/result_annotation.txt"));

        for (String line : lines) {
            String uuid;
            String label;
            if (line.contains("#")) {
                String[] fields = line.split("\\#");
                uuid = fields[0].trim();
                label = fields[1].trim();
            } else {
                int indexFirstSpace = line.indexOf(" ");
                uuid = line.substring(0, indexFirstSpace).trim();
                label = line.substring(indexFirstSpace).trim();
            }
            AnnotatedDocument doc = predictedLabels.get(uuid);
            if (doc == null) {
                String shorterUUID = uuid.substring(0, 8);
                for (Map.Entry<String, AnnotatedDocument> entry : predictedLabels.entrySet()) {
                    if (entry.getKey().startsWith(shorterUUID)) {
                        doc = entry.getValue();
                        break;
                    }
                }
            }
            if (doc == null){
                String shorterUUID = uuid.substring(32);
                for (Map.Entry<String, AnnotatedDocument> entry : predictedLabels.entrySet()) {
                    if (entry.getKey().endsWith(shorterUUID)) {
                        doc = entry.getValue();
                        break;
                    }
                }
            }
            if (doc == null){
                System.out.println("Houston we still have a problem");
                System.out.println("faulty line: " + line);
            }

            switch (label.toLowerCase()) {
                case "negative" -> {
                    Annotation annotation = Annotation.empty().withSentiment(Sentiment.NEGATIVE);
                    doc.addAnnotation(annotation);
                }
                case "positive" -> {
                    Annotation annotation = Annotation.empty().withSentiment(Sentiment.POSITIVE);
                    doc.addAnnotation(annotation);
                }
                case "neutral" -> {
                    Annotation annotation = Annotation.empty().withSentiment(Sentiment.NEUTRAL);
                    doc.addAnnotation(annotation);
                }
                default -> {
                    Annotation annotation = Annotation.empty().withSentiment(Sentiment.NOT_SET);
                    doc.addAnnotation(annotation);
                }
            }

        }
        String json = jsonb.toJson(predictedLabels);
        Files.writeString(getPathResultOfOneEval("mpqa", "OpenHermes-2-Mistral-7B"), json, StandardCharsets.UTF_8);

    }

    private static Path getPathResultOfOneEval(String datasetName, String model) {
        return Path.of(datasetName, "results", datasetName + "_evaluated_with_" + model + ".txt");
    }

}
