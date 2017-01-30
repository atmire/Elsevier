/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
(function ($) {


    function publisherVersionVisibility(showPublisherVersion) {
        if (showPublisherVersion) {
            $('.publiserVersionLink').removeClass("hidden");
            $('.nonPublisherViewerLink').addClass("hidden");
            $('a.no_accessThumbnailLinking')
            $('a.embeddedViewOpenLink').attr('href',$('a.no_accessThumbnailLinking').attr('href'));
            $('a.embeddedViewOpenLink').removeClass("hidden");

        } else {
            $('.nonPublisherViewerLink').removeClass("hidden");
            // Don't link to the embedded page if the entitlement check has failed
            $('a.embeddedViewOpenLink').addClass("hidden");
            $('a.no_accessThumbnailLinking').removeAttr("href");
        }
    }
    $(document).ready(function () {
        var url;
        var params;
        var DSpace;
        var entitlementLink;
        var wrapper;

        DSpace = window.DSpace;
        url = DSpace.elsevier_entitlement_url; // is undefined if entitlement checks are disabled
        if(url) {
            wrapper = $('.entitlement-wrapper');
            entitlementLink = $('.entitlement-link');

            url = url.replace('http:', '');
            params = {
                apiKey: DSpace.elsevier_apikey,
                httpAccept: 'application/json'
            };

            var doCall = false;
            var showPublisherVersion = false;
            if (DSpace.item_pii) {
                doCall = true;
                url += '/pii/' + DSpace.item_pii;
                showPublisherVersion=true;
            } else if (DSpace.item_eid) {
                doCall = true;
                url += '/eid/' + DSpace.item_eid;
            }else if (DSpace.item_doi) {
                doCall = true;
                url += '/doi/' + DSpace.item_doi;
                showPublisherVersion=true;
            }else if (DSpace.item_scopus_id) {
                doCall = true;
                url += '/scopus_id/' + DSpace.item_scopus_id;
            }else if (DSpace.item_pubmed_id) {
                doCall = true;
                url += '/pubmed_id/' + DSpace.item_pubmed_id;
            }

            if (doCall) {

                function handleSuccess(response) {
                    var document = response['entitlement-response']['document-entitlement'];
                    var entitledString = String(document['entitled']);
                    var link = document['link']['@href'];

                    if (entitledString === 'true' || entitledString === 'open_access') {
                        // If entitled-> Always show publisher version
                        $('.publiserVersionLink').removeClass("hidden");
                        $('.nonPublisherViewerLink').removeClass("hidden");
                        $('.embeddedViewOpenLink').removeClass("hidden");

                        $('#elsevier-embed-wrapper').find('.noaccess').addClass("hidden");
                        var access = $('#elsevier-embed-wrapper').find('.access');
                        access.removeClass("hidden");
                        if (entitledString === 'open_access') {
                            access.find('.open-access').removeClass("hidden");
                            access.find('.full-text-access').addClass("hidden");
                        } else if (entitledString === 'true') {
                            access.find('.open-access').addClass("hidden");
                            access.find('.full-text-access').removeClass("hidden");
                        }
                    } else{
                        // If not entitled, use "default" visibility
                        publisherVersionVisibility(showPublisherVersion);
                    }
                }

                function handleError(response) {
                    publisherVersionVisibility(showPublisherVersion);
                }

                $.ajax({
                    dataType: 'json',
                    url: url,
                    data: params,
                    success: handleSuccess,
                     error: handleError
                });
            }
        }

    });
})(jQuery);