package org.nuxeo.importer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.io.DocumentTranslationMap;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class ImporterPipe extends DocumentPipeImpl {

    protected final static Log log = LogFactory.getLog(ImporterPipe.class);

    protected long counter = 0;

    public ImporterPipe(int pageSize) {
        super(pageSize);
    }

    @Override
    public DocumentTranslationMap run() throws IOException {
        if (getReader() == null) {
            throw new IllegalArgumentException("Pipe reader cannot be null");
        }
        if (getWriter() == null) {
            throw new IllegalArgumentException("Pipe writer cannot be null");
        }

        List<DocumentTranslationMap> maps = new ArrayList<DocumentTranslationMap>();
        readAndWriteDocs(maps);
        return null;
    }

    @Override
    public void applyTransforms(ExportedDocument doc) throws IOException {
        super.applyTransforms(doc);
        counter++;
    }

    @Override
    public void applyTransforms(ExportedDocument[] docs) throws IOException {
        super.applyTransforms(docs);
        counter = counter + docs.length;
    }

    @Override
    protected void handleBatchEnd() {
        log.info("Commit transaction : imported " + counter + " documents");
        System.out.println("############ Commit transaction : imported " + counter + " documents");
        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();
    }

}
