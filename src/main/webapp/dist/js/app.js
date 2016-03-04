/*! AdminLTE app.js
 * ================
 * Main JS application file for AdminLTE v2. This file
 * should be included in all pages. It controls some layout
 * options and implements exclusive AdminLTE plugins.
 *
 * @Author  Almsaeed Studio
 * @Support <http://www.almsaeedstudio.com>
 * @Email   <support@almsaeedstudio.com>
 * @version 2.1.2
 * @license MIT <http://opensource.org/licenses/MIT>
 */

'use strict';

//Make sure jQuery has been loaded before app.js
if (typeof jQuery === "undefined") {
    throw new Error("AdminLTE requires jQuery");
}

//localStorage.name = 'dh2storage';
//console.log(localStorage.name);

/* AdminLTE
 *
 * @type Object
 * @description $.AdminLTE is the main object for the template's app.
 *              It's used for implementing functions and options related
 *              to the template. Keeping everything wrapped in an object
 *              prevents conflict with other plugins and is a better
 *              way to organize our code.
 */

var dhLocalObj = JSON.parse(localStorage.getItem("dh2storage"));

console.log("dhLocalObj:" + dhLocalObj);
if (dhLocalObj == null) {

    dhLocalObj = {
        currentPath: "core",
        currentResource: "params",
        currentResname: "Core"

    };
    localStorage.setItem("dh2storage", JSON.stringify(dhLocalObj));
}
else {
    console.log('retrievedObject: ', dhLocalObj.currentPath);
}
$.AdminLTE = {};

/* --------------------
 * - AdminLTE Options -
 * --------------------
 * Modify these options to suit your implementation
 */
$.AdminLTE.options = {

    // Add slimscroll to navbar menus
    // This requires you to load the slimscroll plugin in every page before app.js
    navbarMenuSlimscroll: true,
    navbarMenuSlimscrollWidth: "3px", //The width of the scroll bar
    navbarMenuHeight: "200px", //The height of the inner menu
    //General animation speed for JS animated elements such as box collapse/expand and
    //sidebar treeview slide up/down. This options accepts an integer as milliseconds,
    //'fast', 'normal', or 'slow'
    animationSpeed: 500,
    //Sidebar push menu toggle button selector
    sidebarToggleSelector: "[data-toggle='offcanvas']",
    //Activate sidebar push menu
    sidebarPushMenu: true,
    //Activate sidebar slimscroll if the fixed layout is set (requires SlimScroll Plugin)
    sidebarSlimScroll: true,
    //Enable sidebar expand on hover effect for sidebar mini
    //This option is forced to true if both the fixed layout and sidebar mini
    //are used together
    sidebarExpandOnHover: false,
    //BoxRefresh Plugin
    enableBoxRefresh: true,
    //Bootstrap.js tooltip
    enableBSToppltip: true,
    BSTooltipSelector: "[data-toggle='tooltip']",
    //Enable Fast Click. Fastclick.js creates a more
    //native touch experience with touch devices. If you
    //choose to enable the plugin, make sure you load the script
    //before AdminLTE's app.js
    enableFastclick: true,
    //Control Sidebar Options
    enableControlSidebar: true,
    controlSidebarOptions: {
        //Which button should trigger the open/close event
        toggleBtnSelector: "[data-toggle='control-sidebar']",
        //The sidebar selector
        selector: ".control-sidebar",
        //Enable slide over content
        slide: true
    },

    //Box Widget Plugin. Enable this plugin
    //to allow boxes to be collapsed and/or removed
    enableBoxWidget: true,
    //Box Widget plugin options
    boxWidgetOptions: {
        boxWidgetIcons: {
            //Collapse icon
            collapse: 'fa-minus',
            //Open icon
            open: 'fa-plus',
            //Remove icon
            remove: 'fa-times'
        },
        boxWidgetSelectors: {
            //Remove button selector
            remove: '[data-widget="remove"]',
            //Collapse button selector
            collapse: '[data-widget="collapse"]'
        }
    },

    //Direct Chat plugin options
    directChat: {
        //Enable direct chat by default
        enable: true,
        //The button to open and close the chat contacts pane
        contactToggleSelector: '[data-widget="chat-pane-toggle"]'
    },

    //Define the set of colors to use globally around the website
    colors: {
        lightBlue: "#3c8dbc",
        red: "#f56954",
        green: "#00a65a",
        aqua: "#00c0ef",
        yellow: "#f39c12",
        blue: "#0073b7",
        navy: "#001F3F",
        teal: "#39CCCC",
        olive: "#3D9970",
        lime: "#01FF70",
        orange: "#FF851B",
        fuchsia: "#F012BE",
        purple: "#8E24AA",
        maroon: "#D81B60",
        black: "#222222",
        gray: "#d2d6de"
    },

    //The standard screen sizes that bootstrap uses.
    //If you change these in the variables.less file, change
    //them here too.
    screenSizes: {
        xs: 480,
        sm: 768,
        md: 992,
        lg: 1200
    },
    currentDate: null,
    endTimeVal: null,
    startTimeVal: null,
    currentPath: dhLocalObj.currentPath,
    currentResource: dhLocalObj.currentResource,
    currentResname: dhLocalObj.currentResname,
    interval: null

};

