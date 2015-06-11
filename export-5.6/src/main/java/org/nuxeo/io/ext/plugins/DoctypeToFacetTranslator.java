package org.nuxeo.io.ext.plugins;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.io.ext.ExportExtension;

public class DoctypeToFacetTranslator implements ExportExtension {

    protected final String docType;

    protected final String newDocType;

    protected final String facet;

    public DoctypeToFacetTranslator(String docType, String newDocType, String facet) {
        this.docType = docType;
        this.newDocType = newDocType;
        this.facet = facet;
    }

    @Override
    public void updateExport(DocumentModel docModel, ExportedDocumentImpl result) throws Exception {
        if (docModel.getType().equals(docType)) {
            Element root = result.getDocument().getRootElement();
            Element sys = root.element("system");
            sys.element("type").setText(newDocType);
            sys.addElement("facet").setText(facet);
        }
    }

}
