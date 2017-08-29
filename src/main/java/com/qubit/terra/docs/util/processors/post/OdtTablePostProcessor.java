package com.qubit.terra.docs.util.processors.post;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.odftoolkit.odfdom.dom.OdfDocumentNamespace;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.style.props.OdfStylePropertiesSet;
import org.odftoolkit.odfdom.dom.style.props.OdfStyleProperty;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.pkg.OdfName;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.Fields;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.qubit.terra.docs.util.helpers.OpenofficeInProcessConverter;

public class OdtTablePostProcessor extends ReportGeneratorPostProcessor {

    public static final Pattern PATTERN = Pattern.compile("\\$\\$qub(.+)-(\\d+)");
    public static final String PAGE_ANCHOR = "$$qub";

    //TODO: Put all this node names same place
    protected static String TABLE_COLUMN_NODE_NAME = "table:table-column";
    protected static String TABLE_HEADER_ROWS_NODE_NAME = "table:table-header-rows";
    protected static String TABLE_ROW_NODE_NAME = "table:table-row";
    protected static String SOFT_PAGE_BREAK_NODE_NAME = "text:soft-page-break";
    protected static String TEXT_NOTE_NODE_NAME = "text:note";
    protected static String TEXT_PARAGRAPH_NODE_NAME = "text:p";

    @Override
    protected void visit() {
        try {
            List<String> tableNames = new ArrayList<>();
            byte[] copiedByteArray = save();
            //Modify document copy to add the current page to last three columns if different break that table in a more suitable way
            Document copiedDocument = Document.loadDocument(new ByteArrayInputStream(copiedByteArray));
            for (Table table : copiedDocument.getTableList()) {
                int numberOfRows = Integer.min(table.getRowCount(), 3);

                for (int i = 1; i <= numberOfRows; i++) {
                    Row row = table.getRowByIndex(table.getRowCount() - i);

                    Paragraph paragraph = getOrCreateParagraph(row, PAGE_ANCHOR + table.getTableName() + "-");

                    Fields.createCurrentPageNumberField(paragraph.getOdfElement());
                }
            }
            //In order to fill the page number we need to convert odt to pdf
            String property = "java.io.tmpdir";
            String tempDir = System.getProperty(property);
            ByteArrayOutputStream copiedByteArrayStream = new ByteArrayOutputStream();
            copiedDocument.save(copiedByteArrayStream);
            copiedByteArray = OpenofficeInProcessConverter.convert(copiedByteArrayStream.toByteArray(), "odt", tempDir, "pdf");
            copiedDocument.close();
            copiedByteArrayStream.close();

            tableNames.addAll(processPdf(copiedByteArray));

            processTables(tableNames);
        } catch (Exception e) {
            throw new RuntimeException("Error in visiting Odf Document. " + e.getMessage());
        }

    }