/* ------------------
 * - Implementation -
 * ------------------
 * The next block of code implements AdminLTE's
 * functions and plugins as specified by the
 * options above.
 */
$(function () {

    $(document).ajaxStart(function () {
        Pace.restart();
    });

    Handlebars.registerHelper('if_even', function (conditional, options) {
        if ((conditional % 2) == 0) {
            return options.fn(this);
        } else {
            return options.inverse(this);
        }
    });

    Handlebars.registerHelper("math", function (lvalue, operator, rvalue, options) {
        lvalue = parseFloat(lvalue);
        rvalue = parseFloat(rvalue);

        return {
            "+": lvalue + rvalue,
            "-": lvalue - rvalue,
            "*": lvalue * rvalue,
            "/": lvalue / rvalue,
            "%": lvalue % rvalue
        }[operator];
    });


    Handlebars.registerHelper('ifCond', function (v1, v2, options) {
        if (v1 === v2) {
            return options.fn(this);
        }
        return options.inverse(this);
    });

    //Extend options if external options exist
    if (typeof AdminLTEOptions !== "undefined") {
        $.extend(true,
            $.AdminLTE.options,
            AdminLTEOptions);
    }

    //Easy access to options
    var o = $.AdminLTE.options;

    //Set up the object
    _init();

    //Activate the layout maker
    $.AdminLTE.layout.activate();

    //Enable sidebar tree view controls
    $.AdminLTE.tree('.sidebar');

    //Enable control sidebar
    if (o.enableControlSidebar) {
        $.AdminLTE.controlSidebar.activate();
    }

    //Add slimscroll to navbar dropdown
    if (o.navbarMenuSlimscroll && typeof $.fn.slimscroll != 'undefined') {
        $(".navbar .menu").slimscroll({
            height: o.navbarMenuHeight,
            alwaysVisible: false,
            size: o.navbarMenuSlimscrollWidth
        }).css("width", "100%");
    }

    //Activate sidebar push menu
    if (o.sidebarPushMenu) {
        $.AdminLTE.pushMenu.activate(o.sidebarToggleSelector);
    }

    //Activate Bootstrap tooltip
    if (o.enableBSToppltip) {
        $('body').tooltip({
            selector: o.BSTooltipSelector
        });
    }

    //Activate box widget
    if (o.enableBoxWidget) {
        $.AdminLTE.boxWidget.activate();
    }

    //Activate fast click
    if (o.enableFastclick && typeof FastClick != 'undefined') {
        FastClick.attach(document.body);
    }

    //Activate direct chat widget
    if (o.directChat.enable) {
        $(o.directChat.contactToggleSelector).on('click', function () {
            var box = $(this).parents('.direct-chat').first();
            box.toggleClass('direct-chat-contacts-open');
        });
    }

    var currentDate = new Date();
    $.AdminLTE.options.endTimeVal = moment(currentDate);
    $.AdminLTE.options.startTimeVal = moment(currentDate).subtract(30, 'minutes');

    var endTime = $.AdminLTE.options.endTimeVal.format('DD-MM-YYYY-HH-mm');
    var startTime = $.AdminLTE.options.startTimeVal.format('DD-MM-YYYY-HH-mm');

    displayDateInterval($.AdminLTE.options.startTimeVal, $.AdminLTE.options.endTimeVal);

    //
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });


    //initial view loading the first page
    getAndDisplayCharts($.AdminLTE.options.currentResname, $.AdminLTE.options.currentPath, $.AdminLTE.options.currentResource);

    //initialize the date range and add a listener when new interval is selected
    $('#daterange-btn').daterangepicker(
        {
            timePicker: true,
            timePickerIncrement: 1,
            locale: {
                format: 'DD/MM/YYYY h:mm A'
            },

            ranges: {
                '5 minutes': [moment().subtract(5, 'minutes'), moment()],
                '15 minutes': [moment().subtract(15, 'minutes'), moment()],
                '30 minutes': [moment().subtract(30, 'minutes'), moment()],
                '1 hour': [moment().subtract(1, 'hours'), moment()],
                '3 hours': [moment().subtract(3, 'hours'), moment()],
                '6 hours': [moment().subtract(6, 'hours'), moment()],
                '12 hours': [moment().subtract(12, 'hours'), moment()],
                '24 hours': [moment().subtract(24, 'hours'), moment()],
                'Today': [moment().startOf('day'), moment()],
                'Last 2 Days': [moment().subtract(2, 'days'), moment()],
                'Last 7 Days': [moment().subtract(6, 'days'), moment()],
                'Last 30 Days': [moment().subtract(29, 'days'), moment()],
                'This Month': [moment().startOf('month'), moment().endOf('month')]
            },
            startDate: moment().subtract(1, 'day'),
            endDate: moment()
        },
        function (start, end) {
            endTime = end.format('DD-MM-YYYY-HH-mm');
            startTime = start.format('DD-MM-YYYY-HH-mm');
            $.AdminLTE.options.endTimeVal = end;
            $.AdminLTE.options.startTimeVal = start;
            displayDateInterval($.AdminLTE.options.startTimeVal, $.AdminLTE.options.endTimeVal);
            getAndDisplayCharts($.AdminLTE.options.currentResname, $.AdminLTE.options.currentPath, $.AdminLTE.options.currentResource);
        }
    );

    //http://localhost:7001/domainhealth/rest/resources/workmgr?startTime=01-09-2014-00-00&endTime=17-09-2015-0-00

    var source = $("#menu-template").html();
    var template = Handlebars.compile(source);


    var sourceGraph = $("#graph-template").html();
    var templateHighstock = Handlebars.compile(sourceGraph);

    // ----------------------------------------------------------------
    // Added by gregoan
    // JMS contains an ACTION column
    // -----------------------------
    var sourceDashboardActionJms = $("#dashboard-action-template-jms").html();
    var templateDashboardActionJms = Handlebars.compile(sourceDashboardActionJms);

    var sourceDashboardActionSaf = $("#dashboard-action-template-saf").html();
    var templateDashboardActionSaf = Handlebars.compile(sourceDashboardActionSaf);
    //----------------------------------------------------------------

    $("#move-left").click(function () {
        var duration = moment.duration($.AdminLTE.options.endTimeVal.diff($.AdminLTE.options.startTimeVal));
        var minutes = duration.asMinutes();
        //alert("Navigation by interval"+minutes);
        $.AdminLTE.options.endTimeVal = $.AdminLTE.options.startTimeVal;
        $.AdminLTE.options.startTimeVal = moment($.AdminLTE.options.startTimeVal).subtract(minutes, 'minutes');
        displayDateInterval($.AdminLTE.options.startTimeVal, $.AdminLTE.options.endTimeVal);
        endTime = $.AdminLTE.options.endTimeVal.format('DD-MM-YYYY-HH-mm');
        startTime = $.AdminLTE.options.startTimeVal.format('DD-MM-YYYY-HH-mm');
        getAndDisplayCharts($.AdminLTE.options.currentResname, $.AdminLTE.options.currentPath, $.AdminLTE.options.currentResource);

    });

    $("#move-right").click(function () {
        var duration = moment.duration($.AdminLTE.options.endTimeVal.diff($.AdminLTE.options.startTimeVal));
        var minutes = duration.asMinutes();
        //alert("Navigation by interval"+minutes);
        $.AdminLTE.options.startTimeVal = $.AdminLTE.options.endTimeVal;
        $.AdminLTE.options.endTimeVal = moment($.AdminLTE.options.endTimeVal).add(minutes, 'minutes');
        displayDateInterval($.AdminLTE.options.startTimeVal, $.AdminLTE.options.endTimeVal);
        endTime = $.AdminLTE.options.endTimeVal.format('DD-MM-YYYY-HH-mm');
        startTime = $.AdminLTE.options.startTimeVal.format('DD-MM-YYYY-HH-mm');
        getAndDisplayCharts($.AdminLTE.options.currentResname, $.AdminLTE.options.currentPath, $.AdminLTE.options.currentResource);
    });

    Handlebars.registerHelper('shortName', function (name) {
        return name.substr(0, 22);
    });

    function displayDateInterval(start, end) {
        var duration = moment.duration($.AdminLTE.options.endTimeVal.diff($.AdminLTE.options.startTimeVal));
        var minutes = Math.round(duration.asMinutes());
        var tooltip = start.format('DD/MM/YYYY-hh:mm') + "----" + end.format('DD/MM/YYYY-hh:mm');
        if (minutes < 60) {
            var s1 = "<span  data-toggle=/'tooltip/' title=" + tooltip + ">" + minutes + " minutes </span>";
            $('#daterange-btn').html(s1);
        }
        else if (minutes >= 60 && minutes < 180) {
            $('#daterange-btn').html("<span  data-toggle=/'tooltip/' title=" + tooltip + ">" + Math.round(minutes / 60) + " hour </span>");
        }
        else if (minutes >= 180 && minutes <= 24 * 60) {
            $('#daterange-btn').html("<span  data-toggle=/'tooltip/' title=" + tooltip + ">" + Math.round(minutes / 60) + " hours </span>");
        }
        else {
            $('#daterange-btn').html(tooltip);
        }
    }

    function getAndDisplayDashboard(resname, respath, value) {
        $.ajax({
            url: '/domainhealth/rest/dashboard/' + respath + '/' + value,
            cache: false,
            success: function (response) {
                $.AdminLTE.options.selectedPath = resname + " > " + value;
                $.AdminLTE.options.renderedData = response;
                $.AdminLTE.options.currentPath = respath;
                $.AdminLTE.options.currentResource = value;
                $.AdminLTE.options.currentResname = resname;
                // ------------------------------------------------
                // Updated by gregoan
                // ------------------
                //$(".content-wrapper").html(templateDashboard($.AdminLTE));
                if (respath == "safdashboard") {
                    $(".content-wrapper").html(templateDashboardActionSaf($.AdminLTE));
                } else {
                    $(".content-wrapper").html(templateDashboardActionJms($.AdminLTE));
                }
                //dhLocalObj.currentPath = $.AdminLTE.options.currentPath;
                //dhLocalObj.currentResource = $.AdminLTE.options.currentResource;
                //dhLocalObj.currentResname = $.AdminLTE.options.currentResname;
                //localStorage.setItem("dh2storage",JSON.stringify(dhLocalObj));
                // ------------------------------------------------

            },
            error: function (xhr) {
                alert("error");
            }
        });
    }


    function getAndDisplayCharts(resname, respath, value) {
        $.ajax({
            url: '/domainhealth/rest/stats/' + respath + '/' + value + '?',
            cache: false,
            data: {startTime: startTime, endTime: endTime},
            success: function (response) {

                $.AdminLTE.options.renderedData = response;
                $.AdminLTE.options.selectedPath = resname + " > " + value;
                $.AdminLTE.options.currentPath = respath;
                $.AdminLTE.options.currentResource = value;
                $.AdminLTE.options.currentResname = resname;
                //console.log($.AdminLTE.options.renderedData);
                $(".content-wrapper").html(templateHighstock($.AdminLTE));
                dhLocalObj.currentPath = $.AdminLTE.options.currentPath;
                dhLocalObj.currentResource = $.AdminLTE.options.currentResource;
                dhLocalObj.currentResname = $.AdminLTE.options.currentResname;
                //dhLocalObj.startTimeVal = $.AdminLTE.options.startTimeVal;
                //dhLocalObj.endTimeVal = $.AdminLTE.options.endTimeVal;
                localStorage.setItem("dh2storage", JSON.stringify(dhLocalObj));

            },
            error: function (xhr) {
                alert("error");
            }
        });
    }

    function addListener(res, resname, respath) {
        $.each(res.list, function (key, value) {
            $("#" + res.uniquename + value).click(function () {
                getAndDisplayCharts(resname, respath, value);
            });
        });
    }


    function addDashboardListener(res, resname, respath) {
        $.each(res.list, function (key, value) {
            $("#" + res.uniquename + value).click(function () {
                getAndDisplayDashboard(resname, respath, value);
            });
        });
    }

    $("#core").click(function () {
        getAndDisplayCharts("Core", "core", "params");
    });

    // Added by gregoan
    $("#jvm").click(function () {
        getAndDisplayCharts("JVM", "jvm", "params");
    });

    // Added by gregoan
    $("#hostmachine").click(function () {
        getAndDisplayCharts("HostMachine", "hostmachine", "params");
    });

    $("#dhnavigatorCb").click(function () {
        getAndDisplayCharts($.AdminLTE.options.currentResname, $.AdminLTE.options.currentPath, $.AdminLTE.options.currentResource);
    });

    $("#livedataCb").click(function () {


        //if livedata is selected start interval
        if ($('#livedataCb').is(':checked')) {


            console.log("start live data ...");
            $.AdminLTE.options.interval = setInterval(function () {

                var charts = $(".currentcharts");
                var chartMap = new Object();
                $.each(charts, function (key, value) {
                    var chart = $("#" + value.id).highcharts();
                    chartMap[value.id] = chart;
                });

                var intrevalTime = 60;
                var now = new Date();
                var end = moment(now);
                var start = moment(end).subtract(intrevalTime, 'seconds');
                var endTimeInterval = end.format('DD-MM-YYYY-HH-mm');
                var startTimeInterval = start.format('DD-MM-YYYY-HH-mm');

                $.ajax({
                    url: '/domainhealth/rest/stats/' + $.AdminLTE.options.currentPath + '/' + $.AdminLTE.options.currentResource + '?',
                    cache: false,
                    data: {startTime: startTimeInterval, endTime: endTimeInterval},
                    success: function (response) {
                        $.each(response, function (key, value) {
                            var currentChart = chartMap[key];
                            //each chart
                            for (var seriesIndex = 0; seriesIndex < value.length; seriesIndex++) {
                                // each series in the chart
                                var currentSeries = currentChart.get(value[seriesIndex].id);
                                
                                for (var i = 0; i < value[seriesIndex].data.length; i++) {
                                    var point = value[seriesIndex].data[i];
                                    var index = currentSeries.xData.indexOf(point[0]);
                                    if (index == -1) {
                                        currentSeries.addPoint([point[0], point[1]], true, true);
                                    }
                                }
                            }
                        });
                    },
                    error: function (xhr) {
                        alert("error");
                    }
                });

                console.log("tick ...");
            }, 30000);
        } else {
            console.log("stop live data ...");
            clearInterval($.AdminLTE.options.interval);
        }


    });


    $.ajax({
        url: '/domainhealth/rest/resources',
        dataType: 'JSON',
        data: {startTime: startTime, endTime: endTime},
        success: function (response) {
            var resources = response;
            var res = new Object();


            res.uniquename = "datasource"
            res.list = response[res.uniquename];
            $("#datasource").html(template(res));
            addListener(res, "Datasource", "datasource");

            res.uniquename = "destination"
            res.list = response[res.uniquename];
            $("#destination").html(template(res));
            addListener(res, "JMS", "destination");


            res.uniquename = "saf"
            res.list = response[res.uniquename];
            $("#saf").html(template(res));
            addListener(res, "Store and Forward", "saf");


            res.uniquename = "webapp"
            res.list = response[res.uniquename];
            $("#webapp").html(template(res));
            addListener(res, "Web Applications", "webapp");


            res.uniquename = "ejb"
            res.list = response[res.uniquename];
            $("#ejb").html(template(res));
            addListener(res, "EJBs", "ejb");


            res.uniquename = "svrchnl"
            res.list = response[res.uniquename];
            $("#svrchnl").html(template(res));
            addListener(res, "Channels", "svrchnl");

            res.uniquename = "jmsdashboard"
            res.list = response[res.uniquename];
            $("#jmsdashboard").html(template(res));
            addDashboardListener(res, "JMS Dashboard", "jmsdashboard");


            res.uniquename = "safdashboard"
            res.list = response[res.uniquename];
            $("#safdashboard").html(template(res));
            addDashboardListener(res, "SAF Dashboard", "safdashboard");

        },
        error: function (xhr) {
            alert("error");
        }
    });


    //var previousDate = moment(currentDate, 'DD-MM-YYYY-HH-mm').be;
    //$("#datasources").append('<li><a href="/user/messages"><span class="tab">'+endDate+'</span></a></li>');

    /*
     * INITIALIZE BUTTON TOGGLE
     * ------------------------
     */
    $('.btn-group[data-toggle="btn-toggle"]').each(function () {
        var group = $(this);
        $(this).find(".btn").on('click', function (e) {
            group.find(".btn.active").removeClass("active");
            $(this).addClass("active");
            e.preventDefault();

        });

    });
});

