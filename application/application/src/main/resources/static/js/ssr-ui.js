(function () {
    const PIN_STORAGE_KEY = 'ecoTracker.pinnedEcosystems';

    function getPinnedIds() {
        try {
            const value = window.localStorage.getItem(PIN_STORAGE_KEY);
            const parsed = value ? JSON.parse(value) : [];
            return Array.isArray(parsed) ? parsed : [];
        } catch (error) {
            console.error(error);
            return [];
        }
    }

    function savePinnedIds(ids) {
        window.localStorage.setItem(PIN_STORAGE_KEY, JSON.stringify(ids));
    }

    function isPinned(id) {
        return getPinnedIds().includes(id);
    }

    function togglePin(id) {
        const pins = getPinnedIds();
        if (pins.includes(id)) {
            savePinnedIds(pins.filter((item) => item !== id));
        } else {
            pins.push(id);
            savePinnedIds(pins);
        }
        applyPinnedState(document);
    }

    function applyPinnedState(root) {
        root.querySelectorAll('[data-ecosystem-id]').forEach((element) => {
            const id = element.dataset.ecosystemId;
            const pinned = isPinned(id);
            const card = element.querySelector('.eco-card');
            const button = element.querySelector('[data-pin-toggle]');
            const badge = element.querySelector('.pin-badge');

            if (card) {
                card.classList.toggle('pinned', pinned);
            }
            if (button) {
                button.textContent = pinned ? 'Unpin' : 'Pin';
                button.classList.toggle('btn-success', pinned);
                button.classList.toggle('btn-outline-secondary', !pinned);
            }
            if (badge) {
                badge.classList.toggle('d-none', !pinned);
            }
        });

        root.querySelectorAll('.workspace-card').forEach((element) => {
            const pinned = isPinned(element.dataset.ecosystemId);
            element.style.order = pinned ? '-1' : '0';
        });
    }

    function closeQuickPanel(panel) {
        if (!panel) return;
        panel.classList.add('d-none');
        panel.querySelectorAll('form').forEach((form) => {
            form.classList.add('d-none');
            form.reset();
        });
    }

    function openQuickPanel(id, mode) {
        document.querySelectorAll('[data-quick-panel]').forEach((panel) => {
            if (panel.dataset.quickPanel !== id) {
                closeQuickPanel(panel);
            }
        });

        const panel = document.querySelector(`[data-quick-panel="${id}"]`);
        if (!panel) return;

        panel.classList.remove('d-none');
        panel.querySelectorAll('[data-quick-form]').forEach((form) => {
            form.classList.toggle('d-none', form.dataset.quickForm !== mode);
        });

        const title = panel.querySelector('.quick-panel-title');
        if (title) {
            title.textContent = mode === 'task' ? 'Quick Task' : 'Quick Log';
        }
    }

    function closeClosestDetails(element) {
        const details = element.closest('details');
        if (details) {
            details.open = false;
        }
    }

    function openEcosystemEdit(root) {
        const view = root.querySelector('[data-ecosystem-view]');
        const form = root.querySelector('[data-ecosystem-edit-form]');
        if (!view || !form) return;
        view.classList.add('d-none');
        form.classList.remove('d-none');
    }

    function cancelEcosystemEdit(root) {
        const view = root.querySelector('[data-ecosystem-view]');
        const form = root.querySelector('[data-ecosystem-edit-form]');
        if (!view || !form) return;
        form.reset();
        form.classList.add('d-none');
        view.classList.remove('d-none');
    }

    function bindGlobalEvents() {
        document.addEventListener('click', (event) => {
            const pinButton = event.target.closest('[data-pin-toggle]');
            if (pinButton) {
                togglePin(pinButton.dataset.ecosystemId);
                return;
            }

            const quickToggle = event.target.closest('[data-quick-toggle]');
            if (quickToggle) {
                openQuickPanel(quickToggle.dataset.ecosystemId, quickToggle.dataset.quickToggle);
                return;
            }

            const quickClose = event.target.closest('[data-quick-close]');
            if (quickClose) {
                closeQuickPanel(document.querySelector(`[data-quick-panel="${quickClose.dataset.quickClose}"]`));
                return;
            }

            const closeDetails = event.target.closest('[data-close-details]');
            if (closeDetails) {
                closeClosestDetails(closeDetails);
                return;
            }

            const openEdit = event.target.closest('[data-open-ecosystem-edit]');
            if (openEdit) {
                const root = openEdit.closest('.edit-ecosystem-shell');
                if (root) {
                    openEcosystemEdit(root);
                }
                return;
            }

            const cancelEdit = event.target.closest('[data-cancel-ecosystem-edit]');
            if (cancelEdit) {
                const root = cancelEdit.closest('.edit-ecosystem-shell');
                if (root) {
                    cancelEcosystemEdit(root);
                }
                return;
            }

            const resetTaskFilters = event.target.closest('[data-reset-task-filters]');
            if (resetTaskFilters) {
                const form = document.getElementById('task-filter-form');
                if (!form) return;

                const search = form.querySelector('input[name="taskSearch"]');
                const taskFilter = form.querySelector('select[name="taskFilter"]');
                const taskSource = form.querySelector('select[name="taskSource"]');

                if (search) search.value = '';
                if (taskFilter) taskFilter.value = 'ALL';
                if (taskSource) taskSource.value = 'ALL';

                if (window.htmx) {
                    window.htmx.ajax('GET', resetTaskFilters.dataset.taskFragmentUrl, {
                        source: resetTaskFilters,
                        target: '#tasks-panel',
                        swap: 'innerHTML'
                    });
                }
            }
        });

        document.body.addEventListener('htmx:afterSwap', (event) => {
            applyPinnedState(event.target);
        });

        document.body.addEventListener('htmx:beforeRequest', (event) => {
            const form = event.target.closest ? event.target.closest('form') : null;
            if (!form) return;
            const feedbackTarget = form.getAttribute('hx-target');
            if (!feedbackTarget) return;
            const feedback = document.querySelector(feedbackTarget);
            if (feedback) {
                feedback.innerHTML = '';
            }
        });

        document.body.addEventListener('htmx:afterRequest', (event) => {
            const xhr = event.detail && event.detail.xhr;
            if (!xhr) return;
            if (xhr.getResponseHeader('HX-Refresh') === 'true') {
                document.querySelectorAll('[data-quick-panel]').forEach(closeQuickPanel);
                document.querySelectorAll('details[open]').forEach((details) => { details.open = false; });
                document.querySelectorAll('.edit-ecosystem-shell').forEach(cancelEcosystemEdit);
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            bindGlobalEvents();
            applyPinnedState(document);
        });
    } else {
        bindGlobalEvents();
        applyPinnedState(document);
    }
})();
