package org.nuxeo.io.reader;

import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_CREATED;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_DESCRIPTION;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_LABEL;
import static org.nuxeo.ecm.core.api.CoreSession.IMPORT_VERSION_VERSIONABLE_ID;

import java.util.List;

import org.dom4j.Element;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.VersionModel;
import org.nuxeo.ecm.core.io.impl.ExportedDocumentImpl;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;

public class VersionInfoExportExtension implements ExportExtension {

    @Override
    public void updateExport(DocumentModel docModel, ExportedDocumentImpl result) throws Exception {

        Element versionElement = result.getDocument().getRootElement().addElement("version");

        if (docModel.isVersion()) {
            // IMPORT_VERSION_LABEL
            versionElement.addElement("isVersion").setText("true");
            ;
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
                if (description != null) {
                    versionElement.addElement(IMPORT_VERSION_DESCRIPTION.substring(4)).setText(description);
                }

                // IMPORT_VERSION_CREATED
                if (version.getCreated() != null) {
                    String created = new DateType().encode(version.getCreated());
                    versionElement.addElement(IMPORT_VERSION_CREATED.substring(4)).setText(created);
                }
                break;
            }
        }
    }
}
