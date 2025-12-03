// src/main/resources/static/js/modal.js

console.log("1. modal.js 파일이 로드되었습니다.");

document.addEventListener("DOMContentLoaded", function() {
    console.log("2. DOM 로드 완료");

    // ---------------------------------------------------------
    // [추가] 1. 헤더 높이 측정
    // ---------------------------------------------------------
    const headerBar = document.querySelector(".header-bar");
    let headerHeight = 0; // 기본값

    if (headerBar) {
        headerHeight = headerBar.offsetHeight; // 헤더의 실제 픽셀 높이
    }

    // 2. 측정된 높이를 '--header-height'라는 CSS 변수로 저장
    document.documentElement.style.setProperty('--header-height', headerHeight + 'px');

    // ---------------------------------------------------------
    // [추가] 검색바 애니메이션 처리
    // ---------------------------------------------------------
    const searchButton = document.getElementById("searchButton");
    const searchCloseBtn = document.getElementById("searchCloseBtn");
    const searchInput = document.getElementById("searchInput");

    console.log("검색 버튼:", searchButton);
    console.log("닫기 버튼:", searchCloseBtn);
    console.log("검색 입력:", searchInput);

    if (searchButton && headerBar) {
        // 검색 버튼 클릭 시
        searchButton.addEventListener("click", function(event) {
            event.preventDefault();
            event.stopPropagation();
            console.log("검색 버튼 클릭!");
            headerBar.classList.add("search-active");
            // 검색창에 포커스
            setTimeout(() => {
                if (searchInput) searchInput.focus();
            }, 300);
        });
    }

    if (searchCloseBtn && headerBar) {
        // 검색바 닫기 버튼 클릭 시
        searchCloseBtn.addEventListener("click", function(event) {
            event.preventDefault();
            event.stopPropagation();
            console.log("닫기 버튼 클릭!");
            headerBar.classList.remove("search-active");
            if (searchInput) searchInput.value = ""; // 검색어 초기화
        });
        
        // [추가] 마우스오버 이벤트로 클릭 가능 여부 확인
        searchCloseBtn.addEventListener("mouseenter", function() {
            console.log("닫기 버튼 위에 마우스가 있습니다.");
        });
    }

    // ESC 키로 검색바 닫기
    document.addEventListener("keydown", function(event) {
        if (event.key === "Escape" && headerBar && headerBar.classList.contains("search-active")) {
            headerBar.classList.remove("search-active");
            if (searchInput) searchInput.value = "";
        }
    });

    // 검색 입력창에서 엔터 키 처리
    if (searchInput) {
        searchInput.addEventListener("keypress", function(event) {
            if (event.key === "Enter") {
                const searchTerm = searchInput.value.trim();
                if (searchTerm) {
                    console.log("검색어:", searchTerm);
                    performSearch(searchTerm);
                }
            }
        });
    }
    
    // [수정] 검색 기능 구현 (기존 마커 제거 + 초기 위치 복원)
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
                    if (headerBar) headerBar.classList.remove("search-active");
                    if (searchInput) searchInput.value = "";
                } else {
                    // 여러 개 - 리스트 표시
                    showSearchResults(results, searchTerm);
                    
                    // 검색창 닫기
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
    
    // [추가] 검색 결과 표시 함수
    function showSearchResults(results, searchTerm) {
        const listContainer = document.getElementById('stationListContainer');
        const stationListModal = document.getElementById('stationListModal');
        
        if (!listContainer || !stationListModal) return;
        
        listContainer.innerHTML = '';
        
        if (results.length === 0) {
            // 결과 없음
            listContainer.innerHTML = `
                <div style="padding: 40px 20px; text-align: center;">
                    <i class="fa-solid fa-magnifying-glass" style="font-size: 3em; color: #ccc; margin-bottom: 15px;"></i>
                    <p style="color: #666; font-size: 1.1em; margin-bottom: 5px;">
                        '${searchTerm}' 주유소를 찾을 수 없습니다
                    </p>
                    <p style="color: #999; font-size: 0.9em;">
                        다른 검색어를 시도해보세요.
                    </p>
                </div>
            `;
        } else {
            // 결과 목록 표시
            const resultHeader = document.createElement('div');
            resultHeader.style.cssText = 'padding: 15px 20px; background-color: #f8f9fa; border-bottom: 2px solid #e9ecef; font-weight: bold; color: #495057;';
            resultHeader.innerHTML = `검색 결과: ${results.length}개`;
            listContainer.appendChild(resultHeader);
            
            // 좌표 변환을 위한 정의
            var katecDef = "+proj=tmerc +lat_0=38 +lon_0=128 +k=0.9999 +x_0=400000 +y_0=600000 +ellps=bessel +towgs84=-115.80,474.99,674.11,1.16,-2.31,-1.63,6.43 +units=m +no_defs";
            
            results.forEach(function(station) {
                var distStr = "";
                var distVal = parseFloat(station.distance);
                if (!isNaN(distVal)) {
                    if (distVal >= 1000) distStr = (distVal / 1000).toFixed(1) + "km";
                    else distStr = Math.round(distVal) + "m";
                }
                
                var listItem = document.createElement('div');
                listItem.className = 'list-item';
                listItem.innerHTML = `
                    <span class="dist">${distStr}</span>
                    <b>${station.name}</b><br>
                    <span class="price">${station.price}원</span>
                `;
                
                // 클릭 이벤트
                (function(stationId, distance, stationData) {
                    listItem.addEventListener('click', function() {
                        // 좌표 변환
                        var x = parseFloat(stationData.katecX);
                        var y = parseFloat(stationData.katecY);
                        var converted = proj4(katecDef, "WGS84", [x, y]);
                        var lon = converted[0];
                        var lat = converted[1];
                        var latlng = new kakao.maps.LatLng(lat, lon);
                        
                        // 마커 추가
                        addSearchMarker(latlng, stationData.name);
                        
                        if (window.showStationDetail) {
                            window.showStationDetail(stationId, distance, latlng);
                        }
                    });
                })(station.id, station.distance, station);
                
                listContainer.appendChild(listItem);
            });
        }
        
        // 모달 열기
        stationListModal.classList.add('open');
    }
    
    // [추가] 검색 결과 마커 추가 함수
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
    }

    // ---------------------------------------------------------
    // 필터 모달 처리
    // ---------------------------------------------------------
    const filterButton = document.getElementById("filterButton");
    const filterModal = document.getElementById("filterModal");
    const modalOverlay = document.getElementById("modalOverlay");
    const applyButton = document.getElementById("applyFilterButton");
    const closeBtn = filterModal ? filterModal.querySelector(".close-btn") : null;

    // 요소가 잘 찾아졌는지 확인
    console.log("필터 버튼 찾음?:", filterButton);
    console.log("필터 모달 찾음?:", filterModal);

    if (!filterButton || !filterModal) {
        console.error("HTML 요소를 찾을 수 없습니다. ID를 확인하세요.");
        return;
    }

    function openModal() {
        console.log("모달 열기 실행!");
        filterModal.classList.add("open");
        modalOverlay.classList.add("open");
    }

    function closeModal() {
        filterModal.classList.remove("open");
        modalOverlay.classList.remove("open");
    }

    // 이벤트 리스너 연결
    filterButton.addEventListener("click", function(event) {
        event.preventDefault();
        openModal();
    });

    if (modalOverlay) modalOverlay.addEventListener("click", closeModal);
    if (closeBtn) closeBtn.addEventListener("click", function(e) { e.preventDefault(); closeModal(); });

    if (applyButton) {
        applyButton.addEventListener("click", function() {

            // (1~2) 반경, 유종 읽기 (기존 코드)
            const selectedRadius = document.querySelector('input[name="radius"]:checked').value;
            const selectedProdcd = document.querySelector('input[name="prodcd"]:checked').value;

            // [추가] (3) 정렬 값 읽기
            const selectedSort = document.querySelector('input[name="sort"]:checked').value;

            // 전역 변수 업데이트
            window.currentRadius = parseInt(selectedRadius, 10);
            window.currentProdcd = selectedProdcd;
            window.currentSort = parseInt(selectedSort, 10);

            // 검색 모드 해제 및 검색 마커 제거
            window.isSearchMode = false;
            if (window.searchMarker) {
                window.searchMarker.setMap(null);
                window.searchMarker = null;
            }

            closeModal();
            window.reloadMapData();
        });
    }

    // ---------------------------------------------------------
    // [수정] 하단 주유소 모달(Sheet) 닫기 이벤트
    // ---------------------------------------------------------
    const stationModal = document.getElementById('stationDetailModal');

    // 1. 하단 모달의 'X' 버튼 클릭 시
    if (stationModal) {
        const closeBtn = stationModal.querySelector('.close-btn');
        if (closeBtn) {
            closeBtn.addEventListener('click', function(event) {
                event.preventDefault();
                stationModal.classList.remove('open');
                
                // [수정] 검색 모드인 경우 초기 위치로, 아니면 이전 위치로 복원
                if (window.isSearchMode && window.initialMapState && window.initialMapState.center && window.map) {
                    // 검색 모드: 사용자 초기 위치로 복원
                    window.map.setLevel(window.initialMapState.level, {animate: true});
                    setTimeout(function() {
                        window.map.panTo(window.initialMapState.center);
                    }, 300);
                    // 검색 모드 해제
                    window.isSearchMode = false;
                } else if (window.previousMapState && window.previousMapState.center && window.map) {
                    // 일반 모드: 이전 위치로 복원
                    window.map.setLevel(window.previousMapState.level, {animate: true});
                    setTimeout(function() {
                        window.map.panTo(window.previousMapState.center);
                    }, 300);
                }
            });
        }
    }
});
