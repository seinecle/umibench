/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datamodel;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.Objects;

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
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + Objects.hashCode(this.modelName);
        hash = 47 * hash + Objects.hashCode(this.datasetName);
        hash = 47 * hash + Objects.hashCode(this.task.name());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Score other = (Score) obj;
        if (!Objects.equals(this.modelName, other.modelName)) {
            return false;
        }
        if (!Objects.equals(this.datasetName, other.datasetName)) {
            return false;
        }
        return this.task.name().equals(other.task.name());
    }

    @Override
    public int compareTo(Object o) {
        Score os = (Score)o;
        return (this.modelName+this.datasetName+this.task.name()).compareTo((os.modelName+os.datasetName+os.task.name()));
    }
    
    
    
    

}
