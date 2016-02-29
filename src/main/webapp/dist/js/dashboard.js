/**
 * Created by cristianchiovari on 2/28/16.
 */


function jmsaction(actionName) {

    var checkboxesChecked = $("#dashboardtable input[type='checkbox']:checked");
    alert($.AdminLTE.currentResource);
    checkboxesChecked.each(function (index) {
        $.ajax({
            url: '/domainhealth/rest/jmsaction/jmsdestination/' + $.AdminLTE.currentResource + '/' + $(this).attr("id") + '/'+actionName,
            cache: false,
            success: function (response) {
                alert("Display dashboard");
                //$("#dashboardMedRecJMSServer").trigger("click");
                //getAndDisplayDashboard("XXX","jmsdashboard","");
            },
            error: function (xhr) {
                alert("error");
            }
        });
    });

}

var checkboxes = $("#dashboardtable input[type='checkbox']");

checkboxes.click(function () {

    $("#prdtgl").attr("disabled", !checkboxes.is(":checked"));
    $("#constgl").attr("disabled", !checkboxes.is(":checked"));
    $("#instgl").attr("disabled", !checkboxes.is(":checked"));
});

$("#pauseprd").click(function () {
    jmsaction("pauseProduction");
});


$("#resumeprd").click(function () {
    jmsaction("resumeProduction");
});

$("#pausecons").click(function () {
    jmsaction("pauseConsumption");
});


$("#resumecons").click(function () {
    jmsaction("resumeConsumption");
});

$("#pauseins").click(function () {
    jmsaction("pauseInsertion");
});


$("#resumeins").click(function () {
    jmsaction("resumeInsertion");
});

$("#prdtgl").prop("disabled", true);
$("#constgl").prop("disabled", true);
$("#instgl").prop("disabled", true);


