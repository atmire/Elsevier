<!--

    The contents of this file are subject to the license and copyright
    detailed in the LICENSE and NOTICE files at the root of the source
    tree and available online at

    http://www.dspace.org/license/

-->
<!--
    Templates to cover the common dri elements.

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

    <!--Removed the automatic font sizing for headers, because while I liked the idea,
     in practice it's too unpredictable.
     Also made all head's follow the same rule: count the number of ancestors that have
     a head, that's the number after the 'h' in the tagname-->
    <xsl:template name="renderHead">
        <xsl:param name="class"/>
        <xsl:variable name="head_count" select="count(ancestor::dri:*[dri:head])"/>
        <xsl:element name="h{$head_count}">
            <xsl:call-template name="standardAttributes">
                <xsl:with-param name="class" select="$class"/>
            </xsl:call-template>
            <xsl:apply-templates />
        </xsl:element>
    </xsl:template>


    <xsl:template match="dri:div/dri:head" priority="3">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-div-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- The second case is the header on tables, which always creates an HTML h3 element -->
    <xsl:template match="dri:table/dri:head" priority="2">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-table-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <!-- The third case is the header on lists, which creates an HTML h3 element for top level lists and
        and h4 elements for all sublists. -->
    <xsl:template match="dri:list/dri:head" priority="2" mode="nested">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-list-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:list/dri:list/dri:head" priority="3" mode="nested">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-sublist-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:referenceSet/dri:head" priority="2">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-list-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:options/dri:list/dri:head" priority="3">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-option-set-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:head" priority="1">
        <xsl:call-template name="renderHead">
            <xsl:with-param name="class">ds-head</xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="dri:div[@n='lookup-modal']" priority="2">
        <div id="lookup-search-results" class="hidden" role="dialog">
            <div>
                <div>
                    <div>
                        <h4 >Publication Results</h4>
                    </div>
                    <div class="modal-body">
                        <p/>
                    </div>
                    <div>
                        <button class="ds-button-field btn btn-default pull-left" id="publication-pagination-previous">Previous results</button>
                        <button class="ds-button-field btn btn-default pull-left" id="publication-pagination-next">Next results</button>
                        <button type="button" class="btn btn-default close-modal-results">Close</button>
                    </div>
                </div>
            </div>
        </div>
    </xsl:template>
    
    <xsl:template match="dri:xref">
        <a>
            <xsl:if test="@target">
                <xsl:attribute name="href"><xsl:value-of select="@target"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="@rend">
                <xsl:attribute name="class"><xsl:value-of select="@rend"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="@n">
                <xsl:attribute name="name"><xsl:value-of select="@n"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="@onclick">
                <xsl:attribute name="onclick"><xsl:value-of select="@onclick"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="@rend='external'">
                <xsl:attribute name="target">
                    <xsl:text>_blank</xsl:text>
                </xsl:attribute>
            </xsl:if>

            <xsl:apply-templates />
        </a>
    </xsl:template>

</xsl:stylesheet>
