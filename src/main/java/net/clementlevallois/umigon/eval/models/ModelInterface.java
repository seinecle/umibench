/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.models;

import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Factuality;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.datamodel.Task;

/**
 *
 * @author LEVALLOIS
 */
public interface ModelInterface {



    public String getName();

    public String sendApiCall(AnnotatedDocument annotatedDocument);

    public Task getTask();
    
    public String getPaperWebLink();

    public String getAPIWebLink();
    
    public Boolean areConcurrentAPICallsPossible();

    public Sentiment extractSentimentLabelFromAPiResponse(String response);

    public Factuality extractFactualityLabelFromAPiResponse(String response);

}
