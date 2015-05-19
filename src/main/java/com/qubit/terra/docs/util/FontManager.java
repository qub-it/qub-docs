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

import java.awt.FontFormatException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.qubit.terra.docs.core.DocumentTemplateEngine;

public class FontManager {

    protected static final Map<Integer, String> STYLES_SUFFIX = new HashMap<Integer, String>();

    protected static FontManager fontManager;

    protected String fontsDirectory;

    protected List<FontEntry> fonts = Lists.newArrayList();

    static {
        STYLES_SUFFIX.put(Font.BOLD, "b");
        STYLES_SUFFIX.put(Font.ITALIC, "i");
        STYLES_SUFFIX.put(Font.NORMAL, "");
        STYLES_SUFFIX.put(Font.BOLDITALIC, "bi");
    }

    public FontManager() {
        scanFonts();
    }

    protected void scanFonts() {
        this.fontsDirectory = DocumentTemplateEngine.getServiceImplementation().getFontsPath();

        try {
            File file = new File(fontsDirectory);
            File[] ttfFileArray = file.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith("ttf");
                }
            });

            for (File ttfFile : ttfFileArray) {
                FontEntry fontEntry = createFontEntry(ttfFile);
                fonts.add(fontEntry);
            }

        } catch (final Exception e) {
            throw new ReportGenerationException("Error scanning fonts", e);
        }

    }

    protected FontEntry createFontEntry(File ttfFile) throws FontFormatException, IOException, FileNotFoundException,
            DocumentException {
        InputStream is = null;
        try {
            is = new FileInputStream(ttfFile);
            byte[] fontData = RandomAccessFileOrArray.InputStreamToArray(is);
            BaseFont baseFont =
                    BaseFont.createFont(ttfFile.getName(), BaseFont.IDENTITY_H, BaseFont.EMBEDDED, false, fontData, null);
            FontEntry fontEntry = new FontEntry(ttfFile.getName(), baseFont);
            return fontEntry;
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public BaseFont findBaseFont(final String familyName, int style) {
        for (FontEntry f : fonts) {
            if (f.isFor(familyName, style)) {
                return f.baseFont;
            }
        }

        throw new ReportGenerationException(String.format("Font not found for name '%s' and style '%s' ", familyName, style));
    }

    public static FontManager sharedInstance() {
        if (fontManager == null) {
            fontManager = new FontManager();
        }

        if (!DocumentTemplateEngine.getServiceImplementation().getFontsPath().equals(fontManager.fontsDirectory)) {
            fontManager.scanFonts();
        }

        return fontManager;
    }

    protected static class FontEntry {
        protected String filename;
        protected BaseFont baseFont;

        public FontEntry(final String filename, final BaseFont baseFont) {
            this.filename = filename;
            this.baseFont = baseFont;
        }

        protected boolean isFor(final String familyName, final int style) {

            String suffix = STYLES_SUFFIX.get(style);

            return String.format("%s%s.ttf", familyName.toLowerCase().replace(' ', '_').replaceAll("[\\d]", ""), suffix).equals(
                    filename);
        }
    }

}
