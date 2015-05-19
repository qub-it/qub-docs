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

package com.qubit.terra.docs.core;

import java.util.Collection;

import com.qubit.terra.docs.util.IReportDataProvider;

public class DocumentTemplateEngine {

    private static IDocumentTemplateService documentTemplateService;

    public static void registerServiceImplementations(IDocumentTemplateService service) {
        documentTemplateService = service;
    }

    public static IDocumentTemplateService getServiceImplementation() {
        return documentTemplateService;
    }

    public DocumentGenerator createGeneratorForDocx(final IDocumentTemplate template,
            final Collection<? extends IReportDataProvider> dataProviders) {
        return createGenerator(template, dataProviders, DocumentGenerator.DOCX);
    }

    public DocumentGenerator createGeneratorForPdf(final IDocumentTemplate template,
            final Collection<? extends IReportDataProvider> dataProviders) {
        return createGenerator(template, dataProviders, DocumentGenerator.PDF);
    }

    public DocumentGenerator createGenerator(final IDocumentTemplate template,
            final Collection<? extends IReportDataProvider> dataProviders, final String mimeTypeFormat) {
        DocumentGenerator documentGenerator = DocumentGenerator.create(template, mimeTypeFormat);
        documentGenerator.registerDataProviders(dataProviders);
        return documentGenerator;
    }

}
