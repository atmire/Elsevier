# CHANGES

The releases are tagged with the date of the release. Currently, there are no version numbers.

## Unreleased

Unreleased changes can be found on the stable_5x and stable_6x branches

- Breaking change: ...
- API fix: ...
- API change: ...
- Fix: ...
- Validation: ...
- Testing improvements: ...

## August 1th, 2018 

patch for DSpace version 5.9 and 6.3:
- API fix: Updated the ScienceDirect DSpace backend to support the latest changes in the ScienceDirect API.
- Fix: Fixed a bug in the maven dependencies that caused a dependency conflict with the SWORD V2 DSpace webapp
- Fix: Improved the behaviour of the submission modal lookup window. It will now always be centered in the screen. 
- Fix: Fixed the “Next Results” and “Previous Results” buttons in the submission modal lookup window.
- Improvement: Added support for i18n messages to the submission modal lookup window texts. 

## March 14th, 2018

- Fix: On theme Mirage 2 search results in the live import submission step now shows a link to the source record.
- Fix: On theme Mirage 2 the search results dialog in the live import submission step now shows a clearer message when no results are found.
- Fix: On theme Mirage 2 a loading icon is shown inside the search button on the live import submission step while a search is being performed.
- API change: All API calls to Elsevier services now go over HTTPS instead of HTTP.

## September 1st, 2017

- Fix: Raised the default height to 800px and the default width to 100% for the Elsevier embed page.
- API fix: Updated the URL that is used for the Scopus import.

## April 6th, 2017

- API change: Enabled mapping of metadata fields that are only exposed in the ScienceDirect complete view.
- API change: Multiple DOI fields can now be configured for the Entitlements lookups.
- API fix: Updated the PubMed import record mapping to be able to map an abstract text that is divided into multiple parts.
- API fix: Updated the PubMed import record mapping to be able to map a DOI from multiple pubmed fields. 
- Fix: Entitlements API integration and links now also work when the Article Embedding API is disabled.

## March 24th, 2017 (DSpace 5 only)

- API change: Accepted Manuscripts are shown on the embed page if the user is not entitled to the full PDF and the record is not under embargo.
- Fix: Fixed a bug in the bulk import layout for Mirage 1 that caused some checkboxes to be rendered on the right side of the page.

## February 24th, 2017

- API change: Import source URL configuration for Scopus and Pubmed has been moved to config file elsevier-sciencedirect.cfg
- API change: Scopus import source view configuration has been moved to config file elsevier-sciencedirect.cfg
- API change: Scopus abstracts are mapped to DSpace metadata field elsevier.description.scopusabstract
- API fix: According to the Scopus policies the Scopus abstracts are not allowed to be displayed publicly. Scopus abstracts are hidden for all users except DSpace administrators. 

## February 22th, 2017

- API change: Support separating a record value from an import source into multiple DSpace metadata values
- API change: Extra logging in import sources
- Fix: Java class ThemeResourceReader is now included in the patch to prevent errors in DSpace 5.4 to 5.0

## February 3rd, 2017

- API change: Added a batch item import step to select the import source
- API change: Added support for batch item imports from Scopus and PubMed

## January 30th, 2017

- API change: Added a submission step to select the import source
- API change: Added support for item imports from Scopus and PubMed
- API change: Added support for entitlement checks by Scopus ID, PubMed ID or DOI
- API change: Added support for the embedded view of items with a Scopus ID, PubMed ID or DOI
- Fix: The "View publisher version" link on the item page is hidden when the publisher url is not available
- Fix: Updated the encoding of search queries to the import sources to encode spaces correctly

## December 9th, 2016

- API change: The entitlement check now differentiates between "Open Access" and “You have access”
- API change: On theme Mirage the entitlements check now also appears on the full item view
- Fix: The access option selected in the submission upload step is now applied correctly
