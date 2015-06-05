package org.nuxeo.io.ext;

import java.io.File;
import java.io.IOException;

import org.nuxeo.ecm.core.io.ExportedDocument;
import org.nuxeo.ecm.core.io.impl.plugins.XMLDirectoryReader;

public class XMLDirectoryReaderExtended extends XMLDirectoryReader {

    public XMLDirectoryReaderExtended(File source) {
        super(source);
    }

    @Override
    public ExportedDocument read() throws IOException {

        ExportedDocument doc = super.read();

        return doc;

    }
}
