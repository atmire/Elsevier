# Prerequisites

## Codebase

The Elsevier API integration has been released as a "patch" for DSpace as this allows for the easiest installation process of the incremental codebase. The code needed to install and deploy the integration can be found in the [Download patch](#prerequisites-download) section, which needs to be applied to your DSpace source code.

!> Below, we will explain you how to apply the patch to your existing installation. This will affect your source code. Before applying a patch, it is **always** recommended to create backup of your DSpace source code.

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

## Firewall verification

The server should be able to reach Elsevier and PubMed. This can be verified easily using the following commands:

```
curl -Is http://api.elsevier.com/ | head -1
curl -Is http://eutils.ncbi.nlm.nih.gov/ | head -1 
```

If *any* HTTP status code is returned you can reach the specified URL properly (even though it's a 403 or 500). If nothing is returned, the server is not able to reach the URL. Please verify whether the firewall is configured to authorize outgoing connections.

## Download patch

!> This work has been proposed as a contribution to the DSpace mainline code in [DS-2877](https://jira.duraspace.org/browse/DS-2877). This patch builds on a new import framework that was included in DSpace 6.0 (see [DS-2876](https://jira.duraspace.org/browse/DS-2876)). Because this framework was not yet part of DSpace 5, the DSpace 5 patch also contains the inclusion of this framework.
<br /><br />For the time being, please use the patches offered by this repository to install the functionality.

Atmire's modifications to a standard DSpace for Elsevier are tracked on Github. The newest patch can therefore be generated from git.

| DSpace | Patch                                                                              |
| ------ | ---------------------------------------------------------------------------------- |
| 5.x    | [Download](https://github.com/atmire/Elsevier/compare/dspace_5x...stable_5x.patch) |
| 6.x    | [Download](https://github.com/atmire/Elsevier/compare/dspace_6x...stable_6x.patch) |


Save this file under a meaningful name. It will later be referred to as `<patch>`.

## Elsevier API key

The integration will send requests to Elsevier's APIs to retrieve metadata and links from Scopus and ScienceDirect. These APIs are protected with user accounts and keys to avoid abuse by robots and malicious users. To register for an API key, follow this two step approach:

1. Get hold of an API key. Go to [https://dev.elsevier.com/apikey/create](https://dev.elsevier.com/apikey/create) to create a new key. If your institution already has a Scopus API key, you can submit your Scopus API key to have the settings for the ScienceDirect API services added.

1. Register for the Institutional Repository Program. Go to [https://www.elsevier.com/solutions/sciencedirect/forms/institutional-repository-managers?rURL=Direct](https://www.elsevier.com/solutions/sciencedirect/forms/institutional-repository-managers?rURL=Direct) to submit your API key.

1. The Elsevier integration support team will add the ScienceDirect Institutional Repository settings to your API key and confirm back.

Further support for the API key registration process is available from [integrationsupport@elsevier.com](mailto:integrationsupport@elsevier.com)

More information about the Institutional Repository Program and the corresponding policies can be found on [http://dev.elsevier.com/ir_cris_vivo.html](http://dev.elsevier.com/ir_cris_vivo.html)

## HTTPS support: April 2017 status

Reports related to malfunctioning HTTPS calls are currently under investigation. The patch should be fully functional on HTTP. If you run into any HTTPS or SSL certificate related errors, please report them by creating a Github Issue.