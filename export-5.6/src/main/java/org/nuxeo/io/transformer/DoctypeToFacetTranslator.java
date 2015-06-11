package org.nuxeo.io.transformer;

import java.io.IOException;

import org.dom4j.Element;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;

public class DoctypeToFacetTranslator implements DocumentTransformer {

    protected final String docType;

    protected final String newDocType;

    protected final String facet;

    public DoctypeToFacetTranslator(String docType, String newDocType, String facet) {
        this.docType = docType;
        this.newDocType = newDocType;
        this.facet = facet;
    }

    @Override
    public boolean transform(ExportedDocument xdoc) throws IOException {
        if (xdoc.getType().equals(docType)) {
            Element root = xdoc.getDocument().getRootElement();
            Element sys = root.element("system");
            sys.element("type").setText(newDocType);
            sys.addElement("facet").setText(facet);
        }
        return true;
    }

}
