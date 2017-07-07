package com.qubit.terra.docs.util.processors.post;

import org.odftoolkit.simple.text.Section;

public class OdtSectionPostProcessor extends ReportGeneratorPostProcessor {

    protected static final String INPUT_AREA_NAME = "InputArea";

    @Override
    protected void visit() {
        Section section = document.getSectionByName(INPUT_AREA_NAME);

        if (section != null) {
            section.remove();
        }
    }

}
