// 1. 전역 상태 관리 (설정 값들을 한곳에서 관리)
const appState = {
    radius: 3000,       // 기본 반경 3km
    fuelType: 'B027',   // 기본 유종: 휘발유 (B027), 경유(D047), 고급유(B034), LPG(K015)
    sort: 1,            // 기본 정렬: 가격순(1), 거리순(2)
    currentLat: 37.566826, // 기본 좌표 (서울시청)
    currentLon: 126.9786567,
    markers: []         // 지도 마커 관리용 배열
};

// 좌표 변환용 (WGS84 -> KATEC) Proj4 정의
const katecDef = "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=400000 +y_0=600000 +ellps=bessel +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43 +units=m +no_defs";

// 카카오맵 객체
var map;

document.addEventListener("DOMContentLoaded", function() {
    initMap();       // 지도 생성
    getUserLocation(); // 내 위치 가져오기 및 주유소 로드

    // 이벤트 리스너 등록
    document.getElementById('myLocationBtn').addEventListener('click', getUserLocation);
});

// ---------------------------------------------------------
// [1] 지도 초기화
// ---------------------------------------------------------
function initMap() {
    var container = document.getElementById('map');
    var options = {
        center: new kakao.maps.LatLng(appState.currentLat, appState.currentLon),
        level: 4
    };
    map = new kakao.maps.Map(container, options);
}

// ---------------------------------------------------------
// [2] 사용자 위치 가져오기
// ---------------------------------------------------------
function getUserLocation() {
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(function(position) {
            // 상태 업데이트
            appState.currentLat = position.coords.latitude;
            appState.currentLon = position.coords.longitude;

            // 지도 이동
            var locPosition = new kakao.maps.LatLng(appState.currentLat, appState.currentLon);
            map.panTo(locPosition);

            // ★ 주유소 데이터 로드 요청
            loadGasStations();

        }, function(error) {
            console.error("위치 오류:", error);
            alert("위치 정보를 가져올 수 없어 기본 위치에서 검색합니다.");
            loadGasStations(); // 실패해도 기본 위치로 검색
        });
    } else {
        alert("이 브라우저는 위치 정보를 지원하지 않습니다.");
        loadGasStations();
    }
}

// ---------------------------------------------------------
// [3] 주유소 데이터 로드 (서버 API 호출) - 핵심 부분
// ---------------------------------------------------------
function loadGasStations() {
    // 1. 좌표 변환 (WGS84 -> KATEC)
    // 오피넷 API가 KATEC 좌표를 요구하므로 변환
    if (typeof proj4 === 'undefined') {
        console.error("Proj4 라이브러리가 로드되지 않았습니다.");
        return;
    }

    var result = proj4("WGS84", katecDef, [appState.currentLon, appState.currentLat]);
    var katecX = Math.round(result[0]);
    var katecY = Math.round(result[1]);

    // 2. 현재 설정값(반경, 유종, 정렬)을 포함한 URL 생성
    const params = new URLSearchParams({
        x: katecX,
        y: katecY,
        radius: appState.radius,
        prodcd: appState.fuelType,
        sort: appState.sort
    });

    console.log(`데이터 요청: 반경 ${appState.radius}m, 유종 ${appState.fuelType}`);

    // 3. API 호출
    fetch(`/api/gas-stations?${params.toString()}`)
        .then(response => response.json())
        .then(data => {
            if (data && data.RESULT && data.RESULT.OIL) {
                displayStations(data.RESULT.OIL);
            } else {
                console.log("주변에 주유소가 없습니다.");
                document.getElementById('stationListContainer').innerHTML =
                    '<div class="empty-msg"><p>검색 결과가 없습니다.</p></div>';
                clearMarkers();
            }
        })
        .catch(error => {
            console.error("API 호출 실패:", error);
        });
}

// ---------------------------------------------------------
// [4] 화면 표시 (지도 마커 + 사이드바 리스트)
// ---------------------------------------------------------
function displayStations(stations) {
    clearMarkers(); // 기존 마커 삭제

    const listContainer = document.getElementById('stationListContainer');
    listContainer.innerHTML = ""; // 리스트 초기화

    // 반경에 따라 지도 레벨 자동 조정
    let zoomLevel = 4;
    if (appState.radius <= 1000) zoomLevel = 5;
    else if (appState.radius <= 3000) zoomLevel = 6;
    else zoomLevel = 7;
    map.setLevel(zoomLevel, {animate: true});

    stations.forEach((station, index) => {
        // (1) 좌표 변환 (KATEC -> WGS84)
        var cvt = proj4(katecDef, "WGS84", [parseFloat(station.GIS_X_COOR), parseFloat(station.GIS_Y_COOR)]);
        var markerPos = new kakao.maps.LatLng(cvt[1], cvt[0]);

        // (2) 마커 생성
        var marker = new kakao.maps.Marker({
            position: markerPos,
            map: map,
            title: station.OS_NM
        });
        appState.markers.push(marker); // 배열에 저장

        // (3) 리스트 아이템 생성
        var item = document.createElement('div');
        item.className = 'list-item';

        // 거리 계산 (m -> km 변환)
        let distDisplay = parseFloat(station.DISTANCE) < 1000
            ? Math.round(station.DISTANCE) + "m"
            : (station.DISTANCE / 1000).toFixed(1) + "km";

        item.innerHTML = `
            <strong>${station.OS_NM}</strong>
            <div style="margin-top:4px;">
                <span class="price">${station.PRICE}원</span>
                <span class="dist">${distDisplay}</span>
            </div>
            <div style="font-size:12px; color:#666; margin-top:2px;">
                ${station.POLL_DIV_CD}
            </div>
        `;

        // 클릭 이벤트 (리스트 클릭 -> 지도 이동)
        item.addEventListener('click', function() {
            map.panTo(markerPos);
            // 필요 시 인포윈도우 열기 로직 추가
        });

        // 마커 클릭 이벤트
        kakao.maps.event.addListener(marker, 'click', function() {
            // 상세 모달 열기 로직 등 연결
            alert(station.OS_NM + "\n가격: " + station.PRICE + "원");
        });

        listContainer.appendChild(item);
    });
}

// 마커 전체 삭제 함수
function clearMarkers() {
    appState.markers.forEach(m => m.setMap(null));
    appState.markers = [];
}

// ---------------------------------------------------------
// [5] 필터 적용 함수 (모달에서 '적용' 버튼 클릭 시 호출)
// ---------------------------------------------------------
function applyFilter() {
    // 1. HTML 요소에서 선택된 값 가져오기
    // (index.html에 해당 id를 가진 select/input 태그가 있어야 함)
    const radiusVal = document.getElementById('radiusSelect').value;
    const fuelVal = document.querySelector('input[name="fuelType"]:checked').value; // 라디오버튼 가정

    // 2. 상태 업데이트
    appState.radius = parseInt(radiusVal);
    appState.fuelType = fuelVal;
    // appState.sort = ... (정렬도 있다면 추가)

    // 3. 모달 닫기
    closeModal('filterModal');

    // 4. 데이터 다시 로드
    loadGasStations();
}

// ---------------------------------------------------------
// [6] 모달 제어 함수들
// ---------------------------------------------------------
function openModal(id) { document.getElementById(id).classList.add('open'); }
function closeModal(id) { document.getElementById(id).classList.remove('open'); }
function switchModal(closeId, openId) { closeModal(closeId); openModal(openId); }
window.onclick = function(e) {
    if(e.target.classList.contains('modal-overlay')) e.target.classList.remove('open');
};