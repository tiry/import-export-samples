package org.nuxeo.io.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.io.DocumentTranslationMap;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.AbstractDocumentWriter;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryWriter;

public class ExtensibleXMLDirectoryWriter extends XMLDirectoryWriter {

    protected boolean skipBlobs = false;

    public ExtensibleXMLDirectoryWriter(String destinationPath, boolean skipBlobs) {
        super(destinationPath);
        this.skipBlobs=skipBlobs;
    }

    public ExtensibleXMLDirectoryWriter(File destination, boolean skipBlobs) {
        super(destination);
        this.skipBlobs=skipBlobs;
    }

    @Override
    public DocumentTranslationMap write(ExportedDocument doc) throws IOException {

        File file = new File(getDestination() + File.separator + doc.getPath().toString());
        if (!file.mkdirs()) {
            throw new IOException("Cannot create target directory: " + file.getAbsolutePath());
        }
        OutputFormat format = AbstractDocumentWriter.createPrettyPrint();
        XMLWriter writer = null;
        try {
            writer = new XMLWriter(new FileOutputStream(file.getAbsolutePath() + File.separator + "document.xml"),
                    format);
            writer.write(doc.getDocument());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }

        if (skipBlobs) {
            Map<String, Blob> blobs = doc.getBlobs();
            for (Map.Entry<String, Blob> entry : blobs.entrySet()) {
                String blobPath = file.getAbsolutePath() + File.separator + entry.getKey();
                entry.getValue().transferTo(new File(blobPath));
            }
        }

        // write external documents
        for (Map.Entry<String, Document> entry : doc.getDocuments().entrySet()) {
            writer = null;
            try {
                writer = new XMLWriter(new FileOutputStream(file.getAbsolutePath() + File.separator + entry.getKey()
                        + ".xml"), format);
                writer.write(entry.getValue());
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }
        }

        return null;
    }


}
