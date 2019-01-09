/**
 * Created by cristianchiovari on 2/28/16.
 */

function jmsruntimeaction(actionName) {

//console.log("jmsruntimeaction");
//console.log("    currentResource is [" + $.AdminLTE.options.currentResource + "]");
//console.log("    actionName is [" + actionName + "]");
	
    var checkboxesChecked = $("#dashboardtable input[type='checkbox']:checked");
    checkboxesChecked.each(function (index) {
    	
//console.log("    id is [" + $(this).attr("id") + "]");
    	
        $.ajax({
	        	url: '/domainhealth/rest/jmsaction/jmsdestination/' + $.AdminLTE.options.currentResource + '/' + $(this).attr("id") + '/' + actionName,
	        	cache: false,
            success: function (response) {
               $("#jmsruntimedashboard"+ $.AdminLTE.options.currentResource).trigger("click");
            },
            error: function (xhr) {
                alert("error");
            }
        });
    });
}

function listMessage() {

console.log("listMessage");
	
    var checkboxesChecked = $("#dashboardtable input[type='checkbox']:checked");
    checkboxesChecked.each(function (index) {

var destinationName = $(this).attr("id");
console.log("    destinationName is [" + destinationName + "]");

var sourceDashboardListMessageJmsRuntime = $("#dashboard-list-message-template-jms-runtime").html();
var templateDashboardListMessageJmsRuntime = Handlebars.compile(sourceDashboardListMessageJmsRuntime);

		$.ajax({
	        	url: '/domainhealth/rest/jmsaction/jmsmessages/list/' + $.AdminLTE.options.currentResource + '/' + destinationName,
	        	dataType: 'JSON',
	        	cache: false,
	        success: function (response) {
	        	
			   $.AdminLTE.options.selectedPath = $.AdminLTE.options.currentResource + " > " + destinationName;
	           $.AdminLTE.options.renderedData = response;
	           $.AdminLTE.options.currentPath = $.AdminLTE.options.currentResource;
	           //$.AdminLTE.options.currentResource = destinationName;
	           //$.AdminLTE.options.currentResname = $.AdminLTE.options.currentResource;
	           $.AdminLTE.options.currentResname = destinationName;
	           
	           $(".content-wrapper").html(templateDashboardListMessageJmsRuntime($.AdminLTE));
	               
	        },
	        error: function (xhr) {
	            alert("error");
	        }
		});
    });
    
console.log("");
    
}

function viewMessage() {

console.log("viewMessage");
	
    var checkboxesChecked = $("#dashboardtable input[type='checkbox']:checked");
    checkboxesChecked.each(function (index) {

		var jmsServerName = $.AdminLTE.options.currentResource;
		var destinationName = $.AdminLTE.options.currentResname;
    	
console.log("    jmsServerName is [" + jmsServerName + "]");
console.log("    destinationName is [" + destinationName + "]");

var messageId = $(this).attr("id");
console.log("    messageId is [" + messageId + "]");

		var sourceDashboardViewMessageJmsRuntime = $("#dashboard-view-message-template-jms-runtime").html();
		var templateDashboardViewMessageJmsRuntime = Handlebars.compile(sourceDashboardViewMessageJmsRuntime);

	    $.ajax({
	        	        	
	    		url: '/domainhealth/rest/jmsaction/jmsmessage/get/' + jmsServerName + '/' + destinationName  +  '/' + messageId,
	        	dataType: 'JSON',
	        	cache: false,
            success: function (response) {
            	            	
            	   $.AdminLTE.options.renderedData = response;
               $(".content-wrapper").html(templateDashboardViewMessageJmsRuntime($.AdminLTE));
                   
            },
            error: function (xhr) {
                alert("error");
            }
        });
    });
console.log("");
    
}

