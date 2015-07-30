package org.nuxeo.importer.test;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.PathRef;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.platform.audit.AuditFeature;
import org.nuxeo.ecm.platform.audit.api.AuditReader;
import org.nuxeo.ecm.platform.audit.api.LogEntry;
import org.nuxeo.importer.SampleImporter;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.runtime.transaction.TransactionHelper;

@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class, AuditFeature.class})
@Deploy("org.nuxeo.import.sample")
@LocalDeploy({"org.nuxeo.import.sample:docTypes.xml"})
public class ImportTest {

    @Inject
    protected CoreSession session;


    @Inject
    protected AuditReader auditReader;


    public static final String IODIR = "NX-Export-Import";

    protected File getImportDirectory() {

        String tempDir = System.getProperty("java.io.tmpdir");

        File dir = new File(tempDir + "/" + IODIR);

        return dir;
    }

    @Test
    public void testExportAsZipAndReimport() throws Exception {

        File in = getImportDirectory();

        Assert.assertTrue(in.exists());

        SampleImporter importer = new SampleImporter(session.getRootDocument(), in);

        importer.run();

        // get invalidations !
        session.save();

        DocumentModelList alldocs = session.query("select * from Document order by ecm:path");

        StringBuffer sb = new StringBuffer();

        dump(sb,alldocs);

        String listing = sb.toString();

        Assert.assertTrue(listing.contains("/ws1/folder/file"));

        // TODO
        // Check that UUIDs are stables

        // Check versions

        DocumentRef ref = new PathRef("/ws1/folder/file");
        DocumentModel doc = session.getDocument(ref);
        Assert.assertNotNull(doc);

        Assert.assertEquals("approved", doc.getCurrentLifeCycleState());


        List<DocumentModel> versions = session.getVersions(ref);
        Assert.assertEquals(2, versions.size());

        System.out.println(sb.toString());

        // check transtyping for Invoice !
        DocumentModel invoice = session.getDocument(new PathRef("/ws1/invoice"));
        Assert.assertEquals("File", invoice.getType());
        Assert.assertTrue(invoice.hasFacet("Invoice"));
        Assert.assertEquals("$10,000", invoice.getPropertyValue("iv:InvoiceAmount"));

        // check field translation
        Assert.assertEquals("XYZ", invoice.getPropertyValue("iv:B"));
        String[] lst = (String[]) invoice.getPropertyValue("iv:A");
        Assert.assertEquals("A", lst[0]);
        Assert.assertEquals("B", lst[1]);

        // check new schema
        Assert.assertEquals("foo", invoice.getPropertyValue("nw:Y"));

        TransactionHelper.commitOrRollbackTransaction();
        TransactionHelper.startTransaction();
        List<LogEntry> entries = auditReader.getLogEntriesFor(doc.getId());

        Assert.assertTrue(entries.size()>1);

        // check lock
        Assert.assertTrue(invoice.isLocked());

    }


    protected void dump(StringBuffer sb, DocumentModelList alldocs) {
        for (DocumentModel doc : alldocs) {
            sb.append(doc.getId());
            sb.append(" - ");
            sb.append(doc.getPathAsString());
            sb.append(" - ");
            sb.append(doc.getType());
            sb.append(" - ");
            sb.append(doc.getTitle());
            sb.append(" - ");
            sb.append(doc.isVersion());
            sb.append(" - ");
            sb.append(doc.getVersionLabel());
            sb.append("\n");


        }
    }

}
