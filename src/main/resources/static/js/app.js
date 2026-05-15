let selectedSpots = [];

function searchSpots() {
    const cityName = document.getElementById('cityInput').value.trim();
    if (!cityName) {
        alert('请输入城市名称');
        return;
    }

    selectedSpots = [];
    updateSelectionBar();

    const resultContainer = document.getElementById('resultContainer');
    resultContainer.innerHTML = '<div class="loading">正在搜索景点...</div>';

    fetch(`/api/scenic-spots/top-rated?cityName=${encodeURIComponent(cityName)}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('网络请求失败');
            }
            return response.json();
        })
        .then(data => {
            displayResults(data);
        })
        .catch(error => {
            resultContainer.innerHTML = `<div class="error-msg">搜索失败，请稍后重试</div>`;
            console.error('Error:', error);
        });
}

function handleKeyPress(event) {
    if (event.key === 'Enter') {
        searchSpots();
    }
}

function displayResults(spots) {
    const resultContainer = document.getElementById('resultContainer');

    if (!spots || spots.length === 0) {
        resultContainer.innerHTML = '<div class="empty-msg">未找到相关景点，请尝试其他城市</div>';
        return;
    }

    const gridHtml = spots.map((spot, index) => `
        <div class="spot-card" data-index="${index}" onclick="toggleSelection(${index}, event)">
            <div class="spot-name">${escapeHtml(spot.name)}</div>
            <div class="spot-address">${escapeHtml(spot.address || '地址暂无')}</div>
            <div class="spot-rating">
                <span class="rating-star">★</span>
                <span class="rating-score">${escapeHtml(spot.rating || '0.0')}</span>
            </div>
        </div>
    `).join('');

    resultContainer.innerHTML = `<div class="spots-grid">${gridHtml}</div>`;

    window.currentSpots = spots;
}

function toggleSelection(index, event) {
    event.stopPropagation();

    const spot = window.currentSpots[index];
    const card = document.querySelector(`.spot-card[data-index="${index}"]`);

    const existingIndex = selectedSpots.findIndex(s => s.name === spot.name);

    if (existingIndex > -1) {
        selectedSpots.splice(existingIndex, 1);
        card.classList.remove('selected');
    } else {
        selectedSpots.push(spot);
        card.classList.add('selected');
    }

    updateSelectionBar();
}

function updateSelectionBar() {
    const selectionBar = document.getElementById('selectionBar');
    const selectionCount = document.getElementById('selectionCount');

    if (selectedSpots.length > 0) {
        selectionBar.classList.add('active');
        selectionCount.textContent = `已选择 ${selectedSpots.length} 个景点`;
    } else {
        selectionBar.classList.remove('active');
    }
}

function searchHotels() {
    if (selectedSpots.length === 0) {
        alert('请先选择景点');
        return;
    }

    const points = selectedSpots.map(spot => {
        const [lng, lat] = spot.location.split(',').map(Number);
        return { longitude: lng, latitude: lat };
    });

    sessionStorage.setItem('spotData', JSON.stringify(selectedSpots));

    fetch('/api/scenic-spots/top-hotel', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(points)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('网络请求失败');
            }
            return response.json();
        })
        .then(data => {
            sessionStorage.setItem('hotelData', JSON.stringify(data));
            window.open('hotel.html', '_blank');
        })
        .catch(error => {
            console.error('Error:', error);
            alert('酒店查询失败，请稍后重试');
        });
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
