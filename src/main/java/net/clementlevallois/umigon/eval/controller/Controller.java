/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.controller;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import net.clementlevallois.umigon.eval.datasets.Clef2023;
import net.clementlevallois.umigon.eval.datasets.KaggleHeadlines;
import net.clementlevallois.umigon.eval.datasets.MPQA;
import net.clementlevallois.umigon.eval.datasets.SubjQA;
import net.clementlevallois.umigon.eval.datasets.XFact;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Annotation;
import net.clementlevallois.umigon.eval.datamodel.Factuality;
import net.clementlevallois.umigon.eval.datamodel.OverallScore;
import net.clementlevallois.umigon.eval.datamodel.Score;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.datamodel.Task;
import static net.clementlevallois.umigon.eval.datamodel.Task.FACTUALITY;
import static net.clementlevallois.umigon.eval.datamodel.Task.FACTUALITY_AND_SENTIMENT;
import static net.clementlevallois.umigon.eval.datamodel.Task.SENTIMENT;
import net.clementlevallois.umigon.eval.datasets.Alexa;
import net.clementlevallois.umigon.eval.models.ModelInterface;
import net.clementlevallois.umigon.eval.models.TimeLMs;
import net.clementlevallois.utils.Clock;
import net.clementlevallois.umigon.eval.datasets.DatasetInterface;
import net.clementlevallois.umigon.eval.leaderboardgenerator.GenerateLeaderBoard;
import net.clementlevallois.umigon.eval.models.GPT35AdvancedPrompt;
import net.clementlevallois.umigon.eval.models.GPT35BasicPrompt;
import net.clementlevallois.umigon.eval.models.Mistral7BHermesAdvancedPrompt;
import net.clementlevallois.umigon.eval.models.Mistral7BHermesBasicPrompt;
import net.clementlevallois.umigon.eval.models.Thesis_Titan;
import net.clementlevallois.umigon.eval.models.Umigon;

/**
 *
 * @author LEVALLOIS
 */
public class Controller {

    private final boolean skipAllAPICalls = false;
    private final boolean onlyUmigonAPICalls = true;
    private final boolean printFalseClassificationsForUmigon = true;
    private static final int LIMIT_RECORDS_FOR_TESTS = Integer.MAX_VALUE;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private static final StringBuilder log = new StringBuilder();
    public static String HUGGINGFACE_API_KEY;
    public static String UMIGON_API_KEY;
    public static String OPENAI_API_KEY;

    public static Set<DatasetInterface> datasets;
    public static Set<ModelInterface> models;

    public static void main(String[] args) throws URISyntaxException, IOException {

        Controller controller = new Controller();
        controller.loadProperties();
        controller.initDataSetsAndModels();
//        controller.runEvaluations(datasets, models);
        ConcurrentSkipListSet <Score> scores = controller.computeF1Scores(datasets, models);
        List<OverallScore> overallScores = controller.computeOverallScores(scores);
        GenerateLeaderBoard generator = new GenerateLeaderBoard(datasets, models, scores, overallScores);
        generator.generateFullReadMe();
    }

    public void initDataSetsAndModels() {
        datasets = new HashSet();
        datasets.add(new Alexa());
        datasets.add(new Clef2023());
        datasets.add(new KaggleHeadlines());
        datasets.add(new MPQA());
        datasets.add(new SubjQA());
        datasets.add(new XFact());

        models = new HashSet();
        models.add(new Umigon());
        models.add(new Thesis_Titan());
        models.add(new TimeLMs());
        models.add(new Mistral7BHermesBasicPrompt());
        models.add(new Mistral7BHermesAdvancedPrompt());
        models.add(new GPT35BasicPrompt());
        models.add(new GPT35AdvancedPrompt());

        F1.setLimitForTests(Integer.MAX_VALUE);

    }

    private void runEvaluations(Set<DatasetInterface> datasetReaders, Set<ModelInterface> models) throws IOException {

        Clock generalClock = new Clock("general clock for evaluations on " + LocalDateTime.now());
        appendString(log, generalClock.getAction());
        datasetReaders.parallelStream().map(datasetReader -> {
            datasetReader.read();
            return datasetReader;
        }).forEach(datasetReader -> {
            StringBuilder logOneEval = new StringBuilder();
            models.parallelStream().forEach(model -> {
                Clock clock = new Clock("evaluating " + datasetReader.getName() + " with " + model.getName());
                appendString(logOneEval, clock.getAction());
                Map<String, AnnotatedDocument> predictedLabels = evalOneDataSetWithOneModel(datasetReader, model);
                appendString(log, clock.closeAndPrintClockToString("\n"));
            });
        });
        appendString(log, generalClock.closeAndPrintClockToString("\nend of evaluations"));
        Files.writeString(Path.of("logs", "run of " + LocalDateTime.now().toString().replaceAll(":", "_") + ".txt"), log.toString());
    }

