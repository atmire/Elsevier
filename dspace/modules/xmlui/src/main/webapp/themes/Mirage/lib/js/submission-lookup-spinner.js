window.onload=function(){
    $("#aspect_submission_StepTransformer_field_submit_lookup").click(function(event){
        var spinner =  " <i class=\"fa fa-spinner fa-spin\"></i>";
        $(this).parent().append(spinner)
    })
}