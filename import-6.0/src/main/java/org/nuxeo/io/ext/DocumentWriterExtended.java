package org.nuxeo.io.ext;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.dom4j.Element;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.io.DocumentTranslationMap;
import org.nuxeo.ecm.core.io.ExportConstants;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;
import org.nuxeo.ecm.core.versioning.VersioningService;

public class DocumentWriterExtended extends DocumentModelWriter {

    public DocumentWriterExtended(CoreSession session, String parentPath) {
        super(session, parentPath);
    }

    @Override
    public DocumentTranslationMap write(ExportedDocument doc) throws IOException {

        if (doc.getPath().toString().contains("__versions__")) {
            return null;
        }

        List<Object> facets = doc.getDocument().getRootElement().element("system").elements("facets");
        for (Object facet : facets) {
            Element f = (Element) facet;
            if ("Immutable".equalsIgnoreCase(f.getTextTrim())) {
                return null;
            }
        }

        return super.write(doc);

    }

    @Override
    protected DocumentModel createDocument(ExportedDocument xdoc, Path toPath) throws ClientException {
        Path parentPath = toPath.removeLastSegments(1);
        String name = toPath.lastSegment();

        DocumentModel doc = new DocumentModelImpl(parentPath.toString(), name, xdoc.getType());

        // set lifecycle state at creation
        Element system = xdoc.getDocument().getRootElement().element(ExportConstants.SYSTEM_TAG);
        String lifeCycleState = system.element(ExportConstants.LIFECYCLE_STATE_TAG).getText();
        doc.putContextData("initialLifecycleState", lifeCycleState);

        // loadFacets before schemas so that additional schemas are not skipped
        loadFacetsInfo(doc, xdoc.getDocument());

        // then load schemas data
        loadSchemas(xdoc, doc, xdoc.getDocument());

        if (doc.hasSchema("uid")) {
            doc.putContextData(ScopeType.REQUEST, VersioningService.SKIP_VERSIONING, true);
        }

        String uuid = xdoc.getId();
        if (uuid != null) {
            ((DocumentModelImpl) doc).setId(uuid);
        }

        if (doc.getId() != null) {
            session.importDocuments(Collections.singletonList(doc));
        } else {
            doc = session.createDocument(doc);
        }

        // load into the document the system properties, document needs to exist
        loadSystemInfo(doc, xdoc.getDocument());

        unsavedDocuments += 1;
        saveIfNeeded();

        return doc;
    }

}
