package org.nuxeo.io.transformer;

import java.io.IOException;

import org.dom4j.Element;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;

public class SchemaRemover implements DocumentTransformer {

    protected final String docType;

    protected final String schema;

    public SchemaRemover(String docType, String schema) {
        this.docType = docType;
        this.schema = schema;
    }

    @Override
    public boolean transform(ExportedDocument xdoc) throws IOException {
        if (docType == null || xdoc.getType().equals(docType)) {
            Element root = xdoc.getDocument().getRootElement();
            for (Object f : root.elements("schema")) {
                if (schema.equals(((Element) f).attribute("name").getText())) {
                    ((Element) f).detach();
                }
            }
        }
        return true;
    }
}
