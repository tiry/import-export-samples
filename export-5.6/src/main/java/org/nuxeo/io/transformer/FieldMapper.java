package org.nuxeo.io.transformer;

import java.io.IOException;
import java.util.List;

import org.dom4j.Element;
import org.dom4j.Node;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;

public class FieldMapper implements DocumentTransformer {

    protected final String srcSchemaName;

    protected final String dstSchemaName;

    protected final String srcField;

    protected final String dstField;

    public FieldMapper(String srcSchemaName, String dstSchemaName, String srcField, String dstField) {
        this.srcSchemaName = srcSchemaName;
        if (dstSchemaName == null && dstField!=null ) {
            this.dstSchemaName = srcSchemaName;
        } else {
            this.dstSchemaName = dstSchemaName;
        }
        this.srcField = srcField;
        this.dstField = dstField;

    }

    @Override
    public boolean transform(ExportedDocument xdoc) throws IOException {

        Element root = xdoc.getDocument().getRootElement();

        List<Object> schemas = root.elements("schema");
        Node detached = null;
        if (schemas != null) {
            for (Object s : schemas) {
                Element schema = (Element) s;
                String name = schema.attribute("name").getText();

                if (srcSchemaName.equalsIgnoreCase(name)) {
                    Element src = schema.element(srcField);
                    // nodesToMode = src.elements();
                    detached = src.detach();
                }
            }

            if (dstField==null) {

            } else {
                for (Object s : schemas) {
                    Element schema = (Element) s;
                    String name = schema.attribute("name").getText();

                    if (dstSchemaName.equalsIgnoreCase(name)) {
                        Element dst = schema.element(dstField);

                        if (dst == null) {
                            dst = schema.addElement(dstField);
                        }

                    }
                }
            }

        }
        return true;
    }

}
