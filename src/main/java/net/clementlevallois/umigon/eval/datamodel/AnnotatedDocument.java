/*
 * Copyright Clement Levallois 2021-2023. License Attribution 4.0 Intertnational (CC BY 4.0)
 */
package net.clementlevallois.umigon.eval.datamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 *
 * @author LEVALLOIS
 */
public class AnnotatedDocument implements Serializable {

    private String id;
    private String text;
    private String folder;
    private String fileName;
    private int startChar;
    private int endChar;
    private int lineNumber;
    private List<Annotation> annotations;

    public AnnotatedDocument() {
        this(null);
    }

    public AnnotatedDocument(String id, String text) {
        this.id = id;
        this.text = text;
        this.annotations = new ArrayList();
    }

    public AnnotatedDocument(String text) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.annotations = new ArrayList();
    }

    public AnnotatedDocument addAnnotation(Annotation annotation) {
        if (annotation != null) {
            this.annotations.add(annotation);
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public Optional<Annotation> getAnnotation() {
        if (annotations.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(annotations.get(0));
        }
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getStartChar() {
        return startChar;
    }

    public void setStartChar(int startChar) {
        this.startChar = startChar;
    }

    public int getEndChar() {
        return endChar;
    }

    public void setEndChar(int endChar) {
        this.endChar = endChar;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

}
