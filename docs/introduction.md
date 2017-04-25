# DSpace Elsevier patch

## Introduction

[Elsevier](https://www.elsevier.com) is a global information analytics company that helps institutions and professionals progress science, advance healthcare and improve performance. This patch offers enhancements for DSpace 5 and 6 that leverage Elsevier's APIs for ScienceDirect, Scopus and PubMed for following use cases:

* Importing publication metadata at the start of a new submission
* Batch importing several publications at once, either as archived items or as workflow items
* Batch enrichment of existing items in the repository through command line scripts
* Embedded viewing of the publication in the repository. A user can read the publication without leaving the repository.
* See a publication's open access status automatically on the repository item page
* Enhancement of the DSpace file upload step to more easily enter embargo information

You can watch a [demo](https://youtu.be/ye2V-nU5qbw) showcasing some of the features.

The DSpace Elsevier API integration patch has been developed and is maintained by [Atmire](https://www.atmire.com), a registered service provider for DSpace.

## Conditions for use

Using this integration does not require a paid subscription to any of Elsevier's commercial services. The key condition for using this API is that the link to the record of the publication on ScienceDirect/Scopus/PubMed should be prominently visible on the publication's page in the repository.

Full details on the use of the leveraged APIs are available on their respective pages:

* Elsevier: https://dev.elsevier.com/ir_cris_vivo.html
* PubMed: https://www.ncbi.nlm.nih.gov/books/NBK25497/#chapter2.Usage_Guidelines_and_Requiremen

## Postcardware

You're free to use this patch (see DSpace source code [license](https://github.com/atmire/Elsevier/blob/master/LICENSE)), but if it makes it to your production environment we highly appreciate you sending us a postcard from your hometown, mentioning which of our patches you are using.

Our addresses are (pick one):

| Belgian office (HQ) | US office |
|---------------------|-----------|
| Atmire NV<br>Gaston Geenslaan 14<br>3001 Leuven<br>Belgium | Atmire Inc.<br>250 Lucius Gordon Drive, Suite B-3A<br>West Henrietta, NY 14586<br>U.S.A. |

## About Atmire

Atmire is a registered service provider for DSpace. Our [services](https://www.atmire.com/services) include DSpace installations, customizations, general support & hosting. We also offer several  homegrown [licensed modules](https://www.atmire.com/modules). Please find an overview of all our activities [on our website](https://www.atmire.com).
