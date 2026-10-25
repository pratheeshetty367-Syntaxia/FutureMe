/*
 * FutureMe - live countdown for locked letters.
 *
 * The server writes the number of seconds remaining into data-seconds. The
 * browser only counts down from that number (it never compares the unlock date
 * with the computer's own clock), so a wrong PC clock cannot unlock anything.
 * When the countdown reaches zero the page reloads once, and the SERVER decides
 * whether the letter is really unlocked.
 */
(function () {
    'use strict';
 
    var elements = document.querySelectorAll('[data-seconds]');
    if (elements.length === 0) {
        return;
    }
 
    var loadedAt = Date.now();
    var items = [];
    var reloadScheduled = false;
 
    elements.forEach(function (element) {
        items.push({
            element: element,
            total: parseInt(element.getAttribute('data-seconds'), 10)
        });
    });
 
    function pad(number) {
        return number < 10 ? '0' + number : String(number);
    }
 
    function format(totalSeconds) {
        var days = Math.floor(totalSeconds / 86400);
        var hours = Math.floor((totalSeconds % 86400) / 3600);
        var minutes = Math.floor((totalSeconds % 3600) / 60);
        var seconds = totalSeconds % 60;
        var clock = pad(hours) + ':' + pad(minutes) + ':' + pad(seconds);
        if (days === 0) {
            return clock;
        }
        return days + (days === 1 ? ' day ' : ' days ') + clock;
    }
 
    function tick() {
        var elapsed = Math.floor((Date.now() - loadedAt) / 1000);
        items.forEach(function (item) {
            var left = item.total - elapsed;
            if (left > 0) {
                item.element.textContent = 'Opens in ' + format(left);
            } else {
                item.element.textContent = 'Unlocking...';
                if (!reloadScheduled) {
                    reloadScheduled = true;
                    window.setTimeout(function () {
                        window.location.reload();
                    }, 1000);
                }
            }
        });
    }
 
    tick();
    window.setInterval(tick, 1000);
})();
