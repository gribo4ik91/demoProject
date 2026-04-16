(function () {
    const { getJson, sendJson, loadAuthStatus } = window.EcoTrackerApi;
    const { escapeHtml, parseNullableNumber, parseNullableInteger } = window.EcoTrackerUtils;

    const PIN_STORAGE_KEY = 'ecoTracker.pinnedEcosystems';
    let ecosystemCards = [];
    let toastCounter = 0;
    let workspaceReloadTimer = null;
    let currentWorkspacePage = 0;
    let totalWorkspaceCards = 0;
    let workspaceHasNext = false;
    const workspacePageSize = 9;
    let quickActionState = {
        ecosystemId: null,
        ecosystemName: '',
        mode: null
    };

    window.addEventListener('load', async () => {
        bindEvents();
        await loadAuthStatus();
        await loadWorkspace();
    });

    async function loadWorkspace() {
        setLoadingState();
        currentWorkspacePage = 0;

        try {
            const cardsParams = buildWorkspaceQueryParams({ page: 0, size: workspacePageSize });
            const overviewParams = buildWorkspaceQueryParams();
            const [cardPage, overview] = await Promise.all([
                getJson(`/api/v1/ecosystems/cards${cardsParams}`, 'Failed to load ecosystems.'),
                getJson(`/api/v1/ecosystems/overview${overviewParams}`, 'Failed to load ecosystems.')
            ]);

            clearToasts();
            totalWorkspaceCards = cardPage.totalElements ?? 0;
            workspaceHasNext = Boolean(cardPage.hasNext);

            if ((cardPage.items || []).length === 0) {
                ecosystemCards = [];
                renderOverview(overview);
                renderPriority([]);
                renderPinned([]);
                renderEcosystems([], totalWorkspaceCards);
                renderWorkspacePagination();
                return;
            }

            ecosystemCards = normalizeWorkspaceCards(cardPage.items || []);
            renderOverview(overview);
            renderPriority(selectPriorityCards(ecosystemCards));
            renderPinned(ecosystemCards.filter((card) => isPinned(card.id)));
            renderEcosystems(ecosystemCards, totalWorkspaceCards);
            renderWorkspacePagination();
        } catch (error) {
            ecosystemCards = [];
            totalWorkspaceCards = 0;
            workspaceHasNext = false;
            renderOverview([]);
            renderPriority([]);
            renderPinned([]);
            renderWorkspaceError(error.message || 'Something went wrong while loading ecosystems.');
            renderWorkspacePagination();
            showToast(error.message || 'Something went wrong while loading ecosystems.', 'danger');
        }
    }

    async function loadMoreWorkspaceCards() {
        if (!workspaceHasNext) {
            return;
        }

        const nextPage = currentWorkspacePage + 1;
        const button = document.getElementById('load-more-button');
        button.disabled = true;
        button.textContent = 'Loading...';

        try {
            const params = buildWorkspaceQueryParams({ page: nextPage, size: workspacePageSize });
            const cardPage = await getJson(`/api/v1/ecosystems/cards${params}`, 'Failed to load more ecosystems.');
            const nextCards = normalizeWorkspaceCards(cardPage.items || []);

            currentWorkspacePage = cardPage.page ?? nextPage;
            totalWorkspaceCards = cardPage.totalElements ?? totalWorkspaceCards;
            workspaceHasNext = Boolean(cardPage.hasNext);
            ecosystemCards = [...ecosystemCards, ...nextCards];

            renderPriority(selectPriorityCards(ecosystemCards));
            renderPinned(ecosystemCards.filter((card) => isPinned(card.id)));
            renderEcosystems(ecosystemCards, totalWorkspaceCards);
            renderWorkspacePagination();
        } catch (error) {
            showToast(error.message || 'Failed to load more ecosystems.', 'danger');
            renderWorkspacePagination();
        }
    }

    function normalizeWorkspaceCards(cards) {
        return cards.map((ecosystem) => ({
            ...ecosystem,
            summaryStatus: ecosystem.status || 'NO_RECENT_DATA',
            lastRecordedAt: ecosystem.lastRecordedAt || null,
            logsLast7Days: ecosystem.logsLast7Days ?? 0,
            openTasks: ecosystem.openTasks ?? 0,
            overdueTasks: ecosystem.overdueTasks ?? 0
        }));
    }

    function selectPriorityCards(cards) {
        return cards
            .filter((card) => card.summaryStatus === 'NEEDS_ATTENTION' || card.overdueTasks > 0)
            .slice(0, 4);
    }

    function buildWorkspaceQueryParams(options = {}) {
        const params = new URLSearchParams();
        const search = document.getElementById('search-input').value.trim();
        const status = document.getElementById('status-filter').value;
        const sort = document.getElementById('sort-select').value;

        if (search) params.set('search', search);
        if (status && status !== 'ALL') params.set('status', status);
        if (sort) params.set('sort', sort);
        if (options.page !== undefined) params.set('page', String(options.page));
        if (options.size !== undefined) params.set('size', String(options.size));

        const query = params.toString();
        return query ? `?${query}` : '';
    }

    function scheduleWorkspaceReload() {
        window.clearTimeout(workspaceReloadTimer);
        workspaceReloadTimer = window.setTimeout(loadWorkspace, 220);
    }

    function renderOverview(overview) {
        const safeOverview = overview && !Array.isArray(overview) ? overview : {
            totalEcosystems: 0,
            needsAttention: 0,
            noRecentData: 0,
            overdueTasks: 0
        };

        document.getElementById('overview-metrics').innerHTML = [
            metricCard('Total Ecosystems', safeOverview.totalEcosystems),
            metricCard('Needs Attention', safeOverview.needsAttention),
            metricCard('Overdue Tasks', safeOverview.overdueTasks),
            metricCard('No Recent Data', safeOverview.noRecentData)
        ].join('');
    }

    function metricCard(label, value) {
        return `<div class="col-6"><div class="metric p-3"><div class="text-muted small">${escapeHtml(label)}</div><div class="metric-value fw-semibold">${escapeHtml(String(value))}</div></div></div>`;
    }

    function renderPriority(cards) {
        const container = document.getElementById('priority-container');
        container.innerHTML = cards.length === 0
            ? '<div class="col-12"><div class="border rounded-3 p-4 text-center text-muted">Nothing urgent right now. Your workspace looks calm.</div></div>'
            : cards.map((card) => renderCard(card, true)).join('');
    }

    function renderPinned(cards) {
        const container = document.getElementById('pinned-container');
        container.innerHTML = cards.length === 0
            ? '<div class="col-12"><div class="border rounded-3 p-4 text-center text-muted">No pinned ecosystems yet.</div></div>'
            : cards.map((card) => renderCard(card, false)).join('');
    }

    function renderEcosystems(cards, totalElements) {
        const container = document.getElementById('ecosystems-container');
        const results = document.getElementById('workspace-results');
        const orderedCards = orderCardsForDisplay(cards);
        const safeTotal = Number.isFinite(totalElements) ? totalElements : cards.length;

        if (safeTotal === 0 && !hasActiveWorkspaceFilters()) {
            container.innerHTML = '<div class="col-12"><div class="border rounded-3 p-4 text-center"><h6 class="mb-2">No ecosystems yet</h6><p class="text-muted mb-0">Create your first ecosystem above to start tracking observations.</p></div></div>';
            results.textContent = '0 ecosystems';
            return;
        }

        if (cards.length === 0) {
            container.innerHTML = '<div class="col-12"><div class="border rounded-3 p-4 text-center"><h6 class="mb-2">No ecosystems match these filters</h6><p class="text-muted mb-0">Try a different search or clear the current filters.</p></div></div>';
            results.textContent = '0 matching ecosystems';
            return;
        }

        container.innerHTML = orderedCards.map((card) => renderCard(card, false)).join('');
        results.textContent = orderedCards.length >= safeTotal
            ? `${safeTotal} ecosystem${safeTotal === 1 ? '' : 's'} visible`
            : `Showing ${orderedCards.length} of ${safeTotal} ecosystems`;
    }

    function renderWorkspacePagination() {
        const pagination = document.getElementById('workspace-pagination');
        const button = document.getElementById('load-more-button');

        if (!workspaceHasNext || ecosystemCards.length === 0) {
            pagination.classList.add('d-none');
            button.disabled = false;
            button.textContent = 'Load More';
            return;
        }

        pagination.classList.remove('d-none');
        button.disabled = false;
        button.textContent = `Load More (${ecosystemCards.length}/${totalWorkspaceCards})`;
    }

    function hasActiveWorkspaceFilters() {
        return Boolean(document.getElementById('search-input').value.trim())
            || document.getElementById('status-filter').value !== 'ALL'
            || document.getElementById('sort-select').value !== 'PRIORITY';
    }

    function renderCard(card, compact) {
        return `
            <div class="${compact ? 'col-lg-6' : 'col-md-6 col-xl-4'}">
                <div class="eco-card p-3 p-lg-4 ${isPinned(card.id) ? 'pinned' : ''}">
                    <div class="d-flex justify-content-between align-items-start gap-3 mb-3">
                        <div>
                            <div class="d-flex align-items-center gap-2 flex-wrap mb-2">
                                ${statusBadge(card.summaryStatus)}
                                ${isPinned(card.id) ? '<span class="badge text-bg-light border">Pinned</span>' : ''}
                            </div>
                            <h4 class="h5 mb-1">${escapeHtml(card.name)}</h4>
                            <div class="text-muted small">${escapeHtml(typeLabel(card.type))}</div>
                        </div>
                        <button type="button" class="btn btn-sm ${isPinned(card.id) ? 'btn-success' : 'btn-outline-secondary'}" data-action="toggle-pin" data-ecosystem-id="${card.id}">${isPinned(card.id) ? 'Unpin' : 'Pin'}</button>
                    </div>
                    <p class="text-muted small mb-3">${escapeHtml(card.description || 'No description provided yet.')}</p>
                    <div class="freshness-note mb-3 ${freshnessClass(card.lastRecordedAt)}">${escapeHtml(freshnessLabel(card.lastRecordedAt))}</div>
                    <div class="mini-metrics mb-3">
                        <div class="mini-metric"><div class="mini-label">Last activity</div><div class="mini-value">${escapeHtml(formatDate(card.lastRecordedAt))}</div></div>
                        <div class="mini-metric"><div class="mini-label">Logs last 7 days</div><div class="mini-value">${escapeHtml(String(card.logsLast7Days))}</div></div>
                        <div class="mini-metric"><div class="mini-label">Open tasks</div><div class="mini-value">${escapeHtml(String(card.openTasks))}</div></div>
                        <div class="mini-metric"><div class="mini-label">Overdue tasks</div><div class="mini-value">${escapeHtml(String(card.overdueTasks))}</div></div>
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <a href="ecosystem.html?id=${card.id}" class="btn btn-outline-success flex-grow-1">Open Dashboard</a>
                        <button type="button" class="btn btn-outline-secondary" data-action="open-quick-action" data-mode="log" data-ecosystem-id="${card.id}">Quick Log</button>
                        <button type="button" class="btn btn-outline-secondary" data-action="open-quick-action" data-mode="task" data-ecosystem-id="${card.id}">Quick Task</button>
                    </div>
                </div>
            </div>
        `;
    }

    function orderCardsForDisplay(cards) {
        return [...cards].sort((left, right) => Number(isPinned(right.id)) - Number(isPinned(left.id)));
    }

    function statusBadge(status) {
        if (status === 'STABLE') return '<span class="status-pill status-stable">Stable</span>';
        if (status === 'NEEDS_ATTENTION') return '<span class="status-pill status-warning">Needs Attention</span>';
        return '<span class="status-pill status-neutral">No Recent Data</span>';
    }

    function typeLabel(type) {
        const labels = {
            FORMICARIUM: 'Formicarium',
            FLORARIUM: 'Florarium',
            INDOOR_PLANTS: 'Indoor Plants',
            DIY_INCUBATOR: 'DIY Incubator'
        };
        return labels[type] || type;
    }

    function formatDate(value) {
        if (!value) return 'No logs yet';
        return new Date(value).toLocaleString([], { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
    }

    function openQuickActionModal(ecosystemId, mode) {
        const ecosystem = ecosystemCards.find((card) => card.id === ecosystemId);
        if (!ecosystem) {
            showToast('Could not find the selected ecosystem.', 'danger');
            return;
        }

        quickActionState = { ecosystemId, ecosystemName: ecosystem.name, mode };
        clearQuickActionError();
        document.getElementById('quick-log-form').reset();
        document.getElementById('quick-task-form').reset();
        document.getElementById('quick-action-title').textContent = mode === 'log' ? 'Add quick log' : 'Create quick task';
        document.getElementById('quick-action-subtitle').textContent = `${ecosystem.name}: ${mode === 'log' ? 'capture a fresh observation' : 'add a maintenance task'} without leaving the workspace.`;
        document.getElementById('quick-log-form').classList.toggle('show', mode === 'log');
        document.getElementById('quick-task-form').classList.toggle('show', mode === 'task');
        document.getElementById('quick-action-overlay').classList.add('show');
        document.body.classList.add('modal-open');
    }

    function closeQuickActionModal() {
        document.getElementById('quick-action-overlay').classList.remove('show');
        document.body.classList.remove('modal-open');
        clearQuickActionError();
        quickActionState = { ecosystemId: null, ecosystemName: '', mode: null };
    }

    function handleOverlayClick(event) {
        if (event.target.id === 'quick-action-overlay') {
            closeQuickActionModal();
        }
    }

    async function submitQuickLog(event) {
        event.preventDefault();
        if (!quickActionState.ecosystemId) return;

        const submitButton = event.submitter;
        submitButton.disabled = true;
        clearQuickActionError();

        try {
            await sendJson(`/api/v1/ecosystems/${quickActionState.ecosystemId}/logs`, 'POST', {
                eventType: document.getElementById('quick-log-event').value,
                temperatureC: parseNullableNumber(document.getElementById('quick-log-temp').value),
                humidityPercent: parseNullableInteger(document.getElementById('quick-log-humidity').value),
                notes: document.getElementById('quick-log-notes').value.trim() || null
            }, 'Failed to create quick log.');

            const ecosystemName = quickActionState.ecosystemName;
            closeQuickActionModal();
            showToast(`Quick log saved for ${ecosystemName}.`, 'success');
            await loadWorkspace();
        } catch (error) {
            showQuickActionError(error.message || 'Failed to create quick log.');
        } finally {
            submitButton.disabled = false;
        }
    }

    async function submitQuickTask(event) {
        event.preventDefault();
        if (!quickActionState.ecosystemId) return;

        const submitButton = event.submitter;
        submitButton.disabled = true;
        clearQuickActionError();
        const title = document.getElementById('quick-task-title').value.trim();

        if (!title) {
            showQuickActionError('Task title is required.');
            submitButton.disabled = false;
            return;
        }

        try {
            await sendJson(`/api/v1/ecosystems/${quickActionState.ecosystemId}/tasks`, 'POST', {
                title,
                taskType: document.getElementById('quick-task-type').value,
                dueDate: document.getElementById('quick-task-due-date').value || null
            }, 'Failed to create quick task.');

            const ecosystemName = quickActionState.ecosystemName;
            closeQuickActionModal();
            showToast(`Quick task created for ${ecosystemName}.`, 'success');
            await loadWorkspace();
        } catch (error) {
            showQuickActionError(error.message || 'Failed to create quick task.');
        } finally {
            submitButton.disabled = false;
        }
    }

    function showQuickActionError(message) {
        const error = document.getElementById('quick-action-error');
        error.textContent = message;
        error.classList.remove('d-none');
    }

    function clearQuickActionError() {
        const error = document.getElementById('quick-action-error');
        error.textContent = '';
        error.classList.add('d-none');
    }

    function togglePin(ecosystemId) {
        const ids = getPinnedIds();
        if (ids.includes(ecosystemId)) {
            savePinnedIds(ids.filter((id) => id !== ecosystemId));
        } else {
            ids.push(ecosystemId);
            savePinnedIds(ids);
        }

        renderPinned(ecosystemCards.filter((card) => isPinned(card.id)));
        renderPriority(selectPriorityCards(ecosystemCards));
        renderEcosystems(ecosystemCards, totalWorkspaceCards);
    }

    function clearWorkspaceControls() {
        document.getElementById('search-input').value = '';
        document.getElementById('status-filter').value = 'ALL';
        document.getElementById('sort-select').value = 'PRIORITY';
        loadWorkspace();
    }

    function getPinnedIds() {
        try {
            const value = localStorage.getItem(PIN_STORAGE_KEY);
            const parsed = value ? JSON.parse(value) : [];
            return Array.isArray(parsed) ? parsed : [];
        } catch (error) {
            console.error(error);
            return [];
        }
    }

    function savePinnedIds(ids) {
        localStorage.setItem(PIN_STORAGE_KEY, JSON.stringify(ids));
    }

    function isPinned(ecosystemId) {
        return getPinnedIds().includes(ecosystemId);
    }

    function setLoadingState() {
        document.getElementById('workspace-results').textContent = 'Loading ecosystems...';
        document.getElementById('workspace-pagination').classList.add('d-none');
        document.getElementById('ecosystems-container').innerHTML = '<div class="col-12"><div class="border rounded-3 p-4 text-center text-muted">Loading ecosystems...</div></div>';
    }

    function renderWorkspaceError(message) {
        document.getElementById('priority-container').innerHTML = `<div class="col-12"><div class="border rounded-3 p-4 text-center text-danger">${escapeHtml(message)}</div></div>`;
        document.getElementById('pinned-container').innerHTML = '<div class="col-12"><div class="border rounded-3 p-4 text-center text-muted">Pins will appear here after the workspace loads.</div></div>';
        document.getElementById('ecosystems-container').innerHTML = '<div class="col-12"><div class="border rounded-3 p-4 text-center text-danger-emphasis bg-danger-subtle">Could not load ecosystems right now.</div></div>';
        document.getElementById('workspace-results').textContent = 'Workspace unavailable';
    }

    async function submitEcosystem(event) {
        event.preventDefault();
        const submitButton = event.submitter;
        submitButton.disabled = true;

        try {
            await sendJson('/api/v1/ecosystems', 'POST', {
                name: document.getElementById('eco-name').value,
                type: document.getElementById('eco-type').value,
                description: document.getElementById('eco-desc').value
            }, 'Failed to create ecosystem.');

            document.getElementById('create-eco-form').reset();
            showToast('Ecosystem created successfully.', 'success');
            await loadWorkspace();
        } catch (error) {
            showToast(error.message || 'Failed to create ecosystem.', 'danger');
        } finally {
            submitButton.disabled = false;
        }
    }

    function showToast(message, type) {
        const variant = normalizeToastType(type);
        const toastId = `toast-${++toastCounter}`;
        const toast = document.createElement('div');
        toast.id = toastId;
        toast.className = `toast-card toast-${variant}`;
        toast.innerHTML = `
            <div class="toast-bar"></div>
            <div class="p-3">
                <div class="d-flex justify-content-between align-items-start gap-3">
                    <div>
                        <div class="fw-semibold">${escapeHtml(toastTitle(variant))}</div>
                        <div class="small text-muted mt-1">${escapeHtml(message)}</div>
                    </div>
                    <button type="button" class="btn-close btn-close-sm" aria-label="Close" data-action="dismiss-toast" data-toast-id="${toastId}"></button>
                </div>
            </div>
        `;

        document.getElementById('toast-stack').appendChild(toast);
        if (variant !== 'danger') window.setTimeout(() => dismissToast(toastId), 3200);
    }

    function dismissToast(toastId) {
        const toast = document.getElementById(toastId);
        if (toast) toast.remove();
    }

    function clearToasts() {
        document.getElementById('toast-stack').innerHTML = '';
    }

    function normalizeToastType(type) {
        return type === 'success' || type === 'danger' || type === 'warning' ? type : 'info';
    }

    function toastTitle(type) {
        if (type === 'success') return 'Saved';
        if (type === 'danger') return 'Something went wrong';
        if (type === 'warning') return 'Heads up';
        return 'Update';
    }

    function freshnessLabel(lastRecordedAt) {
        if (!lastRecordedAt) return 'No observations recorded yet';
        const ageDays = daysSince(lastRecordedAt);
        if (ageDays >= 7) return `Stale activity: last updated ${ageDays} day${ageDays === 1 ? '' : 's'} ago`;
        return `Fresh activity: updated ${ageDays === 0 ? 'today' : `${ageDays} day${ageDays === 1 ? '' : 's'} ago`}`;
    }

    function freshnessClass(lastRecordedAt) {
        if (!lastRecordedAt) return 'freshness-empty';
        return daysSince(lastRecordedAt) >= 7 ? 'freshness-stale' : 'freshness-fresh';
    }

    function daysSince(value) {
        const now = new Date();
        const date = new Date(value);
        return Math.max(0, Math.floor((now.getTime() - date.getTime()) / 86400000));
    }

    document.addEventListener('keydown', (event) => {
        if (event.key === 'Escape' && quickActionState.mode) {
            closeQuickActionModal();
        }
    });

    function bindEvents() {
        document.getElementById('create-eco-form').addEventListener('submit', submitEcosystem);
        document.getElementById('clear-workspace-controls').addEventListener('click', clearWorkspaceControls);
        document.getElementById('search-input').addEventListener('input', scheduleWorkspaceReload);
        document.getElementById('status-filter').addEventListener('change', loadWorkspace);
        document.getElementById('sort-select').addEventListener('change', loadWorkspace);
        document.getElementById('load-more-button').addEventListener('click', loadMoreWorkspaceCards);
        document.getElementById('quick-action-overlay').addEventListener('click', handleOverlayClick);
        document.getElementById('close-quick-action-button').addEventListener('click', closeQuickActionModal);
        document.getElementById('quick-log-form').addEventListener('submit', submitQuickLog);
        document.getElementById('quick-task-form').addEventListener('submit', submitQuickTask);
        document.getElementById('toast-stack').addEventListener('click', handleToastClick);
        document.addEventListener('click', handleDocumentClick);
    }

    function handleDocumentClick(event) {
        const actionElement = event.target.closest('[data-action]');
        if (!actionElement) {
            return;
        }

        const { action } = actionElement.dataset;
        if (action === 'toggle-pin') {
            togglePin(actionElement.dataset.ecosystemId);
        } else if (action === 'open-quick-action') {
            openQuickActionModal(actionElement.dataset.ecosystemId, actionElement.dataset.mode);
        } else if (action === 'close-quick-action') {
            closeQuickActionModal();
        }
    }

    function handleToastClick(event) {
        const dismissButton = event.target.closest('[data-action="dismiss-toast"]');
        if (!dismissButton) {
            return;
        }

        dismissToast(dismissButton.dataset.toastId);
    }
})();
