package org.nuxeo.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.io.DocumentPipe;
import org.nuxeo.ecm.core.io.DocumentReader;
import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.DocumentWriter;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.DocumentPipeImpl;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryWriter;
import org.nuxeo.io.ext.DocumentTreeReaderExtended;
import org.nuxeo.io.ext.plugins.DoctypeToFacetTranslator;
import org.nuxeo.io.ext.plugins.VersionInfoExtension;
import org.nuxeo.runtime.transaction.TransactionHelper;

public class SampleExporter {

    protected final DocumentModel root;

    protected final File destination;

    public SampleExporter(DocumentModel root, File destination) {
        this.root = root;
        this.destination = destination;
    }

    public void run() throws Exception {

        final DocumentTreeReaderExtended reader = new DocumentTreeReaderExtended(root.getCoreSession(), root);
        DocumentWriter writer = new XMLDirectoryWriter(destination);

        // register extensions !
        reader.registerExtension(new VersionInfoExtension());
        reader.registerExtension(new DoctypeToFacetTranslator("Invoice", "File", "Invoice"));

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

                // remove the Imutable facet since this is a pseudo facet that is actually generated at runtine
                List<Object> facets =xmlDoc.getRootElement().element("system").elements("facet");
                for (Object facet : facets) {
                    Element f = (Element) facet;
                    if ("Immutable".equalsIgnoreCase(f.getTextTrim())) {
                        f.detach();
                    }
                }

                // do change the xml document as needed

                return true;
            }
        });
        pipe.run();

    }

}
