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
 * This file is part of Qub Docs.
 *
 * Qub Docs is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Qub Docs is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Qub Docs.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.qubit.terra.docs.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;

import com.qubit.terra.docs.util.IReportDataProvider;
import com.qubit.terra.docs.util.ReportGenerationException;
import com.qubit.terra.docs.util.ReportGenerator;

public class DocumentGenerator extends ReportGenerator {

    protected DocumentGenerator(final IDocumentTemplate documentTemplate, final String mimeType) {
        this(documentTemplate.getCurrentVersion().getContent(), mimeType);
    }

    protected DocumentGenerator(final byte[] template, final String mimeType) {
        super(template, DocumentTemplateEngine.getServiceImplementation().getFontsPath(), mimeType);
    }

    public DocumentGenerator registerDataProviders(final Collection<? extends IReportDataProvider> providers) {
        for (IReportDataProvider provider : providers) {
            registerDataProvider(provider);
        }

        return this;
    }

    public static DocumentGenerator create(final IDocumentTemplate documentTemplate, final String mimeType) {
        return new DocumentGenerator(documentTemplate, mimeType);
    }

    public static DocumentGenerator create(final String template, final String mimeType) {
        try {
            return new DocumentGenerator(FileUtils.readFileToByteArray(new File(template)), mimeType);
        } catch (FileNotFoundException e) {
            throw new ReportGenerationException("Error finding template", e);
        } catch (IOException e) {
            throw new ReportGenerationException("Error finding template", e);
        }
    }

}
