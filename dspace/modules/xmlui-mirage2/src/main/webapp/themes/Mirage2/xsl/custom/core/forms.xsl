<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->

<!--
    Templates to cover the forms and forms fields.

    Author: art.lowel at atmire.com
    Author: lieven.droogmans at atmire.com
    Author: ben at atmire.com
    Author: Alexey Maslov

-->

<xsl:stylesheet xmlns:i18n="http://apache.org/cocoon/i18n/2.1"
                xmlns:dri="http://di.tamu.edu/DRI/1.0/"
                xmlns:mets="http://www.loc.gov/METS/"
                xmlns:xlink="http://www.w3.org/TR/xlink/"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
                xmlns:dim="http://www.dspace.org/xmlns/dspace/dim"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:mods="http://www.loc.gov/mods/v3"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="i18n dri mets xlink xsl dim xhtml mods dc">

    <xsl:output indent="yes"/>

    <xsl:template match="dri:div[@n='lookup-modal']" priority="2" mode="outside">
    <div id="lookup-search-results" class="modal fade" role="dialog">
        <div class="modal-dialog">
            <div class="modal-content">
                <div class="modal-header">
                    <button type="button" class="close" data-dismiss="modal">&#215;</button>
                    <h4 class="modal-title"><i18n:text>xmlui.Submission.submit.LiveImportStep.results</i18n:text></h4>
                </div>
                <div class="modal-body">
                    <p class="help-block"><i18n:text>xmlui.Submission.submit.LiveImportStep.results_help</i18n:text></p>
                </div>
                <div class="modal-footer">
                    <button class="ds-button-field btn btn-default pull-left" id="import-pagination-previous"><i18n:text>xmlui.Submission.submit.LiveImportStep.results_previous</i18n:text></button>
                    <button class="ds-button-field btn btn-default pull-left" id="import-pagination-next"><i18n:text>xmlui.Submission.submit.LiveImportStep.results_next</i18n:text></button>
                    <button type="button" class="btn btn-default" data-dismiss="modal"><i18n:text>xmlui.Submission.submit.LiveImportStep.results_close</i18n:text></button>
                </div>
            </div>
        </div>
    </div>
</xsl:template>

</xsl:stylesheet>