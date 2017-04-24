# Configuration overview

## Configuration parameters

A new `elsevier-sciencedirect.cfg` file will be created in `[dspace-src]/dspace/config/modules`.

!> All configurations in DSpace 6.x are prefixed with `elsevier-sciencedirect.`, e.g. `elsevier-sciencedirect.api.key`.

| Property | Description | Default 
| -------- | ----------- | -------|
|`api.key`| Elsevier API key that has been registered [before](#prerequisites-api). You can either fill out the API key manually or include in your Maven/Puppet/Ansible configuration profiles. |${elsevier.api.key}
|`api.article.url`| The base URL for the retrieval of an article. The default value should probably be left untouched. |http://api.elsevier.com/content/article
|`api.entitlement.url`| The base URL for the retrieval of identifiers and the entitlement status. The default value should probably be left untouched. |http://api.elsevier.com/content/article/entitlement
|`api.scidir.url`| The base URL for the interacting with ScienceDirect's API. The default value should probably be left untouched. |http://api.elsevier.com/content/search/scidir
|`api.pubmed.url`| The base URL for the interacting with PubMed's API. The default value should probably be left untouched. |https://eutils.ncbi.nlm.nih.gov/entrez/eutils/
|`api.scopus.url`| The base URL for the interacting with Scopus' API. The default value should probably be left untouched. |http://api.elsevier.com/content/search/scopus
|`api.scopus.view`| The Scopus view to use for retrieving results. Should either be `STANDARD` or `COMPLETE`. Only set to `COMPLETE` if your Elsevier API key allows the use of the Scopus COMPLETE view. Please refer to the [API description](http://api.elsevier.com/documentation/search/SCOPUSSearchViews.htm) for more information. |STANDARD
|`ui.article.url`| The base URL for retrieval based on Elsevier PIIs. The default value should probably be left untouched. |http://www.sciencedirect.com/science/article
|`entitlement.check.enabled`| Whether an article's entitlement (access options) should be fetched for the current user. |true
|`metadata.field.pii`| The metadata field holding the PII value. Used for entitlement retrieval and embedded viewing. |elsevier.identifier.pii
|`metadata.field.doi`| The metadata field holding the DOI value. Used for entitlement retrieval and embedded viewing. Multiple fields can be added, separated by a comma. |elsevier.identifier.doi,dc.identifier
|`metadata.field.eid`| The metadata field holding the EID value. Used for entitlement retrieval and embedded viewing. |elsevier.identifier.eid
|`metadata.field.scopus_id`| The metadata field holding the Scopus ID value. Used for entitlement retrieval and embedded viewing. |elsevier.identifier.scopusid
|`metadata.field.pubmed_id`| The metadata field holding the PubMed ID value. Used for entitlement retrieval and embedded viewing. |elsevier.identifier.pubmedid
|`embed.display`| Whether publisher versions should be embedded for viewing within the repository. If false, the user is redirected to an off-site Elsevier website. |false
|`embed.display.width`| The width of the embed frame (in px) in case `embed.display` is set to true. |700
|`embed.display.height`| The height of the embed frame (in px) in case `embed.display` is set to true. |500
|`embed.link.position`| Whether the link to the publisher version should be rendered above or below the file section on the item page. Should either be `top` or `bottom`. |top

The `dspace/config/dspace.cfg` file will also receive an additional parameter to hide abstracts originating from Scopus according to [Scopus' policies](https://dev.elsevier.com/policy.html). This way only administrators will be able to see the abstract.

```
metadata.hide.elsevier.description.scopusabstract = true
```

## Additional tailoring

The patch applies a number of changes to standard DSpace configuration files. This overview can help you to enable/disable certain pieces of the patch.

### Hide Scopus

If you are not a Scopus subscriber, the Scopus import functionality will return no results. To hide the Scopus option from the import source selection menus, open the `dspace/config/spring/api/general-import-services.xml` file and remove the `ScopusImportService` entry from the `ImportServices` util map.

### Submission steps
For users to see the import source lookup steps at the start of a new submission, the patch adds two submission steps in `dspace/config/item-submission.xml`. If you have multiple steps early in your submission process, please make sure that the `submit.progressbar.sourcechoice` and `submit.progressbar.liveimport` steps appear before the *Describe Metadata* steps.

Additionally a custom UploadStep class has been created in order for file permissions to be automatically suggested based on the information supplied by Elsevier. The patch will comment out the built-in UploadStep and then adds the ElsevierUploadStep. Please review carefully if you have modified UploadStep.

### Batch import
To disable the batch importing functionality, the `ScienceDirect` aspect should be commented out (or removed) in `dspace/config/xmlui.xconf`. 

### Input form fields
If you want to make Elsevier's internal ID values editable (e.g. `pii`,  `eid`, `scopus_id` & `pubmed_id`), they should be added to `dspace/config/input-forms.xml`. For example:

```
<field>
     <dc-schema>elsevier</dc-schema>
     <dc-element>identifier</dc-element>
     <dc-qualifier>pii</dc-qualifier>
     <repeatable>false</repeatable>
     <label>PII</label>
     <input-type>onebox</input-type>
     <hint>Enter the PII for this item.</hint>
     <required></required>
</field>
```

### API field mapping
In case you have a custom metadata schema and/or fields, you might want to update the field mappings in order for your custom fields to be populated automatically after publication imports. The `dspace/config/spring/api/scidir-service.xml`, `dspace/config/spring/api/scopus-service.xml` and `dspace/config/spring/api/pubmed-service.xml` files contain the Spring configuration for the beans used by the corresponding integrations. To set up a custom field mapping, follow these steps.

!> Class names and namespaces differ between DSpace 5 and DSpace 6. Please replace each occurrence of `[CLASS]` with the corresponding version for your DSpace.

1. In `dspace/config/spring/api/general-import-services`, the DSpace metadata field that should be populated (e.g. `dc.title`) must be configured as a Spring bean *if this is not already the case*.
   
   ```
   <bean id="[MAPPED_FIELD_ID]" class="[CLASS]">
       <constructor-arg value="[DSPACE_FIELD]"/>
   </bean>
   ```

| Placeholder         | Value
| ------------------- | ------
| `[MAPPED_FIELD_ID]` | The bean ID to be referenced later. E.g. `dcTitle`.
| `[CLASS]`           | <ul><li>**DSpace 5**: `com.atmire.import_citations.configuration.metadatamapping.MetadataField`</li><li>**DSpace 6**: `org.dspace.importer.external.metadatamapping.MetadataFieldConfig`</li></ul>
| `[DSPACE_FIELD]`    | The DSpace metadata field to populate. E.g. `dc.title`.
      

1. In `dspace/config/spring/api/general-import-services`, an additional Spring bean should be created to define which API value to use for populating the mapped field.
   
   ```
    <bean id="[VALUE_MAPPING_ID]" class="[CLASS]">
        <property name="field" ref="[MAPPED_FIELD_ID]"/>
        <property name="query" value="[XPATH]"/>
    </bean>
   ```
   
| Placeholder          | Value
| -------------------- | ------
| `[VALUE_MAPPING_ID]` | The bean ID to be referenced later. E.g. `titleContrib`.
| `[CLASS]`            | <ul><li>**DSpace 5**: `com.atmire.import_citations.configuration.metadatamapping.SimpleXpathMetadatumContributor`</li><li>**DSpace 6**: `org.dspace.importer.external.metadatamapping.contributor.SimpleXpathMetadatumContributor`</li></ul>
| `[XPATH]`            | The xpath expression used to fetch the API value from an XML response. The root for the query is the "entry" element. E.g. `dc:title`.
   
1. In `dspace/config/spring/api/scidir-services` (or the corresponding file of the import source of your choice), an entry should be added to the `scidirMetadataFieldMap` (or similar in case of another import source).
   
   ```
    <util:map id="scidirMetadataFieldMap" ...>
        ...
        <entry key-ref="[MAPPED_FIELD_ID]" value-ref="[VALUE_MAPPING_ID]"/>
        ...
    </util:map>   
   ```
   
| Placeholder          | Value
| -------------------- | ------
| `[MAPPED_FIELD_ID]`  | The ID of the mapped field bean, defined earlier.
| `[VALUE_MAPPING_ID]` | The ID of the value mapping bean, defined earlier.
   
**Combining API values into one field**

In case the populated value should be constructed from multiple API values, step 2 of the described process should be expanded to the following:

1. First, define all value mapping beans for the different pieces. Note that `[MAPPED_FIELD_ID]` is the same for both beans.

    ```
    <bean id="[INTERMEDIARY_VALUE_MAPPING_ID_1]" class="[CLASS]">
        <property name="field" ref="[MAPPED_FIELD_ID]"/>
        <property name="query" value="[XPATH_1]"/>
    </bean>

    <bean id="[INTERMEDIARY_VALUE_MAPPING_ID_2]" class="[CLASS]">
        <property name="field" ref="[MAPPED_FIELD_ID]"/>
        <property name="query" value="[XPATH_2]"/>
    </bean>
    ```

1. Then create a new Spring list containing references to the intermediary value mapping beans.

    ```
    <util:list id="[COMBINED_MAPPING_ID]" value-type="[CLASS]" list-class="java.util.LinkedList">
        <ref bean="[INTERMEDIARY_VALUE_MAPPING_ID_1]"/>
        <ref bean="[INTERMEDIARY_VALUE_MAPPING_ID_2]"/>
    </util:list>
    ```
    
| Placeholder            | Value
| ----------------------- | ------
| `[COMBINED_MAPPING_ID]` | The bean ID to be referenced later. E.g. `combinedAuthorList`.
| `[CLASS]`               | <ul><li>**DSpace 5**: `com.atmire.import_citations.configuration.metadatamapping.MetadataContributor`</li><li>**DSpace 6**: `org.dspace.importer.external.metadatamapping.contributor.MetadataContributor`</li></ul>

1. Finally create the value mapping bean while providing the Spring list.

    ```
    <bean id="[VALUE_MAPPING_ID]" class="[CLASS]">
        <property name="separator" value="[SEPARATOR]"/>
        <property name="metadatumContributors" ref="[COMBINED_MAPPING_ID]"/>
        <property name="field" ref="[MAPPED_FIELD_ID]"/>
    </bean>
    ```
    
| Placeholder          | Value
| -------------------- | ------
| `[VALUE_MAPPING_ID]` | The bean ID to be referenced later. E.g. `authorContrib`.
| `[CLASS]`            | <ul><li>**DSpace 5**: `com.atmire.import_citations.configuration.metadatamapping.CombinedMetadatumContributor`</li><li>**DSpace 6**: `org.dspace.importer.external.metadatamapping.contributor.CombinedMetadatumContributor`</li></ul>
| `[SEPARATOR]`        | The string to use for separating the combined values. E.g. `, `.
| `[COMBINED_MAPPING_ID]` | The ID of the list bean, defined earlier.
