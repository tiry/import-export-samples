

## About this module

This is a sample code for importing documents into a Nuxeo 6.0 instance.

## Core-IO 

This code provides a `SampleImporter.java` that actually simply assemble a core-io pipe :

    reader => transformer => writer

### ExtensibleDocumentWriter

This implementation provides a version of the `DocumentModelWriter` that :

 - handles version import based on the information provided by `VersionInfoExportExtension`
 - use `CoreSession.importDocuments` to be able to keep the uuids 

In addition, this implementation can have contributed `ImportExtension`.

Here we provide a `DocumentHistoryImporter` that will import document history at the same time then the Document if some history data is available.

### DocumentTransformer

For now there are not `DocumentTransformer` added inside the pipe : the transformation work is currently done at export time.

### Http Bindings

The `SampleImporter` is exposed via an Automation Operation `Room.Import` that can run the import synchronously or asynchronously.

The operation takes as input the root Document.

Parameters include :

 - sync : boolean flag to configure if import should be synchonous (default) or asynchrounous
 - sourcePath : file system path where exported data is localed


 





