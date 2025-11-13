// src/main/resources/static/js/modal.js

console.log("1. modal.js 파일이 로드되었습니다.");

document.addEventListener("DOMContentLoaded", function() {
    console.log("2. DOM 로드 완료");

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
            // ... 필터 적용 로직 ...
            const selectedRadius = document.querySelector('input[name="radius"]:checked').value;
            const selectedProdcd = document.querySelector('input[name="prodcd"]:checked').value;

            window.currentRadius = parseInt(selectedRadius, 10);
            window.currentProdcd = selectedProdcd;

            closeModal();

            if (typeof window.reloadMapData === "function") {
                window.reloadMapData();
            } else {
                console.error("reloadMapData 함수가 없습니다.");
            }
        });
    }
});