/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its 
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa 
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: anil.mamede@qub-it.com, diogo.simoes@qub-it.com
 *
 * 
 * This file is part of qub-docs.
 *
 * qub-docs is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * qub-docs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with qub-docs.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.qubit.terra.docs.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfCopyFields;
import com.lowagie.text.pdf.PdfReader;
import com.qubit.terra.docs.core.DocumentTemplateEngine;
import com.qubit.terra.docs.util.helpers.IDocumentHelper;

import fr.opensagres.xdocreport.core.XDocReportException;
import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.XDocReport;
import fr.opensagres.xdocreport.document.images.ByteArrayImageProvider;
import fr.opensagres.xdocreport.document.images.IImageProvider;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.ITemplateEngine;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.template.freemarker.FreemarkerTemplateEngine;
import freemarker.template.Configuration;

public class ReportGenerator implements IDocumentFieldsData {

    public static final String DOCX = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    public static final String PDF = "application/pdf";
    public static final String ODT = "application/vnd.oasis.opendocument.text";

    public static final String DASH = "-";

    protected InputStream template;
    protected String documentMimeType;
    protected String fontsPath;
    protected ContextMap contextMap;
    protected FieldsMetadata fieldsMetadata;

    public LinkedList<IReportConverter> converters;

    protected ReportGenerator(final byte[] templateData, final String fontsPath, final String mimeType) {
        this.documentMimeType = mimeType;
        this.template = new BufferedInputStream(new ByteArrayInputStream(templateData));
        this.fontsPath = fontsPath;
        this.contextMap = new ContextMap();
        this.fieldsMetadata = new FieldsMetadata();

        loadDefaultConverts();
    }

    public void registerHelper(String helperKey, IDocumentHelper documentHelper) {
        this.contextMap.put(helperKey, documentHelper);
    }

    protected void configureTemplateEngine(final ITemplateEngine engine) {
        FreemarkerTemplateEngine freemarkerEngine = (FreemarkerTemplateEngine) engine;
        Configuration freemarkerConfiguration = freemarkerEngine.getFreemarkerConfiguration();
        freemarkerConfiguration.setNumberFormat("0");
    }

    protected void loadDefaultConverts() {
        this.converters = new LinkedList<IReportConverter>();
        this.converters.add(new DocxToPdfReportConverter(fontsPath));
        this.converters.add(new DocxToDocxReportConverter());
        this.converters.add(DocumentTemplateEngine.getServiceImplementation()
                .isOpenOfficeConverting() ? new OdtToPdfOpenofficeConverter() : new OdtToPdfReportConverter(fontsPath));
        this.converters.add(new OdtToOdtReportConverter());
        //TODOJN: create a generic report converter - OdtToDocxReportConverter
        if (DocumentTemplateEngine.getServiceImplementation().isOpenOfficeConverting()) {
            this.converters.add(new OdtToDocxOpenofficeConverter());
        }
    }

    public ReportGenerator registerDataProvider(final IReportDataProvider provider) {
        this.contextMap.registerProvider(provider);
        return this;
    }

    @Override
    public IDocumentFieldsData registerCollectionAsField(final String collectionName) {
        this.fieldsMetadata.addFieldAsList(collectionName);
        return this;
    }

    @Override
    public IDocumentFieldsData registerImage(final String imageName, final byte[] image) {
        IImageProvider imageProvider = new ByteArrayImageProvider(image);
        this.contextMap.put(imageName, imageProvider);
        this.fieldsMetadata.addFieldAsImage(imageName, true);
        return this;
    }

    public ReportGenerator registerConverter(IReportConverter converter) {
        converters.addFirst(converter);
        return this;
    }

    public void generateReport(final String outputFile) {
        generateReport(new File(outputFile));
    }

