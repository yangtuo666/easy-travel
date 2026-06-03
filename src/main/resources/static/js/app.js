let selectedSpots = [];

// 页面加载时检查是否从酒店页面返回，并恢复状态
window.addEventListener('DOMContentLoaded', function() {
    const fromHotelPage = sessionStorage.getItem('fromHotelPage');
    if (fromHotelPage === 'true') {
        // 清除标记
        sessionStorage.removeItem('fromHotelPage');
        console.log('从酒店页面返回，恢复搜索状态');
        
        // 恢复搜索状态
        restoreSearchState();
    }
});

function restoreSearchState() {
    try {
        const stateStr = sessionStorage.getItem('searchState');
        if (!stateStr) {
            console.log('没有找到保存的搜索状态');
            return;
        }
        
        const state = JSON.parse(stateStr);
        console.log('恢复搜索状态:', state);
        
        // 恢复城市输入
        if (state.cityName) {
            document.getElementById('cityInput').value = state.cityName;
        }
        
        // 恢复景点数据
        if (state.allSpots && state.allSpots.length > 0) {
            window.currentSpots = state.allSpots;
            displayResults(state.allSpots);
            
            // 恢复选中的景点
            selectedSpots = state.selectedSpots || [];
            
            // 更新选中状态显示（等待 DOM 渲染完成）
            setTimeout(() => {
                selectedSpots.forEach(spot => {
                    const index = state.allSpots.findIndex(s => s.name === spot.name);
                    if (index !== -1) {
                        const card = document.querySelector(`.spot-card[data-index="${index}"]`);
                        if (card) {
                            card.classList.add('selected');
                        }
                    }
                });
                updateSelectionBar();
                console.log('搜索状态恢复完成');
            }, 100);
        }
    } catch (e) {
        console.error('恢复搜索状态失败:', e);
    }
}

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

    // 保存选中景点数据到 sessionStorage（hotel.html 需要）
    sessionStorage.setItem('spotData', JSON.stringify(selectedSpots));
    
    // 保存完整的搜索状态，以便返回时恢复
    const searchState = {
        cityName: document.getElementById('cityInput').value.trim(),
        selectedSpots: selectedSpots,
        allSpots: window.currentSpots || []
    };
    sessionStorage.setItem('searchState', JSON.stringify(searchState));

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
            // 保存酒店数据
            sessionStorage.setItem('hotelData', JSON.stringify(data));
            // 保存搜索状态，以便返回时恢复
            sessionStorage.setItem('fromHotelPage', 'true');
            // 使用 replace 而不是直接赋值，这样可以控制历史行为
            window.location.href = 'hotel.html';
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
