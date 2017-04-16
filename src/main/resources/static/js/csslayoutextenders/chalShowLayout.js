$(document).ready(function () {
    $(".hr_divider").addClass("col-lg-12 col-md-12 col-sm-12 col-xs-12");
    $(".chal_column").addClass("col-lg-6 col-md-6 col-sm-6 col-xs-12");
    $(".main-column").addClass("col-lg-12 col-md-12 col-sm-12 col-xs-12");

});


(function ($) {
    $(window).on("load", function () {
        if ($(window).height() < ($('.fixed_profile_info').height() + 70)) {
            $('.fixed_profile_info').css('position', 'initial');
        }
        var t = setTimeout(function () {
            $(".firstCollapsable").click();
        }, 700);
    });
    $(window).on("resize", function () {
        if ($(window).height() > ($('.fixed_profile_info').height() + 70)) {
            $('.fixed_profile_info').css('position', '');
            if ($(window).scrollTop() < 120) {
                $('.fixed_profile_info').removeClass('affix');
                $('.fixed_profile_info').addClass('affix-top');
            }
        } else {
            $('.fixed_profile_info').css('position', 'initial');
        }
    });
    $(window).on("scroll", function () {
        if ($(window).height() > ($('.fixed_profile_info').height() + 70)) {
            if ($(window).scrollTop() > 120) {
                $('.fixed_profile_info').removeClass('affix-top');
                $('.fixed_profile_info').addClass('affix');
            }
        }
    });
})(jQuery);

