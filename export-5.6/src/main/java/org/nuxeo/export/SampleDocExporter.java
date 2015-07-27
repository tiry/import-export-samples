package org.nuxeo.export;

import java.io.File;

import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryWriter;
import org.nuxeo.io.reader.AuditInfoExportExtension;
import org.nuxeo.io.reader.ExtensibleDocumentTreeReader;
import org.nuxeo.io.reader.VersionInfoExportExtension;
import org.nuxeo.io.transformer.DoctypeToFacetTranslator;
import org.nuxeo.io.transformer.FacetRemover;
import org.nuxeo.io.transformer.FieldMapper;
import org.nuxeo.io.transformer.SchemaRemover;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class SampleDocExporter {

    protected final DocumentModel root;

    protected final File destination;

    protected final boolean skipBlobs;

    public SampleDocExporter(DocumentModel root, File destination, boolean skipBlobs) {
        this.root = root;
        this.destination = destination;
        this.skipBlobs=skipBlobs;
    }

    public void run() throws Exception {

        final ExtensibleDocumentTreeReader reader = new ExtensibleDocumentTreeReader(root.getCoreSession(), root);
        DocumentWriter writer = new XMLDirectoryWriter(destination);

        // register extensions !
        reader.registerExtension(new VersionInfoExportExtension());
        reader.registerExtension(new AuditInfoExportExtension());

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

        pipe.addTransformer(new DoctypeToFacetTranslator("Invoice", "File", "Invoice"));
        pipe.addTransformer(new FacetRemover(null, "IOnlyExistsInV1"));
        pipe.addTransformer(new FacetRemover(null, "Immutable"));
        pipe.addTransformer(new FieldMapper("deprecated", "dep:fieldA", "invoice", "inv:A"));
        pipe.addTransformer(new FieldMapper("deprecated", "dep:fieldB", "invoice", "inv:B"));
        pipe.addTransformer(new FieldMapper("deprecated", "dep:fieldC", "new", "nw:Y"));
        pipe.addTransformer(new SchemaRemover(null, "deprecated"));

        pipe.run();
    }

}