/* ----------------------------------
 * - Initialize the AdminLTE Object -
 * ----------------------------------
 * All AdminLTE functions are implemented below.
 */
function _init() {

    /* Layout
     * ======
     * Fixes the layout height in case min-height fails.
     *
     * @type Object
     * @usage $.AdminLTE.layout.activate()
     *        $.AdminLTE.layout.fix()
     *        $.AdminLTE.layout.fixSidebar()
     */
    $.AdminLTE.layout = {
        activate: function () {
            var _this = this;
            _this.fix();
            _this.fixSidebar();
            $(window, ".wrapper").resize(function () {
                _this.fix();
                _this.fixSidebar();
            });
        },
        fix: function () {
            //Get window height and the wrapper height
            var neg = $('.main-header').outerHeight() + $('.main-footer').outerHeight();
            var window_height = $(window).height();
            var sidebar_height = $(".sidebar").height();
            //Set the min-height of the content and sidebar based on the
            //the height of the document.
            if ($("body").hasClass("fixed")) {
                $(".content-wrapper, .right-side").css('min-height', window_height - $('.main-footer').outerHeight());
            } else {
                var postSetWidth;
                if (window_height >= sidebar_height) {
                    $(".content-wrapper, .right-side").css('min-height', window_height - neg);
                    postSetWidth = window_height - neg;
                } else {
                    $(".content-wrapper, .right-side").css('min-height', sidebar_height);
                    postSetWidth = sidebar_height;
                }

                //Fix for the control sidebar height
                var controlSidebar = $($.AdminLTE.options.controlSidebarOptions.selector);
                if (typeof controlSidebar !== "undefined") {
                    if (controlSidebar.height() > postSetWidth)
                        $(".content-wrapper, .right-side").css('min-height', controlSidebar.height());
                }

            }
        },
        fixSidebar: function () {
            //Make sure the body tag has the .fixed class
            if (!$("body").hasClass("fixed")) {
                if (typeof $.fn.slimScroll != 'undefined') {
                    $(".sidebar").slimScroll({destroy: true}).height("auto");
                }
                return;
            } else if (typeof $.fn.slimScroll == 'undefined' && console) {
                console.error("Error: the fixed layout requires the slimscroll plugin!");
            }
            //Enable slimscroll for fixed layout
            if ($.AdminLTE.options.sidebarSlimScroll) {
                if (typeof $.fn.slimScroll != 'undefined') {
                    //Destroy if it exists
                    $(".sidebar").slimScroll({destroy: true}).height("auto");
                    //Add slimscroll
                    $(".sidebar").slimscroll({
                        height: ($(window).height() - $(".main-header").height()) + "px",
                        color: "rgba(0,0,0,0.2)",
                        size: "3px"
                    });
                }
            }
        }
    };

    /* PushMenu()
     * ==========
     * Adds the push menu functionality to the sidebar.
     *
     * @type Function
     * @usage: $.AdminLTE.pushMenu("[data-toggle='offcanvas']")
     */
    $.AdminLTE.pushMenu = {
        activate: function (toggleBtn) {
            //Get the screen sizes
            var screenSizes = $.AdminLTE.options.screenSizes;

            //Enable sidebar toggle
            $(toggleBtn).on('click', function (e) {
                e.preventDefault();

                //Enable sidebar push menu
                if ($(window).width() > (screenSizes.sm - 1)) {
                    if ($("body").hasClass('sidebar-collapse')) {
                        $("body").removeClass('sidebar-collapse').trigger('expanded.pushMenu');
                    } else {
                        $("body").addClass('sidebar-collapse').trigger('collapsed.pushMenu');
                    }
                }
                //Handle sidebar push menu for small screens
                else {
                    if ($("body").hasClass('sidebar-open')) {
                        $("body").removeClass('sidebar-open').removeClass('sidebar-collapse').trigger('collapsed.pushMenu');
                    } else {
                        $("body").addClass('sidebar-open').trigger('expanded.pushMenu');
                    }
                }
            });

            $(".content-wrapper").click(function () {
                //Enable hide menu when clicking on the content-wrapper on small screens
                if ($(window).width() <= (screenSizes.sm - 1) && $("body").hasClass("sidebar-open")) {
                    $("body").removeClass('sidebar-open');
                }
            });

            //Enable expand on hover for sidebar mini
            if ($.AdminLTE.options.sidebarExpandOnHover
                || ($('body').hasClass('fixed')
                && $('body').hasClass('sidebar-mini'))) {
                //alert("x");
                this.expandOnHover();
            }
        },
        expandOnHover: function () {
            var _this = this;
            var screenWidth = $.AdminLTE.options.screenSizes.sm - 1;
            //Expand sidebar on hover
            $('.main-sidebar').hover(function () {
                if ($('body').hasClass('sidebar-mini')
                    && $("body").hasClass('sidebar-collapse')
                    && $(window).width() > screenWidth) {
                    _this.expand();
                }
            }, function () {
                if ($('body').hasClass('sidebar-mini')
                    && $('body').hasClass('sidebar-expanded-on-hover')
                    && $(window).width() > screenWidth) {
                    _this.collapse();
                }
            });
        },
        expand: function () {
            $("body").removeClass('sidebar-collapse').addClass('sidebar-expanded-on-hover');
        },
        collapse: function () {
            if ($('body').hasClass('sidebar-expanded-on-hover')) {
                $('body').removeClass('sidebar-expanded-on-hover').addClass('sidebar-collapse');
            }
        }
    };

    /* Tree()
     * ======
     * Converts the sidebar into a multilevel
     * tree view menu.
     *
     * @type Function
     * @Usage: $.AdminLTE.tree('.sidebar')
     */
    $.AdminLTE.tree = function (menu) {
        var _this = this;
        var animationSpeed = $.AdminLTE.options.animationSpeed;
        $("li a", $(menu)).on('click', function (e) {
            //Get the clicked link and the next element
            var $this = $(this);
            var checkElement = $this.next();

            //Check if the next element is a menu and is visible
            if ((checkElement.is('.treeview-menu')) && (checkElement.is(':visible'))) {
                //Close the menu
                checkElement.slideUp(animationSpeed, function () {
                    checkElement.removeClass('menu-open');
                    //Fix the layout in case the sidebar stretches over the height of the window
                    //_this.layout.fix();
                });
                checkElement.parent("li").removeClass("active");
            }
            //If the menu is not visible
            else if ((checkElement.is('.treeview-menu')) && (!checkElement.is(':visible'))) {
                //Get the parent menu
                var parent = $this.parents('ul').first();
                //Close all open menus within the parent
                var ul = parent.find('ul:visible').slideUp(animationSpeed);
                //Remove the menu-open class from the parent
                ul.removeClass('menu-open');
                //Get the parent li
                var parent_li = $this.parent("li");

                //Open the target menu and add the menu-open class
                checkElement.slideDown(animationSpeed, function () {
                    //Add the class active to the parent li
                    checkElement.addClass('menu-open');
                    parent.find('li.active').removeClass('active');
                    parent_li.addClass('active');
                    //Fix the layout in case the sidebar stretches over the height of the window
                    _this.layout.fix();
                });
            }
            //if this isn't a link, prevent the page from being redirected
            if (checkElement.is('.treeview-menu')) {
                e.preventDefault();
            }
        });
    };

    /* ControlSidebar
     * ==============
     * Adds functionality to the right sidebar
     *
     * @type Object
     * @usage $.AdminLTE.controlSidebar.activate(options)
     */
    $.AdminLTE.controlSidebar = {
        //instantiate the object
        activate: function () {

            //Get the object
            var _this = this;
            //Update options
            var o = $.AdminLTE.options.controlSidebarOptions;
            //Get the sidebar
            var sidebar = $(o.selector);
            //The toggle button
            var btn = $(o.toggleBtnSelector);

            //Listen to the click event
            btn.on('click', function (e) {
                e.preventDefault();
                //If the sidebar is not open
                //alert("cucu");
                if (!sidebar.hasClass('control-sidebar-open')
                    && !$('body').hasClass('control-sidebar-open')) {
                    //Open the sidebar
                    _this.open(sidebar, o.slide);
                } else {
                    _this.close(sidebar, o.slide);
                }
            });

            //If the body has a boxed layout, fix the sidebar bg position
            var bg = $(".control-sidebar-bg");
            _this._fix(bg);

            //If the body has a fixed layout, make the control sidebar fixed
            if ($('body').hasClass('fixed')) {
                _this._fixForFixed(sidebar);
            } else {
                //If the content height is less than the sidebar's height, force max height
                if ($('.content-wrapper, .right-side').height() < sidebar.height()) {
                    _this._fixForContent(sidebar);
                }
            }
        },
        //Open the control sidebar
        open: function (sidebar, slide) {
            var _this = this;
            //Slide over content
            if (slide) {
                sidebar.addClass('control-sidebar-open');
            } else {
                //Push the content by adding the open class to the body instead
                //of the sidebar itself
                $('body').addClass('control-sidebar-open');
            }
        },

        //Close the control sidebar
        close: function (sidebar, slide) {
            if (slide) {
                sidebar.removeClass('control-sidebar-open');
            } else {
                $('body').removeClass('control-sidebar-open');
            }
        },
        _fix: function (sidebar) {
            var _this = this;
            if ($("body").hasClass('layout-boxed')) {
                sidebar.css('position', 'absolute');
                sidebar.height($(".wrapper").height());
                $(window).resize(function () {
                    _this._fix(sidebar);
                });
            } else {
                sidebar.css({
                    'position': 'fixed',
                    'height': 'auto'
                });
            }
        },
        _fixForFixed: function (sidebar) {
            sidebar.css({
                'position': 'fixed',
                'max-height': '100%',
                'overflow': 'auto',
                'padding-bottom': '50px'
            });
        },
        _fixForContent: function (sidebar) {
            $(".content-wrapper, .right-side").css('min-height', sidebar.height());
        }
    };

    /* BoxWidget
     * =========
     * BoxWidget is a plugin to handle collapsing and
     * removing boxes from the screen.
     *
     * @type Object
     * @usage $.AdminLTE.boxWidget.activate()
     *        Set all your options in the main $.AdminLTE.options object
     */
    $.AdminLTE.boxWidget = {
        selectors: $.AdminLTE.options.boxWidgetOptions.boxWidgetSelectors,
        icons: $.AdminLTE.options.boxWidgetOptions.boxWidgetIcons,
        animationSpeed: $.AdminLTE.options.animationSpeed,
        activate: function (_box) {
            var _this = this;
            if (!_box) {
                _box = document; // activate all boxes per default
            }
            //Listen for collapse event triggers
            $(_box).find(_this.selectors.collapse).on('click', function (e) {
                e.preventDefault();
                _this.collapse($(this));
            });

            //Listen for remove event triggers
            $(_box).find(_this.selectors.remove).on('click', function (e) {
                e.preventDefault();
                _this.remove($(this));
            });
        },
        collapse: function (element) {
            var _this = this;
            //Find the box parent
            var box = element.parents(".box").first();
            //Find the body and the footer
            var box_content = box.find("> .box-body, > .box-footer, > form  >.box-body, > form > .box-footer");
            if (!box.hasClass("collapsed-box")) {
                //Convert minus into plus
                element.children(":first")
                    .removeClass(_this.icons.collapse)
                    .addClass(_this.icons.open);
                //Hide the content
                box_content.slideUp(_this.animationSpeed, function () {
                    box.addClass("collapsed-box");
                });
            } else {
                //Convert plus into minus
                element.children(":first")
                    .removeClass(_this.icons.open)
                    .addClass(_this.icons.collapse);
                //Show the content
                box_content.slideDown(_this.animationSpeed, function () {
                    box.removeClass("collapsed-box");
                });
            }
        },
        remove: function (element) {
            //Find the box parent
            var box = element.parents(".box").first();
            box.slideUp(this.animationSpeed);
        }
    };
}

/* ------------------
 * - Custom Plugins -
 * ------------------
 * All custom plugins are defined below.
 */