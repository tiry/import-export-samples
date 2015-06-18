package org.nuxeo.export;

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

@Operation(id = DoExportOperation.ID, category = "Export", label = "Export Sub Tree", description = "Exports a Sub tree on a FileSystem")
public class DoExportOperation {

    public static final String ID = "Room.Export";

    @Context
    protected CoreSession session;

    @Param(name = "sync", required = false)
    protected Boolean sync = true;

    @Param(name = "targetPath", required = true)
    protected String targetPath = null;

    @OperationMethod
    public String run(final DocumentModel root) throws Exception {

        // XXX this is dangerous since the path is not protected
        // => only valid for migration time !!!
        File target = new File(targetPath);
        if (!target.exists()) {
            return "targetPath does not exist";
        }

        if (sync) {
            doExport(root, target);
        } else {
            WorkManager wm = Framework.getLocalService(WorkManager.class);

            final File out = target;

            wm.schedule(new AbstractWork() {

                @Override
                public String getTitle() {
                    return "Migration Work";
                }

                @Override
                public void work() throws Exception {
                    doExport(root, out);
                }
            });

            return "scheduled";
        }

        return "done";
    }


    public void doExport(DocumentModel root, File target) throws Exception  {
        SampleDocExporter exporter = new SampleDocExporter(root, target);
        exporter.run();
    }
}
