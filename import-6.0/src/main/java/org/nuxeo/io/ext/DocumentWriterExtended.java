package org.nuxeo.io.ext;

import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_CREATED;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_DESCRIPTION;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_LABEL;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_VERSIONABLE_ID;

import java.io.Serializable;
import java.util.Collections;

import org.dom4j.Element;
import org.nuxeo.common.collections.ScopeType;
import org.nuxeo.common.utils.Path;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.DocumentModelImpl;
import org.nuxeo.ecm.core.io.ExportConstants;
import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.DocumentModelWriter;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;
import org.nuxeo.ecm.core.versioning.VersioningService;

public class DocumentWriterExtended extends DocumentModelWriter {

    public DocumentWriterExtended(CoreSession session, String parentPath) {
        super(session, parentPath);
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

        Element version = xdoc.getDocument().getRootElement().element("version");
        if (version != null) {

            Element e = version.element("isVersion");
            String isVersion = version.elementText("isVersion");

            if ("true".equals(isVersion)) {
                String label = version.elementText(IMPORT_VERSION_LABEL.substring(4));
                String sourceId = version.elementText(IMPORT_VERSION_VERSIONABLE_ID.substring(4));
                String desc = version.elementText(IMPORT_VERSION_DESCRIPTION.substring(4));
                String created = version.elementText(IMPORT_VERSION_CREATED.substring(4));

                if (label != null) {
                    doc.putContextData(ScopeType.REQUEST, IMPORT_VERSION_LABEL, label);
                }
                if (sourceId != null) {
                    doc.putContextData(ScopeType.REQUEST, IMPORT_VERSION_VERSIONABLE_ID, sourceId);
                }
                if (desc != null) {
                    doc.putContextData(ScopeType.REQUEST, IMPORT_VERSION_DESCRIPTION, desc);
                }
                if (created != null) {
                    doc.putContextData(ScopeType.REQUEST,IMPORT_VERSION_CREATED, (Serializable) new DateType().decode(created));
                }
                doc.setPathInfo(null, name);
                ((DocumentModelImpl) doc).setIsVersion(true);

                doc.putContextData(ScopeType.REQUEST, CoreSession.IMPORT_VERSION_MAJOR,
                        doc.getPropertyValue("uid:major_version"));
                doc.putContextData(ScopeType.REQUEST, CoreSession.IMPORT_VERSION_MINOR,
                        doc.getPropertyValue("uid:minor_version"));
                doc.putContextData(ScopeType.REQUEST, CoreSession.IMPORT_IS_VERSION, true);
            }
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
