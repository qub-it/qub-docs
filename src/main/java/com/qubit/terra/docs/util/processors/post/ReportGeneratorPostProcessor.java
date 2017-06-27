package com.qubit.terra.docs.util.processors.post;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.odftoolkit.simple.Document;

public abstract class ReportGeneratorPostProcessor {

    protected Document document;

    protected void init(final byte[] reportByteArray) {
    }

    protected byte[] save() {
        try {
            ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
            document.save(byteArray);
            return byteArray.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error in generating final version of template. " + e.getMessage());
        }
    }

    public byte[] process(final byte[] reportByteArray) {
        try {
            document = Document.loadDocument(new ByteArrayInputStream(reportByteArray));
            visit();
            return save();
        } catch (Exception e) {
            throw new RuntimeException("Error in creating Odf Document. " + e.getMessage());
        }
    }

    protected abstract void visit();

}