function moveMessage() {

console.log("moveMessage");

var jmsServerName = $.AdminLTE.options.currentResource;
var destinationName = $.AdminLTE.options.currentResname;
        	
console.log("    jmsServerName is [" + jmsServerName + "]");
console.log("    destinationName is [" + destinationName + "]");

    var messagesChecked = $("#dashboardtable input[type='checkbox']:checked");
    
$.AdminLTE.options.messagesId = messagesChecked;
$.AdminLTE.options.fromJmsServerName = jmsServerName;
$.AdminLTE.options.fromDestinationName = destinationName;

var sourceDashboardListJmsServerJmsRuntime = $("#dashboard-list-jmsserver-template-jms-runtime").html();
var templateDashboardListJmsServerJmsRuntime = Handlebars.compile(sourceDashboardListJmsServerJmsRuntime);
    	    	
    $.ajax({
        	url: '/domainhealth/rest/jmsaction/jmsservers/list/',
	        	cache: false,
            success: function (response) {
            	
console.log("Got the JMS server list");
        	
        	    $.AdminLTE.options.selectedPath = $.AdminLTE.options.currentResource  + " > " + destinationName;
            $.AdminLTE.options.renderedData = response;
            $.AdminLTE.options.currentPath = $.AdminLTE.options.currentResource;
            //$.AdminLTE.options.currentResource = destinationName;
            //$.AdminLTE.options.currentResname = $.AdminLTE.options.currentResource;
//          $.AdminLTE.options.currentResname = destinationName;
            
            $.AdminLTE.options.jmsServerList = response;
            
            
            
            
            
            $.ajax({
        		
		    	    	// Hardcoded for testing purpose
		    	    	url: '/domainhealth/rest/jmsaction/jmsdestinations/list/JMSServer',
		    		        	cache: false,
		    	            success: function (responseBis) {
		    	            	
		    	                $.AdminLTE.options.renderedDataBis = responseBis;
		    	                
console.log("Got the destination of JMSServer servers");
		    	        
		    	    },
		    	    error: function (xhr) {
		    	        alert("error");
		    	    }
		    	});
            
            
            
            
            $(".content-wrapper").html(templateDashboardListJmsServerJmsRuntime($.AdminLTE));
        },
        error: function (xhr) {
            alert("error");
        }
    });
    
    /*
	$.ajax({
		
	    	// Hardcoded for testing purpose
	    	url: '/domainhealth/rest/jmsaction/jmsdestinations/list/JMSServer',
		        	cache: false,
	            success: function (responseBis) {
	            	
	                $.AdminLTE.options.renderedDataBis = responseBis;
console.log("Got the destination of JMSServer servers");
	        
	    },
	    error: function (xhr) {
	        alert("error");
	    }
	});
	*/
        
console.log("");
        
    //$(".content-wrapper").html(templateDashboardListJmsServerJmsRuntime($.AdminLTE));
}

function executeMoveMessage() {

console.log("executeMoveMessage");

var messagesId = $.AdminLTE.options.messagesId;
var fromJmsServerName = $.AdminLTE.options.fromJmsServerName;
var fromDestinationName = $.AdminLTE.options.fromDestinationName;

console.log("    fromJmsServerName is [" + fromJmsServerName + "]");
console.log("    fromDestinationName is [" + fromDestinationName + "]");

var jmsserverChecked = $("#jmsservertable input[type='checkbox']:checked");
jmsserverChecked.each(function (index) {
	
	var toJmsServerName = $(this).attr("id");
console.log("    toJmsServerName is [" + toJmsServerName + "]");
	
});

var jmsdestinationChecked = $("#jmsdestinationtable input[type='checkbox']:checked");
jmsdestinationChecked.each(function (index) {
	
	var toDestinationName = $(this).attr("id");
console.log("    toDestinationName is [" + toDestinationName + "]");
	
});

messagesId.each(function (index) {
	
	var messageId = $(this).attr("id");
	console.log("    messageId is [" + messageId + "]");
});
console.log("");














}

function deleteMessage() {

console.log("deleteMessage");
	
    var checkboxesChecked = $("#dashboardtable input[type='checkbox']:checked");
    checkboxesChecked.each(function (index) {
    	
var jmsServerName = $.AdminLTE.options.currentResource;
var destinationName = $.AdminLTE.options.currentResname;
        	
console.log("    jmsServerName is [" + jmsServerName + "]");
console.log("    destinationName is [" + destinationName + "]");

var messageId = $(this).attr("id");
console.log("    messageId is [" + messageId + "]");

var sourceDashboardListMessageJmsRuntime = $("#dashboard-list-message-template-jms-runtime").html();
var templateDashboardListMessageJmsRuntime = Handlebars.compile(sourceDashboardListMessageJmsRuntime);

        $.ajax({
	        	url: '/domainhealth/rest/jmsaction/jmsmessage/delete/' + jmsServerName + '/' + destinationName  +  '/' + messageId,
	        	cache: false,
            success: function (response) {
            	
console.log("The message [" + messageId + "] is deleted");
               
            },
            error: function (xhr) {
                alert("error");
            }
        });
        
        // Get the updated datas - refresh the table
        $.ajax({
	        	url: '/domainhealth/rest/jmsaction/jmsmessages/list/' + jmsServerName + '/' + destinationName,
	        	dataType: 'JSON',
	        	cache: false,
            success: function (response) {
            	
			   $.AdminLTE.options.selectedPath = $.AdminLTE.options.currentResource + " > " + destinationName;
               $.AdminLTE.options.renderedData = response;
               $.AdminLTE.options.currentPath = $.AdminLTE.options.currentResource;
               //$.AdminLTE.options.currentResource = destinationName;
               //$.AdminLTE.options.currentResname = $.AdminLTE.options.currentResource;
               $.AdminLTE.options.currentResname = destinationName;
               
               $(".content-wrapper").html(templateDashboardListMessageJmsRuntime($.AdminLTE));
                   
            },
            error: function (xhr) {
                alert("error");
            }
        });
        
        // Redirect the user on screen listing the Destination
        //$("#jmsruntimedashboard"+ $.AdminLTE.options.currentResource).trigger("click");
        
    });
}


















