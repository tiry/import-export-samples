package org.nuxeo.io.reader;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;

public interface ExportExtension {

    void updateExport(DocumentModel docModel, ExportedDocumentImpl result) throws Exception;

}
