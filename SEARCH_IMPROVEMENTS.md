# 검색 기능 개선 - 수정 가이드

## 수정 완료 항목

### 1. modal.js 전체 수정 완료 ✅
- 검색 시 기존 마커 제거 기능 추가
- 검색 모드 플래그 추가
- 초기 위치 복원 로직 추가

### 2. map.html에서 추가된 변수들 ✅
```javascript
// 초기 위치 저장 변수 추가됨
var initialMapState = {
    center: null,
    level: null
};

// 전역 변수로 노출
window.initialMapState = initialMapState;
window.markers = markers;
window.currentInfoWindow = currentInfoWindow;
window.isSearchMode = false;

// 초기 위치 저장 (사용자 위치 로드 후)
setTimeout(function() {
    initialMapState.center = map.getCenter();
    initialMapState.level = map.getLevel();
    console.log('초기 맵 상태 저장:', initialMapState);
}, 1000);
```

## 수동으로 수정할 부분

### map.html의 kakao.maps.event.addListener(map, 'click', ...) 부분

**현재 코드:**
```javascript
kakao.maps.event.addListener(map, 'click', function() {
    if (currentInfoWindow) {
        currentInfoWindow.close();
        currentInfoWindow = null;
    }

    var stationModal = document.getElementById('stationDetailModal');
    if (stationModal && stationModal.classList.contains('open')) {
        stationModal.classList.remove('open');
        
        // [추가] 지도를 이전 상태로 복원 (자연스러운 애니메이션)
        if (previousMapState && previousMapState.center) {
            // 먼저 줄 레벨 복원 (스먼 아웃)
            map.setLevel(previousMapState.level, {animate: true});
            // 약간의 딜레이 후 위치 이동
            setTimeout(function() {
                map.panTo(previousMapState.center);
            }, 300);
        }
    }
});
```

**변경할 코드:**
```javascript
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
```

## 동작 방식

### 검색 시:
1. `window.isSearchMode = true` 설정
2. 기존 마커 전부 제거 (검색 마커 제외)
3. 검색된 주유소에 빨간 별 마커 표시

### 상세 모달 닫을 때:
- **검색 모드인 경우**: 사용자의 초기 위치(맨 처음 로드된 위치)로 복원
- **일반 모드인 경우**: 상세 모달 열기 전 위치로 복원

### 필터 적용 시:
- 검색 모드 해제
- 검색 마커 제거
- 일반 주유소 목록 다시 로드

## 테스트 방법

1. 맵 로드 후 주유소 검색
2. 검색 결과 클릭하여 상세 정보 확인
3. 상세 모달 닫기 → **사용자 초기 위치로 돌아가는지 확인**
4. 다시 검색하여 다른 주유소 클릭
5. 상세 모달 닫기 → **다시 초기 위치로 돌아가는지 확인**
6. 필터 변경 → 검색 마커가 사라지고 일반 목록이 나타나는지 확인