var checkboxes = $("#dashboardtable input[type='checkbox']");
checkboxes.click(function () {

    $("#prdtgl").attr("disabled", !checkboxes.is(":checked"));
    $("#constgl").attr("disabled", !checkboxes.is(":checked"));
    $("#instgl").attr("disabled", !checkboxes.is(":checked"));
    
    // Get the number of selected checkbox
	var checkboxesSelected = $("#dashboardtable input[type='checkbox']:checked");
	//console.log("    checkboxesSelected.length is [" + checkboxesSelected.length + "]");
	
	// We enable the button only if one element is selected
	//$("#runtgl").attr("disabled", !checkboxes.is(":checked"));
	if(checkboxesSelected.length == 1) $("#runlisttgl").attr("disabled", false);
	else $("#runlisttgl").attr("disabled", true);
	
	/*
	// We enable the button only if one element is selected
	if(checkboxesSelected.length == 1) $("#jmsserverlisttgl").attr("disabled", false);
	else $("#jmsserverlisttgl").attr("disabled", true);
	
	// We enable the button only if one element is selected
	if(checkboxesSelected.length == 1) $("#jmsdestinationlisttgl").attr("disabled", false);
	else $("#jmsdestinationlisttgl").attr("disabled", true);
	*/
	
	$("#runactiontgl").attr("disabled", !checkboxes.is(":checked"));
    
});














var checkboxesJmsServer = $("#jmsservertable input[type='checkbox']");
checkboxesJmsServer.click(function () {
	
console.log("In checkboxesJmsServer.click method");

	// Get the number of selected checkbox
	var checkboxesJmsServerSelected = $("#jmsservertable input[type='checkbox']:checked");
	
	// Get the number of selected checkbox
	var checkboxesJmsDestinationSelected = $("#jmsdestinationtable input[type='checkbox']:checked");
	
console.log("    checkboxesJmsServerSelected.length is [" + checkboxesJmsServerSelected.length + "]");
console.log("    checkboxesJmsDestinationSelected.length is [" + checkboxesJmsDestinationSelected.length + "]");
	
	// We enable the button only if one element is selected
	if(checkboxesJmsServerSelected.length == 1 && checkboxesJmsDestinationSelected.length == 1) $("#jmsactionlisttgl").attr("disabled", false);
	else $("#jmsactionlisttgl").attr("disabled", true);
console.log("");
});






var checkboxesJmsDestination = $("#jmsdestinationtable input[type='checkbox']");
checkboxesJmsDestination.click(function () {
	
console.log("In checkboxesJmsDestination.click method");

	// Get the number of selected checkbox
	var checkboxesJmsServerSelected = $("#jmsservertable input[type='checkbox']:checked");
	
	// Get the number of selected checkbox
	var checkboxesJmsDestinationSelected = $("#jmsdestinationtable input[type='checkbox']:checked");
	
console.log("    checkboxesJmsServerSelected.length is [" + checkboxesJmsServerSelected.length + "]");
console.log("    checkboxesJmsDestinationSelected.length is [" + checkboxesJmsDestinationSelected.length + "]");
	
	// We enable the button only if one element is selected
	if(checkboxesJmsServerSelected.length == 1 && checkboxesJmsDestinationSelected.length == 1) $("#jmsactionlisttgl").attr("disabled", false);
	else $("#jmsactionlisttgl").attr("disabled", true);
console.log("");
});






// ------------------------------------------------
$("#pauseprd").click(function () {
	jmsruntimeaction("pauseProduction");
});

$("#resumeprd").click(function () {
	jmsruntimeaction("resumeProduction");
});
//------------------------------------------------

//------------------------------------------------
$("#pausecons").click(function () {
	jmsruntimeaction("pauseConsumption");
});

$("#resumecons").click(function () {
	jmsruntimeaction("resumeConsumption");
});
//------------------------------------------------

//------------------------------------------------
$("#pauseins").click(function () {
	jmsruntimeaction("pauseInsertion");
});

$("#resumeins").click(function () {
	jmsruntimeaction("resumeInsertion");
});
//------------------------------------------------











//------------------------------------------------
$("#listmsg").click(function () {
	listMessage();
});

$("#viewmsg").click(function () {
	viewMessage();
});

$("#movemsg").click(function () {
	moveMessage();
});

$("#deletemsg").click(function () {
	deleteMessage();
});





$("#executemovemsg").click(function () {
	executeMoveMessage();
});















$("#fakeButton").click(function () {
	console.log("Click on the fake button");
});
//------------------------------------------------










//------------------------------------------------
$("#prdtgl").prop("disabled", true);
$("#constgl").prop("disabled", true);
$("#instgl").prop("disabled", true);

$("#runlisttgl").prop("disabled", true);
$("#runactiontgl").prop("disabled", true);

//$("#jmsserverlisttgl").prop("disabled", true);
//$("#jmsdestinationlisttgl").prop("disabled", true);
$("#jmsactionlisttgl").prop("disabled", true);

//------------------------------------------------