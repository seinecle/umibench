/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datamodel;

import java.text.DecimalFormat;
import java.time.LocalDateTime;

/**
 *
 * @author LEVALLOIS
 */
public class Score implements Comparable {

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private float score;
    private String modelName;
    private String datasetName;
    private int datasetSize;
    private LocalDateTime dateTime;
    private Task task;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getDatasetSize() {
        return datasetSize;
    }

    public void setDatasetSize(int datasetSize) {
        this.datasetSize = datasetSize;
    }

    @Override
    public int compareTo(Object o) {
        Score os = (Score)o;
        return (this.modelName+this.datasetName).compareTo((os.modelName+os.datasetName));
    }
    
    
    
    

}
