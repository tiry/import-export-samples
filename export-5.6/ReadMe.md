

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

### Http Bindings

The `SampleExporter` is exposed via an Automation Operation `Room.Export` that can run the import synchronously or asynchronously.

The operation takes as input the root Document.

Parameters include :

 - sync : boolean flag to configure if export should be synchonous (default) or asynchrounous
 - targetPath : file system path where exported data will be stored

 