    protected List<String> processPdf(final byte[] pdfContent) {
        List<String> tablesToProcess = new ArrayList<>();
        PDDocument pdfDoc = null;
        try {
            //Read PDF and find pattern
            Map<String, Set<Integer>> verifyTablesSplit = new HashMap<>();
            pdfDoc = PDDocument.load(pdfContent);
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String text = pdfStripper.getText(pdfDoc);
            Matcher matcher = PATTERN.matcher(text);
            while (matcher.find()) {
                Set<Integer> lst = verifyTablesSplit.get(matcher.group(1));
                if (lst == null) {
                    lst = new HashSet<>();
                }
                lst.add(Integer.valueOf(matcher.group(2)));
                verifyTablesSplit.put(matcher.group(1), lst);
            }
            // for each table if the last three rows are on the same page then don't need to split
            for (Entry<String, Set<Integer>> entry : verifyTablesSplit.entrySet()) {
                String tableName = entry.getKey();
                Set<Integer> rowsPageNumber = entry.getValue();
                if (rowsPageNumber.size() != 1) {
                    tablesToProcess.add(tableName);
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (pdfDoc != null) {
                try {
                    pdfDoc.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        return tablesToProcess;
    }

    protected void processTables(final List<String> tableNames) {
        //Find tables with only one or two rows on a new page
        for (TableTableElement table : document.getTables()) {
            if (tableNames.contains(table.getTableNameAttribute())) {
                NodeList childList = table.getChildNodes();
                if (childList.getLength() > 3) {
                    splitTable(table, childList.getLength() - 4);
                } else {
                    //TODO Best way here maybe force a page break and be everything together
                    System.out.println("[WARN] Document has a table, " + table.getTableNameAttribute()
                            + ", with less than five rows and they are splitted! Check this situation");
                }
            }
        }
    }

    //ATTENTION: The element in splitIndex is the first element in the second table
    private void splitTable(final TableTableElement originalTable, final int breakIndex) {
        TableTableElement copiedTable =
                (TableTableElement) ((TextDocument) document).insertOdfElement(originalTable, document, originalTable, false);
        // 1st - Remove all lines after the page break on the old table
        processOriginalTable(originalTable, breakIndex);
        // 2nd - Create new table
        //     - Remove all rows until page break
        //     - Remove footnotes
        processCopiedTable(copiedTable, breakIndex);
    }

    private void processOriginalTable(final TableTableElement originalTable, final int breakIndex) {
        NodeList childList = originalTable.getChildNodes();
        List<Node> childsToRemove = new ArrayList<>();
        // Collect all rows after the page break in original table
        for (int i = breakIndex; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child.getNodeName().equals(TABLE_ROW_NODE_NAME) || child.getNodeName().equals(SOFT_PAGE_BREAK_NODE_NAME)) {
                childsToRemove.add(child);
            }
        }
        // Remove all collected rows
        for (Node node : childsToRemove) {
            originalTable.removeChild(node);
        }

    }

    private void processCopiedTable(final TableTableElement copiedTable, final int breakIndex) {
        // Collect all rows before the page break in copied table
        // Remove all foot notes in copied table
        NodeList childList = copiedTable.getChildNodes();
        List<Node> childsToRemove = new ArrayList<>();
        for (int i = 0; i < childList.getLength(); i++) {
            Node child = childList.item(i);
            if (child.getNodeName().equals(TABLE_COLUMN_NODE_NAME)) {
                continue;
            }
            if (child.getNodeName().equals(TABLE_HEADER_ROWS_NODE_NAME)) {
                deleteFootNotes(child);
                continue;
            }
            if (child.getNodeName().equals(TABLE_ROW_NODE_NAME) && i < breakIndex) {
                childsToRemove.add(child);
                continue;
            }
            if (child.getNodeName().equals(SOFT_PAGE_BREAK_NODE_NAME)) {
                childsToRemove.add(child);
                continue;
            }
        }
        //Create new style for copied table
        OdfStyle copiedTableStyle = (OdfStyle) copiedTable.getAutomaticStyle().cloneNode(true);
        copiedTableStyle.setStyleNameAttribute(copiedTableStyle.getStyleNameAttribute() + "Clone2");
        copiedTable.getAutomaticStyles().appendChild(copiedTableStyle);
        copiedTable.setStyleName(copiedTableStyle.getStyleNameAttribute());

        // Insert page break
        OdfStyleProperty breakPageProperty = OdfStyleProperty.get(OdfStylePropertiesSet.TableProperties,
                OdfName.newName(OdfDocumentNamespace.FO, "break-before"));
        copiedTableStyle.setProperty(breakPageProperty, "page");

        // Remove all collected rows
        for (Node node : childsToRemove) {
            copiedTable.removeChild(node);
        }
    }

    private void deleteFootNotes(final Node node) {
        NodeList childNodes = node.getChildNodes();
        List<Node> childToRemove = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeName().equals(TEXT_NOTE_NODE_NAME)) {
                childToRemove.add(child);
                continue;
            }
            deleteFootNotes(child);
        }

        for (Node child : childToRemove) {
            node.removeChild(child);
        }
    }

    private Paragraph getOrCreateParagraph(final Row row, final String prefix) {
        Cell cell = row.getCellByIndex(0);
        Paragraph p;
        if (cell.getParagraphIterator().hasNext()) {
            p = cell.getParagraphIterator().next();
            String content = p.getTextContent();
            String styleName = p.getStyleName();
            cell.removeParagraph(p);
            int sizeDifference = content.length() - prefix.length() - 1;
            if (sizeDifference > 0) {
                p = cell.addParagraph(content.substring(0, content.length() - prefix.length()) + prefix);
            } else {
                p = cell.addParagraph(prefix);
            }
            p.setStyleName(styleName);
        } else {
            p = cell.addParagraph(prefix);
        }

        return p;
    }

}
