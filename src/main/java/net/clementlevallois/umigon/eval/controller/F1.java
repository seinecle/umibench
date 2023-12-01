/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import net.clementlevallois.umigon.eval.datamodel.Factuality;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import static net.clementlevallois.umigon.eval.datamodel.Factuality.OBJ;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import static net.clementlevallois.umigon.eval.datamodel.Sentiment.NEGATIVE;

/**
 *
 * @author LEVALLOIS
 */
public class F1 {

    private static int limitRecordsForTests = Integer.MAX_VALUE;

    public static void setLimitForTests(int i) {
        limitRecordsForTests = i;
    }

    public static float computeBasedOnFactuality(Map<String, AnnotatedDocument> gold, Map<String, AnnotatedDocument> modelToBeEvaluated, String datasetName, String modelName, boolean printErrors) {
        try {
            float factualFalseNegatives = 0;
            float factualFalsePositives = 0;
            float factualTruePositives = 0;
            float subjectiveFalseNegatives = 0;
            float subjectiveFalsePositives = 0;
            float subjectiveTruePositives = 0;
            StringBuilder sbGoldLabelObjective = new StringBuilder();
            StringBuilder sbGoldLabelSubjective = new StringBuilder();

            int trueFactualInstances = 0;
            int trueSubjectiveInstances = 0;

            sbGoldLabelObjective.append("gold label was objective not subjective: ");
            sbGoldLabelObjective.append("\n");
            sbGoldLabelObjective.append("\n");
            sbGoldLabelSubjective.append("gold label was subjective not objective: ");
            sbGoldLabelSubjective.append("\n");
            sbGoldLabelSubjective.append("\n");

            int i = 0;

            Set<Map.Entry<String, AnnotatedDocument>> entrySet = modelToBeEvaluated.entrySet();
            for (Map.Entry<String, AnnotatedDocument> entry : entrySet) {
                if (i++ > limitRecordsForTests) {
                    break;
                }
                try {
                    String idOfDocument = entry.getKey();
                    AnnotatedDocument docWithPredictedLabel = entry.getValue();
                    Factuality predictedLabelFactuality = docWithPredictedLabel.getAnnotation().get().getFactuality();

                    AnnotatedDocument goldDoc = gold.get(idOfDocument);
                    Factuality goldLabelFactuality = goldDoc.getAnnotation().get().getFactuality();
                    switch (goldLabelFactuality) {
                        case OBJ ->
                            trueFactualInstances++;
                        case SUBJ ->
                            trueSubjectiveInstances++;
                        case NOT_SET ->
                            System.out.println("a doc had no factuality set");
                    }
                    
                    
                    if (goldLabelFactuality.equals(predictedLabelFactuality) && goldLabelFactuality.equals(Factuality.OBJ)) {
                        factualTruePositives++;
                    }
                    if (goldLabelFactuality.equals(predictedLabelFactuality) && goldLabelFactuality.equals(Factuality.SUBJ)) {
                        subjectiveTruePositives++;
                    }
                    if (goldLabelFactuality.equals(Factuality.OBJ) & predictedLabelFactuality.equals(Factuality.SUBJ)) {
                        if (printErrors) {
                            sbGoldLabelObjective.append(goldDoc.getText());
                            sbGoldLabelObjective.append("\n");
                            sbGoldLabelObjective.append("----");
                            sbGoldLabelObjective.append("\n");
                        }
                        subjectiveFalsePositives++;
                        factualFalseNegatives++;
                    }
                    if (goldLabelFactuality.equals(Factuality.SUBJ) & predictedLabelFactuality.equals(Factuality.OBJ)) {
                        if (printErrors) {
                            sbGoldLabelSubjective.append(goldDoc.getText());
                            sbGoldLabelSubjective.append("\n");
                            sbGoldLabelSubjective.append("----");
                            sbGoldLabelSubjective.append("\n");
                        }
                        subjectiveFalseNegatives++;
                        factualFalsePositives++;
                    }
                } catch (Exception e) {
                    System.out.println("ex: " + e);
                }
            }
            float factualRecall = factualTruePositives / (factualTruePositives + factualFalseNegatives);
            float subjectiveRecall = subjectiveTruePositives / (subjectiveTruePositives + subjectiveFalseNegatives);

            float factualPrecision = factualTruePositives / (factualTruePositives + factualFalsePositives);
            float subjectivePrecision = subjectiveTruePositives / (subjectiveTruePositives + subjectiveFalsePositives);

            float factualF1 = (2 * factualTruePositives) / (2 * factualTruePositives + factualFalsePositives + factualFalseNegatives);
            float subjectiveF1 = (2 * subjectiveTruePositives) / (2 * subjectiveTruePositives + subjectiveFalsePositives + subjectiveFalseNegatives);

            System.out.println("F1 factual class: " + factualF1);
            System.out.println("F1 subjective class: " + subjectiveF1);

            float weightedF1;
            if (trueFactualInstances == 0) {
                weightedF1 = subjectiveF1;
            } else if (trueSubjectiveInstances == 0) {
                weightedF1 = factualF1;
            } else {
                weightedF1 = (factualF1 * trueFactualInstances + subjectiveF1 * trueSubjectiveInstances) / (trueFactualInstances + trueSubjectiveInstances);
            }

            if (printErrors) {
                Files.writeString(Path.of(datasetName, "results", modelName + " - objective statement misclassified as subjective.txt"), sbGoldLabelObjective.toString(), StandardCharsets.UTF_8);
                Files.writeString(Path.of(datasetName, "results", modelName + " - subjective statement misclassified as objective.txt"), sbGoldLabelSubjective.toString(), StandardCharsets.UTF_8);
            }
            return weightedF1;
        } catch (IOException ex) {
            Logger.getLogger(F1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0f;
    }

    public static float computeBasedOnSentiment(Map<String, AnnotatedDocument> gold, Map<String, AnnotatedDocument> modelToBeEvaluated, String datasetname, String modelName, boolean printErrors) {
        try {
            float positiveClassFalseNegatives = 0;
            float positiveClassFalsePositives = 0;
            float positiveClassTruePositives = 0;
            float positiveClassTrueNegatives = 0;
            float negativeClassFalseNegatives = 0;
            float negativeClassFalsePositives = 0;
            float negativeClassTruePositives = 0;
            float negativeClassTrueNegatives = 0;
            float neutralClassFalseNegatives = 0;
            float neutralClassFalsePositives = 0;
            float neutralClassTruePositives = 0;
            float neutralClassTrueNegatives = 0;

            int truePositiveInstances = 0;
            int trueNegativeInstances = 0;
            int trueNeutralInstances = 0;

            StringBuilder sbGoldLabelPositive = new StringBuilder();
            StringBuilder sbGoldLabelNegative = new StringBuilder();
            StringBuilder sbGoldLabelNeutral = new StringBuilder();

            sbGoldLabelPositive.append("gold label was positive: ");
            sbGoldLabelPositive.append("\n");
            sbGoldLabelPositive.append("\n");
            sbGoldLabelNegative.append("gold label was negative: ");
            sbGoldLabelNegative.append("\n");
            sbGoldLabelNegative.append("\n");
            sbGoldLabelNeutral.append("gold label was neutral: ");
            sbGoldLabelNeutral.append("\n");
            sbGoldLabelNeutral.append("\n");

            Set<Map.Entry<String, AnnotatedDocument>> entrySet = modelToBeEvaluated.entrySet();
            int i = 0;
            for (Map.Entry<String, AnnotatedDocument> entry : entrySet) {
                if (i++ > limitRecordsForTests) {
                    break;
                }
                try {
                    String idOfDocument = entry.getKey();
                    AnnotatedDocument docWithPredictedLabel = entry.getValue();
                    Sentiment predictedLabel = docWithPredictedLabel.getAnnotation().get().getSentiment();

                    AnnotatedDocument goldDoc = gold.get(idOfDocument);
                    Sentiment goldLabel = goldDoc.getAnnotation().get().getSentiment();
                    switch (goldLabel) {
                        case NEGATIVE ->
                            trueNegativeInstances++;
                        case POSITIVE ->
                            truePositiveInstances++;
                        case NEUTRAL ->
                            trueNeutralInstances++;
                    }

                    if (goldLabel.equals(predictedLabel) & predictedLabel.equals(Sentiment.POSITIVE)) {
                        positiveClassTruePositives++;
                    }
                    if (goldLabel.equals(predictedLabel) & predictedLabel.equals(Sentiment.NEUTRAL)) {
                        neutralClassTruePositives++;
                    }
                    if (goldLabel.equals(predictedLabel) & predictedLabel.equals(Sentiment.NEGATIVE)) {
                        negativeClassTruePositives++;
                    }
                    if (goldLabel.equals(Sentiment.POSITIVE) & predictedLabel.equals(Sentiment.NEUTRAL)) {
                        if (printErrors) {
                            sbGoldLabelPositive.append(goldDoc.getText());
                            sbGoldLabelPositive.append("\n");
                            sbGoldLabelPositive.append("----");
                            sbGoldLabelPositive.append("\n");
                        }
                        positiveClassFalseNegatives++;
                        neutralClassFalsePositives++;
                        negativeClassTrueNegatives++;
                    }
                    if (goldLabel.equals(Sentiment.POSITIVE) & predictedLabel.equals(Sentiment.NEGATIVE)) {
                        if (printErrors) {
                            sbGoldLabelPositive.append(goldDoc.getText());
                            sbGoldLabelPositive.append("\n");
                            sbGoldLabelPositive.append("----");
                            sbGoldLabelPositive.append("\n");
                        }
                        positiveClassFalseNegatives++;
                        neutralClassTrueNegatives++;
                        negativeClassFalsePositives++;
                    }
                    if (goldLabel.equals(Sentiment.NEGATIVE) & predictedLabel.equals(Sentiment.NEUTRAL)) {
                        if (printErrors) {
                            sbGoldLabelNegative.append(goldDoc.getText());
                            sbGoldLabelNegative.append("\n");
                            sbGoldLabelNegative.append("----");
                            sbGoldLabelNegative.append("\n");
                        }
                        neutralClassFalsePositives++;
                        negativeClassFalseNegatives++;
                        positiveClassTrueNegatives++;
                    }
                    if (goldLabel.equals(Sentiment.NEGATIVE) & predictedLabel.equals(Sentiment.POSITIVE)) {
                        if (printErrors) {
                            sbGoldLabelNegative.append(goldDoc.getText());
                            sbGoldLabelNegative.append("\n");
                            sbGoldLabelNegative.append("----");
                            sbGoldLabelNegative.append("\n");
                        }
                        neutralClassTrueNegatives++;
                        negativeClassFalseNegatives++;
                        positiveClassFalsePositives++;
                    }
                    if (goldLabel.equals(Sentiment.NEUTRAL) & predictedLabel.equals(Sentiment.NEGATIVE)) {
                        if (printErrors) {
                            sbGoldLabelNeutral.append(goldDoc.getText());
                            sbGoldLabelNeutral.append("\n");
                            sbGoldLabelNeutral.append("----");
                            sbGoldLabelNeutral.append("\n");
                        }
                        neutralClassFalseNegatives++;
                        positiveClassTrueNegatives++;
                        negativeClassFalsePositives++;
                    }
                    if (goldLabel.equals(Sentiment.NEUTRAL) & predictedLabel.equals(Sentiment.POSITIVE)) {
                        if (printErrors) {
                            sbGoldLabelNeutral.append(goldDoc.getText());
                            sbGoldLabelNeutral.append("\n");
                            sbGoldLabelNeutral.append("----");
                            sbGoldLabelNeutral.append("\n");
                        }
                        neutralClassFalseNegatives++;
                        positiveClassFalsePositives++;
                        negativeClassTrueNegatives++;
                    }
                } catch (Exception e) {
                    System.out.println("ex: " + e);
                }
            }
            if (printErrors) {
                Files.writeString(Path.of(datasetname, "results", modelName + "  - positive sentiment misclassified.txt"), sbGoldLabelPositive.toString(), StandardCharsets.UTF_8);
                Files.writeString(Path.of(datasetname, "results", modelName + " - negative sentiment misclassified.txt"), sbGoldLabelNegative.toString(), StandardCharsets.UTF_8);
                Files.writeString(Path.of(datasetname, "results", modelName + " - neutral sentiment misclassified.txt"), sbGoldLabelNeutral.toString(), StandardCharsets.UTF_8);
            }

            float positiveRecall = positiveClassTruePositives / (positiveClassTruePositives + positiveClassFalseNegatives);
            float negativeRecall = negativeClassTruePositives / (negativeClassTruePositives + negativeClassFalseNegatives);
            float neutralRecall = neutralClassTruePositives / (neutralClassTruePositives + neutralClassFalseNegatives);

            float positivePrecision = positiveClassTruePositives / (positiveClassTruePositives + positiveClassFalsePositives);
            float negativePrecision = negativeClassTruePositives / (negativeClassTruePositives + negativeClassFalsePositives);
            float neutralPrecision = neutralClassTruePositives / (neutralClassTruePositives + neutralClassFalsePositives);

            float positiveClassF1 = 2 * positiveClassTruePositives / (2 * positiveClassTruePositives + positiveClassFalsePositives + positiveClassFalseNegatives);
            float negativeClassF1 = 2 * negativeClassTruePositives / (2 * negativeClassTruePositives + negativeClassFalsePositives + negativeClassFalseNegatives);
            float neutralClassF1 = 2 * neutralClassTruePositives / (2 * neutralClassTruePositives + neutralClassFalsePositives + neutralClassFalseNegatives);

            if (Float.isNaN(positiveClassF1)) {
                System.out.println("positive class had no entry");
                positiveClassF1 = 0;
            }
            if (Float.isNaN(negativeClassF1)) {
                System.out.println("negative class had no entry");
                negativeClassF1 = 0;
            }
            if (Float.isNaN(neutralClassF1)) {
                System.out.println("neutral class had no entry");
                neutralClassF1 = 0;
            }

            System.out.println("F1 positive class: " + positiveClassF1);
            System.out.println("F1 negative class: " + negativeClassF1);
            System.out.println("F1 neutral class: " + neutralClassF1);

            float weightedF1 = (positiveClassF1 * truePositiveInstances + negativeClassF1 * trueNegativeInstances + neutralClassF1 * trueNeutralInstances) / (truePositiveInstances + trueNegativeInstances + trueNeutralInstances);

            return weightedF1;
        } catch (IOException ex) {
            Logger.getLogger(F1.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0f;
    }

}
