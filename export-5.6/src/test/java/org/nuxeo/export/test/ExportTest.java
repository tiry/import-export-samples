package org.nuxeo.export.test;

import java.io.File;

import javax.inject.Inject;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.VersioningOption;
import org.nuxeo.ecm.core.api.impl.blob.StringBlob;
import org.nuxeo.ecm.core.test.CoreFeature;
import org.nuxeo.ecm.core.test.TransactionalFeature;
import org.nuxeo.ecm.core.test.annotations.BackendType;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.core.versioning.VersioningService;
import org.nuxeo.export.SampleExporter;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;

@RunWith(FeaturesRunner.class)
@Features({TransactionalFeature.class ,CoreFeature.class})
@RepositoryConfig(repositoryName = "default", type = BackendType.H2)
@Deploy("org.nuxeo.export.sample")
@LocalDeploy({"org.nuxeo.export.sample:docTypes.xml"})
public class ExportTest {

    @Inject
    protected CoreSession session;

    protected DocumentModel createSomethingToExport() throws Exception {

        DocumentModel rootDocument = session.getRootDocument();

        DocumentModel workspace = session.createDocumentModel(rootDocument.getPathAsString(), "ws1", "Workspace");
        workspace.setProperty("dublincore", "title", "test WS");
        workspace = session.createDocument(workspace);

        DocumentModel invoiceDoc = session.createDocumentModel(workspace.getPathAsString(), "invoice", "Invoice");
        invoiceDoc.setProperty("dublincore", "title", "MyDoc");
        invoiceDoc.setProperty("invoice", "InvoiceNumber", "0001");
        invoiceDoc.setPropertyValue("inv:InvoiceAmount", "$10,000");


        Blob blob = new StringBlob("SomeDummyContent");
        blob.setFilename("dummyBlob.txt");
        invoiceDoc.setProperty("file", "content", blob);

        invoiceDoc = session.createDocument(invoiceDoc);

        invoiceDoc.addFacet("HiddenInNavigation");
        invoiceDoc = session.saveDocument(invoiceDoc);


        DocumentModel folderDoc = session.createDocumentModel(workspace.getPathAsString(), "folder", "Folder");
        folderDoc.setProperty("dublincore", "title", "MyFolder");
        folderDoc = session.createDocument(folderDoc);


        DocumentModel fileDoc2 = session.createDocumentModel(folderDoc.getPathAsString(), "file", "File");
        fileDoc2.setProperty("dublincore", "title", "MyDoc");

        Blob blob2 = new StringBlob("SomeDummyContent2");
        blob.setFilename("dummyBlob2.txt");
        fileDoc2.setProperty("file", "content", blob2);

        fileDoc2 = session.createDocument(fileDoc2);

        fileDoc2.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MAJOR);
        fileDoc2.setPropertyValue("dc:description", "Youhou");
        fileDoc2 = session.saveDocument(fileDoc2);

        fileDoc2.putContextData(VersioningService.VERSIONING_OPTION, VersioningOption.MINOR);
        fileDoc2.setPropertyValue("dc:description", "Youhou2");
        fileDoc2 = session.saveDocument(fileDoc2);

        session.save();

        return workspace;
    }

    public static final String IODIR = "NX-Export-Import";

    protected File getExportDirectory() {

        String tempDir = System.getProperty("java.io.tmpdir");

        File dir = new File(tempDir + "/" + IODIR);

        if (dir.exists()) {
            FileUtils.deleteQuietly(dir);
        }
        dir.mkdirs();
        return dir;
    }

    @Test
    public void testExportAsZipAndReimport() throws Exception {

        DocumentModel root = createSomethingToExport();

        DocumentModelList versions = session.query("select * from Document where ecm:isCheckedInVersion = 1");

        Assert.assertEquals(2, versions.size());

        File out = getExportDirectory();

        SampleExporter exporter = new SampleExporter(root, out);

        exporter.run();

        StringBuffer sb = new StringBuffer();

        dump(sb,out);

        String listing = sb.toString();

        // check file exported
        Assert.assertTrue(listing.contains("ws1/folder/file/document.xml"));

        // check version exported
        Assert.assertTrue(listing.contains("ws1/folder/file/__versions__/1.0/document.xml"));

        // check invoice exported
        Assert.assertTrue(listing.contains("ws1/invoice/document.xml"));
        String xml = FileUtils.readFileToString(new File(out,"ws1/invoice/document.xml"));
        Assert.assertTrue(xml.contains("<type>File</type>"));
        Assert.assertTrue(xml.contains("<facet>Invoice</facet>"));

        System.out.println(sb.toString());

    }


    protected void dump(StringBuffer sb, File root) {
        for (File f : root.listFiles()) {
            sb.append(f.getAbsolutePath());
            sb.append("\n");
            if (f.isDirectory()) {
                dump(sb, f);
            }
        }
    }

}
