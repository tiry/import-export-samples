package org.nuxeo.io.ext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.DocumentTreeIterator;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelReader;

public class DocumentTreeReaderExtended extends DocumentModelReader {

    protected DocumentTreeIterator iterator;

    protected int pathSegmentsToRemove = 0;

    protected List<DocumentModel> pendingVersions = new LinkedList<DocumentModel>();

    protected List<ExportExtension> extensions = new ArrayList<ExportExtension>();

    public DocumentTreeReaderExtended(CoreSession session, DocumentModel root, boolean excludeRoot)
            throws ClientException {
        super(session);
        iterator = new DocumentTreeIterator(session, root, excludeRoot);
        pathSegmentsToRemove = root.getPath().segmentCount() - (excludeRoot ? 0 : 1);
    }

    public DocumentTreeReaderExtended(CoreSession session, DocumentRef root) throws ClientException {
        this(session, session.getDocument(root));
    }

    public DocumentTreeReaderExtended(CoreSession session, DocumentModel root) throws ClientException {
        this(session, root, false);
    }

    public void registerExtension(ExportExtension ext) {
        extensions.add(ext);
    }

    @Override
    public void close() {
        super.close();
        iterator.reset();
        iterator = null;
    }

    @Override
    public ExportedDocument read() throws IOException {

        DocumentModel docModel = null;
        if (pendingVersions.size() > 0) {
            docModel = pendingVersions.remove(0);
        } else {
            if (iterator.hasNext()) {
                docModel = iterator.next();
                try {
                    List<DocumentModel> versions = session.getVersions(docModel.getRef());
                    if (!versions.isEmpty()) {
                        pendingVersions.addAll(0, versions);
                    }
                } catch (Exception e) {
                    throw new IOException("Unable to get versions", e);
                }
            }
        }

        ExportedDocumentImpl result = null;
        if (docModel != null) {
            if (pathSegmentsToRemove > 0) {
                // remove unwanted leading segments
                result = new ExportedDocumentImpl(docModel,
                        docModel.getPath().removeFirstSegments(pathSegmentsToRemove), inlineBlobs);
            } else {
                result = new ExportedDocumentImpl(docModel, inlineBlobs);
            }

            // flag versions
            if (docModel.isVersion()) {
                Path path = docModel.getPath().append("__versions__").append(docModel.getVersionLabel());
                if (pathSegmentsToRemove > 0) {
                    path = path.removeFirstSegments(pathSegmentsToRemove);
                }
                result.setPath(path);
            }

            try {
                for (ExportExtension ext : extensions) {
                    ext.updateExport(docModel, result);
                }
            } catch (Exception e) {
                throw new IOException("Unable to process versions", e);
            }
            // add history
            addHistory(docModel, result);
        }
        return result;
    }

    protected void addHistory(DocumentModel docModel, ExportedDocumentImpl result) {
        // to discuss if we want to migrate history tied to doc or not
    }

}
