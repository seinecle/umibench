/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.tests;

import net.clementlevallois.umigon.eval.controller.Controller;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.models.GPT35BasicPrompt;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author LEVALLOIS
 */
public class GPT35Test {

    @Test
    public void conductTests() {
        Controller controller = new Controller();
        controller.loadProperties();
        AnnotatedDocument doc = new AnnotatedDocument("I hate chocolate");
        GPT35BasicPrompt gpt4 = new GPT35BasicPrompt();
        String response = gpt4.sendApiCall(doc);
        Sentiment sentimentResponse = gpt4.extractSentimentLabelFromAPiResponse(response);
        assertThat(sentimentResponse).isEqualTo(Sentiment.NEGATIVE);
    }

}
