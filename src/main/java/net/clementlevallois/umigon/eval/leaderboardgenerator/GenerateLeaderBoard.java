/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.leaderboardgenerator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import net.clementlevallois.umigon.eval.datamodel.OverallScore;
import net.clementlevallois.umigon.eval.datamodel.Score;
import static net.clementlevallois.umigon.eval.datamodel.Task.FACTUALITY;
import static net.clementlevallois.umigon.eval.datamodel.Task.SENTIMENT;
import net.clementlevallois.umigon.eval.datasets.DatasetInterface;
import net.clementlevallois.umigon.eval.models.ModelInterface;
import net.steppschuh.markdowngenerator.table.Table;
import net.steppschuh.markdowngenerator.table.TableRow;

/**
 *
 * @author LEVALLOIS
 */
public class GenerateLeaderBoard {

    Set<DatasetInterface> datasets;
    Set<ModelInterface> models;
    ConcurrentSkipListSet <Score> scores;
    List<OverallScore> overallScores;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.000");
    Map<String, ModelInterface> modelsDetails = new HashMap();
    Map<String, DatasetInterface> datasetsDetails = new HashMap();

    public GenerateLeaderBoard(Set<DatasetInterface> datasets, Set<ModelInterface> models, ConcurrentSkipListSet <Score> scores, List<OverallScore> overallScores) {
        this.datasets = datasets;
        this.models = models;
        this.scores = scores;
        this.overallScores = overallScores;
        for (ModelInterface model : models) {
            modelsDetails.put(model.getName(), model);
        }
        for (DatasetInterface dataset : datasets) {
            datasetsDetails.put(dataset.getName(), dataset);
        }
    }

    public void generateFullReadMe() throws IOException {
        Map<Integer, String> fragments = loadFragments();

        StringBuilder sb = new StringBuilder();
        sb.append(fragments.get(1))
        .append("\n")
        .append(writeListOfModels(models))
        .append("\n")
        .append(fragments.get(2))
        .append("\n")
        .append(writeListOfDatasets(datasets))
        .append("\n")
        .append(fragments.get(3))
        .append("\n")
        .append(writeLeaderBoardOnIndividualDatasetsForFactuality(scores))
        .append("\n")
        .append(fragments.get(4))
        .append("\n")
        .append(overallScoresForFactuality(overallScores))
        .append("\n")
        .append(fragments.get(5))
        .append("\n")
        .append(writeLeaderBoardOnIndividualDatasetsForSentiment(scores))
        .append("\n")
        .append(fragments.get(6))
        .append("\n")
        .append(overallScoresForSentiment(overallScores))
        .append("\n")
        .append(fragments.get(7))
        .append("\n")
        .append("\n")
        .append("\n")
        .append("_")
        .append("This readme file and the leaderboard it includes has been generated on ")
        .append(LocalDateTime.now())
        .append("_")        
        .append("\n");

        Files.writeString(Path.of("README.md"), sb.toString(), StandardCharsets.UTF_8);
    }

    public String writeListOfModels(Set<ModelInterface> models) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (ModelInterface model : models) {
            sb.append(i++).append(". ");
            sb.append(model.getName()).append(" ([").append("paper").append("]");
            sb.append("(").append(model.getPaperWebLink()).append(")");
            sb.append(", ");
            sb.append(" [").append("api").append("]");
            sb.append("(").append(model.getAPIWebLink()).append("))");
            sb.append("\n");
        }

