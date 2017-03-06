**Table of Contents**  

- [Introduction](#Introduction)
	- [Watch a demo](#Demo)
	- [Conditions for use](#Conditions)
	- [Contribution to DSpace mainline code](#Contribution)
	- [Branches in this repository](#Branches)
- [Registering a Developer API key ](#Registering-a-Developer-API-key)
- [Adding the functionality to your DSpace codebase ](#Patch-installation-procedures)
	- [Prerequisites](#Prerequisites)
	- [Obtaining a recent patch file](#Obtaining-recent-patch)
	- [Patch installation ](#Patch-Installation)
		- [1. Go to the DSpace Source directory. ](#goto-DSpace-Source)
		- [2. Run the Git command to check whether the patch can be correctly applied. ](#Run-git-command)
		- [3. Apply the patch ](#Apply-patch)
		- [4. Rebuild and redeploy your repository ](#Rebuild-redeploy)
		- [5. Restart your Tomcat ](#Restart-tomcat)
	- [Changes to your standard DSpace configuration files](#Standard-Configuration)
		- [Submission step for Live Import](#Live-import)
		- [Input Form fields](#Form-fields)
		- [xmlui.xconf aspect](#XMLUI-XCONF)
		- [Optionally: disabling Scopus import](#Scopus-Disable)
		- [Improved upload step with file permissions](#File-permissions-in-the-upload-step)
- [In-Depth Documentation ](#In-Depth-Documentation)
	- [Plugin for the Search API](#Plugin-for-the-Search-API)
		- [Configuration](#Configuration)
		- [Mapping](#Mapping)
			- [Configuring the mapping](#Configuring-the-mapping)
		- [Live import](#Live-import-detail)
			- [Configuration](#Live-import-configuration)
		- [Batch import](#Batch-import)
		- [Import views](#import-views)
		- [File Upload Step](#File-upload-step)
	- [Plugin for the Entitlements check](#Entitlements)
	- [Embedding of the PDFs](#Embedding-of-the-PDFs)
	- [Batch Elsevier items update script](#Batch-Elsevier-items-update-script)
		- [Introduction](#Batch-Introduction)
		- [Usage](#Usage)

# Introduction<a name="Introduction"></a> #
This code repository offers enhancements for DSpace 5 and 6 that leverage PubMed and Elsevier Scopus and ScienceDirect APIs for following use cases:
* Importing publication metadata from ScienceDirect at the start of a new submission through the DSpace XMLUI
* Batch importing several ScienceDirect publications at once, either as archived items or as workflow items.
* Batch enrichment of existing items in the repository through command line scripts.
* Embedded viewing of the publication in the repository. A user can read the publication without leaving the repository.
* See the publication open access status automatically on the repository item page.
* Enhancement of the DSpace file upload step to more easily enter embargo information.

## Watch a demo<a name="Demo"></a> ##

https://youtu.be/ye2V-nU5qbw 

## Conditions for use<a name="Conditions"></a> ##

Using this integration does not require a paying subscription to any of the commercial Elsevier services. The key condition for using this API is that the link to the record of the publication on ScienceDirect should be prominently visible on the publication's page in the repository.

## Contribution to DSpace mainline code<a name="Contribution"></a> ##

This work has been proposed as a contribution to the DSpace mainline code in [https://jira.duraspace.org/browse/DS-2877](https://jira.duraspace.org/browse/DS-2877). Elsevier and Atmire will continue the work to see if these features can be included in new releases of DSpace 6.

The feature builds on a new import framework that was included in DSpace 6.0 [https://jira.duraspace.org/browse/DS-2876](https://jira.duraspace.org/browse/DS-2876). Because this framework was not yet part of DSpace 5, the DSpace 5 patches also contain the inclusion of this framework.

For the time being, please use the patches offered by this repository to install the functionality on the DSpace 5 or 6 repository.

## Branches in this repository<a name="Branches"></a> ##

The dspace_5x and dspace_6x branches in this repository are kept up to date with the DSpace mainline branches with the same name. They contain the very last changes to the official DSpace 5 and DSpace 6 code.

The stable_5x and stable_6x branches contain Atmire's latest patches and developments for the integration functionality. As you will see later in the process, the entire diff of changes between stable_5x and dspace_5x, represents the changes necessary to patch your DSpace 5.

The master branch in this repository is primarily used for managing the README.MD. The code corresponds with a version of stable_6x but may not contain all latest changes on stable_6x.

# Registering a Developer API key<a name="Registering-a-Developer-API-key"></a> #

The functionality will send requests to ScienceDirect APIs to retrieve metadata and links. These APIs are protected with user accounts and keys to avoid abuse by robots and malicious users.

To register for the ScienceDirect API program for institutional repositories, follow this two step approach:

1. To register an API key, go to: [https://dev.elsevier.com/apikey/create](https://dev.elsevier.com/apikey/create). If your institution already has a Scopus API key, you can submit your Scopus API key to have the settings for the ScienceDirect API services added.

2. To register for the institutional repository program and submit your API key, go to: [https://www.elsevier.com/solutions/sciencedirect/forms/institutional-repository-managers?rURL=Direct](https://www.elsevier.com/solutions/sciencedirect/forms/institutional-repository-managers?rURL=Direct).

The Elsevier integration support team will add the ScienceDirect IR settings to your API key, and confirm back. 

Further support for the API key registration process is available from [integrationsupport@elsevier.com](mailto:integrationsupport@elsevier.com)

More information and policies: [http://dev.elsevier.com/ir_cris_vivo.html](http://dev.elsevier.com/ir_cris_vivo.html) 

# Adding the functionality to your DSpace codebase <a name="Patch-installation-procedures"></a> #

## Prerequisites  <a name="Prerequisites"></a> ##

The Elsevier changes have been released as a "patch" for DSpace as this allows for the easiest installation process of the incremental codebase. The code needed to install and deploy the Elsevier changes can be found in the [Obtaining a recent patch file](#Obtaining-recent-patch) section, which needs to be applied to your DSpace source code.

**__Important note__**: Below, we will explain you how to apply the patch to your existing installation. This will affect your source code. Before applying a patch, it is **always** recommended to create backup of your DSpace source code.

In order to apply the patch, you will need to locate the **DSpace source code** on your server. That source code directory contains a directory _dspace_, as well as the following files:  _LICENSE_,  _NOTICE_ ,  _README_ , ....

For every release of DSpace, generally two release packages are available. One package has "src" in its name and the other one doesn't. The difference is that the release labelled "src" contains ALL of the DSpace source code, while the other release retrieves precompiled packages for specific DSpace artifacts from maven central. **The Elsevier patches were designed to work on both "src" and other release packages of DSpace**. 

To be able to install the patch, you will need the following prerequisites:

* A running DSpace 5.x or 6.x instance. 
* Git should be installed on the machine. The patch will be applied using several git commands as indicated in the next section. 

## Obtaining a recent patch file<a name="Obtaining-recent-patch"></a> ##

Atmire's modifications to a standard DSpace for Elsevier are tracked on Github. The newest patch can therefore be generated from git.

DSPACE 6.x [https://github.com/atmire/Elsevier/compare/dspace_6x…stable_6x.patch](https://github.com/atmire/Elsevier/compare/dspace_6x…stable_6x.patch)  
DSPACE 5.x [https://github.com/atmire/Elsevier/compare/dspace_5x…stable_5x.patch](https://github.com/atmire/Elsevier/compare/dspace_5x…stable_5x.patch)  

Save this file under a meaningful name. It will be later referred to as \<patch file\> 

## Patch installation<a name="Patch-Installation"></a> ##

To install the patch, the following steps will need to be performed. 

### 1. Go to the DSpace Source directory.<a name="goto-DSpace-Source"></a> ###

This folder should have a structure similar to:   
dspace  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    config  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    modules  
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;    ...  
pom.xml

### 2. Run the Git command to check whether the patch can be correctly applied.<a name="Run-git-command"></a> ###

Run the following command where \<patch file\> needs to be replaced with the name of the patch:

``` 
git apply --check <patch file>
```

This command will return whether it is possible to apply the patch to your installation. This should pose no problems in case the DSpace is not customized or in case not much customizations are present.   
In case the check is successful, the patch can be installed as explained in the next steps. 


### 3. Apply the patch<a name="Apply-patch"></a> ###

To apply the patch, the following command should be run where \<patch file\> is replaced with the name of the patch file. 

```
git apply <patch file>
```

There may be various warnings about whitespaces, but these will pose no problems when applying the patch and can be ignored. 

### 4. Rebuild and redeploy your repository <a name="Rebuild-redeploy"></a> ###

After the patch has been applied, the repository will need to be rebuild.   
DSpace repositories are typically built using Maven and deployed using Ant. 

### 5. Restart your Tomcat <a name="Restart-tomcat"></a> ###

After the repository has been rebuild and redeployed, Tomcat will need to be restarted to bring the changes to production. 

## Changes to your standard DSpace configuration files<a name="Standard-Configuration"></a> ##

The patch applies a number of changes to standard DSpace configuration files. This overview can help you to disable certain pieces of the patch. Also, if you have modified these configuration files yourself, it is possible that the patch can not automatically apply these changes, so you have to add them in manually.

### Submission step for Live Import <a name="Live-import"></a> ###

To ensure your users see the import source lookup steps at the start of submission, the steps have to be enabled in  *dspace/config/item-submission.xml*. If you have multiple steps early in your submission process, it is important that these steps appear before the Describe Metadata steps. Note that this patch does not support JSPUI, the JSPUI binding refers to the default liveimportstep of JSPUI:

###### DSpace 5 configuration ######

```
<step>
    <heading>submit.progressbar.sourcechoice</heading>
    <processing-class>com.atmire.submit.step.SourceChoiceStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPInitialQuestionsStep</jspui-binding>
    <xmlui-binding>com.atmire.app.xmlui.aspect.submission.submit.SourceChoiceStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
<step>
    <heading>submit.progressbar.liveimport</heading>
    <processing-class>com.atmire.submit.step.LiveImportStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPStartSubmissionLookupStep</jspui-binding>
    <xmlui-binding>org.dspace.app.xmlui.aspect.submission.submit.LiveImportStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
```

###### DSpace 6 configuration ######

```
<step>
    <heading>submit.progressbar.sourcechoice</heading>
    <processing-class>org.dspace.submit.step.SourceChoiceStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPInitialQuestionsStep</jspui-binding>
    <xmlui-binding>org.dspace.app.xmlui.aspect.submission.submit.SourceChoiceStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
<step>
    <heading>submit.progressbar.liveimport</heading>
    <processing-class>org.dspace.submit.step.LiveImportStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPStartSubmissionLookupStep</jspui-binding>
    <xmlui-binding>org.dspace.app.xmlui.aspect.submission.submit.LiveImportStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
```

### Input Form fields <a name="Form-fields"></a> ###

To ensure that users can see the metadata fields used by the integration, they should be added to input-forms.xml. Example:

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

### xmlui.xconf aspect <a name="XMLUI-XCONF"></a> ###

The aspect that allows for the metadata to be imported from ScienceDirect needs to be enabled in *dspace/config/xmlui.xconf*.

```
<aspect name="ScienceDirect" path="resource://aspects/ScienceDirect/" />
```

### Improved upload step with file permissions <a name="File-permissions-in-the-upload-step"></a> ###

The automatic suggestion of the file permissions should be enabled in the *dspace/config/item-submission.xml*. The step below should replace the default upload step:

###### DSpace 5 configuration ######

```       
<step>
    <heading>submit.progressbar.upload</heading>
    <processing-class>com.atmire.dspace.submit.step.ElsevierUploadStep</processing-class>
    <xmlui-binding>com.atmire.dspace.app.xmlui.aspect.submission.submit.ElsevierUploadStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
```

###### DSpace 6 configuration ######

```       
<step>
    <heading>submit.progressbar.upload</heading>
    <processing-class>org.dspace.submit.step.ElsevierUploadStep</processing-class>
    <xmlui-binding>org.dspace.app.xmlui.aspect.submission.submit.ElsevierUploadStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
```
### Optionally: Disabling Scopus import functionality <a name="Scopus-Disable"></a> ###

If you are not a Scopus subscriber, the scopus import functionality will return no results. To hide the scopus option from the import source selection menus, you can alter the configuration as follows:

1. Go to the file config/spring/api/general-import-services.xml
2. Remove the line with "ScopusImportService" from the util map "ImportServices"

# In-Depth Documentation <a name="In-Depth-Documentation"></a> #

In-Depth documentation about the APIs is available from Elsevier at http://dev.elsevier.com/tecdoc_sd_ir_integration.html

## Plugin for the Search API <a name="Plugin-for-the-Search-API"></a> ##

### Configuration <a name="Configuration"></a> ###

The basic API configuration can be found in file *dspace/config/modules/elsevier-sciencedirect.cfg*. This file contains the API key and the API urls. You can either fill out the API key manually in this file, or include in your maven/puppet/ansible configuration profiles.

###### DSpace 5 configuration ######

```
# Api key to be able to make the calls to retrieve the articles, this will need to be requested by the appropriate instance
api.key = ${elsevier.api.key}

# This represents the base url to use for the retrieval of an article
api.article.url=http://api.elsevier.com/content/article
# The base of rest endpoints to represent identifiers and entitlement status associated with requested full text articles
api.entitlement.url=http://api.elsevier.com/content/article/entitlement
# The search interfaces associated with ScienceDirect
api.scidir.url=http://api.elsevier.com/content/search/scidir
# The search interfaces associated with pubmed
api.pubmed.url=https://eutils.ncbi.nlm.nih.gov/entrez/eutils/
# The search interfaces associated with Scopus
api.scopus.url=http://api.elsevier.com/content/search/scopus
# The scopus search view. The view can be either STANDARD or COMPLETE.
api.scopus.view = STANDARD
# Url to base later rest calls on, such as retrieval based on PII etc
ui.article.url=http://www.sciencedirect.com/science/article
# Check statuses associated with the requested articles
entitlement.check.enabled=true
```

###### DSpace 6 configuration ######

```
# Api key to be able to make the calls to retrieve the articles, this will need to be requested by the appropriate instance
elsevier-sciencedirect.api.key = ${elsevier.api.key}

# This represents the base url to use for the retrieval of an article
elsevier-sciencedirect.api.article.url=http://api.elsevier.com/content/article
# The base of rest endpoints to represent identifiers and entitlement status associated with requested full text articles
elsevier-sciencedirect.api.entitlement.url=http://api.elsevier.com/content/article/entitlement
# The search interfaces associated with ScienceDirect
elsevier-sciencedirect.api.scidir.url=http://api.elsevier.com/content/search/scidir
# The search interfaces associated with pubmed
elsevier-sciencedirect.api.pubmed.url=https://eutils.ncbi.nlm.nih.gov/entrez/eutils/
# The search interfaces associated with Scopus
elsevier-sciencedirect.api.scopus.url=http://api.elsevier.com/content/search/scopus
# The scopus search view. The view can be either STANDARD or COMPLETE.
elsevier-sciencedirect.api.scopus.view = STANDARD
# Url to base later rest calls on, such as retrieval based on PII etc
elsevier-sciencedirect.ui.article.url=http://www.sciencedirect.com/science/article
# Check statuses associated with the requested articles
elsevier-sciencedirect.entitlement.check.enabled=true
```

### Mapping <a name="Mapping"></a> ###

The file *dspace/config/spring/api/scidir-service.xml* contains the spring configuration for the beans used by the Elsevier service.

Part of this configuration is the mapping of Science Direct fields to dspace metadata fields. 

#### Configuring the mapping <a name="Configuring-the-mapping"></a> ####


Each DSpace metadata field that will be used for the mapping must first be configured as a spring bean of class *org.dspace.importer.external.metadatamapping.MetadataFieldConfig*.

###### DSpace 5 configuration ######

```
<bean id="dc.title" class="com.atmire.import_citations.configuration.metadatamapping.MetadataField">
    <constructor-arg value="dc.title"/>
</bean>
```

###### DSpace 6 configuration ######

```
<bean id="dc.title" class="org.dspace.importer.external.metadatamapping.MetadataFieldConfig">
    <constructor-arg value="dc.title"/>
</bean>
```

Hereafter this metadata field can be used to create a mapping. To add a mapping for the "dc.title" field declared above, a new spring bean configuration of class *org.dspace.importer.external.metadatamapping.contributor.SimpleXpathMetadatumContributor* needs to be added. This bean expects 2 property values:

* field: A reference to the configured spring bean of the DSpace metadata field. e.g. the "dc.title" bean declared above. 
* query: The xpath expression used to select the Elsevier value from the XML returned by the Elsevier API. The root for the xpath query is the "entry" element. 

###### DSpace 5 configuration ######

```
<bean id="titleContrib" class="com.atmire.import_citations.configuration.metadatamapping.SimpleXpathMetadatumContributor">
    <property name="field" ref="dc.title"/>
    <property name="query" value="dc:title"/>
</bean>
```

###### DSpace 6 configuration ######

```
<bean id="titleContrib" class="org.dspace.importer.external.metadatamapping.contributor.SimpleXpathMetadatumContributor">
    <property name="field" ref="dc.title"/>
    <property name="query" value="dc:title"/>
</bean>
```

This is a (shortened) example of the XML of an entry returned by the elsevier API:

```
<entry>
  <dc:title>
    Integrating phenotypic small-molecule profiling and human genetics: the next phase in drug discovery
  </dc:title>
  <authors>
    <author>
      <given-name>Cory M.</given-name>
      <surname>Johannessen</surname>
    </author>
  </authors>
</entry>
```

Because the given-name and surname of an author are contained in one metadata field value in DSpace, multiple Elsevier fields can also be combined into one value. To implement a combined mapping first create a "*SimpleXpathMetadatumContributor*" as explained above for each part of the field. 

###### DSpace 5 configuration ######

```
<bean id="lastNameContrib" class="com.atmire.import_citations.configuration.metadatamapping.SimpleXpathMetadatumContributor">
    <property name="field" ref="dc.contributor.author"/>
    <property name="query" value="x:authors/x:author/x:surname"/>
</bean>

<bean id="firstNameContrib" class="com.atmire.import_citations.configuration.metadatamapping.SimpleXpathMetadatumContributor">
    <property name="field" ref="dc.contributor.author"/>
    <property name="query" value="x:authors/x:author/x:given-name"/>
</bean>
```

###### DSpace 6 configuration ######

```
<bean id="lastNameContrib" class="org.dspace.importer.external.metadatamapping.contributor.SimpleXpathMetadatumContributor">
    <property name="field" ref="dc.contributor.author"/>
    <property name="query" value="x:authors/x:author/x:surname"/>
</bean>
<bean id="firstNameContrib" class="org.dspace.importer.external.metadatamapping.contributor.SimpleXpathMetadatumContributor">
    <property name="field" ref="dc.contributor.author"/>
    <property name="query" value="x:authors/x:author/x:given-name"/>
</bean>
```

Note that for elements without namespace, namespace "x" is appended. This is the default namespace. The namespace configuration can be found in map "FullprefixMapping" in the same spring configuration file. 

Then create a new list in the spring configuration containing references to all "*SimpleXpathMetadatumContributor*" beans that need to be combined.

###### DSpace 5 configuration ######

```
<util:list id="combinedauthorList" value-type="com.atmire.import_citations.configuration.metadatamapping.MetadataContributor" list-class="java.util.LinkedList">
    <ref bean="lastNameContrib"/>
    <ref bean="firstNameContrib"/>
</util:list>
```

###### DSpace 6 configuration ######

```
<util:list id="combinedauthorList" value-type="org.dspace.importer.external.metadatamapping.contributor.org.dspace.importer.external.metadatamapping.contributor.MetadataContributor" list-class="java.util.LinkedList">
    <ref bean="lastNameContrib"/>
    <ref bean="firstNameContrib"/>
</util:list>
```

Finally create a spring bean configuration of class *org.dspace.importer.external.metadatamapping.contributor.CombinedMetadatumContributor*. This bean expects 3 values:

* field: A reference to the configured spring bean of the DSpace metadata field. e.g. the "dc.title" bean declared above. 
* metadatumContributors: A reference to the list containing all the single Elsevier field mappings that need to be combined. 
* separator: These characters will be added between each Elsevier field value when they are combined into one field. 

###### DSpace 5 configuration ######

```
<bean id="authorContrib" class="com.atmire.import_citations.configuration.metadatamapping.CombinedMetadatumContributor">
    <property name="separator" value=", "/>
    <property name="metadatumContributors" ref="combinedauthorList"/>
    <property name="field" ref="dc.contributor.author"/>
</bean>
```

###### DSpace 6 configuration ######

```
<bean id="authorContrib" class="org.dspace.importer.external.metadatamapping.contributor.CombinedMetadatumContributor">
    <property name="separator" value=", "/>
    <property name="metadatumContributors" ref="combinedauthorList"/>
    <property name="field" ref="dc.contributor.author"/>
</bean>
```

Each contributor must also be added to the "scidirMetadataFieldMap" map in the same spring configuration file. Each entry of this map maps a metadata field bean to a contributor. For the contributors created above this results in the following configuration:

###### DSpace 5 configuration ######

```
<util:map id="scidirMetadataFieldMap" key-type="com.atmire.import_citations.MetadataField"
          value-type="com.atmire.import_citations.configuration.metadatamapping.MetadataContributor">
    <entry key-ref="dc.title" value-ref="titleContrib"/>
    <entry key-ref="dc.contributor.author" value-ref="authorContrib"/>
</util:map>

```

###### DSpace 6 configuration ######

```
<util:map id="scidirMetadataFieldMap" key-type="org.dspace.importer.external.metadatamapping.MetadataFieldConfig"
          value-type="org.dspace.importer.external.metadatamapping.contributor.MetadataContributor">
    <entry key-ref="dc.title" value-ref="titleContrib"/>
    <entry key-ref="dc.contributor.author" value-ref="authorContrib"/>
</util:map>

```

Note that the single field mappings used for the combined author mapping are not added to this list. 

### Live import <a name="Live-import-detail"></a> ###

The first submission step is the "import source choice" step, here the user can choose from which import source they would like to import an item. At the moment, there are 3 available import sources: ScienceDirect, Scopus and PubMed.

![ImportSourceChoice](../../raw/master/images/elsevier-import-choice-step.png "ImportSourceChoice")
This step can be skipped by clicking "Skip Import" or not selecting an import source and clicking "Next" at the bottom of the page. 

The search step allows the user to import a publication from the previously chosen import source. 

![Liveimport](../../raw/master/images/elsevier-import-step.png "Liveimport")

To search for a publication to import fill in at least one of the 4 search fields and click on "Search". A new window will appear containing the search results. To import a publication click on the "Import" button next to it.

Publications that are already imported are shown with a gray background. 

![Liveimport](../../raw/master/images/elsevier-liveimport-2.png "Liveimport")

When the publication is imported its title and authors are shown at the bottom of the Elsevier import step:

![Liveimport](../../raw/master/images/elsevier-liveimport-3.png "Liveimport")

Note that importing a publication removes any fields that were already added to the item. 

#### Configuration <a name="Live-import-Configuration"></a> ####

To enable the import source steps, add the following 2 steps to the submission-process in *dspace/config/item-submission.xml*.

###### DSpace 5 configuration ######

```
<step>
    <heading>submit.progressbar.sourcechoice</heading>
    <processing-class>com.atmire.submit.step.SourceChoiceStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPInitialQuestionsStep</jspui-binding>
    <xmlui-binding>com.atmire.app.xmlui.aspect.submission.submit.SourceChoiceStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>

<step>
    <heading>submit.progressbar.liveimport</heading>
    <processing-class>com.atmire.submit.step.LiveImportStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPStartSubmissionLookupStep</jspui-binding>
    <xmlui-binding>com.atmire.app.xmlui.aspect.submission.submit.LiveImportStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
```

###### DSpace 6 configuration ######

```
<step>
    <heading>submit.progressbar.sourcechoice</heading>
    <processing-class>org.dspace.submit.step.SourceChoiceStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPInitialQuestionsStep</jspui-binding>
    <xmlui-binding>org.dspace.app.xmlui.aspect.submission.submit.SourceChoiceStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
<step>
    <heading>submit.progressbar.liveimport</heading>
    <processing-class>org.dspace.submit.step.LiveImportStep</processing-class>
    <jspui-binding>org.dspace.app.webui.submit.step.JSPStartSubmissionLookupStep</jspui-binding>
    <xmlui-binding>org.dspace.app.xmlui.aspect.submission.submit.LiveImportStep</xmlui-binding>
    <workflow-editable>true</workflow-editable>
</step>
```

### Batch import <a name="Batch-import"></a> ###

Import multiple publications from Elsevier using the batch import.

The batch import page can be found by clicking on "Source Import" ("Elsevier import" in DSpace 5) in the administrative menu, or by browsing to *{dspace-url}/liveimport.*

Start by selecting the import source to import items from. At the moment, there are 3 available import sources: ScienceDirect, Scopus and PubMed.

![Elsevier Batch Import](../../raw/master/images/elsevier-batchimport-4.png "Elsevier Batch Import")

Fill in at least one of the 4 search fields to query the selected import source for publications, then click on "Search".

![Elsevier Batch Import](../../raw/master/images/elsevier-batchimport-1.png "Elsevier Batch Import")

A list of the publications returned by the source will be shown. Next to each publication is a checkbox which can be clicked to select the publication for import. Under the publications list a counter shows how many publications are already selected for import. This counter is updated each time the user browses through the publications. 

![Elsevier Batch Import](../../raw/master/images/elsevier-batchimport-2.png "Elsevier Batch Import")


When "Next" is clicked the user is taken to the import page. At the top of this page all publications that are selected for import are listed. 

One of the "Select action" options must be chosen to specify what will happen to the imported items:

* Send imported items to workspace:  The items are added to the users "Unfinished submissions" on the submission page. 
* Send imported items to workflow: The items are added to the workflow to be reviewed by the reviewers of the collection the item is added to. 
* Archive imported items: The items are archived immediately. 

A collection to which the items are added must be selected from the "Select collection" dropdown. Click on "Import" to start the import.

![Elsevier Batch Import](../../raw/master/images/elsevier-batchimport-3.png "Elsevier Batch Import")

### Import views <a name="import-views"></a> ###

The Scopus import source supports a standard and a complete view. If your Elsevier API key allows the use of the Scopus complete view, activate it in the elsevier-sciencedirect.cfg configuration by replacing "STANDARD" with "COMPLETE":

###### DSpace 5 configuration ######

```
# The scopus search view. The view can be either STANDARD or COMPLETE.
api.scopus.view = COMPLETE
```

###### DSpace 6 configuration ######

```
# The scopus search view. The view can be either STANDARD or COMPLETE.
elsevier-sciencedirect.api.scopus.view = COMPLETE
```

The Scopus complete view will expose more article fields than the standard view. 

According to the Scopus policies some of these fields are not allowed to be displayed publicly. These fields are also hidden in DSpace using the "metadata.hide.*" configuration in dspace.cfg.

##### For example: #####

Scopus abstracts are mapped to DSpace metadata field elsevier.description.scopusabstract, this field is configured in dspace/config/dspace.cfg to only be visible to DSpace administrators, just like the default dc.description.provenance field:

```
metadata.hide.elsevier.description.scopusabstract = true
```

### File Upload Step <a name="File-Upload-Step"></a> ###

The file upload step has been altered to allow people to select the accessibility of files, it can be restricted from users, placed under embargo so it's not available until a specified date, or simply be made regularly available.

![Elsevier Batch Import](../../raw/master/images/elsevier-file-upload.png "Elsevier Batch Import")

If you encounter the warning message below in the DSpace logs, please verify whether the permissions of your API key are sufficient to retrieving the hosting permissions:

Error retrieving required nodes from the response, please verify whether your ScienceDirect API key has sufficient permissions: <service-error>    <status>                <statusCode>AUTHORIZATION_ERROR</statusCode>        <statusText>APIKey XXXX with IP address X.X.X.X is unrecognized or has insufficient privileges for access to this resource</statusText>       </status></service-error>

## Plugin for the Entitlements check<a name="Entitlements"></a> ##

The Elsevier entitlements API (http://api.elsevier.com/content/article/entitlement) is called asynchronously through java script after an item page is loaded, to display open access status and access information on the item page.


This API relies on following identifier field mapping in *${dspace.dir}/config/modules/elsevier-sciencedirect.cfg*.

###### DSpace 5 configuration ######

```
# Identifiers used to perform the entitlements API retrieval and embed page retrieval.
metadata.field.pii = elsevier.identifier.pii
metadata.field.doi = elsevier.identifier.doi
metadata.field.eid = elsevier.identifier.eid
metadata.field.scopus_id = elsevier.identifier.scopusid
metadata.field.pubmed_id = elsevier.identifier.pubmedid
```

###### DSpace 6 configuration ######

```
# Identifiers used to perform the entitlements API retrieval and embed page retrieval.
elsevier-sciencedirect.metadata.field.pii = elsevier.identifier.pii
elsevier-sciencedirect.metadata.field.doi = elsevier.identifier.doi
elsevier-sciencedirect.metadata.field.eid = elsevier.identifier.eid
elsevier-sciencedirect.metadata.field.scopus_id = elsevier.identifier.scopusid
elsevier-sciencedirect.metadata.field.pubmed_id = elsevier.identifier.pubmedid
```
The check will take following orders into account and will only check the next values if the "previous" one isn't found. ("elsevier-sciencedirect.metadata.field." prefix implied in the following list)
* pii -> This value is used as is in the entitlements call.
* doi -> A check is done to see if this identifier starts with "10.1016", if not, its ignored. The identifier might start with "DOI:" but this is removed before the previous check.
* eid -> This value is used as is in the entitlements call
* (IF the value in the item's dc.publisher starts with elsevier, otherwise the following are ignored).
 * scopus_id -> Checks the identifier if it starts with "SCOPUS_ID:" and parses this accordingly. 
 * pubmed_id -> This value is used as in in the entitlement call.


If you experience CORS related problems, please see following note copied from http://dev.elsevier.com/tecdoc_sd_ir_integration.html

Important Note: The Article Entitlement Retrieval API should be called from user's browser by integrating it directly with the IR's web page via JavaScript. In order to comply with cross-origin security policy, we will ensure that your IR's domain is added to the API key configuration to enable W3C CORS support. You can request to add IR domains by emailing Elsevier integrationsupport your API key and a list of authorized domains.

## Embedding of the PDFs <a name="Embedding-of-the-PDFs"></a> ##

The PDF is collected via the identifier (as configured above) and shown in an embedded reader.
Note that if the entitlement check fails, meaning that the access is denied, or simply invalid. A link to the actual article will be displayed, but this is only present for PII and DOI because there are no other persistent linking methods for eid, scopus_id and pubmed_id using the sciencedirect API.

Can be defined in the *${dspace.dir}/config/modules/elsevier-sciencedirect.cfg*:

###### DSpace 5 configuration ######

```
# Whether or not to embed the display + its respective width and height
embed.display=true
embed.display.width=700
embed.display.height=500
```

###### DSpace 6 configuration ######

```
# Whether or not to embed the display + its respective width and height
elsevier-sciencedirect.embed.display=false
elsevier-sciencedirect.embed.display.width=700
elsevier-sciencedirect.embed.display.height=500
```

If width or height are set to '0', they default to the values shown here (700 and 500 respectively).

Apart from the settings mentioned above, the embed also requires the api key from the entitlement config (mentioned above).

A link is shown on the item page which links to a page with an embedded PDF viewer (from the browser).

![Elsevier Entitlements Embed](../../raw/master/images/elsevier-entitlements-embed.png "Elsevier Entitlements Embed")

For Mirage based themes the position of the link to the publisher version of the document can be configured in *${dspace.dir}/config/modules/elsevier-sciencedirect.cfg.*

Set "embed.link.position" to "top" to render the link above the file section, or set it to "bottom" to render the link under the file section.

###### DSpace 5 configuration ######

```
# Define if the link to the embed display should be rendered above (top) or under (bottom) the file section on the item page.
# Only supported by theme Mirage
embed.link.position = top
```

###### DSpace 6 configuration ######

```
# Define if the link to the embed display should be rendered above (top) or under (bottom) the file section on the item page.
# Only supported by theme Mirage
elsevier-sciencedirect.embed.link.position = top
```

## Batch Elsevier items update script <a name="Batch-Elsevier-items-update-script"></a> ##

### Introduction <a name="Batch-Introduction"></a> ###

To accommodate for changes  of previously imported items, an update scripts has been created.
This scripts enables the possibility to use a PII or DOI to re-check an item for possible updates in file permissions, Identifiers and Metadata. This check is done against the originally used [Search API](#Plugin-for-the-Search-API) and uses the same [Configuration](#Configuration).

### Usage <a name="Usage"></a> ###


The script can be run using the following command in the dspace installation directory.

###### DSpace 5 configuration ######

```
bin/dspace dsrun com.atmire.script.UpdateElsevierItems (options)
```

###### DSpace 6 configuration ######

```
bin/dspace dsrun org.dspace.importer.external.scidir.UpdateElsevierItems (options)
```

These options can be used to specify what has to be updated or not

* -t: test -> Only test the changes done by the script if this option is given.

* -f: forcefully update the requested type of data

* -a: assign pii: 
    * If the PII exists (and not testing), update the item metadata to include the PII
    * If “-f” is enabled, also verify the items with a DOI and a PII
        * If a PII exists in the API, and differs from the current PII, update the PII (if not testing)
        * If a PII exists in the AP, and is identical to the current PII, leave it unchanged
        * If the PII doesn’t exist in the API, but the current metadata contains a PII, remove it (if not testing)

* -p: update permissions
    * If not testing, and this type of data should be updated, adjust the file permissions applied to the item if:
        * “-f” is enabled
        * or the permissions were not manually overruled during the submission or workflow

* -m: import metadata,
    * If not forced, this task won’t do anything. 
    * If “-f” is enabled:
        * update the metadata fields retrieved from the API, leaving the other metadata fields unchanged (based on the current configuration of metadata fields)
        * Don’t update any metadata fields if they are all correct
        * Keep in mind that any manual additions to the configured metadata fields will be overruled

* -i: item handle 
    * If this option is given, only run the script for this specific item
    * It this option is omitted, run the script for all archived items

These options are not mutually exclusive, meaning that all updates can be run simultaneously.

An example of the script would be to forcefully test all updates on metadata,pii/doi,permissions for the item with handle 123456789/99

###### DSpace 5 configuration ######

```
./dspace dsrun com.atmire.script.UpdateElsevierItems -t -p -a -m -f -i 123456789/99
```

###### DSpace 6 configuration ######

```
./dspace dsrun org.dspace.importer.external.scidir.UpdateElsevierItems -t -p -a -m -f -i 123456789/99
```

This will result in the changes being shown in the command line. An example of these changes would be 

```
permission of bitstream with id 59d3ccc7-3ce4-472f-98b5-f74b68fef9d8 would be updated to audience Public start date 2016-06-10
permission of bitstream with id d94f0a08-b1b6-4354-897a-afe1f14375e2 would be updated to audience Public start date 2016-06-10
pii for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f would be removed
metadata dc.identifier would be updated with value DOI:10.1016/j.jnutbio.2015.02.010 for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f
metadata elsevier.identifier.eid would be updated with value 1-s2.0-S0955286315000716 for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f
metadata dc.identifier would be updated with value 8 for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f
metadata dc.format.extent would be updated with value 817 for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f
metadata dc.date.available would be updated with value August 2015 for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f
metadata dc.rights would be updated with value for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f
metadata dc.type would be updated with value Research Article for item with id 2fea9fff-90b0-46f7-9429-064bf35d173f
```

If these changes are to be applied to the item, the -t option can be left out and the item would then be actually updated
