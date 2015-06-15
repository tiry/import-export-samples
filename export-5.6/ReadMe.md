

## About this module

This is a sample code for exporting documents from a Nuxeo 5.6 instance.

## Core-IO 

This code provides a `SampleExporter.java` that actually simply assemble a core-io pipe :

    reader => transformer => writer

### ExtensibleDocumentTreeReader

This implementation provides a version of the `DocumentTreeReader` that is pluggable.

This allows to be able to register `ExportExtension` that are executed as part of the reader process. 

Typical use case is to add more infornation inside the `ExporteDocument` that will be output.

This sample code provide an example via the `VersionInfoExportExtension` that will export versions of each documents as children.

Typically the tree will look like :

     NX-Export-Import/ws1/folder/file/document.xml
     NX-Export-Import/ws1/folder/file/__versions__/1.1/document.xml
     NX-Export-Import/ws1/folder/file/__versions__/1.0/document.xml 


This sample code also provides `AuditInfoExportExtension` that exports Audit entris related to the document in the `<auditInfo>` virtual schema.

As a side note, migrating Audit info using this `ExportExtension` allows to migrate only the infornation related to the selected documents, but as a side effect, only Document related entries are exported/migrated :

 - Login/Lougout events are not migrated
 - any event that is not Document related is not migrated

### DocumentTransformer

In order to handle the different use case, the code does provide several `DocumentTransformer` examples :

 - `DoctypeToFacetTranslator` : transforns a Document of a given type in an other type + a facet
 - `FacetRemover` : removes a facet
 - `SchemaRemover` : removes a schema
 - `FieldMapper` : translate a field from one schema to an other, creating the target schema if needed

Of course, order does matter :

        pipe.setReader(reader);
        pipe.setWriter(writer);

        pipe.addTransformer(new DoctypeToFacetTranslator("Invoice", "File", "Invoice"));
        pipe.addTransformer(new FacetRemover(null, "IOnlyExistsInV1"));
        pipe.addTransformer(new FacetRemover(null, "Immutable"));
        pipe.addTransformer(new FieldMapper("deprecated", "dep:fieldA", "invoice", "inv:A"));
        pipe.addTransformer(new FieldMapper("deprecated", "dep:fieldB", "invoice", "inv:B"));
        pipe.addTransformer(new FieldMapper("deprecated", "dep:fieldC", "new", "nw:Y"));
        pipe.addTransformer(new SchemaRemover(null, "deprecated"));
        
        pipe.run();

Technically, these `DocumentTransfornmer`s can be used inside the Import pipe as well as inside the Export pipe : this work exactly the same.

Here the `DocumentTransformer` were integrated inside the export pipe :

 - to make unit testing easier : the export pipe is tested against the expected result
 - to make the import more efficient :
     - export can easily be multi-threaded to scale the XML transformnations across several CPU
     - import is more complicated to scale because of the DB concurrency  

### Http Bindings

The `SampleExporter` is exposed via an Automation Operation `Room.Export` that can run the import synchronously or asynchronously.

The operation takes as input the root Document.

Parameters include :

 - sync : boolean flag to configure if export should be synchonous (default) or asynchrounous
 - targetPath : file system path where exported data will be stored

 


