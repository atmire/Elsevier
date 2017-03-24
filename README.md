# DSpace Elsevier API integration patch

- [Introduction](#introduction)
- [Prerequisites](#prerequisites)
    - [Codebase](#prerequisites-codebase)
    - [Firewall verification](#prerequisites-firewall)
    - [Download patch](#prerequisites-download)
    - [Elsevier API key](#prerequisites-api)
- [Installation](#installation)
	- [1. Go to the dspace directory](#installation-src)
	- [2. Check patch compatibility](#installation-check)
	- [3. Apply the patch](#installation-apply)
	- [4. Rebuild and redeploy](#installation-rebuild)
	- [5. Restart Tomcat](#installation-restart)
- [Functionality overview](#functionality)
    - [Import during submission](#functionality-submission-import)
    - [Batch import](#functionality-batch-import)
    - [File upload access options](#functionality-file-upload)
    - [Access indicator](#functionality-entitlements)
    - [Embedded PDF](#functionality-embed)
    - [Update script](#functionality-update-script)
- [Configuration overview](#configuration)
	- [Configuration parameters](#configuration-parameters)
	- [Additional tailoring](#configuration-tailor)

## <a name="introduction"></a>  Introduction

[Elsevier](https://www.elsevier.com) is a global information analytics company that helps institutions and professionals progress science, advance healthcare and improve performance. This patch offers enhancements for DSpace 5 and 6 that leverage Elsevier's APIs for ScienceDirect, Scopus and PubMed for following use cases:

* Importing publication metadata at the start of a new submission
* Batch importing several publications at once, either as archived items or as workflow items
* Batch enrichment of existing items in the repository through command line scripts
* Embedded viewing of the publication in the repository. A user can read the publication without leaving the repository.
* See a publication's open access status automatically on the repository item page
* Enhancement of the DSpace file upload step to more easily enter embargo information

You can watch a [demo](https://youtu.be/ye2V-nU5qbw) showcasing some of the features.

The DSpace Elsevier API integration patch has been developed and is maintained by [Atmire](https://www.atmire.com), a registered service provider for DSpace.

## <a name="conditions"></a> Conditions for use

Using this integration does not require a paid subscription to any of Elsevier's commercial services. The key condition for using this API is that the link to the record of the publication on ScienceDirect/Scopus/PubMed should be prominently visible on the publication's page in the repository.

## <a name="prerequisites"></a> Prerequisites

### <a name="prerequisites-codebase"></a> Codebase

The Elsevier API integration has been released as a "patch" for DSpace as this allows for the easiest installation process of the incremental codebase. The code needed to install and deploy the integration can be found in the [Download patch](#prerequisites-download) section, which needs to be applied to your DSpace source code.

**__Important note__**: Below, we will explain you how to apply the patch to your existing installation. This will affect your source code. Before applying a patch, it is **always** recommended to create backup of your DSpace source code.

In order to apply the patch, you will need to locate the **DSpace source code** on your server. That source code directory should look similar to the following structure:

```
[dspace-src]
  - dspace
  - ...
  - LICENSE
  - NOTICE
  - README 
```

For every release of DSpace, generally two release packages are available. One package has "src" in its name and the other one doesn't. The difference is that the release labelled "src" contains ALL of the DSpace source code, while the other release retrieves precompiled packages for specific DSpace artifacts from maven central. **The Elsevier API integration patch was designed to work on both "src" and other release packages of DSpace**.

To be able to install the patch, you will need the following prerequisites:

* A running DSpace 5.x or 6.x instance, featuring the XML User Interface. JSPUI is unsupported.
* Git should be installed on the machine. The patch will be applied using several git commands as indicated in the next section.

### <a name="prerequisites-firewall"></a> Firewall verification

The server should be able to reach Elsevier and PubMed. This can be verified easily using the following commands:

```
curl -Is http://api.elsevier.com/ | head -1
curl -Is http://eutils.ncbi.nlm.nih.gov/ | head -1 
```

If *any* HTTP status code is returned you can reach the specified URL properly (even though it's a 403 or 500). If nothing is returned, the server is not able to reach the URL. Please verify whether the firewall is configured to authorize outgoing connections.

### <a name="prerequisites-download"></a> Download patch

> ⚠️ **Important note**
<br /><br />This work has been proposed as a contribution to the DSpace mainline code in [DS-2877](https://jira.duraspace.org/browse/DS-2877). This patch builds on a new import framework that was included in DSpace 6.0 (see [DS-2876](https://jira.duraspace.org/browse/DS-2876)). Because this framework was not yet part of DSpace 5, the DSpace 5 patch also contains the inclusion of this framework.
<br />For the time being, please use the patches offered by this repository to install the functionality.

Atmire's modifications to a standard DSpace for Elsevier are tracked on Github. The newest patch can therefore be generated from git.

| DSpace | Patch                                                                              |
| ------ | ---------------------------------------------------------------------------------- |
| 5.x    | [Download](https://github.com/atmire/Elsevier/compare/dspace_5x...stable_5x.patch) |
| 6.x    | [Download](https://github.com/atmire/Elsevier/compare/dspace_6x...stable_6x.patch) |


Save this file under a meaningful name. It will later be referred to as `<patch>`.

### <a name="prerequisites-api"></a> Elsevier API key

The integration will send requests to Elsevier's APIs to retrieve metadata and links. These APIs are protected with user accounts and keys to avoid abuse by robots and malicious users. To register for an API key, follow this two step approach:

1. Get hold of an API key. Go to [https://dev.elsevier.com/apikey/create](https://dev.elsevier.com/apikey/create) to create a new key. If your institution already has a Scopus API key, you can submit your Scopus API key to have the settings for the ScienceDirect API services added.

1. Register for the Institutional Repository Program. Go to [https://www.elsevier.com/solutions/sciencedirect/forms/institutional-repository-managers?rURL=Direct](https://www.elsevier.com/solutions/sciencedirect/forms/institutional-repository-managers?rURL=Direct) to submit your API key.

1. The Elsevier integration support team will add the ScienceDirect Institutional Repository settings to your API key and confirm back.

Further support for the API key registration process is available from [integrationsupport@elsevier.com](mailto:integrationsupport@elsevier.com)

More information about the Institutional Repository Program and the corresponding policies can be found on [http://dev.elsevier.com/ir_cris_vivo.html](http://dev.elsevier.com/ir_cris_vivo.html)


## <a name="installation"></a> Installation

To install the patch, the following steps will need to be performed.

### <a name="installation-src"></a> 1. Go to the `dspace` directory

This folder should have a structure similar to:

```
[dspace-src]
  - dspace          <-- Change the working directory to this folder
      - config
      - modules
      - ...
      - pom.xml
  - ...
  - LICENSE
  - NOTICE
  - README 
```

### <a name="installation-check"></a> 2. Check patch compatibility

Run the following command where `<patch>` needs to be replaced with the name of the patch:

```bash
git apply --check <patch>
```

This command will return whether it is possible to apply the patch to your installation. This should pose no problems in case the DSpace is not customized or in case few customizations are present.
In case the check is successful, the patch can be installed as explained in the next steps.

### <a name="installation-apply"></a> 3. Apply the patch

To apply the patch, the following command should be run where  `<patch>` is replaced with the name of the patch file.

```bash
git apply --whitespace=nowarn --reject <patch>
```

This command will tell git to apply the patch and ignore unharmful whitespace issues. The `--reject` flag instructs the command to continue when conflicts are encountered and saves the corresponding code hunks to a `.rej` file so you can review and apply them manually later on. This flag can be omitted if desired.

Applying the patch should result in an output similar to the following:

```
...
Applied patch dspace/config/dspace.cfg cleanly.
Applied patch dspace/config/registries/elsevier-types.xml cleanly.
Applied patch dspace/config/spring/api/general-import-service.xml cleanly.
Applied patch dspace/config/spring/api/scopus-service.xml cleanly.

```

Some IDEs might have a built-in UI which allows you to apply patches visually. This could help during conflicts.

### <a name="installation-rebuild"></a> 4. Rebuild and redeploy

After the patch has been applied, the repository will need to be rebuilt.
DSpace repositories are typically built using Maven and deployed using Ant.

Please use `mvn clean package` instead of `mvn package` to avoid errors in the user interface. If `clean` is not specified some classes might not be updated correctly.

### <a name="installation-restart"></a> 5. Restart Tomcat

After the repository has been rebuilt and redeployed, Tomcat will need to be restarted to bring the changes live.

## <a name="functionality"></a> Functionality overview

### <a name="functionality-submission-import"></a> Import during submission

Two steps will be added to the submission process. In the first step the user will be presented with a dropdown to choose the import source from, i.e. the API to search in. If a manual submission should be made, importing can be skipped by clicking "Skip Import" or not selecting an import source and clicking "Next" at the bottom of the page. 

![ImportSourceChoice](../../raw/master/images/elsevier-import-choice-step.png "ImportSourceChoice")

The second step allows the user to search and import a publication from the previously chosen import source. Fill in at least one of the presented search fields and click on "Search". A new window will appear containing the search results. To import a publication click on the "Import" button next to it.

![Liveimport](../../raw/master/images/elsevier-import-step.png "Liveimport")

Publications that are already imported are shown with a grayish background.

![Liveimport](../../raw/master/images/elsevier-liveimport-2.png "Liveimport")

When a publication is selected for import its title and authors are shown at the bottom of the import step:

![Liveimport](../../raw/master/images/elsevier-liveimport-3.png "Liveimport")

Note that importing a publication removes any fields that were already added to the item.

### <a name="functionality-batch-import"></a> Batch import

The batch import functionality allows administrators to import multiple publications at once. The batch import page can be found by clicking on "Elsevier import" in the administrative menu, or by browsing to {dspace-url}/liveimport.

Just as with importing during submission, the administrator is presented with a dropdown to choose the import source from, followed by search fields.

![Elsevier Batch Import](../../raw/master/images/elsevier-batchimport-1.png "Elsevier Batch Import")

After searching, a list of corresponding publications will be shown. A checkbox is available next to each publication to select it for import.

![Elsevier Batch Import](../../raw/master/images/elsevier-batchimport-2.png "Elsevier Batch Import")

On the next page all publications that are selected for import are listed. Here you can choose what to do with the selected publications:

* Send imported items to workspace:  The items are added to the user's workspace ("Unfinished submissions").
* Send imported items to workflow: The items are added to the workflow to be reviewed by the reviewers of the collection the item is added to.
* Archive imported items: The items are archived immediately.

A collection to which the items are added must be selected from the "Select collection" dropdown.

![Elsevier Batch Import](../../raw/master/images/elsevier-batchimport-3.png "Elsevier Batch Import")

### <a name="functionality-file-upload"></a> File upload access options

The file upload step has been altered to allow users to select the accessibility of files.

![Elsevier Batch Import](../../raw/master/images/elsevier-file-upload.png "Elsevier Batch Import")

If you encounter the following warning message in the DSpace logs, please verify whether the permissions of your API key are sufficient to retrieve hosting permissions:

> Error retrieving required nodes from the response, please verify whether your ScienceDirect API key has sufficient permissions: APIKey XXXX with IP address X.X.X.X is unrecognized or has insufficient privileges for access to this resource

### <a name="functionality-entitlements"></a> Access indicator

The Elsevier [Entitlements API](http://api.elsevier.com/content/article/entitlement) is called asynchronously through Javascript after an item page is loaded to display Open Access status and access information on the item page.

If you experience CORS-related problems, please consider the following note (copied from [dev.elsevier.com/tecdoc_sd_ir_integration.html](http://dev.elsevier.com/tecdoc_sd_ir_integration.html))

> The Article Entitlement Retrieval API should be called from user's browser by integrating it directly with the IR's web page via JavaScript. In order to comply with cross-origin security policy, we will ensure that your IR's domain is added to the API key configuration to enable W3C CORS support. You can request to add IR domains by emailing Elsevier integrationsupport your API key and a list of authorized domains.

### <a name="functionality-embed"></a> Embedded PDF

If enabled, the link to the publisher version will redirect to an embed page on your repository directly displaying the corresponding PDF. When access to the PDF is denied, a check will occur To verify if the record is under embargo. If the record is not under embargo, the Accepted Manuscript will be loaded on the embed page instead of the full PDF. Otherwise, an outgoing link to the actual article on Elsevier's website will be rendered in case the item holds a valid PII or DOI.

![Elsevier Entitlements Embed](../../raw/master/images/elsevier-entitlements-embed.png "Elsevier Entitlements Embed")

### <a name="functionality-update-script"></a> Update script

An update script is available to accommodate for changes in previously imported items. This scripts enables the possibility to use a PII or DOI to re-check an item for possible updates in file permissions, identifiers and metadata. This check is done against the originally used source API and uses the same configuration.

The script can be run using the following command in the `[dspace]` directory:

**DSpace 5**

```
[dspace]/bin/dspace dsrun com.atmire.script.UpdateElsevierItems
```

**DSpace 6**

```
[dspace]/bin/dspace dsrun org.dspace.importer.external.scidir.UpdateElsevierItems
```

Here's an overview of the available options to specify which items need to be updated:

| Option | Description 
| -------- | -----------
| `-t`<br />`--test` | Only test the changes done by the script.
| `-f`<br />`--force` | Forcefully update.
| `-a`<br />`--assignpii` | Resets the current PII with the one fetched from the API.
| `-p`<br />`--permissions` | Adjust Bitstream permissions with those fetched from the API. Only applies to Bitstreams for which permissions were not manually overruled.
| `-m`<br />`--metadata` | Adjust metadata values with those fetched from the API. Only takes effect when used in conjunction with `--force`. Note that manually entered metadata will be overridden!
| `-i`<br />`--item` *123456789/99* | If provided, the script will only be run for the provided handle. If omitted all archived items will be iterated.

## <a name="configuration"></a> Configuration overview

### <a name="configuration-parameters"></a> Configuration parameters

A new `elsevier-sciencedirect.cfg` file will be created in `[dspace-src]/dspace/config/modules`.

> **Important note**: all configurations in DSpace 6.x are prefixed with `elsevier-sciencedirect.`, e.g. `elsevier-sciencedirect.api.key`.

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
|`metadata.field.doi`| The metadata field holding the DOI value. Used for entitlement retrieval and embedded viewing. |elsevier.identifier.doi
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

### <a name="configuration-tailor"></a> Additional tailoring

The patch applies a number of changes to standard DSpace configuration files. This overview can help you to enable/disable certain pieces of the patch.

#### Hide Scopus

If you are not a Scopus subscriber, the Scopus import functionality will return no results. To hide the Scopus option from the import source selection menus, open the `dspace/config/spring/api/general-import-services.xml` file and remove the `ScopusImportService` entry from the `ImportServices` util map.

#### Submission steps
For users to see the import source lookup steps at the start of a new submission, the patch adds two submission steps in `dspace/config/item-submission.xml`. If you have multiple steps early in your submission process, please make sure that the `submit.progressbar.sourcechoice` and `submit.progressbar.liveimport` steps appear before the *Describe Metadata* steps.

Additionally a custom UploadStep class has been created in order for file permissions to be automatically suggested based on the information supplied by Elsevier. The patch will comment out the built-in UploadStep and then adds the ElsevierUploadStep. Please review carefully if you have modified UploadStep.

#### Batch import
To disable the batch importing functionality, the `ScienceDirect` aspect should be commented out (or removed) in `dspace/config/xmlui.xconf`. 

#### Input form fields
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

#### API field mapping
In case you have a custom metadata schema and/or fields, you might want to update the field mappings in order for your custom fields to be populated automatically after publication imports. The `dspace/config/spring/api/scidir-service.xml`, `dspace/config/spring/api/scopus-service.xml` and `dspace/config/spring/api/pubmed-service.xml` files contain the Spring configuration for the beans used by the corresponding integrations. To set up a custom field mapping, follow these steps.

> ⚠️ **Heads up**
<br /><br />Class names and namespaces differ between DSpace 5 and DSpace 6.Please replace each occurrence of `[CLASS]` with the corresponding version for your DSpace.

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

