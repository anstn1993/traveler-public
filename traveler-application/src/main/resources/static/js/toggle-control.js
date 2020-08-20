window.addEventListener("load", function() {
    const nav = document.querySelector("nav");
    const $toggles = document.querySelectorAll(".toggle");
    const toggleBtn = document.querySelector("#toggle-btn");

    window.addEventListener("resize", function() {
        if(window.innerWidth > 1024) {
            disableToggle(nav, $toggles);
        }
    });

    toggleBtn.addEventListener("click", function() {
        toggleElements(nav, $toggles);
    });

    function toggleElements(nav, $toggles) {
        nav.classList.toggle("on");
        [].forEach.call($toggles, function(toggle) {
            toggle.classList.toggle("on");
        });
    }

    function disableToggle(nav, $toggles) {
        nav.classList.remove("on");
        [].forEach.call($toggles, function(toggle) {
            toggle.classList.remove("on");
        });
    }
});



