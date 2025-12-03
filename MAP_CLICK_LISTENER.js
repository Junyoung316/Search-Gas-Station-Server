// map.html의 kakao.maps.event.addListener 부분을 찾아서 아래 코드로 교체하세요
// 약 580-600 라인 근처입니다

kakao.maps.event.addListener(map, 'click', function() {
    if (currentInfoWindow) {
        currentInfoWindow.close();
        currentInfoWindow = null;
    }

    var stationModal = document.getElementById('stationDetailModal');
    if (stationModal && stationModal.classList.contains('open')) {
        stationModal.classList.remove('open');
        
        // [수정] 검색 모드인 경우 초기 위치로, 아니면 이전 위치로 복원
        if (window.isSearchMode && initialMapState && initialMapState.center) {
            // 검색 모드: 사용자 초기 위치로 복원
            map.setLevel(initialMapState.level, {animate: true});
            setTimeout(function() {
                map.panTo(initialMapState.center);
            }, 300);
            // 검색 모드 해제
            window.isSearchMode = false;
        } else if (previousMapState && previousMapState.center) {
            // 일반 모드: 이전 위치로 복원
            map.setLevel(previousMapState.level, {animate: true});
            setTimeout(function() {
                map.panTo(previousMapState.center);
            }, 300);
        }
    }
});
