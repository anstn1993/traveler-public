let map;
let infoWindow;
let marker;
let geocoder;
let address;
let searchAddressInput;
let latLng;
function initMap() {
    map = new google.maps.Map(document.getElementById("map"), {
        center: { lat: 0, lng: 0 },
        zoom: 10,
    });
    geocoder = new google.maps.Geocoder;
    infoWindow = new google.maps.InfoWindow;
    searchAddressInput = document.querySelector(".section-box .content-section .inner .map-box [type='text']");
    marker = new google.maps.Marker({
        map: map
    });

    //permit location permission
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            latLng = {
                lat: position.coords.latitude,
                lng: position.coords.longitude
            };
            placeMarkerByLatLng(marker, latLng);
        }, function() {
            latLng = {
                lat: 0,
                lng: 0
            };
            marker.setPosition(latLng);
            map.setCenter(latLng);
        });
    }

    map.addListener('click', function(event){
        placeMarkerByLatLng(marker, event.latLng);
    });

    searchAddressInput.addEventListener("keyup", function(event) {
        const key = event.key;
        const searchText = searchAddressInput.value;
        if(key === "Enter") {
            placeMarkerByAddress(marker, searchText);
        }
    });
}

//마커를 클릭한 위, 경도 위치로 옮기고 지도 위치 이동
function placeMarkerByLatLng(marker, latLng) {
    //get address
    geocoder.geocode({"location": latLng}, function(results, status) {
        if(status === "OK") {
            if(results[0]) {
                // address = results[0].formatted_address;
                address = (results[0].formatted_address.indexOf("unnamed load") === -1)?results[0].formatted_address.replace("unnamed load, ", ""):results[0].formatted_address;
                //set title
                marker.setTitle((address !== undefined) ? address : "not found");
                infoWindow.setContent((address !== undefined) ? address : "not found");
                //place marker
                marker.setPosition(latLng);
                //position marker center of the map
                map.setCenter(latLng);
                infoWindow.open(map, marker);
            }
            else {
                alert("결과가 없습니다.");
            }
        }
        else {
            alert("failed to get Address");
        }
    });
}

function placeMarkerByAddress(marker, searchText) {
    geocoder.geocode({"address": searchText}, function(results, status) {
        if (status == 'OK') {
            address = (results[0].formatted_address.indexOf("unnamed load") === -1)?results[0].formatted_address.replace("unnamed load, ", ""):results[0].formatted_address;
            latLng = results[0].geometry.location;
            marker.setTitle((address !== undefined) ? address : "not found");
            infoWindow.setContent((address !== undefined) ? address : "not found");
            //place marker
            marker.setPosition(latLng);
            //position marker center of the map
            map.setCenter(latLng);
            infoWindow.open(map, marker);
        } else {
            alert('검색 실패: ' + status);
        }
    });
}
