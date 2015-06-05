

## About the repository

Here are some sample code for managing Data Migration between 2 Nuxeo servers in different vesions using `Nuxeo-core-io`.

## Why Core-IO ?

When upgrading Nuxeo from one version to an other, if needed, the data structure are automatically updated at startup : migration is transparent.

In the worst case, migrating et SQL level shoule be straight forward.

Then why use `core-io` :

 - if you migrate from one storage backend to an other
   + SQL to MongoDB for example
 - if you migrate one Nuxeo Application to an other
   + you content model has changed and needs to be translated
 - if you need to migrate on a per tenant / per workspace basis
   + at database level it is hard to manage granularity
   
## Principles 

Basically, `nuxeo-core-io` allows to define pipes for Read/Transform/Write Nuxeo Document, so we need : 

 - an export on the source tree
     - read data from source repository
     - strip the data that is not needed
     - write the data to disk
 - an import tree
     - read data from disk
     - transform data as needed
     - create Nuxeo Documents inside the target repository

## Modules

`export-5.6` : is a sample code for exporting documents from a Nuxeo 5.6 instance

`import-6.0` : is a sample code for importing documents into a Nuxeo 6.0 instance

