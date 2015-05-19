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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.xwpf.converter.pdf.PdfConverter;
import org.apache.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class DocxToPdfReportConverter implements IReportConverter {

    private FontProvider fontProvider;

    public DocxToPdfReportConverter(final String fontDirectory) {
        fontProvider = FontProvider.create();
    }

    @Override
    public boolean isForType(final String mimeType) {
        return ReportGenerator.PDF.equals(mimeType);
    }

    @Override
    public byte[] convert(final InputStream document) {
        try {
            XWPFDocument xWPFDocument = new XWPFDocument(document);
            PdfOptions options = PdfOptions.getDefault();
            options.fontProvider(fontProvider);

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            PdfConverter.getInstance().convert(xWPFDocument, result, options);

            return result.toByteArray();
        } catch (IOException e) {
            throw new ReportGenerationException("Error converting the report", e);
        }
    }

    @Override
    public boolean convertFromType(final String mimeType) {
        return ReportGenerator.DOCX.equals(mimeType);
    }

}