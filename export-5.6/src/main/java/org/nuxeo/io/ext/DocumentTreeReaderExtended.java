package org.nuxeo.io.ext;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.dom4j.Element;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.DocumentTreeIterator;
import org.nuxeo.ecm.core.api.VersionModel;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelReader;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;

import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_CREATED;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_DESCRIPTION;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_LABEL;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_VERSIONABLE_ID;


public class DocumentTreeReaderExtended extends DocumentModelReader {

    protected DocumentTreeIterator iterator;

    protected int pathSegmentsToRemove = 0;

    protected List<DocumentModel> pendingVersions = new LinkedList<DocumentModel>();

    public DocumentTreeReaderExtended(CoreSession session, DocumentModel root, boolean excludeRoot) throws ClientException {
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

    @Override
    public void close() {
        super.close();
        iterator.reset();
        iterator = null;
    }

    @Override
    public ExportedDocument read() throws IOException {

        DocumentModel docModel = null;
        if (pendingVersions.size()>0) {
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
        if (docModel!=null) {
            if (pathSegmentsToRemove > 0) {
                // remove unwanted leading segments
                result =  new ExportedDocumentImpl(docModel, docModel.getPath().removeFirstSegments(pathSegmentsToRemove),
                        inlineBlobs);
            } else {
                result =  new ExportedDocumentImpl(docModel, inlineBlobs);
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
                // add version info
                addVersionInfo(docModel, result);
            } catch (Exception e) {
                throw new IOException("Unable to process versions", e);
            }
            // add history
            addHistory(docModel, result);
        }
        return result;
    }

    protected void addHistory (DocumentModel docModel,ExportedDocumentImpl result ) {
        // to discuss if we want to migrate history tied to doc or not
    }

    protected void addVersionInfo(DocumentModel docModel,ExportedDocumentImpl result ) throws Exception {

        Element versionElement = result.getDocument().getRootElement().addElement("version");


        if (docModel.isVersion()) {
            // IMPORT_VERSION_LABEL
            versionElement.addElement("isVersion").setText("true");;
            versionElement.addElement(IMPORT_VERSION_LABEL.substring(4)).setText(docModel.getVersionLabel());

            // IMPORT_VERSION_VERSIONABLE_ID
            String sourceId = docModel.getSourceId();
            versionElement.addElement(IMPORT_VERSION_VERSIONABLE_ID.substring(4)).setText(sourceId);
            DocumentModel liveDocument = docModel.getCoreSession().getSourceDocument(docModel.getRef());

            List<VersionModel> versions = docModel.getCoreSession().getVersionsForDocument(liveDocument.getRef());
            for (VersionModel version : versions) {
                if (!docModel.getVersionLabel().equals(version.getLabel())) {
                    continue;
                }
                // IMPORT_VERSION_DESCRIPTION
                String description = version.getDescription();
                if (description!=null) {
                    versionElement.addElement(IMPORT_VERSION_DESCRIPTION.substring(4)).setText(description);
                }

                //IMPORT_VERSION_CREATED
                if (version.getCreated()!=null) {
                    String created =  new DateType().encode(version.getCreated());
                    versionElement.addElement(IMPORT_VERSION_CREATED.substring(4)).setText(created);
                }
                break;
            }



        }

    }

}
