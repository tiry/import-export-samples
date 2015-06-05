package org.nuxeo.importer;

import java.io.File;
import java.io.IOException;

import org.dom4j.Document;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.io.ext.DocumentWriterExtended;
import org.nuxeo.io.ext.XMLDirectoryReaderExtended;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class SampleImporter {

    protected final DocumentModel root;

    protected final File source;

    public SampleImporter(DocumentModel root, File destination) {
        this.root = root;
        source = destination;
    }

    public void run() throws Exception {

        final DocumentReader reader = new XMLDirectoryReaderExtended(source);
        DocumentWriter writer = new DocumentWriterExtended(root.getCoreSession(), root.getPathAsString());

        DocumentPipe pipe = new DocumentPipeImpl(10) {

            @Override
            public DocumentReader getReader() {
                return reader;
            }

            @Override
            protected void handleBatchEnd() {
                TransactionHelper.commitOrRollbackTransaction();
                TransactionHelper.startTransaction();
            }

        };

        pipe.setReader(reader);
        pipe.setWriter(writer);
        pipe.addTransformer(new DocumentTransformer() {

            @Override
            public boolean transform(ExportedDocument xDoc) throws IOException {

                Document xmlDoc = xDoc.getDocument();

                // do change the xml document as needed

                return true;
            }
        });
        pipe.run();

    }

}
