'use strict';
window.Office = {
    ctx: document.querySelector('meta[name="context-path"]').content,
    token: function () { return document.querySelector('meta[name="csrf-token"]').content; },
    error: function (xhr) { alert(xhr.status === 401 ? '登录已过期，请重新登录' : xhr.status === 403 ? '无权限或页面已过期，请刷新后重试' : ((xhr.responseJSON || {}).message || '操作失败，请稍后重试')); }
};
document.addEventListener('DOMContentLoaded', function () {
    if (window.jQuery) {
        jQuery.ajaxPrefilter(function (options, original, xhr) {
            if (!options.crossDomain && !/^(GET|HEAD|OPTIONS)$/i.test(options.type)) xhr.setRequestHeader('X-CSRF-TOKEN', Office.token());
        });
        jQuery(document).ajaxError(function (event, xhr) { Office.error(xhr); });
    }
});
