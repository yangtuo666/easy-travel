function searchSpots() {
    const cityName = document.getElementById('cityInput').value.trim();
    if (!cityName) {
        alert('请输入城市名称');
        return;
    }

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

    const gridHtml = spots.map(spot => `
        <div class="spot-card">
            <div class="spot-name">${escapeHtml(spot.name)}</div>
            <div class="spot-address">${escapeHtml(spot.address || '地址暂无')}</div>
            <div class="spot-rating">
                <span class="rating-star">★</span>
                <span class="rating-score">${escapeHtml(spot.rating || '0.0')}</span>
            </div>
        </div>
    `).join('');

    resultContainer.innerHTML = `<div class="spots-grid">${gridHtml}</div>`;
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
