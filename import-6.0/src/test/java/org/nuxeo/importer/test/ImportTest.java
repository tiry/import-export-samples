package org.nuxeo.importer.test;

import java.io.File;

import javax.inject.Inject;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.importer.SampleImporter;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;

@RunWith(FeaturesRunner.class)
@Features({CoreFeature.class})
public class ImportTest {

    @Inject
    protected CoreSession session;


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

        System.out.println(sb.toString());

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
            sb.append("\n");

        }
    }

}
