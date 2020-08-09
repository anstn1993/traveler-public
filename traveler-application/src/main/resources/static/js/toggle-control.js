window.addEventListener("load", function() {
    const $toggles = document.querySelectorAll(".toggle");
    const toggleBtn = document.querySelector("#toggle-btn");

    window.addEventListener("resize", function() {
        if(window.innerWidth > 1024) {
            disableToggle($toggles);
        }
    });

    toggleBtn.addEventListener("click", function() {
        toggleElements($toggles);
    });
  
});

function toggleElements($toggles) {
    [].forEach.call($toggles, function(toggle) {
        toggle.classList.toggle("on");
    });
}

function disableToggle($toggles) {
    [].forEach.call($toggles, function(toggle) {
        toggle.classList.remove("on");
    });
}

