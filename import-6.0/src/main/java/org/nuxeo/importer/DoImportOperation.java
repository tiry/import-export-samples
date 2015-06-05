package org.nuxeo.importer;

import java.io.File;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.work.AbstractWork;
import org.nuxeo.ecm.core.work.api.WorkManager;
import org.nuxeo.runtime.api.Framework;

@Operation(id = DoImportOperation.ID, category = "Import", label = "Import Tree", description = "Import a document tree from a FileSystem")
public class DoImportOperation {

    public static final String ID = "Room.Import";

    @Context
    protected CoreSession session;

    @Param(name = "sync", required = false)
    protected Boolean sync = true;

    @Param(name = "sourcePath", required = true)
    protected String sourcePath = null;

    @OperationMethod
    public String run(final DocumentModel root) throws Exception {

        // XXX this is dangerous since the path is not protected
        // => only valid for migration time !!!
        File source = new File(sourcePath);
        if (!source.exists()) {
            return "sourcePath does not exist";
        }

        if (sync) {
            doImport(root, source);
        } else {
            WorkManager wm = Framework.getLocalService(WorkManager.class);

            final File out = source;

            // XXX Anonymous classes are not serializable => issues with Redis

            wm.schedule(new AbstractWork() {

                private static final long serialVersionUID = 1L;

                @Override
                public String getTitle() {
                    return "Migration Work";
                }

                @Override
                public void work() throws Exception {
                    doImport(root, out);
                }
            });

            return "scheduled";
        }

        return "done";
    }

    public void doImport(DocumentModel root, File target) throws Exception {
        SampleImporter exporter = new SampleImporter(root, target);
        exporter.run();
    }
}
