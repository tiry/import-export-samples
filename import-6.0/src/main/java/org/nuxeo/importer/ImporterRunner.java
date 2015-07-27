package org.nuxeo.importer;

import java.io.File;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.AbstractWork;

public class ImporterRunner extends AbstractWork {
    private static final long serialVersionUID = 1L;

    protected final DocumentModel root;
    protected final File source;

    public ImporterRunner(DocumentModel root, File source) {
        this.root = root;
        this.source = source;
    }

    @Override
    public String getTitle() {
        return "Migration Work";
    }

    @Override
    public void work() throws Exception {
        initSession();
        SampleImporter exporter = new SampleImporter(root, source);
        exporter.run();
    }

}
