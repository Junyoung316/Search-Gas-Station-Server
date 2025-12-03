// 검색 기능 개선 스크립트
// 이 코드를 modal.js의 performSearch 함수와 addSearchMarker 함수를 대체합니다.

// [수정된 버전] 검색 기능 구현
function performSearch(searchTerm) {
    // 검색 모드 활성화
    window.isSearchMode = true;
    
    // 서버에 검색 요청
    fetch(`/api/search-stations?query=${encodeURIComponent(searchTerm)}`)
        .then(response => response.json())
        .then(data => {
            console.log('검색 API 응답:', data);
            
            if (!data.success) {
                alert('검색 중 오류가 발생했습니다.');
                window.isSearchMode = false;
                return;
            }
            
            const results = data.results || [];
            console.log('검색 결과:', results.length + '개');
            
            // 검색 결과가 있으면 기존 마커 제거 (검색 마커 제외)
            if (results.length > 0) {
                removeAllMarkersExceptSearch();
            }
            
            // 검색 결과에 따른 처리
            if (results.length === 0) {
                // 결과 없음 - 메시지 표시
                showSearchResults([], searchTerm);
                window.isSearchMode = false;
            } else if (results.length === 1) {
                // 1개 - 상세 정보 모달 표시
                const station = results[0];
                
                // 좌표 변환
                var katecDef = "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=400000 +y_0=600000 +ellps=bessel +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43 +units=m +no_defs";
                var x = parseFloat(station.katecX);
                var y = parseFloat(station.katecY);
                var converted = proj4(katecDef, "WGS84", [x, y]);
                var lon = converted[0];
                var lat = converted[1];
                var latlng = new kakao.maps.LatLng(lat, lon);
                
                // 마커 추가 및 상세 정보 표시
                addSearchMarker(latlng, station.name);
                if (window.showStationDetail) {
                    window.showStationDetail(station.id, station.distance, latlng);
                }
                
                // 검색창 닫기
                const headerBar = document.querySelector(".header-bar");
                const searchInput = document.getElementById("searchInput");
                if (headerBar) headerBar.classList.remove("search-active");
                if (searchInput) searchInput.value = "";
            } else {
                // 여러 개 - 리스트 표시
                showSearchResults(results, searchTerm);
                
                // 검색창 닫기
                const headerBar = document.querySelector(".header-bar");
                const searchInput = document.getElementById("searchInput");
                if (headerBar) headerBar.classList.remove("search-active");
                if (searchInput) searchInput.value = "";
            }
        })
        .catch(error => {
            console.error('검색 실패:', error);
            alert('검색 중 오류가 발생했습니다.');
            window.isSearchMode = false;
        });
}

// [추가] 검색 마커를 제외한 모든 마커 제거
function removeAllMarkersExceptSearch() {
    if (window.markers && Array.isArray(window.markers)) {
        for (var i = 0; i < window.markers.length; i++) {
            window.markers[i].setMap(null);
        }
        window.markers = [];
    }
    
    // 인포윈도우도 닫기
    if (window.currentInfoWindow) {
        window.currentInfoWindow.close();
        window.currentInfoWindow = null;
    }
    
    console.log('기존 마커 제거 완료 (검색 마커 제외)');
}

// [수정된 버전] 검색 결과 마커 추가 함수
function addSearchMarker(position, title) {
    // 기존 검색 마커 제거
    if (window.searchMarker) {
        window.searchMarker.setMap(null);
    }
    
    // 빨간색 마커 이미지 URL
    var imageSrc = 'https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png';
    var imageSize = new kakao.maps.Size(24, 35);
    var markerImage = new kakao.maps.MarkerImage(imageSrc, imageSize);
    
    // 새 마커 생성
    var marker = new kakao.maps.Marker({
        map: window.map,
        position: position,
        title: title,
        image: markerImage
    });
    
    // 전역 변수에 저장
    window.searchMarker = marker;
    
    console.log('검색 마커 추가:', title);
}

// showSearchResults 함수는 기존 그대로 사용
// (검색 결과 리스트 표시 함수)
