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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Annotation;
import net.clementlevallois.umigon.eval.datamodel.Factuality;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.datamodel.Task;
import net.clementlevallois.umigon.eval.utils.Utils;

/**
 *
 * @author LEVALLOIS
 */
public class MPQA implements DatasetInterface {

    private final String name = "mpqa";
    public final static String GOLD_LABELS = "gold_labels.json";
    public final static String DATA = "mpqa_1_2_database/database.mpqa.1.2/docs";
    public final static String ANNOTATIONS = "mpqa_1_2_database/database.mpqa.1.2/man_anns";
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
        return "https://mpqa.cs.pitt.edu/";
    }

    @Override
    public String getPaperWebLink() {
        return "https://doi.org/10.1007/s10579-005-7880-9";
    }

    @Override
    public String getShortDescription() {
        return "a set of newswire articles on international politics annotated for factuality and sentiment";
    }    
    
    @Override
    public int getNumberOfEntries() {
        return 3198;
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
        goldenDocs = new HashMap();

        Path goldLabelsPath = Path.of(name, GOLD_LABELS);
        if (Files.exists(goldLabelsPath)) {

            try {
                goldenDocs = jsonb.fromJson(Files.newBufferedReader(goldLabelsPath, StandardCharsets.UTF_8), new HashMap<String, AnnotatedDocument>() {
                }.getClass().getGenericSuperclass());
            } catch (IOException ex) {
                Logger.getLogger(MPQA.class.getName()).log(Level.SEVERE, null, ex);
            }
            return goldenDocs;
        }

        String annotationForExpressiveSubjectivity = "GATE_expressive-subjectivity"; // Replace this with the predetermined string
        String annotationForObjectiveSpeechEvent = "GATE_objective-speech-event"; // Replace this with the predetermined string
        String targetStringNot = "polarity=\"uncertain"; // Replace this with the predetermined string
        Map<String, AnnotatedDocument> fileNameAndLineToDoc = new HashMap();

        try (Stream<Path> walk = Files.walk(Path.of(name, ANNOTATIONS))) {
            List<Path> annotationFiles = walk
                    .filter(path -> !path.getParent().getParent().getFileName().toString().endsWith("non_fbis"))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().endsWith("gateman.mpqa.lre.1.2"))
                    .collect(Collectors.toList());

            for (Path annotationsFilePath : annotationFiles) {
                Path parentFolder = annotationsFilePath.getParent().getFileName();
                Path grandParentFolder = annotationsFilePath.getParent().getParent().getFileName();
                Path doc = Path.of(name, DATA).resolve(grandParentFolder).resolve(parentFolder);
                List<String> linesDoc = Files.readAllLines(doc, StandardCharsets.UTF_8);
                List<String> linesAnnotations = Files.readAllLines(annotationsFilePath, StandardCharsets.UTF_8);

                int globalIndex = 0;
                List<Integer> lineStartingCharactersList = new ArrayList();

                int lineNumber = 0;
                for (String line : linesDoc) {
                    AnnotatedDocument goldDoc = new AnnotatedDocument();
                    goldDoc.setLineNumber(lineNumber);
                    goldDoc.setText(line);
                    goldDoc.setFolder(grandParentFolder.toString());
                    goldDoc.setFileName(parentFolder.toString());
                    goldDoc.setStartChar(globalIndex);
                    fileNameAndLineToDoc.put(goldDoc.getFileName() + "_" + String.valueOf(globalIndex), goldDoc);
                    lineStartingCharactersList.add(globalIndex);
                    globalIndex = globalIndex + line.length() + 1;
                    lineNumber++;
                }

                for (String line : linesAnnotations) {
                    if ((line.contains(annotationForExpressiveSubjectivity) | line.contains(annotationForObjectiveSpeechEvent)) && !line.contains(targetStringNot)) {
                        Annotation annotation = Annotation.empty();
                        String[] fields = line.split("\t");
                        String[] boundaryChars = fields[1].split(",");
                        int startChar = Integer.parseInt(boundaryChars[0]);
                        int closestLowerCharacter = Utils.findClosestLowerElement(startChar, lineStartingCharactersList);
                        AnnotatedDocument goldDoc = fileNameAndLineToDoc.get(parentFolder.toString() + "_" + closestLowerCharacter);
                        // LABEL FOR FACTUALITY
                        if (line.contains(annotationForExpressiveSubjectivity) && line.contains("nested-source=\"w\"")) {
                            annotation.setFactuality(Factuality.SUBJ);
                            // LABEL FOR SENTIMENT
                            if (line.contains("polarity=\"positive\"")) {
                                annotation.setSentiment(Sentiment.POSITIVE);
                            } else if (line.contains("polarity=\"negative\"")) {
                                annotation.setSentiment(Sentiment.NEGATIVE);
                            } else if (line.contains("polarity=\"neutral\"")) {
                                annotation.setSentiment(Sentiment.NEUTRAL);
                            } else {
                                annotation.setSentiment(Sentiment.NOT_SET);
                            }
                        } else if (line.contains(annotationForObjectiveSpeechEvent)) {
                            annotation.setFactuality(Factuality.OBJ);
                            annotation.setSentiment(Sentiment.NEUTRAL);
                        } else {
                            annotation.setFactuality(Factuality.NOT_SET);
                        }
                        goldDoc.addAnnotation(annotation);
                        if (!goldDoc.getAnnotation().get().getFactuality().equals(Factuality.NOT_SET)) {
                            goldenDocs.put(goldDoc.getId(), goldDoc);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // print some stats
        int countPositif = 0;
        int countNegatif = 0;
        int countNeutral = 0;
        int countSubjectif = 0;
        int countObjectif = 0;
        for (Map.Entry<String, AnnotatedDocument> entry : goldenDocs.entrySet()) {
            Factuality factuality = entry.getValue().getAnnotation().get().getFactuality();
            Sentiment sentiment = entry.getValue().getAnnotation().get().getSentiment();
            if (factuality.equals(Factuality.SUBJ)){
            switch (sentiment) {
                case POSITIVE -> {
                    countPositif++;
                    countSubjectif++;
                    break;
                }
                case NEGATIVE -> {
                    countNegatif++;
                    countSubjectif++;
                    break;
                }
                case NEUTRAL -> {
                    countNeutral++;
                    countSubjectif++;
                    break;
                }
                case NOT_SET -> {
                    break;
                }
                default -> {
                    break;
                }
            }
            }else{
                countObjectif++;
            }
        }
        System.out.println("some metrics on the golden labels of the MPQA dataset:");
        System.out.println("positif: "+ countPositif);
        System.out.println("negatif: "+ countNegatif);
        System.out.println("neutral: "+ countNeutral);
        System.out.println("subjectif: "+ countSubjectif);
        System.out.println("objectif: "+ countObjectif);

        String json = jsonb.toJson(goldenDocs);
        try {
            Files.writeString(Path.of(name, GOLD_LABELS), json, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Logger.getLogger(MPQA.class.getName()).log(Level.SEVERE, null, ex);
        }
        return goldenDocs;
    }
}