        return sb.toString();
    }

    public String writeListOfDatasets(Set<DatasetInterface> datasets) {
        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (DatasetInterface dataset : datasets) {
            sb.append(i++).append(". ");
            sb.append(dataset.getName()).append(" ([").append("paper").append("]");
            sb.append("(").append(dataset.getPaperWebLink()).append(")");
            sb.append(", ");
            sb.append(" [").append("data source").append("]");
            sb.append("(").append(dataset.getDataWebLink()).append("))");
            sb.append("<br/>");
            sb.append("<span style=\"font-size: .8rem\">");
            sb.append(dataset.getShortDescription());
            sb.append("</span>");
            sb.append("<br/>");
            sb.append("\n");
        }
        return sb.toString();
    }

    public String writeLeaderBoardOnIndividualDatasetsForSentiment(ConcurrentSkipListSet <Score> scores) {
        Set<String> datasetNamesSentiment = new TreeSet();
        Set<String> modelNamesSentiment = new TreeSet();
        Map<String, TreeSet<Score>> modelsToScoresForSentiment = new HashMap();

        for (Score score : scores) {
            if (score.getTask().equals(SENTIMENT)) {
                datasetNamesSentiment.add(score.getDatasetName());
                modelNamesSentiment.add(score.getModelName());
            }
        }

        for (Score score : scores) {
            if (score.getTask().equals(SENTIMENT)) {
                TreeSet<Score> scoreValues = modelsToScoresForSentiment.getOrDefault(score.getModelName(), new TreeSet());
                scoreValues.add(score);
                modelsToScoresForSentiment.put(score.getModelName(), scoreValues);
            }
        }

        Table.Builder tableBuilder = new Table.Builder();

        // inserting headers
        TableRow headers = new TableRow();
        headers.getColumns().add("");
        for (String dataset : datasetNamesSentiment) {
            DatasetInterface currDataset = datasetsDetails.get(dataset);
            headers.getColumns().add("[" + dataset + "](" + currDataset.getDataWebLink()+")");
        }
        tableBuilder.addRow(headers);

        for (String name : modelNamesSentiment) {
            ModelInterface currModel = modelsDetails.get(name);
            TableRow row = new TableRow();
            row.getColumns().add("[" + name + "](" + currModel.getPaperWebLink()+")");
            TreeSet<Score> scoresForOneModel = modelsToScoresForSentiment.get(name);
            for (Score scoreValue : scoresForOneModel) {
                row.getColumns().add(decimalFormat.format(scoreValue.getScore()));
            }
            tableBuilder.addRow(row);
        }

        return tableBuilder.build().toString();
    }

    public String writeLeaderBoardOnIndividualDatasetsForFactuality(ConcurrentSkipListSet <Score> scores) {
        Set<String> datasetNamesFactuality = new TreeSet();
        Set<String> modelNamesFactuality = new TreeSet();
        Map<String, TreeSet<Score>> modelsToScoresForFactuality = new HashMap();

        for (Score score : scores) {
            if (score.getTask().equals(FACTUALITY)) {
                datasetNamesFactuality.add(score.getDatasetName());
                modelNamesFactuality.add(score.getModelName());
            }
        }

        for (Score score : scores) {
            if (score.getTask().equals(FACTUALITY)) {
                TreeSet<Score> scoreValues = modelsToScoresForFactuality.getOrDefault(score.getModelName(), new TreeSet());
                scoreValues.add(score);
                modelsToScoresForFactuality.put(score.getModelName(), scoreValues);
            }
        }

        Table.Builder tableBuilder = new Table.Builder();

        // inserting headers
        TableRow headers = new TableRow();
        headers.getColumns().add("");
        for (String name : datasetNamesFactuality) {
            DatasetInterface currDataset = datasetsDetails.get(name);
            headers.getColumns().add("[" + name + "](" + currDataset.getDataWebLink()+")");

        }
        tableBuilder.addRow(headers);

        for (String name : modelNamesFactuality) {
            ModelInterface currModel = modelsDetails.get(name);
            TableRow row = new TableRow();
            row.getColumns().add("[" + name + "](" + currModel.getPaperWebLink()+")");
            TreeSet<Score> scoreValues = modelsToScoresForFactuality.get(name);
            for (Score scoreValue : scoreValues) {
                row.getColumns().add(decimalFormat.format(scoreValue.getScore()));
            }
            tableBuilder.addRow(row);
        }

        return tableBuilder.build().toString();
    }

    public String overallScoresForFactuality(List<OverallScore> overallScores) {
        Table.Builder tableBuilder = new Table.Builder();

        // inserting headers
        TableRow headers = new TableRow();
        headers.getColumns().add("");
        for (OverallScore score : overallScores) {
            ModelInterface currModel = modelsDetails.get(score.getModelName());
            if (score.getTask().equals(FACTUALITY)) {
                headers.getColumns().add("[" + currModel.getName() + "](" + currModel.getPaperWebLink()+")");
            }
        }
        tableBuilder.addRow(headers);

        // inserting row with overall score in float value
        
        TableRow scoresRow = new TableRow();
        scoresRow.getColumns().add("overall score");
        for (OverallScore score : overallScores) {
            if (score.getTask().equals(FACTUALITY)) {
                scoresRow.getColumns().add(decimalFormat.format(score.getOverallScore()));
            }
        }
        tableBuilder.addRow(scoresRow);

        // inserting row with overall score as a rank

        TableRow rankRow = new TableRow();
        rankRow.getColumns().add("rank");
        for (OverallScore score : overallScores) {
            if (score.getTask().equals(FACTUALITY)) {
                rankRow.getColumns().add(score.getRankFromTop());
            }
        }
        tableBuilder.addRow(rankRow);
        return tableBuilder.build().toString();
    }

    public String overallScoresForSentiment(List<OverallScore> overallScores) {
        Table.Builder tableBuilder = new Table.Builder();

        // inserting headers
        TableRow headers = new TableRow();
        headers.getColumns().add("");
        for (OverallScore score : overallScores) {
            ModelInterface currModel = modelsDetails.get(score.getModelName());
            if (score.getTask().equals(SENTIMENT)) {
                headers.getColumns().add("[" + currModel.getName() + "](" + currModel.getPaperWebLink()+")");
            }
        }
        tableBuilder.addRow(headers);

        
        // inserting row with overall score in float value
        
        TableRow scoresRow = new TableRow();
        scoresRow.getColumns().add("overall score");
        for (OverallScore score : overallScores) {
            if (score.getTask().equals(SENTIMENT)) {
                scoresRow.getColumns().add(decimalFormat.format(score.getOverallScore()));
            }
        }
        tableBuilder.addRow(scoresRow);

        // inserting row with overall score as a rank

        TableRow rankRow = new TableRow();
        rankRow.getColumns().add("rank");
        for (OverallScore score : overallScores) {
            if (score.getTask().equals(SENTIMENT)) {
                rankRow.getColumns().add(score.getRankFromTop());
            }
        }
        tableBuilder.addRow(rankRow);
        return tableBuilder.build().toString();
    }

    private Map<Integer, String> loadFragments() {

        Map<Integer, String> fragments = new HashMap();

        ClassLoader classLoader = GenerateLeaderBoard.class.getClassLoader();

        // intro until the list of models
        String part1Path = "readme-fragments/part-1.txt";
        String part1 = loadToString(classLoader, part1Path);
        fragments.put(1, part1);

        // title for "which list of datasets"
        String part2Path = "readme-fragments/part-2.txt";
        String part2 = loadToString(classLoader, part2Path);
        fragments.put(2, part2);

        // more info on models + Title for leaderboards, starting with factuality - individual scores
        String part3Path = "readme-fragments/part-3.txt";
        String part3 = loadToString(classLoader, part3Path);
        fragments.put(3, part3);

        // presentation of overall leaderboard on factuality
        String part4Path = "readme-fragments/part-4.txt";
        String part4 = loadToString(classLoader, part4Path);
        fragments.put(4, part4);

        // presentation of leaderboard on sentiment - individual scores
        String part5Path = "readme-fragments/part-5.txt";
        String part5 = loadToString(classLoader, part5Path);
        fragments.put(5, part5);

        // presentation of overall leaderboard on sentiment
        String part6Path = "readme-fragments/part-6.txt";
        String part6 = loadToString(classLoader, part6Path);
        fragments.put(6, part6);

        // how to run it, why sentiment and factuality in the same testbench, contact details
        String part7Path = "readme-fragments/part-7.txt";
        String part7 = loadToString(classLoader, part7Path);
        fragments.put(7, part7);

        return fragments;
    }

    private String loadToString(ClassLoader classLoader, String resourcePath) {
        StringBuilder sb = new StringBuilder();
        try (InputStream inputStream = classLoader.getResourceAsStream(resourcePath)) {
            if (inputStream != null) {
                // Read the content of the file using BufferedReader
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
            } else {
                System.out.println("File not found: " + resourcePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();

    }

}
