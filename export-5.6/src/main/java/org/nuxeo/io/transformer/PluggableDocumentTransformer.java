package org.nuxeo.io.transformer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.nuxeo.ecm.core.io.DocumentTransformer;
import org.nuxeo.ecm.core.io.ExportedDocument;

public class PluggableDocumentTransformer implements DocumentTransformer {

    protected List<DocumentTransformer> extensions = new ArrayList<DocumentTransformer>();

    public void registerExtension(DocumentTransformer ext) {
        extensions.add(ext);
    }

    @Override
    public boolean transform(ExportedDocument xdoc) throws IOException {

        for (DocumentTransformer ext : extensions) {
            ext.transform(xdoc);
        }
        return true;
    }

}
