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
public class OverallScore {

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");
    private float overallScore;
    private String modelName;
    private LocalDateTime dateTime;
    private Task task;
    private int rankFromTop;

    public float getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(float overallScore) {
        this.overallScore = overallScore;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
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

    public int getRankFromTop() {
        return rankFromTop;
    }

    public void setRankFromTop(int rankFromTop) {
        this.rankFromTop = rankFromTop;
    }
    
    

}
