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
import java.util.ArrayList;
import java.util.List;

import fr.opensagres.xdocreport.template.TemplateEngineKind;
import fr.opensagres.xdocreport.template.formatter.FieldsMetadata;
import fr.opensagres.xdocreport.utils.StringUtils;

public class FieldsExporter implements IFieldsExporter {

    private DocsMetafields metaFields;

    public FieldsExporter() {
        new FieldsMetadata(TemplateEngineKind.Freemarker.name());
        metaFields = new DocsMetafields();
    }

    @Override
    public FieldsExporter registerSimpleField(String key, String description) {
        metaFields.addFields(new Metafield(key, description, false));
        return this;
    }

    @Override
    public FieldsExporter registerCollectionField(String key, String description) {
        metaFields.addFields(new Metafield(key, description, true));
        return this;
    }

    public byte[] exportFields() {
        ByteArrayOutputStream baos = null;
        try {
            baos = new ByteArrayOutputStream();
            metaFields.generateXML(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new FieldsExporterException("Error exporting the fields", e);
        } finally {
            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    throw new FieldsExporterException("Error exporting the fields", e);
                }
            }
        }
    }

    protected class DocsMetafields {
        private static final String HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<fields templateEngineKind=\"Freemarker\" >\n" + "\t<description>%s</description>\n";

        private static final String FIELD = "\t<field name=\"%s\" list=\"%s\" imageName=\"\" syntaxKind=\"\">\n"
                + "\t\t<description>%s</description>\n" + "\t</field>\n";

        private static final String FOOTER = "</fields>\n";

        private static final String CDATA = "<![CDATA[]]>";

        private static final String LINE_BREAK = "<br/>";

        private String description;

        private List<Metafield> fields;

        public DocsMetafields() {
            fields = new ArrayList<Metafield>();
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<Metafield> getFields() {
            return fields;
        }

        public void addFields(Metafield field) {
            fields.add(field);
        }

        public void clearFields() {
            fields.clear();
        }

        public void generateXML(ByteArrayOutputStream baos) throws IOException {
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append(String.format(HEADER,
                    StringUtils.isNotEmpty(description) ? description.replace("\n", LINE_BREAK) : CDATA));
            for (Metafield field : fields) {
                strBuilder
                        .append(String.format(
                                FIELD,
                                field.getField(),
                                field.isCollection(),
                                StringUtils.isNotEmpty(field.getDescription()) ? field.getDescription().replace("\n", LINE_BREAK) : CDATA));
            }
            strBuilder.append(FOOTER);
            baos.write(strBuilder.toString().getBytes());
        }
    }

    protected class Metafield {
        private String field;
        private String description;
        private boolean collection;

        Metafield(String field, String description, boolean collection) {
            this.field = field;
            this.description = description;
            this.collection = collection;
        }

        public String getField() {
            return field;
        }

        public String getDescription() {
            return description;
        }

        public boolean isCollection() {
            return collection;
        }
    }
}
