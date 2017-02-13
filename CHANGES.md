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