    private ConcurrentSkipListSet <Score> computeF1Scores(Set<DatasetInterface> datasets, Set<ModelInterface> models) {
        Clock generalClock = new Clock("general clock for evaluations on " + LocalDateTime.now());
        ConcurrentSkipListSet <Score> scores = new ConcurrentSkipListSet ();

        appendString(log, generalClock.getAction());
        datasets.parallelStream().map(dataset -> {
            dataset.read();
            return dataset;
        }).forEach(dataset -> {
            models.parallelStream().forEach(model -> {
                JsonbConfig jsonbConfig = new JsonbConfig();
                jsonbConfig.withFormatting(Boolean.TRUE);
                Jsonb jsonb = JsonbBuilder.create(jsonbConfig);
                String fileNameEvaluationsOneDatasetOneModel = dataset.getName() + "_evaluated_with_" + model.getName() + ".txt";
                Map<String, AnnotatedDocument> predictedLabels = null;

                Clock clock = new Clock("computing F1 scores for " + dataset.getName() + " with " + model.getName());
                Boolean printFalseClassifications;
                if (model.getName().equals("umigon") && printFalseClassificationsForUmigon) {
                    printFalseClassifications = Boolean.TRUE;
                } else {
                    printFalseClassifications = Boolean.FALSE;
                }
                switch (model.getTask()) {
                    case FACTUALITY -> {
                        if (dataset.getTask().equals(FACTUALITY_AND_SENTIMENT) || dataset.getTask().equals(FACTUALITY)) {
                            Path predictedLabelsPath = Path.of(dataset.getName(), "results", fileNameEvaluationsOneDatasetOneModel);
                            if (Files.exists(predictedLabelsPath)) {
                                try {
                                    predictedLabels = jsonb.fromJson(Files.newBufferedReader(predictedLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                                    }.getClass().getGenericSuperclass());
                                } catch (IOException ex) {
                                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                System.out.println("file with predicted label not found: " + predictedLabelsPath.toString());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }
                            if (predictedLabels == null) {
                                System.out.println("Error loading predicted labels for " + dataset.getName() + " , " + model.getName());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }
                            Score score = new Score();
                            score.setDatasetName(dataset.getName());
                            score.setModelName(model.getName());
                            score.setDateTime(LocalDateTime.now());
                            Float weightedF1 = F1.computeBasedOnFactuality(dataset.getGoldenLabels(), predictedLabels, dataset.getName(), model.getName(), printFalseClassifications);
                            writeF1ScoreForOneDatasetAndOneTask(dataset.getName(), model.getName(), FACTUALITY, weightedF1);
                            score.setScore(weightedF1);
                            score.setTask(FACTUALITY);
                            score.setDatasetSize(dataset.getNumberOfEntries());
                            scores.add(score);
                        }
                    }
                    case SENTIMENT -> {
                        if (dataset.getTask().equals(FACTUALITY_AND_SENTIMENT) || dataset.getTask().equals(SENTIMENT)) {
                            Path predictedLabelsPath = Path.of(dataset.getName(), "results", fileNameEvaluationsOneDatasetOneModel);
                            if (Files.exists(predictedLabelsPath)) {
                                try {
                                    predictedLabels = jsonb.fromJson(Files.newBufferedReader(predictedLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                                    }.getClass().getGenericSuperclass());
                                } catch (IOException ex) {
                                    Logger.getLogger(Alexa.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                System.out.println("file with predicted label not found: " + predictedLabelsPath.toString());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }
                            if (predictedLabels == null) {
                                System.out.println("Error loading predicted labels for " + dataset.getName() + " , " + model.getName());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }

                            Float weightedF1 = F1.computeBasedOnSentiment(dataset.getGoldenLabels(), predictedLabels, dataset.getName(), model.getName(), printFalseClassifications);
                            Score score = new Score();
                            score.setDatasetName(dataset.getName());
                            score.setModelName(model.getName());
                            score.setDateTime(LocalDateTime.now());
                            score.setDatasetSize(dataset.getNumberOfEntries());
                            writeF1ScoreForOneDatasetAndOneTask(dataset.getName(), model.getName(), SENTIMENT, weightedF1);
                            score.setScore(weightedF1);
                            score.setTask(SENTIMENT);
                            scores.add(score);
                        }
                    }
                    case FACTUALITY_AND_SENTIMENT -> {
                        if (dataset.getTask().equals(FACTUALITY_AND_SENTIMENT) || dataset.getTask().equals(SENTIMENT)) {
                            Path predictedLabelsPath = Path.of(dataset.getName(), "results", fileNameEvaluationsOneDatasetOneModel);
                            if (Files.exists(predictedLabelsPath)) {
                                try {
                                    predictedLabels = jsonb.fromJson(Files.newBufferedReader(predictedLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                                    }.getClass().getGenericSuperclass());
                                } catch (IOException ex) {
                                    Logger.getLogger(Alexa.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                System.out.println("file with predicted label not found: " + predictedLabelsPath.toString());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }
                            if (predictedLabels == null) {
                                System.out.println("Error loading predicted labels for " + dataset.getName() + " , " + model.getName());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }

                            Score score = new Score();
                            score.setDatasetName(dataset.getName());
                            score.setModelName(model.getName());
                            score.setDateTime(LocalDateTime.now());
                            score.setDatasetSize(dataset.getNumberOfEntries());
                            Float weightedF1Sentiment = F1.computeBasedOnSentiment(dataset.getGoldenLabels(), predictedLabels, dataset.getName(), model.getName(), printFalseClassifications);
                            writeF1ScoreForOneDatasetAndOneTask(dataset.getName(), model.getName(), SENTIMENT, weightedF1Sentiment);
                            score.setScore(weightedF1Sentiment);
                            score.setTask(SENTIMENT);
                            scores.add(score);
                        }
                        if (dataset.getTask().equals(FACTUALITY_AND_SENTIMENT) || dataset.getTask().equals(FACTUALITY)) {
                            Path predictedLabelsPath = Path.of(dataset.getName(), "results", fileNameEvaluationsOneDatasetOneModel);
                            if (Files.exists(predictedLabelsPath)) {
                                try {
                                    predictedLabels = jsonb.fromJson(Files.newBufferedReader(predictedLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                                    }.getClass().getGenericSuperclass());
                                } catch (IOException ex) {
                                    Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            } else {
                                System.out.println("file with predicted label not found: " + predictedLabelsPath.toString());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }
                            if (predictedLabels == null) {
                                System.out.println("Error loading predicted labels for " + dataset.getName() + " , " + model.getName());
                                System.out.println("EXITING");
                                System.exit(-1);
                            }
                            Score score = new Score();
                            score.setDatasetName(dataset.getName());
                            score.setModelName(model.getName());
                            score.setDateTime(LocalDateTime.now());
                            score.setDatasetSize(dataset.getNumberOfEntries());
                            Float weightedF1Factuality = F1.computeBasedOnFactuality(dataset.getGoldenLabels(), predictedLabels, dataset.getName(), model.getName(), printFalseClassifications);
                            writeF1ScoreForOneDatasetAndOneTask(dataset.getName(), model.getName(), FACTUALITY, weightedF1Factuality);
                            score.setScore(weightedF1Factuality);
                            score.setTask(FACTUALITY);
                            scores.add(score);
                        }
                    }
                    default -> {
                        System.out.println("error: model task was not set");
                    }
                }
                appendString(log, clock.closeAndPrintClockToString("\n"));
            });
        });
        appendString(log, generalClock.closeAndPrintClockToString("\nend of F1 scores computation"));
        return scores;

    }

    private static Path getPathResultOfOneEval(String datasetName, String model) {
        return Path.of(datasetName, "results", datasetName + "_evaluated_with_" + model + ".txt");
    }

    private static void writeF1ScoreForOneDatasetAndOneTask(String datasetName, String modelName, Task task, Float F1) {
        Path path;
        String info;
        if (task.equals(FACTUALITY) || task.equals(FACTUALITY_AND_SENTIMENT)) {
            path = Path.of(datasetName, "results", "weighted_F1_scores_for_" + datasetName + "_on_factuality_task.txt");
            info = "weighted F1 score for factuality on dataset " + datasetName + " for model " + modelName + ": " + String.valueOf(F1) + "\n";
            info = "evaluation conducted on " + LocalDateTime.now().toString() + "\n" + info + "\n";
            writeToFileThreadSafe(path, info);
        }
        if (task.equals(SENTIMENT) || task.equals(FACTUALITY_AND_SENTIMENT)) {
            path = Path.of(datasetName, "results", "weighted_F1_scores_for_" + datasetName + "_on_sentiment_task.txt");
            info = "weighted F1 score for sentiment on dataset " + datasetName + " for model " + modelName + ": " + String.valueOf(F1) + "\n";
            info = "evaluation conducted on " + LocalDateTime.now().toString() + "\n" + info + "\n";
            writeToFileThreadSafe(path, info);
        }
    }

    private static void writeToFileThreadSafe(Path filePath, String content) {
        try (FileChannel fileChannel = FileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(content);
            fileChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, AnnotatedDocument> evalOneDataSetWithOneModel(DatasetInterface dataset, ModelInterface model) {
        JsonbConfig jsonbConfig = new JsonbConfig();
        jsonbConfig.withFormatting(Boolean.TRUE);
        Jsonb jsonb = JsonbBuilder.create(jsonbConfig);
        Map<String, AnnotatedDocument> predictedLabels = new ConcurrentHashMap();
        try {
            Map<String, AnnotatedDocument> goldMap = dataset.getGoldenLabels();
            float[] i = new float[1];
            i[0] = 0;
            Stream<AnnotatedDocument> stream = null;
            float total = goldMap.size();
            if (model.areConcurrentAPICallsPossible()) {
                stream = goldMap.values().parallelStream();
            } else {
                stream = goldMap.values().stream();
            }
            stream.limit(LIMIT_RECORDS_FOR_TESTS).forEach(docGold -> {
                AnnotatedDocument doc = new AnnotatedDocument(docGold.getId(), docGold.getText());
                String response = model.sendApiCall(doc);
                Annotation annotation;
                switch (model.getTask()) {
                    case FACTUALITY -> {
                        Factuality label = model.extractFactualityLabelFromAPiResponse(response);
                        if (label.equals((Factuality.NOT_SET))) {
                            printErrorInResponse(doc.getText(), response, model.getTask(), model.getName(), dataset.getName());
                        }
                        annotation = Annotation.empty().withFactuality(label);
                    }
                    case SENTIMENT -> {
                        Sentiment label = model.extractSentimentLabelFromAPiResponse(response);
                        annotation = Annotation.empty().withSentiment(label);
                        if (label.equals((Sentiment.NOT_SET))) {
                            printErrorInResponse(doc.getText(), response, model.getTask(), model.getName(), dataset.getName());
                        }
                    }
                    case FACTUALITY_AND_SENTIMENT -> {
                        Sentiment labelSentiment = model.extractSentimentLabelFromAPiResponse(response);
                        Factuality labelFactuality = model.extractFactualityLabelFromAPiResponse(response);
                        if (labelSentiment.equals(Sentiment.NOT_SET) | labelFactuality.equals(Factuality.NOT_SET)) {
                            printErrorInResponse(doc.getText(), response, model.getTask(), model.getName(), dataset.getName());
                        }
                        annotation = Annotation.empty().withSentiment(labelSentiment).withFactuality(labelFactuality);
                    }
                    default -> {
                        System.out.println("a model had no task set");
                        annotation = null;
                    }
                }
                doc.addAnnotation(annotation);
                predictedLabels.put(doc.getId(), doc);
                i[0] = i[0] + 1;
                if (i[0] % 100 == 0) {
                    float progress = (i[0] / total) * 100;
                    System.out.println("");
                    System.out.println("progress (" + dataset.getName() + ", " + model.getName() + "): " + decimalFormat.format(progress) + "%");
                }
            });
            String json = jsonb.toJson(predictedLabels);
            Files.writeString(getPathResultOfOneEval(dataset.getName(), model.getName()), json, StandardCharsets.UTF_8);

        } catch (IOException ex) {
            Logger.getLogger(Controller.class.getName()).log(Level.SEVERE, null, ex);
        }
        return predictedLabels;
    }

    private void printErrorInResponse(String line, String response, Task task, String modelName, String datasetName) {
        System.out.println("---------------");
        System.out.println("context: " + task + ", " + modelName + ", " + datasetName);
        System.out.println("probable error with:");
        System.out.println("line:" + line);
        System.out.println("");
        System.out.println("response:" + response);
        System.out.println("---------------");
        System.out.println("");
    }

    private List<OverallScore> computeOverallScores(ConcurrentSkipListSet <Score> scores) {
        List<OverallScore> overallScores = new ArrayList();
        Map<String, Integer> sizeDatasetsFactuality = new HashMap();
        Map<String, Integer> sizeDatasetsSentiment = new HashMap();
        Map<String, Float> modelOverAllScoresForSentiment = new HashMap();
        Map<String, Float> modelOverAllScoresForFactuality = new HashMap();

        for (Score score : scores) {
            if (score.getTask().equals(FACTUALITY)) {
                sizeDatasetsFactuality.put(score.getDatasetName(), score.getDatasetSize());
            }
            if (score.getTask().equals(SENTIMENT)) {
                sizeDatasetsSentiment.put(score.getDatasetName(), score.getDatasetSize());
            }
        }
        int totalSizeDatasetsFactuality = sizeDatasetsFactuality.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        int totalSizeDatasetsSentiment = sizeDatasetsSentiment.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        for (Score score : scores) {
            if (score.getTask().equals(SENTIMENT)) {
                modelOverAllScoresForSentiment.put(score.getModelName(), 0f);
            }
        }
        for (Score score : scores) {
            if (score.getTask().equals(FACTUALITY)) {
                modelOverAllScoresForFactuality.put(score.getModelName(), 0f);
            }
        }

        // overall scores for SENTIMENT TASK
        for (String modelName : modelOverAllScoresForSentiment.keySet()) {
            Float numerator = 0f;
            for (Score score : scores) {
                if (!score.getTask().equals(SENTIMENT)) {
                    continue;
                }
                if (!score.getModelName().equals(modelName)) {
                    continue;
                }
                numerator = numerator + score.getScore() * score.getDatasetSize();

            }
            float overallScoreOneModel = numerator / totalSizeDatasetsSentiment;
            modelOverAllScoresForSentiment.put(modelName, overallScoreOneModel);
        }

        // overall scores for FACTUALITY TASK
        for (String modelName : modelOverAllScoresForFactuality.keySet()) {
            Float numerator = 0f;
            for (Score score : scores) {
                if (!score.getTask().equals(FACTUALITY)) {
                    continue;
                }
                if (!score.getModelName().equals(modelName)) {
                    continue;
                }
                numerator = numerator + score.getScore() * score.getDatasetSize();

            }
            float overallScoreOneModel = numerator / totalSizeDatasetsFactuality;
            modelOverAllScoresForFactuality.put(modelName, overallScoreOneModel);
        }

        List<Map.Entry<String, Float>> topToLowestScoresForSentiment = modelOverAllScoresForSentiment.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .toList();

        List<Map.Entry<String, Float>> topToLowestScoresForFactuality = modelOverAllScoresForFactuality.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .toList();

        for (Map.Entry<String, Float> entry : topToLowestScoresForSentiment) {
            OverallScore overallScore = new OverallScore();
            overallScore.setDateTime(LocalDateTime.now());
            overallScore.setModelName(entry.getKey());
            overallScore.setTask(SENTIMENT);
            overallScore.setOverallScore(entry.getValue());
            overallScore.setRankFromTop(topToLowestScoresForSentiment.indexOf(entry) + 1);
            overallScores.add(overallScore);
        }
        for (Map.Entry<String, Float> entry : topToLowestScoresForFactuality) {
            OverallScore overallScore = new OverallScore();
            overallScore.setDateTime(LocalDateTime.now());
            overallScore.setModelName(entry.getKey());
            overallScore.setTask(FACTUALITY);
            overallScore.setOverallScore(entry.getValue());
            overallScore.setRankFromTop(topToLowestScoresForFactuality.indexOf(entry) + 1);
            overallScores.add(overallScore);
        }

        return overallScores;
    }

    public void loadProperties() {
        Properties privateProperties;
        try (InputStream is = new FileInputStream(Path.of("private", "properties.txt").toFile())) {
            privateProperties = new Properties();
            privateProperties.load(is);
            HUGGINGFACE_API_KEY = privateProperties.getProperty("huggingface_api_key", null);
            UMIGON_API_KEY = privateProperties.getProperty("umigon_api_key", null);
            OPENAI_API_KEY = privateProperties.getProperty("openai_api_key", null);
        } catch (IOException ex) {
            System.out.println("error in reading properties");
            System.exit(-1);
        }

    }

    private static synchronized void appendString(StringBuilder sb, String str) {
        sb.append(str);
    }
}
