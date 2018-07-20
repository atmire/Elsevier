/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
(function($) {
    var records_template = DSpace.getTemplate('records');

    Handlebars.registerHelper('ifCond', function (v1, operator, v2, options) {

        switch (operator) {
            case '==':
                return (v1 == v2) ? options.fn(this) : options.inverse(this);
            case '<':
                return (v1 < v2) ? options.fn(this) : options.inverse(this);
            default:
                return options.inverse(this);
        }
    });
    var spinner =  " <i class=\"fa fa-spinner fa-spin\"></i>";

    $('#aspect_submission_StepTransformer_field_submit_lookup').click(function(event){
        event.preventDefault();
        var searchInput = "";
        $(this).html($(this).text() + spinner)

        $("input[id^='aspect_submission_StepTransformer_field_'][type='text'], input[id^='aspect_submission_StepTransformer_field_'][type='hidden']").each(function () {
            if($(this).val()) {

                if (searchInput != "") {
                    searchInput += "&";
                }
                var n = $(this).attr('id').lastIndexOf('_');
                searchInput += $(this).attr('id').substring(n + 1) + "=" + $(this).val();
            }
        });

        startLookup($(this), searchInput,0)
    });

    function startLookup(button, searchInput,start) {
        $.ajax({url: window.import.contextPath+"/json/submissionLookup?" + searchInput +"&start="+start,
            type: "POST",
            dataType: "json",
            async: true,
            contentType: "application/x-www-form-urlencoded;charset=UTF-8",
            error: function(xhr, status, error){
                button.html(button.html().replace(spinner, ''));
                var err = eval("(" + xhr.responseText + ")");
                alert(err.Message);
            },
            success: function(info) {
                button.html(button.html().replace(spinner, ''));
                info.shownStart = start + 1;
                info.shownCount = start + info.records.length;

                fillModal(info);
                setPagination(start,info,searchInput);

                $(".records-import-btn").click(function(event) {
                    event.preventDefault();
                    var eid = $(this).attr("id").substring('records-import-'.length);
                    $("#aspect_submission_StepTransformer_field_import_id").val(eid);
                    $("#aspect_submission_StepTransformer_div_submit-lookup").submit();
                });
            }
        });
    }

    function fillModal(info){
        var lookupModal = $('#lookup-search-results');

        var htmlData;
        if(info.records.length>0) {
            htmlData = html = records_template(info);
        }
        else {
            htmlData = html = "<p>No records found</p>";
        }
        lookupModal.find('.modal-body').html(htmlData);
        lookupModal.modal('show');
    }

    function setPagination(start, info, searchInput){
        if(start + info.records.length<info.total){
            $("#import-pagination-next").attr("disabled", false);
        }
        else {
            $("#import-pagination-next").attr("disabled", true);
        }

        if(start>0){
            $("#import-pagination-previous").attr("disabled", false);
        }
        else {
            $("#import-pagination-previous").attr("disabled", true);
        }

        $("#import-pagination-previous").unbind("click");
        $("#import-pagination-previous").click(function(event) {
            $( this ).unbind( event );
            event.preventDefault();
            startLookup($(this), searchInput, start - 20);

        });

        $("#import-pagination-next").unbind("click");
        $("#import-pagination-next").click(function(event) {
            event.preventDefault();
            startLookup($(this), searchInput, start + 20);

        });
    }

    function centerModal() {
        var $dialog  = $(this).find(".modal-dialog");
        $(".modal-body").css("height", "auto");
        // Default offset is 30px
        // If dialog is too large, resize the body of the modal so it fits with a margin of 30px at the bottom and 30px at the top
        if(($dialog.outerHeight() + 60) > window.innerHeight){
            $(".modal-body").outerHeight(window.innerHeight - ($(".modal-header").outerHeight() + $(".modal-footer").outerHeight() + 60));
            $dialog.css("margin-top", 30 );        }
        // If the dialog fits, make sure it's centered
        else {
            $dialog.css("margin-top", (window.innerHeight - $dialog.outerHeight())/2 );
        }
    }

    $(document).on('shown.bs.modal', '.modal', centerModal);
    $(window).on("resize", function () {
        $('.modal:visible').each(centerModal);
    });

})(jQuery);