package org.nuxeo.export;

import java.io.File;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryWriter;
import org.nuxeo.io.reader.ExtensibleDocumentTreeReader;
import org.nuxeo.io.reader.VersionInfoExportExtension;
import org.nuxeo.io.transformer.DoctypeToFacetTranslator;
import org.nuxeo.io.transformer.FacetRemover;
import org.nuxeo.io.transformer.PluggableDocumentTransformer;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class SampleExporter {

    protected final DocumentModel root;

    protected final File destination;

    public SampleExporter(DocumentModel root, File destination) {
        this.root = root;
        this.destination = destination;
    }

    public void run() throws Exception {

        final ExtensibleDocumentTreeReader reader = new ExtensibleDocumentTreeReader(root.getCoreSession(), root);
        DocumentWriter writer = new XMLDirectoryWriter(destination);

        // register extensions !
        reader.registerExtension(new VersionInfoExportExtension());

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

        PluggableDocumentTransformer trf = new PluggableDocumentTransformer();
        trf.registerExtension(new DoctypeToFacetTranslator("Invoice", "File", "Invoice"));
        trf.registerExtension(new FacetRemover(null, "IOnlyExistsInV1"));
        trf.registerExtension(new FacetRemover(null, "Immutable"));

        pipe.addTransformer(trf);
        pipe.run();

    }

}
