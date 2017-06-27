/**
 * This file was created by Quorum Born IT <http://www.qub-it.com/> and its
 * copyright terms are bind to the legal agreement regulating the FenixEdu@ULisboa
 * software development project between Quorum Born IT and Serviços Partilhados da
 * Universidade de Lisboa:
 *  - Copyright © 2015 Quorum Born IT (until any Go-Live phase)
 *  - Copyright © 2015 Universidade de Lisboa (after any Go-Live phase)
 *
 * Contributors: anil.mamede@qub-it.com, diogo.simoes@qub-it.com, diogo.simoes@qub-it.com
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

package com.qubit.terra.docs.util.helpers;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class OpenofficeInProcessConverter {

    private static final long ELAPSE_TIME = 2000;

    public static synchronized byte[] convert(final byte[] odtContent, final String tempDirFullPath,
            final String mimeTypeAbbreviation) {

        try {
            long currentTimeMillis = System.currentTimeMillis();
            if (System.getProperty("os.name").startsWith("Windows")) {
                final String odtFilename = tempDirFullPath + "openofficeConversion-" + currentTimeMillis + ".odt";

                FileUtils.writeByteArrayToFile(new File(odtFilename), odtContent);

                final Process process =
                        Runtime.getRuntime().exec(String.format("soffice --headless --convert-to %s --outdir %s %s",
                                mimeTypeAbbreviation, tempDirFullPath.subSequence(0, tempDirFullPath.length() - 1), odtFilename));

                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                }

                process.destroy();

                final String outputFilename =
                        tempDirFullPath + "openofficeConversion-" + currentTimeMillis + "." + mimeTypeAbbreviation;
                final byte[] output = FileUtils.readFileToByteArray(new File(outputFilename));

                FileUtils.deleteQuietly(new File(odtFilename));
                FileUtils.deleteQuietly(new File(outputFilename));
                return output;

            } else {
                final File inputDir = new File(tempDirFullPath + "/documents-" + currentTimeMillis);
                inputDir.mkdir();
                final String odtFilename = tempDirFullPath + "/documents-" + currentTimeMillis + "/openofficeConversion-"
                        + currentTimeMillis + ".odt";

                FileUtils.writeByteArrayToFile(new File(odtFilename), odtContent);

                final Process process = Runtime.getRuntime()
                        .exec(String.format("soffice --headless --convert-to %s -env:UserInstallation=file://%s --outdir %s %s",
                                mimeTypeAbbreviation, tempDirFullPath, tempDirFullPath, odtFilename));

                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                }

                process.destroy();

                final String outputFilename =
                        tempDirFullPath + "/openofficeConversion-" + currentTimeMillis + "." + mimeTypeAbbreviation;
                final byte[] output = FileUtils.readFileToByteArray(new File(outputFilename));
                FileUtils.deleteQuietly(inputDir);
                FileUtils.deleteQuietly(new File(outputFilename));
                return output;

            }
        } catch (final Throwable e) {
            throw new OpenofficeInProcessConversionException(e);
        }

    }

}