    public void generateReport(final File outputFile) {
        byte[] report = generateReport();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outputFile);
            fos.write(report);
        } catch (IOException e) {
            throw new ReportGenerationException("error writing the report", e);
        } finally {

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    throw new ReportGenerationException("error closing the report file", e);
                }
            }
        }
    }

    public byte[] generateReport() {

        contextMap.registerFieldsMetadata();

        ByteArrayInputStream generatedReport = null;
        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            final String freemarkerEngineKind = TemplateEngineKind.Freemarker.name();
            IXDocReport report =
                    XDocReport.loadReport(template, freemarkerEngineKind, fieldsMetadata, XDocReportRegistry.getRegistry());

            configureTemplateEngine(report.getTemplateEngine());
            report.process(contextMap, outputStream);

            generatedReport = new ByteArrayInputStream(outputStream.toByteArray());

            return convert(generatedReport);
        } catch (XDocReportException | IOException e) {
            throw new ReportGenerationException(e.getMessage(), e);
        } finally {
            if (generatedReport != null) {
                try {
                    generatedReport.close();
                } catch (IOException e) {
                    throw new ReportGenerationException(e.getMessage(), e);
                }
            }

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    throw new ReportGenerationException(e.getMessage(), e);
                }
            }
        }
    }

    protected byte[] convert(final InputStream generatedReport) {
        for (IReportConverter converter : converters) {
            if (converter.convertFromType(ODT) && converter.isForType(documentMimeType)) {
                return converter.convert(generatedReport);
            }
        }

        throw new ReportGenerationException("Error converter not found for: " + documentMimeType);
    }

    public static ReportGenerator create(final String template, final String fontsPath, final String mimeType) {
        try {
            return new ReportGenerator(FileUtils.readFileToByteArray(new File(template)), fontsPath, mimeType);
        } catch (FileNotFoundException e) {
            throw new ReportGenerationException("Error finding template", e);
        } catch (IOException e) {
            throw new ReportGenerationException("Error finding template", e);
        }
    }

    public static byte[] concatPDFs(Collection<byte[]> documents) {

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfCopyFields copy = new PdfCopyFields(outputStream);

            for (byte[] doc : documents) {
                copy.addDocument(new PdfReader(doc));
            }

            copy.close();

            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new ReportGenerationException("Error in opening document", e);
        } catch (IOException e) {
            throw new ReportGenerationException("Error in opening document", e);
        }
    }

    private class ContextMap extends java.util.HashMap<String, Object> {

        private LinkedList<IReportDataProvider> dataProviders = new LinkedList<IReportDataProvider>();

        private static final long serialVersionUID = 1L;

        @Override
        public int size() {
            return super.size();
        }

        @Override
        public boolean isEmpty() {
            return dataProviders.isEmpty() && super.isEmpty();
        }

        @Override
        public boolean containsKey(final Object key) {

            return Iterables.any(this.dataProviders, new Predicate<IReportDataProvider>() {

                @Override
                public boolean apply(final IReportDataProvider provider) {
                    return provider.handleKey((String) key);
                }

            }) || super.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public Object get(Object key) {
            for (IReportDataProvider provider : dataProviders) {
                if (provider.handleKey((String) key)) {
                    return provider.valueForKey((String) key);
                }
            }

            return super.get(key);
        }

        @Override
        public Object put(String key, Object value) {
            return super.put(key, value);
        }

        @Override
        public Object remove(Object key) {
            return super.remove(key);
        }

        @Override
        public void putAll(Map m) {
            super.putAll(m);

        }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public Set<String> keySet() {
            return super.keySet();
        }

        @Override
        public Collection<Object> values() {
            return super.values();
        }

        @Override
        public Set entrySet() {
            return entrySet();
        }

        public ContextMap registerProvider(final IReportDataProvider provider) {
            this.dataProviders.addFirst(provider);
            return this;
        }

        public void registerFieldsMetadata() {
            for (IReportDataProvider provider : dataProviders) {
                provider.registerFieldsAndImages(ReportGenerator.this);
            }
        }

        public LinkedList<IReportDataProvider> getDataProviders() {
            return dataProviders;
        }

    }

}
