package com.qubit.terra.docs.util.processors.post;

import java.util.ArrayList;
import java.util.List;

import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.simple.TextDocument;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class OdtTablePostProcessor extends ReportGeneratorPostProcessor {

    //TODO: Put all this node names same place
    protected static String TABLE_COLUMN_NODE_NAME = "table:table-column";
    protected static String TABLE_HEADER_ROWS_NODE_NAME = "table:table-header-rows";
    protected static String TABLE_ROW_NODE_NAME = "table:table-row";
    protected static String SOFT_PAGE_BREAK_NODE_NAME = "text:soft-page-break";
    protected static String TEXT_NOTE_NODE_NAME = "text:note";
    protected static String TEXT_PARAGRAPH_NODE_NAME = "text:p";

    @Override
    protected void visit() {
        //Find tables with only one or two rows on a new page
        for (TableTableElement table : document.getTables()) {
            NodeList childList = table.getChildNodes();
            for (int i = 0; i < childList.getLength(); i++) {
                Node child = childList.item(i);
                if (child.getNodeName().equals(TABLE_COLUMN_NODE_NAME) || child.getNodeName().equals(TABLE_HEADER_ROWS_NODE_NAME)
                        || child.getNodeName().equals(TABLE_ROW_NODE_NAME)) {
                    continue;
                }
                if (child.getNodeName().equals(SOFT_PAGE_BREAK_NODE_NAME)) {
                    int rowsLeft = childList.getLength() - i - 1;
                    if (rowsLeft < 4) {
                        //Switch soft page break 2 rows up.
                        //This way, it will be 3 rows and the summary in the next page
                        splitTable(table, i - 2);
                    }
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

}
