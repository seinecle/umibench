/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.Map;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import static net.clementlevallois.umigon.eval.datamodel.Factuality.OBJ;
import static net.clementlevallois.umigon.eval.datamodel.Factuality.SUBJ;
import static net.clementlevallois.umigon.eval.datamodel.Sentiment.NEGATIVE;
import static net.clementlevallois.umigon.eval.datamodel.Sentiment.NEUTRAL;
import static net.clementlevallois.umigon.eval.datamodel.Sentiment.POSITIVE;
import net.clementlevallois.umigon.eval.datasets.DatasetInterface;

/**
 *
 * @author LEVALLOIS
 */
public class RunDescriptiveStatistics {

    private static final DecimalFormat decimalFormat = new DecimalFormat("0");

    public static void main(String[] args) throws IOException {
        Controller controller = new Controller();
        controller.loadProperties();
        controller.initDataSetsAndModels();

        for (DatasetInterface dataset : Controller.datasets) {
            float countObj = 0;
            float countSubj = 0;
            float countNeutral = 0;
            float countPositive = 0;
            float countNegative = 0;
            dataset.read();
            Map<String, AnnotatedDocument> goldenLabels = dataset.getGoldenLabels();
            for (Map.Entry<String, AnnotatedDocument> entry : goldenLabels.entrySet()) {
                AnnotatedDocument doc = entry.getValue();
                if (doc.getAnnotation().get().getFactuality().equals(OBJ)) {
                    countObj++;
                }
                if (doc.getAnnotation().get().getFactuality().equals(SUBJ)) {
                    countSubj++;
                }
                if (doc.getAnnotation().get().getSentiment().equals(POSITIVE)) {
                    countPositive++;
                }
                if (doc.getAnnotation().get().getSentiment().equals(NEGATIVE)) {
                    countNegative++;
                }
                if (doc.getAnnotation().get().getSentiment().equals(NEUTRAL)) {
                    countNeutral++;
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append("descriptive statistics on the golden labels for dataset ").append(dataset.getName());
            sb.append("\n");
            sb.append("\n");

            float total = dataset.getNumberOfEntries();

            switch (dataset.getTask()) {
                case FACTUALITY -> {
                    sb = addFactuality(sb, countObj, countSubj, total);
                }
                case SENTIMENT -> {
                    sb = addSentiment(sb, countPositive, countNegative, countNeutral, total);
                }
                case FACTUALITY_AND_SENTIMENT -> {
                    sb = addSentiment(sb, countPositive, countNegative, countNeutral, total);
                    sb = addFactuality(sb, countObj, countSubj, total);
                }
            }
            Files.writeString(Path.of(dataset.getName(), "stats_on_dataset.txt"), sb.toString());
        }
    }

    private static StringBuilder addSentiment(StringBuilder sb, float countPositive, float countNegative, float countNeutral, float total) {
        sb.append("labels on sentiment:");
        sb.append("\n");
        sb.append("positive: ");
        sb.append(countPositive);
        sb.append(" (");
        sb.append(decimalFormat.format(countPositive / total*100));
        sb.append("%)");
        sb.append("\n");
        sb.append("negative: ");
        sb.append(countNegative);
        sb.append(" (");
        sb.append(decimalFormat.format(countNegative / total*100));
        sb.append("%)");
        sb.append("\n");
        sb.append("neutral: ");
        sb.append(countNeutral);
        sb.append(" (");
        sb.append(decimalFormat.format(countNeutral / total*100));
        sb.append("%)");
        sb.append("\n");
        return sb;
    }

    private static StringBuilder addFactuality(StringBuilder sb, float countObj, float countSubj, float total) {
        sb.append("labels on factuality:");
        sb.append("\n");
        sb.append("objective: ");
        sb.append(countObj);
        sb.append(" (");
        sb.append(decimalFormat.format(countObj / total *100));
        sb.append("%)");
        sb.append("\n");
        sb.append("subjective: ");
        sb.append(countSubj);
        sb.append(" (");
        sb.append(decimalFormat.format(countSubj / total*100));
        sb.append("%)");
        sb.append("\n");
        return sb;

    }

}
