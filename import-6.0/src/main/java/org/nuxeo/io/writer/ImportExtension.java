package org.nuxeo.io.writer;

import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.ExportedDocument;

public interface ImportExtension {

    void updateImport(CoreSession session, DocumentModel docModel, ExportedDocument result) throws Exception;

}
