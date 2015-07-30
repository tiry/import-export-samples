package org.nuxeo.io.reader;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.Lock;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;

public class LockExporter implements ExportExtension {

    @Override
    public void updateExport(DocumentModel docModel, ExportedDocumentImpl result) throws Exception {

        if (docModel.isLocked()) {
            Element lockElement = result.getDocument().getRootElement().addElement("lockInfo");
            Lock lock = docModel.getLockInfo();
            Long created = lock.getCreated().getTimeInMillis();
            String owner = lock.getOwner();
            lockElement.addElement("created").setText(created.toString());;
            lockElement.addElement("owner").setText(owner.toString());;
        }
    }

}
