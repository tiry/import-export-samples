package org.nuxeo.export;

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
import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryWriter;
import org.nuxeo.io.ext.DocumentTreeReaderExtended;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class SampleExporter {

    protected final DocumentModel root;

    protected final File destination;

    public SampleExporter(DocumentModel root, File destination) {
        this.root = root;
        this.destination = destination;
    }

    public void run() throws Exception {

        final DocumentReader reader = new DocumentTreeReaderExtended(root.getCoreSession(), root);
        DocumentWriter writer = new XMLDirectoryWriter(destination);

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
