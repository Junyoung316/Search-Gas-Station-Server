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
    // (예: 62px)
    document.documentElement.style.setProperty('--header-height', headerHeight + 'px');

    const filterButton = document.getElementById("filterButton");
    const filterModal = document.getElementById("filterModal");
    const modalOverlay = document.getElementById("modalOverlay");
    const applyButton = document.getElementById("applyFilterButton");
    const closeBtn = filterModal ? filterModal.querySelector(".close-btn") : null;

    // 요소가 잘 찾아졌는지 확인
    console.log("버튼 찾음?:", filterButton);
    console.log("모달 찾음?:", filterModal);

    if (!filterButton || !filterModal) {
        console.error("HTML 요소를 찾을 수 없습니다. ID를 확인하세요.");
        return;
    }

    function openModal() {
        console.log("모달 열기 실행!"); // 클릭 시 이 로그가 떠야 함
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
            window.currentSort = parseInt(selectedSort, 10); // [추가]

            closeModal();
            window.reloadMapData();
        });
    }

    // ---------------------------------------------------------
    // [추가] 하단 주유소 모달(Sheet) 닫기 이벤트
    // ---------------------------------------------------------
    const stationModal = document.getElementById('stationDetailModal');

    // 1. 하단 모달의 'X' 버튼 클릭 시
    if (stationModal) {
        const closeBtn = stationModal.querySelector('.close-btn');
        closeBtn.addEventListener('click', function(event) {
            event.preventDefault();
            stationModal.classList.remove('open');
        });
    }
});
