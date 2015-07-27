package org.nuxeo.importer;

import java.io.File;

import javax.security.auth.login.LoginContext;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.runtime.api.Framework;

public class ImporterRunner extends AbstractWork {
    private static final long serialVersionUID = 1L;

    protected final DocumentRef rootRef;
    protected final File source;

    public ImporterRunner(DocumentModel root, File source) {
        rootRef = root.getRef();
        this.source = source;
    }

    @Override
    public String getTitle() {
        return "Migration Work";
    }

    @Override
    public void work() throws Exception {
        initSession();
        LoginContext lc = Framework.login();
        try {
            DocumentModel root = session.getDocument(rootRef);
            SampleImporter exporter = new SampleImporter(root, source);
            exporter.run();
        } finally {
            lc.logout();
        }
    }

}
