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
import net.clementlevallois.umigon.eval.datamodel.Task;

/**
 *
 * @author LEVALLOIS
 */
public class KaggleHeadlines implements DatasetInterface {

    private final String name = "kaggle-headlines";
    public final static String GOLD_LABELS = "gold_labels.json";
    public final static String DATA = "1000_headlines_labelled_manually.txt";
    public final Task task = Task.FACTUALITY;

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
        return "https://www.kaggle.com/datasets/rmisra/news-category-dataset?resource=download";
    }

    @Override
    public String getPaperWebLink() {
        return "https://arxiv.org/abs/2209.11429";
    }

    @Override
    public int getNumberOfEntries() {
        return 1000;
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
                    Logger.getLogger(KaggleHeadlines.class.getName()).log(Level.SEVERE, null, ex);
                }
                return goldenDocs;
            }
            List<String> headlinesWithLabels = Files.readAllLines(Path.of(name, DATA), StandardCharsets.UTF_8);
            for (String headlineWithLabel : headlinesWithLabels) {
                String[] fields = headlineWithLabel.split("\t");
                String headline = fields[0];
                String labelAsString = fields[1];
                AnnotatedDocument docGold = new AnnotatedDocument(headline);
                String id = docGold.getId();
                Factuality goldLabel = Factuality.valueOf(labelAsString);
                Annotation goldAnnotation = Annotation.empty().withFactuality(goldLabel);
                docGold.addAnnotation(goldAnnotation);
                goldenDocs.put(id, docGold);
            }
            String json = jsonb.toJson(goldenDocs);
            try {
                Files.writeString(Path.of(name, GOLD_LABELS), json, StandardCharsets.UTF_8);
            } catch (IOException ex) {
                Logger.getLogger(KaggleHeadlines.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (IOException ex) {
            Logger.getLogger(KaggleHeadlines.class.getName()).log(Level.SEVERE, null, ex);
        }
        return goldenDocs;
    }

}
