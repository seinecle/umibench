/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.tests;

import net.clementlevallois.umigon.eval.controller.Controller;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Sentiment;
import net.clementlevallois.umigon.eval.models.Mistral7BHermesAdvancedPrompt;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

/**
 *
 * @author LEVALLOIS
 */
public class MistralTest {

    @Test
    public void conductTests() {
        Controller controller = new Controller();
        controller.loadProperties();
        AnnotatedDocument doc = new AnnotatedDocument("The financial crisis has caused a surge in poverty");
        Mistral7BHermesAdvancedPrompt mistral = new Mistral7BHermesAdvancedPrompt();
        String response = mistral.sendApiCall(doc);
        Sentiment sentimentResponse = mistral.extractSentimentLabelFromAPiResponse(response);
        assertThat(sentimentResponse).isEqualTo(Sentiment.NEUTRAL);
    }

}
