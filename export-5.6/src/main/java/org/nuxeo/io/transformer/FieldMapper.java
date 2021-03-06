package org.nuxeo.io.transformer;

import java.io.IOException;
import java.util.List;

import org.dom4j.Element;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;

public class FieldMapper implements DocumentTransformer {

    protected final String srcSchemaName;

    protected final String dstSchemaName;

    protected final String srcField;

    protected final String dstField;

    public FieldMapper(String srcSchemaName, String srcField, String dstSchemaName, String dstField) {
        this.srcSchemaName = srcSchemaName;
        if (dstSchemaName == null && dstField != null) {
            this.dstSchemaName = srcSchemaName;
        } else {
            this.dstSchemaName = dstSchemaName;
        }
        this.srcField = srcField;
        this.dstField = dstField;

    }

    protected String getUnprefixedName(String name) {

        int idx = name.indexOf(":");
        if (idx > 0) {
            return name.substring(idx + 1);
        }
        return name;
    }

    protected String getPrefix(String name) {

        int idx = name.indexOf(":");
        if (idx > 0) {
            return name.substring(0,idx);
        }
        return null;
    }


    @Override
    public boolean transform(ExportedDocument xdoc) throws IOException {

        Element root = xdoc.getDocument().getRootElement();

        List<Object> schemas = root.elements("schema");
        Element src = null;
        if (schemas != null) {
            for (Object s : schemas) {
                Element schema = (Element) s;
                String name = schema.attribute("name").getText();

                if (srcSchemaName.equalsIgnoreCase(name)) {
                    src = schema.element(getUnprefixedName(srcField));
                    src.detach();
                }
            }

            Element dstSchema = null;
            if (dstField == null || src == null) {
                // NOP
            } else {
                for (Object s : schemas) {
                    Element schema = (Element) s;
                    String name = schema.attribute("name").getText();
                    if (dstSchemaName.equalsIgnoreCase(name)) {
                        dstSchema = schema;
                        break;
                    }
                }
                if (dstSchema == null) {
                    dstSchema = root.addElement("schema");
                    dstSchema.addAttribute("name", dstSchemaName);
                }
                Element dst = dstSchema.element(getUnprefixedName(dstField));
                if (dst == null) {
                    String prefix = getPrefix(dstField);
                    if (prefix==null ||  dstSchema.getNamespaceForPrefix(prefix)==null) {
                        dst = dstSchema.addElement(getUnprefixedName(dstField));
                    } else {
                        dst = dstSchema.addElement(dstField);
                    }
                }
                for (Object sub : src.elements()) {
                    Element e = (Element) sub;
                    e.detach();
                    dst.add(e);
                }
                String txt = src.getText();
                if (txt != null) {
                    dst.addText(txt);
                }

            }

        }
        return true;
    }

}
