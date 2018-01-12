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

import java.awt.Color;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;

import fr.opensagres.xdocreport.itext.extension.font.IFontProvider;
import fr.opensagres.xdocreport.utils.StringUtils;

public class FontProvider implements IFontProvider {

    protected FontManager fontManager = null;

    protected FontProvider() {
        fontManager = FontManager.sharedInstance();
    }

    @Override
    public Font getFont(final String familyName, final String encoding, final float size, final int style, final Color color) {
        try {
            BaseFont baseFont = fontManager.findBaseFont(familyName, style);

            Font font = new Font(baseFont, size, style, color);
            if (!StringUtils.isEmpty(familyName)) {
                font.setFamily(familyName);
            }

            return font;
        } catch (Throwable e) {
            e.printStackTrace();
            return FontFactory.getFont(familyName, encoding, size, style, color);
        }
    }

    public static FontProvider create() {
        return new FontProvider();
    }

}
