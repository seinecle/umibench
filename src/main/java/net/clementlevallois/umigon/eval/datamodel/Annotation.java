/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datamodel;

import java.io.Serializable;

/**
 *
 * @author LEVALLOIS
 */
public class Annotation implements Serializable{

    private String annotatedTextFragment;
    private Factuality factuality;
    private Sentiment sentiment;
    private SpeechSource speechSource;

    public Annotation() {
        this(null, null, null, null);
    }

    private Annotation(final String annotatedTextFragment,
            final Factuality factuality, final Sentiment sentiment,
            final SpeechSource speechSource) {

        this.annotatedTextFragment = annotatedTextFragment == null ? "" : annotatedTextFragment;
        this.factuality = factuality == null ? Factuality.NOT_SET : factuality;
        this.sentiment = sentiment == null ? Sentiment.NOT_SET : sentiment;
        this.speechSource = speechSource == null ? SpeechSource.NOT_SET : speechSource;
    }

    public static Annotation empty() {
        return new Annotation();
    }

    protected static Annotation of(final String annotatedTextFragment,
            final Factuality factuality, final Sentiment sentiment,
            final SpeechSource speechSource) {
        return new Annotation(annotatedTextFragment, factuality, sentiment, speechSource);
    }

    public Annotation withAnnotatedFragment(final String annotatedTextFragment) {
        return of(annotatedTextFragment, factuality, sentiment, speechSource);
    }

    public Annotation withFactuality(final Factuality factuality) {
        return of(annotatedTextFragment, factuality, sentiment, speechSource);
    }

    public Annotation withSentiment(final Sentiment sentiment) {
        return of(annotatedTextFragment, factuality, sentiment, speechSource);
    }

    public Annotation withSpeechSource(final SpeechSource speechSource) {
        return of(annotatedTextFragment, factuality, sentiment, speechSource);
    }

    public String getAnnotatedTextFragment() {
        return annotatedTextFragment;
    }

    public Factuality getFactuality() {
        return factuality;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public SpeechSource getSpeechSource() {
        return speechSource;
    }

    public void setAnnotatedTextFragment(String annotatedTextFragment) {
        this.annotatedTextFragment = annotatedTextFragment;
    }

    public void setFactuality(Factuality factuality) {
        this.factuality = factuality;
    }

    public void setSentiment(Sentiment sentiment) {
        this.sentiment = sentiment;
    }

    public void setSpeechSource(SpeechSource speechSource) {
        this.speechSource = speechSource;
    }
    
    


};
