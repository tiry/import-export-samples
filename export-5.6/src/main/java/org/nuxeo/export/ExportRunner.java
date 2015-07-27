package org.nuxeo.export;

import java.io.File;

import javax.security.auth.login.LoginContext;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.runtime.api.Framework;

public class ExportRunner extends AbstractWork {

    protected final DocumentModel root;

    protected final File target;

    protected final boolean skipBlobs;

    public ExportRunner(DocumentModel root, File target, boolean skipBlobs) {
        this.root = root;
        this.target = target;
        this.skipBlobs = skipBlobs;
    }

    @Override
    public String getTitle() {
        return "Migration Work";
    }

    @Override
    public void work() throws Exception {
        LoginContext lc = Framework.login();
        try {
            SampleDocExporter exporter = new SampleDocExporter(root, target, skipBlobs);
            exporter.run();
        } finally {
            lc.logout();
        }
    }

}
