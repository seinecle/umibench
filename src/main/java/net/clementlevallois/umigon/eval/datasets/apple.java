/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datasets;

import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class Apple implements DatasetInterface {

    private final String name = "apple";
    public final static String GOLD_LABELS = "gold_labels.json";
    public final static String DATA = "full-corpus.csv";
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
        return "https://github.com/seinecle/twitter_corpus_sanders_analytics";
    }

    @Override
    public String getPaperWebLink() {
        return "https://oro.open.ac.uk/40660/";
    }

    @Override
    public String getShortDescription() {
        return "a set of tweets mentioning Apple and annotated for neutral, positive and negative sentiment";
    }

    @Override
    public int getNumberOfEntries() {
        return 1_003;
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
        CsvParserSettings settings = new CsvParserSettings();
        settings.detectFormatAutomatically();
        settings.setMaxCharsPerColumn(-1);
        settings.setMaxColumns(5);
        settings.setQuoteDetectionEnabled(true);
        CsvParser parser = new CsvParser(settings);
        List<String[]> rows = parser.parseAll(Path.of(name, DATA).toFile(), StandardCharsets.UTF_8);
        for (String[] row : rows) {
            if (!row[0].equals("apple")) {
                continue;
            }
            String tweet = row[4];
            String labelAsString = row[1];
            AnnotatedDocument docGold = new AnnotatedDocument(tweet);
            String id = docGold.getId();
            Sentiment sentimentGoldLabel;
            Factuality factualityGoldlabel;
            switch (labelAsString) {
                case "neutral" -> {
                    sentimentGoldLabel = Sentiment.NEUTRAL;
                    factualityGoldlabel = Factuality.OBJ;
                }
                case "positive" -> {
                    sentimentGoldLabel = Sentiment.POSITIVE;
                    factualityGoldlabel = Factuality.SUBJ;
                }
                case "negative" -> {
                    sentimentGoldLabel = Sentiment.NEGATIVE;
                    factualityGoldlabel = Factuality.SUBJ;
                }
                default -> {
                    sentimentGoldLabel = Sentiment.NOT_SET;
                    factualityGoldlabel = Factuality.NOT_SET;
                }
            }
            Annotation goldAnnotation = Annotation.empty().withFactuality(factualityGoldlabel).withSentiment(sentimentGoldLabel);
            docGold.addAnnotation(goldAnnotation);
            goldenDocs.put(id, docGold);
        }
        System.out.println("number of entries for Apple dataset: " + goldenDocs.size());
        String json = jsonb.toJson(goldenDocs);
        try {
            Files.writeString(Path.of(name, GOLD_LABELS), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(Carblacac.class.getName()).log(Level.SEVERE, null, ex);
        }
        return goldenDocs;
    }

}
