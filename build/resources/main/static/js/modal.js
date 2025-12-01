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
                    // TODO: 여기에 검색 로직 추가
                    // 예: 주유소 리스트 필터링
                }
            }
        });
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
        if (closeBtn) {
            closeBtn.addEventListener('click', function(event) {
                event.preventDefault();
                stationModal.classList.remove('open');
            });
        }
    }
});
