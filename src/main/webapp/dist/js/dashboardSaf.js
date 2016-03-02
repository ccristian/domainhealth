/**
 * Created by cristianchiovari on 2/28/16.
 */

function safaction(actionName) {
    var checkboxesChecked = $("#dashboardtable input[type='checkbox']:checked");
    checkboxesChecked.each(function (index) {
        $.ajax({
            url: '/domainhealth/rest/jmsaction/safdestination/' + $.AdminLTE.options.currentResource + '/' + $(this).attr("id") + '/' + actionName,
            cache: false,
            success: function (response) {
               $("#safdashboard"+ $.AdminLTE.options.currentResource).trigger("click");
            },
            error: function (xhr) {
                alert("error");
            }
        });
    });
}

var checkboxes = $("#dashboardtable input[type='checkbox']");

checkboxes.click(function () {

    $("#inctgl").attr("disabled", !checkboxes.is(":checked"));
    $("#forwtgl").attr("disabled", !checkboxes.is(":checked"));
    //$("#rectgl").attr("disabled", !checkboxes.is(":checked"));
});

// ------------------------------------------------
$("#pauseinc").click(function () {
    safaction("pauseIncoming");
});

$("#resumeinc").click(function () {
    safaction("resumeIncoming");
});
//------------------------------------------------

//------------------------------------------------
$("#pauseforw").click(function () {
    safaction("pauseForwarding");
});

$("#resumeforw").click(function () {
    safaction("resumeForwarding");
});
//------------------------------------------------

//------------------------------------------------
/*
$("#pauserec").click(function () {
    safaction("pauseReceiving");
});

$("#resumerec").click(function () {
    jmsaction("resumeReceiving");
});
*/
//------------------------------------------------

$("#inctgl").prop("disabled", true);
$("#forwtgl").prop("disabled", true);
//$("#rectgl").prop("disabled", true);