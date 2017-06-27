package com.qubit.terra.docs.util.processors.post;

import java.util.ArrayList;
import java.util.List;

import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.text.TextNoteElement;
import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;
import org.odftoolkit.odfdom.pkg.OdfName;
import org.odftoolkit.simple.TextDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OdtFootEndNotePostProcessor extends ReportGeneratorPostProcessor {

    //TODO: Put all this node names same place
    protected static String TABLE_COLUMN_NODE_NAME = "table:table-column";
    protected static String TABLE_HEADER_ROWS_NODE_NAME = "table:table-header-rows";
    protected static String TABLE_ROW_NODE_NAME = "table:table-row";
    protected static String SOFT_PAGE_BREAK_NODE_NAME = "text:soft-page-break";
    protected static String TEXT_NOTE_NODE_NAME = "text:note";
    protected static String TEXT_PARAGRAPH_NODE_NAME = "text:p";

    protected Boolean alignRight;

    public OdtFootEndNotePostProcessor(final Boolean alignLeft) {
        this.alignRight = alignLeft;
    }

    @Override
    protected void visit() {
        try {
            if (alignRight != null && alignRight) {
                visitFootNotes();
                visitEndNotes();
                visitFooter();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void visitFootNotes() {
        //TODO: This algorithm only visits footnotes in tables
        for (TableTableElement table : document.getTables()) {
            NodeList childList = table.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (child.getNodeName().equals(TABLE_HEADER_ROWS_NODE_NAME)) {
                    List<TextNoteElement> footNotes = findFootNotes(child);
                    processFootNotes(footNotes);
                    break;
                }
            }
        }
    }

    private void processFootNotes(final List<TextNoteElement> footNotes) {
        List<TextParagraphElementBase> paragraphs = new ArrayList<>();
        for (TextNoteElement footNote : footNotes) {
            paragraphs.addAll(findParagraphs(footNote));
        }

        for (TextParagraphElementBase paragraph : paragraphs) {
            OdfStyleProperty marginLeftProperty = OdfStyleProperty.get(OdfStylePropertiesSet.ParagraphProperties,
                    OdfName.newName(OdfDocumentNamespace.FO, "margin-left"));
            OdfStyleProperty marginRightProperty = OdfStyleProperty.get(OdfStylePropertiesSet.ParagraphProperties,
                    OdfName.newName(OdfDocumentNamespace.FO, "margin-right"));
            OdfStyleProperty textIndentProperty = OdfStyleProperty.get(OdfStylePropertiesSet.ParagraphProperties,
                    OdfName.newName(OdfDocumentNamespace.FO, "text-indent"));
            OdfStyleProperty textAutoIndentProperty = OdfStyleProperty.get(OdfStylePropertiesSet.ParagraphProperties,
                    OdfName.newName(OdfDocumentNamespace.FO, "auto-text-indent"));
            OdfStyleProperty writingModeProperty = OdfStyleProperty.get(OdfStylePropertiesSet.ParagraphProperties,
                    OdfName.newName(OdfDocumentNamespace.FO, "writing-mode"));

            paragraph.getAutomaticStyle().setProperty(marginLeftProperty, "9.999cm");
            paragraph.getAutomaticStyle().setProperty(marginRightProperty, "0cm");
            paragraph.getAutomaticStyle().setProperty(textIndentProperty, "-0.6cm");
            paragraph.getAutomaticStyle().setProperty(textAutoIndentProperty, "false");
            paragraph.getAutomaticStyle().setProperty(writingModeProperty, "page");
        }

    }

    private List<TextNoteElement> findFootNotes(final Node node) {
        NodeList childNodes = node.getChildNodes();
        List<TextNoteElement> result = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(TEXT_NOTE_NODE_NAME)) {
                TextNoteElement element = (TextNoteElement) child;
                OdfName noteClassAttribute = OdfName.newName(OdfDocumentNamespace.TEXT, "note-class");
                if (element.hasOdfAttribute(noteClassAttribute)
                        && element.getOdfAttribute(noteClassAttribute).getValue().equals("footnote")) {
                    result.add(element);
                }
                continue;
            }
            result.addAll(findFootNotes(child));
        }

        return result;
    }

    private List<TextParagraphElementBase> findParagraphs(final Node node) {
        NodeList childNodes = node.getChildNodes();
        List<TextParagraphElementBase> result = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(TEXT_PARAGRAPH_NODE_NAME)) {
                TextParagraphElementBase element = (TextParagraphElementBase) child;
                result.add(element);
                continue;
            }
            result.addAll(findParagraphs(child));
        }

        return result;
    }

    private void visitEndNotes() {
        //TODO
    }

    private void visitFooter() throws Exception {
        //Get Footers
        //Get all paragraphs in footers
        List<TextParagraphElementBase> paragraphs = new ArrayList<>();
        NodeList footersList = ((TextDocument) document).getStylesDom().getElementsByTagName("style:footer");
        if (footersList != null) {
            for (int i = 0; i < footersList.getLength(); i++) {
                paragraphs.addAll(findParagraphs(footersList.item(i)));
            }
        }
        footersList = ((TextDocument) document).getStylesDom().getElementsByTagName("style:footer-first");
        if (footersList != null) {
            for (int i = 0; i < footersList.getLength(); i++) {
                paragraphs.addAll(findParagraphs(footersList.item(i)));
            }
        }
        //Process paragraphs
        for (TextParagraphElementBase paragraph : paragraphs) {

            OdfStyleProperty textAlignProperty = OdfStyleProperty.get(OdfStylePropertiesSet.ParagraphProperties,
                    OdfName.newName(OdfDocumentNamespace.FO, "text-align"));
            String textAlignValue = paragraph.getAutomaticStyle().getStylePropertiesDeep().get(textAlignProperty);
            if (textAlignValue != null && textAlignValue.equals("center")) {
                paragraph.getAutomaticStyle().setProperty(textAlignProperty, "right");
                continue;
            }
            if (textAlignValue != null && textAlignValue.equals("right")) {
                continue;
            }

            OdfStyleProperty marginLeftProperty = OdfStyleProperty.get(OdfStylePropertiesSet.ParagraphProperties,
                    OdfName.newName(OdfDocumentNamespace.FO, "margin-left"));

            paragraph.getAutomaticStyle().setProperty(marginLeftProperty, "9.999cm");
        }

    }

}
