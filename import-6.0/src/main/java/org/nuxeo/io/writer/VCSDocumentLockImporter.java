package org.nuxeo.io.writer;

import java.util.Calendar;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.Lock;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.storage.sql.RepositoryImpl;
import org.nuxeo.ecm.core.storage.sql.coremodel.SQLRepositoryService;
import org.nuxeo.runtime.api.Framework;

public class VCSDocumentLockImporter implements ImportExtension {

    @Override
    public void updateImport(CoreSession session, DocumentModel docModel, ExportedDocument xdoc) throws Exception {

        Element lockInfo = xdoc.getDocument().getRootElement().element("lockInfo");
        if (lockInfo != null) {

            String createdMS = lockInfo.element("created").getText();
            String owner = lockInfo.element("owner").getText();;

            Calendar created = Calendar.getInstance();
            created.setTimeInMillis(Long.parseLong(createdMS));
            Lock lock = new Lock(owner, created);

            SQLRepositoryService repositoryService = Framework.getService(SQLRepositoryService.class);
            RepositoryImpl repository = repositoryService.getRepositoryImpl(session.getRepositoryName());

            repository.getLockManager().setLock(docModel.getId(), lock);
        }


    }

}
