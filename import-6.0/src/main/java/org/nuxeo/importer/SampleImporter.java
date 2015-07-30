package org.nuxeo.importer;

import java.io.File;
import java.io.IOException;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryReader;
import org.nuxeo.io.writer.DocumentHistoryImporter;
import org.nuxeo.io.writer.ExtensibleDocumentWriter;
import org.nuxeo.io.writer.VCSDocumentLockImporter;

public class SampleImporter {

    protected final DocumentModel root;

    protected final File source;

    public SampleImporter(DocumentModel root, File destination) {
        this.root = root;
        source = destination;
    }

    public void run() throws Exception {

        final DocumentReader reader = new XMLDirectoryReader(source);
        ExtensibleDocumentWriter writer = new ExtensibleDocumentWriter(root.getCoreSession(), root.getPathAsString());

        writer.registerExtension(new DocumentHistoryImporter());
        writer.registerExtension(new VCSDocumentLockImporter());

        DocumentPipe pipe = new ImporterPipe(10);
        pipe.setReader(reader);
        pipe.setWriter(writer);
        pipe.addTransformer(new DocumentTransformer() {

            @Override
            public boolean transform(ExportedDocument xDoc) throws IOException {

                // Document xmlDoc = xDoc.getDocument();

                // do change the xml document as needed

                return true;
            }
        });
        pipe.run();

    }

}
