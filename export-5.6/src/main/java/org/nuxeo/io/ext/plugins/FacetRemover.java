package org.nuxeo.io.ext.plugins;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.io.ext.ExportExtension;

public class FacetRemover implements ExportExtension {

    protected final String docType;

    protected final String facet;

    public FacetRemover(String docType, String facet) {
        this.docType = docType;
        this.facet = facet;
    }

    @Override
    public void updateExport(DocumentModel docModel, ExportedDocumentImpl result) throws Exception {

        if (docType==null || docModel.getType().equals(docType)) {
            Element root = result.getDocument().getRootElement();
            Element sys = root.element("system");
            for (Object f : sys.elements("facet")) {
                if (facet.equals(((Element)f).getTextTrim())) {
                    ((Element)f).detach();
                }
            }
        }
    }

}
