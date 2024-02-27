/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datasets;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Annotation;
import net.clementlevallois.umigon.eval.datamodel.Factuality;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.datamodel.Task;

/**
 *
 * @author LEVALLOIS
 */
public class Carblacac implements DatasetInterface {

    private final String name = "carblacac";
    public final static String GOLD_LABELS = "gold_labels.json";
    public final static String DATA = "train_150k_clement_levallois.txt";
    public final Task task = Task.FACTUALITY_AND_SENTIMENT;

    private Map<String, AnnotatedDocument> goldenDocs = new ConcurrentHashMap();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Task getTask() {
        return task;
    }

    @Override
    public String getDataWebLink() {
        return "https://huggingface.co/datasets/carblacac/twitter-sentiment-analysis";
    }

    @Override
    public String getPaperWebLink() {
        return "https://huggingface.co/datasets/carblacac/twitter-sentiment-analysis";
    }

    @Override
    public String getShortDescription() {
        return "a set of 200 tweets from anonymous individuals on their daily lives annotated for negative and positive sentiment";
    }

    @Override
    public int getNumberOfEntries() {
        return 200;
    }

    @Override
    public Map<String, AnnotatedDocument> getGoldenLabels() {
        return goldenDocs;
    }

    @Override
    public Map<String, AnnotatedDocument> read() {
        try {
            JsonbConfig jsonbConfig = new JsonbConfig();
            jsonbConfig.withFormatting(Boolean.TRUE);
            Jsonb jsonb = JsonbBuilder.create(jsonbConfig);
            Path goldLabelsPath = Path.of(name, GOLD_LABELS);
            if (Files.exists(goldLabelsPath)) {
                try {
                    goldenDocs = jsonb.fromJson(Files.newBufferedReader(goldLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                    }.getClass().getGenericSuperclass());
                } catch (IOException ex) {
                    Logger.getLogger(Carblacac.class.getName()).log(Level.SEVERE, null, ex);
                }
                return goldenDocs;
            }
            List<String> headlinesWithLabels = Files.readAllLines(Path.of(name, DATA), StandardCharsets.UTF_8);
            for (String headlineWithLabel : headlinesWithLabels) {
                String[] fields = headlineWithLabel.split("\t");
                String tweet = fields[1];
                String labelAsString = fields[0];
                AnnotatedDocument docGold = new AnnotatedDocument(tweet);
                String id = docGold.getId();
                Sentiment sentimentGoldLabel;
                sentimentGoldLabel = switch (labelAsString) {
                    case "0" -> Sentiment.NEGATIVE;
                    case "1" -> Sentiment.POSITIVE;
                    default -> Sentiment.NOT_SET;
                };
                Factuality factualityGoldlabel = Factuality.SUBJ;
                Annotation goldAnnotation = Annotation.empty().withFactuality(factualityGoldlabel).withSentiment(sentimentGoldLabel);
                docGold.addAnnotation(goldAnnotation);
                goldenDocs.put(id, docGold);
            }
            String json = jsonb.toJson(goldenDocs);
            try {
                Files.writeString(Path.of(name, GOLD_LABELS), json, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                Logger.getLogger(Carblacac.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(Carblacac.class.getName()).log(Level.SEVERE, null, ex);
        }
        return goldenDocs;
    }

}
