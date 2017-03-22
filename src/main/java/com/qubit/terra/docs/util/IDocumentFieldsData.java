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

import fr.opensagres.xdocreport.template.formatter.NullImageBehaviour;

public interface IDocumentFieldsData {

    public IDocumentFieldsData registerImageNullBehaviour(final String imageName, final NullImageBehaviour nullImageBehaviour);

    public IDocumentFieldsData registerImage(final String imageName, final byte[] image);

    public IDocumentFieldsData registerCollectionAsField(final String collectionName);

}
