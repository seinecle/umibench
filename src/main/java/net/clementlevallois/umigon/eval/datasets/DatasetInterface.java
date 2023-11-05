/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datasets;

import java.util.Map;
import net.clementlevallois.umigon.eval.datamodel.AnnotatedDocument;
import net.clementlevallois.umigon.eval.datamodel.Task;

/**
 *
 * @author LEVALLOIS
 */
public interface DatasetInterface {

    Map<String, AnnotatedDocument> getGoldenLabels();

    String getName();

    Task getTask();

    String getDataWebLink();

    String getPaperWebLink();

    int getNumberOfEntries();

    Map<String, AnnotatedDocument> read();

}
