package org.nuxeo.io.transformer;

import java.io.IOException;

import org.dom4j.Element;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;

public class FacetRemover implements DocumentTransformer {

    protected final String docType;

    protected final String facet;

    public FacetRemover(String docType, String facet) {
        this.docType = docType;
        this.facet = facet;
    }

    @Override
    public boolean transform(ExportedDocument xdoc) throws IOException {

        if (docType == null || xdoc.getType().equals(docType)) {
            Element root = xdoc.getDocument().getRootElement();
            Element sys = root.element("system");
            for (Object f : sys.elements("facet")) {
                if (facet.equals(((Element) f).getTextTrim())) {
                    ((Element) f).detach();
                }
            }
        }
        return true;
    }
}
