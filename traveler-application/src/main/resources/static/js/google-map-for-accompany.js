function initMap() {
    const address = document.querySelector(".section-box .content-section .content-box .map-box .position").textContent;
    const latitude = Number(document.querySelector(".section-box .content-section .content-box .map-box .latitude").textContent);
    const longitude = Number(document.querySelector(".section-box .content-section .content-box .map-box .longitude").textContent);
    const latLng = {lat: latitude, lng: longitude};
    const map = new google.maps.Map(document.getElementById("map"), {
        center: latLng,
        zoom: 10
    });
    const marker = new google.maps.Marker({
        map: map,
        position: latLng,
        title: address
    });
    const infoWindow = new google.maps.InfoWindow;
    infoWindow.setContent(address);
    infoWindow.open(map, marker);
}
