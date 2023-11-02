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
public class Clef2023 implements DatasetInterface {

    private final String name = "clef2023";
    public final static String GOLD_LABELS = "gold_labels.json";
    public final static String DEV_SET = "subtask-2-english/dev_en.tsv";
    public final static String TRAIN_SET = "subtask-2-english/train_en.tsv";
    public final Task task = Task.FACTUALITY;

    private Map<String, AnnotatedDocument> goldenDocs = new ConcurrentHashMap();

    public Clef2023() {
    }

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
        return "https://gitlab.com/checkthat_lab/clef2023-checkthat-lab/-/tree/main/task2/data/subtask-2-english";
    }

    @Override
    public String getPaperWebLink() {
        return "https://doi.org/10.1007/978-3-031-42448-9";
    }

    @Override
    public int getNumberOfEntries() {
        int dev_set = 243;
        int train_set = 830;
        return dev_set + train_set;
    }

    @Override
    public Map<String, AnnotatedDocument> getGoldenLabels() {
        return goldenDocs;
    }

    @Override
    public Map<String, AnnotatedDocument> read() {
        JsonbConfig jsonbConfig = new JsonbConfig();
        jsonbConfig.withFormatting(Boolean.TRUE);
        Jsonb jsonb = JsonbBuilder.create(jsonbConfig);
        try {

            Path goldLabelsPath = Path.of(name, GOLD_LABELS);
            if (Files.exists(goldLabelsPath)) {
                try {
                    goldenDocs = jsonb.fromJson(Files.newBufferedReader(goldLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                    }.getClass().getGenericSuperclass());
                } catch (IOException ex) {
                    Logger.getLogger(Clef2023.class.getName()).log(Level.SEVERE, null, ex);
                }
                return goldenDocs;
            }

            List<String> headlinesWithLabels = Files.readAllLines(Path.of(name, TRAIN_SET), StandardCharsets.UTF_8);

            for (String headlineWithLabel : headlinesWithLabels) {
                String[] fields = headlineWithLabel.split("\t");
                String id = fields[0];
                String headline = fields[1];
                String labelAsString = fields[2];
                AnnotatedDocument docGold = new AnnotatedDocument(id, headline);
                Factuality goldLabel = Factuality.valueOf(labelAsString);
                Annotation goldAnnotation = Annotation.empty().withFactuality(goldLabel);
                docGold.addAnnotation(goldAnnotation);
                goldenDocs.put(id, docGold);
            }
            headlinesWithLabels = Files.readAllLines(Path.of(name, DEV_SET), StandardCharsets.UTF_8);

            for (String headlineWithLabel : headlinesWithLabels) {
                String[] fields = headlineWithLabel.split("\t");
                String id = fields[0];
                String headline = fields[1];
                String labelAsString = fields[2];
                AnnotatedDocument docGold = new AnnotatedDocument(id, headline);
                Factuality goldLabel = Factuality.valueOf(labelAsString);
                Annotation goldAnnotation = Annotation.empty().withFactuality(goldLabel);
                docGold.addAnnotation(goldAnnotation);
                goldenDocs.put(id, docGold);
            }
        } catch (IOException ex) {
            Logger.getLogger(Clef2023.class.getName()).log(Level.SEVERE, null, ex);
        }
        String json = jsonb.toJson(goldenDocs);
        try {
            Files.writeString(Path.of(name, GOLD_LABELS), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(Clef2023.class.getName()).log(Level.SEVERE, null, ex);
        }

        return goldenDocs;
    }

}
