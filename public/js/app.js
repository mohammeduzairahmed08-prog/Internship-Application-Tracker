/**
 * Internship Application Tracker - Frontend Application Controller
 */

let currentUser = null;
let currentAppPage = 0;
let totalAppPages = 1;
let currentDetailApp = null;
let debounceTimer = null;
let statusChartInstance = null;
let trendChartInstance = null;

// Bootstrap Modal Instances
let appModalInstance = null;
let detailsModalInstance = null;
let interviewModalInstance = null;
let quickStatusModalInstance = null;

document.addEventListener('DOMContentLoaded', async () => {
    // Initialize Bootstrap Modals
    appModalInstance = new bootstrap.Modal(document.getElementById('applicationModal'));
    detailsModalInstance = new bootstrap.Modal(document.getElementById('applicationDetailsModal'));
    interviewModalInstance = new bootstrap.Modal(document.getElementById('interviewModal'));
    quickStatusModalInstance = new bootstrap.Modal(document.getElementById('quickStatusModal'));

    // Check user authentication
    await checkAuth();

    // Setup navigation tabs
    setupNavigation();

    // Default view: Dashboard
    loadDashboard();
});

// ----------------- Auth & User Profile -----------------
async function checkAuth() {
    try {
        currentUser = await API.auth.me();
        if (!currentUser) return;

        const initial = (currentUser.fullName || 'U').charAt(0).toUpperCase();
        document.getElementById('sidebarAvatar').textContent = initial;
        document.getElementById('topbarAvatar').textContent = initial;
        document.getElementById('sidebarUserName').textContent = currentUser.fullName;
        document.getElementById('topbarUserName').textContent = currentUser.fullName.split(' ')[0];
        document.getElementById('sidebarUserEmail').textContent = currentUser.email;
    } catch (err) {
        window.location.href = '/login.html';
    }
}

async function logout() {
    try {
        await API.auth.logout();
    } catch (e) {
        // Continue to redirect
    }
    window.location.href = '/login.html';
}

// ----------------- View Navigation -----------------
function setupNavigation() {
    document.querySelectorAll('.sidebar-link[data-view]').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetView = link.getAttribute('data-view');
            switchView(targetView);
        });
    });
}

function switchView(viewId) {
    document.querySelectorAll('.app-view').forEach(view => view.classList.add('d-none'));
    const target = document.getElementById(viewId);
    if (target) {
        target.classList.remove('d-none');
    }

    document.querySelectorAll('.sidebar-link').forEach(link => {
        if (link.getAttribute('data-view') === viewId) {
            link.classList.add('active');
        } else {
            link.classList.remove('active');
        }
    });

    // Close sidebar on mobile after navigation
    const sidebar = document.getElementById('sidebar');
    if (sidebar.classList.contains('show')) {
        sidebar.classList.remove('show');
    }

    // Refresh view data
    if (viewId === 'dashboardView') {
        loadDashboard();
    } else if (viewId === 'applicationsView') {
        loadApplications(0);
    } else if (viewId === 'interviewsView') {
        loadAllInterviews();
    } else if (viewId === 'deadlinesView') {
        loadDeadlinesView();
    }
}

function toggleSidebar() {
    document.getElementById('sidebar').classList.toggle('show');
}

function handleGlobalSearch(event) {
    if (event.key === 'Enter') {
        const query = event.target.value.trim();
        switchView('applicationsView');
        document.getElementById('filterSearch').value = query;
        loadApplications(0);
    }
}

// ----------------- Dashboard -----------------
async function loadDashboard() {
    try {
        const [summary, distribution, trends, deadlines, interviews] = await Promise.all([
            API.dashboard.getSummary(),
            API.dashboard.getStatusDistribution(),
            API.dashboard.getApplicationTrends(),
            API.dashboard.getUpcomingDeadlines(),
            API.dashboard.getUpcomingInterviews()
        ]);

        if (summary) {
            document.getElementById('kpiTotal').textContent = summary.totalApplications;
            document.getElementById('kpiSubmitted').textContent = summary.applicationsSubmitted;
            document.getElementById('kpiAssessment').textContent = summary.applicationsInAssessment;
            document.getElementById('kpiInterviews').textContent = summary.interviewsScheduled;
            document.getElementById('kpiOffers').textContent = summary.offersReceived;
            document.getElementById('kpiRejected').textContent = summary.rejectedApplications;
            document.getElementById('kpiSuccessRate').textContent = `${summary.successRate}%`;
            document.getElementById('upcomingDeadlinesBadge').textContent = `${summary.upcomingDeadlines} Alerts`;
            document.getElementById('upcomingInterviewsBadge').textContent = `${summary.upcomingInterviews} Upcoming`;

            // Update topbar notification badge
            const totalAlerts = summary.upcomingDeadlines + summary.upcomingInterviews;
            const notifBadge = document.getElementById('notificationBadge');
            if (totalAlerts > 0) {
                notifBadge.textContent = totalAlerts;
                notifBadge.classList.remove('d-none');
            } else {
                notifBadge.classList.add('d-none');
            }
        }

        renderStatusChart(distribution);
        renderTrendChart(trends);
        renderDashboardDeadlines(deadlines);
        renderDashboardInterviews(interviews);
        updateNotificationDropdown(deadlines, interviews);
    } catch (err) {
        console.error('Failed to load dashboard:', err);
    }
}

function renderStatusChart(distribution) {
    const canvas = document.getElementById('statusChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const labels = (distribution || []).map(d => d.label);
    const data = (distribution || []).map(d => d.count);
    const colors = (distribution || []).map(d => d.color);

    if (statusChartInstance) {
        statusChartInstance.destroy();
    }

    const totalCount = data.reduce((a, b) => a + b, 0);

    statusChartInstance = new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: totalCount === 0 ? [1] : data,
                backgroundColor: totalCount === 0 ? ['#e2e8f0'] : colors,
                borderWidth: 2,
                borderColor: '#ffffff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        boxWidth: 12,
                        font: { size: 11 }
                    }
                },
                tooltip: {
                    enabled: totalCount > 0
                }
            },
            cutout: '68%'
        }
    });
}

function renderTrendChart(trends) {
    const canvas = document.getElementById('trendChart');
    if (!canvas) return;
    const ctx = canvas.getContext('2d');

    const labels = (trends || []).map(t => {
        const parts = t.date.split('-');
        return `${parts[1]}/${parts[2]}`;
    });
    const counts = (trends || []).map(t => t.count);

    if (trendChartInstance) {
        trendChartInstance.destroy();
    }

    trendChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: 'Applications Submitted',
                data: counts,
                fill: true,
                backgroundColor: 'rgba(79, 70, 229, 0.1)',
                borderColor: '#4f46e5',
                tension: 0.3,
                borderWidth: 2,
                pointRadius: 2,
                pointHoverRadius: 5
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: { precision: 0 },
                    grid: { color: '#f1f5f9' }
                },
                x: {
                    grid: { display: false },
                    ticks: { maxTicksLimit: 10 }
                }
            },
            plugins: {
                legend: { display: false }
            }
        }
    });
}

function renderDashboardDeadlines(deadlines) {
    const container = document.getElementById('dashboardDeadlinesContainer');
    if (!container) return;

    if (!deadlines || deadlines.length === 0) {
        container.innerHTML = '<div class="text-center text-muted py-4 small"><i class="bi bi-check-circle me-1 text-success"></i> No approaching deadlines this week.</div>';
        return;
    }

    const itemsHtml = deadlines.slice(0, 5).map(d => {
        const isOverdue = d.overdue;
        const daysText = isOverdue
            ? `<span class="badge bg-danger-subtle text-danger border border-danger-subtle">Overdue by ${Math.abs(d.daysRemaining)} days</span>`
            : `<span class="badge bg-warning-subtle text-warning-emphasis border border-warning-subtle">Due in ${d.daysRemaining} days</span>`;

        return `
            <div class="d-flex align-items-center justify-content-between p-2 mb-2 rounded bg-light border-start border-3 ${isOverdue ? 'border-danger' : 'border-warning'}">
                <div>
                    <strong class="d-block text-dark small">${escapeHtml(d.companyName)}</strong>
                    <span class="text-muted" style="font-size:0.775rem;">${escapeHtml(d.jobRole)} • ${formatDate(d.deadline)}</span>
                </div>
                <div class="d-flex align-items-center gap-2">
                    ${daysText}
                    <button class="btn btn-sm btn-link text-primary p-0" onclick="viewApplicationDetails(${d.applicationId})">
                        <i class="bi bi-chevron-right"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = itemsHtml;
}

function renderDashboardInterviews(interviews) {
    const container = document.getElementById('dashboardInterviewsContainer');
    if (!container) return;

    if (!interviews || interviews.length === 0) {
        container.innerHTML = '<div class="text-center text-muted py-4 small"><i class="bi bi-calendar-x me-1"></i> No upcoming interviews scheduled.</div>';
        return;
    }

    const itemsHtml = interviews.slice(0, 5).map(i => {
        const meetBtn = i.meetingLink
            ? `<a href="${escapeHtml(i.meetingLink)}" target="_blank" class="btn btn-sm btn-outline-primary py-0 px-2" style="font-size:0.75rem;"><i class="bi bi-camera-video me-1"></i>Join</a>`
            : '';

        return `
            <div class="d-flex align-items-center justify-content-between p-2 mb-2 rounded bg-light border-start border-3 border-primary">
                <div>
                    <strong class="d-block text-dark small">${escapeHtml(i.companyName)} - ${escapeHtml(i.interviewRound)}</strong>
                    <span class="text-muted" style="font-size:0.775rem;">
                        <i class="bi bi-clock me-1"></i>${formatDateTime(i.interviewDate)} • ${escapeHtml(i.interviewMode)}
                    </span>
                </div>
                <div class="d-flex align-items-center gap-2">
                    ${meetBtn}
                    <button class="btn btn-sm btn-link text-secondary p-0" onclick="viewApplicationDetails(${i.applicationId})">
                        <i class="bi bi-chevron-right"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');

    container.innerHTML = itemsHtml;
}

function updateNotificationDropdown(deadlines, interviews) {
    const list = document.getElementById('notificationList');
    if (!list) return;

    const allAlerts = [];
    (deadlines || []).forEach(d => {
        allAlerts.push(`
            <li class="dropdown-item py-2 border-bottom">
                <span class="badge ${d.overdue ? 'bg-danger' : 'bg-warning text-dark'} mb-1">${d.overdue ? 'Overdue' : 'Approaching'}</span>
                <div class="fw-semibold text-truncate">${escapeHtml(d.companyName)} - ${escapeHtml(d.jobRole)}</div>
                <small class="text-muted">Deadline: ${formatDate(d.deadline)}</small>
            </li>
        `);
    });
    (interviews || []).forEach(i => {
        allAlerts.push(`
            <li class="dropdown-item py-2 border-bottom">
                <span class="badge bg-primary mb-1">Interview</span>
                <div class="fw-semibold text-truncate">${escapeHtml(i.companyName)} - ${escapeHtml(i.interviewRound)}</div>
                <small class="text-muted">${formatDateTime(i.interviewDate)}</small>
            </li>
        `);
    });

    if (allAlerts.length === 0) {
        list.innerHTML = '<div class="text-muted small p-2 text-center">No alerts at this time.</div>';
    } else {
        list.innerHTML = allAlerts.slice(0, 6).join('');
    }
}

// ----------------- Applications View -----------------
function debounceLoadApplications() {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
        loadApplications(0);
    }, 300);
}

function resetFilters() {
    document.getElementById('filterSearch').value = '';
    document.getElementById('filterStatus').value = '';
    document.getElementById('filterWorkMode').value = '';
    document.getElementById('filterLocation').value = '';
    document.getElementById('filterSort').value = 'createdAt,desc';
    loadApplications(0);
}

async function loadApplications(page = 0) {
    currentAppPage = page;
    const search = document.getElementById('filterSearch').value.trim();
    const status = document.getElementById('filterStatus').value;
    const workMode = document.getElementById('filterWorkMode').value;
    const location = document.getElementById('filterLocation').value.trim();
    const sortVal = document.getElementById('filterSort').value.split(',');

    const params = {
        search,
        status,
        workMode,
        location,
        page: currentAppPage,
        size: 10,
        sortBy: sortVal[0] || 'createdAt',
        direction: sortVal[1] || 'desc'
    };

    const tbody = document.getElementById('applicationsTableBody');
    tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4"><span class="spinner-border spinner-border-sm me-2"></span>Loading applications...</td></tr>';

    try {
        const pageData = await API.applications.list(params);
        if (!pageData || !pageData.content || pageData.content.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" class="text-center text-muted py-4">No internship applications found matching your criteria.</td></tr>';
            renderPagination(0, 0, 0);
            return;
        }

        totalAppPages = pageData.totalPages;
        renderApplicationsTable(pageData.content);
        renderPagination(pageData.number, pageData.totalPages, pageData.totalElements);
    } catch (err) {
        tbody.innerHTML = `<tr><td colspan="8" class="text-center text-danger py-4">Error loading applications: ${escapeHtml(err.message)}</td></tr>`;
    }
}

function renderApplicationsTable(apps) {
    const tbody = document.getElementById('applicationsTableBody');
    tbody.innerHTML = apps.map(app => {
        const statusClass = getStatusClass(app.status);
        const priorityClass = getPriorityClass(app.priority);
        const formattedStipend = (app.stipend !== null && app.stipend !== undefined && app.stipend > 0)
            ? `$${Number(app.stipend).toLocaleString('en-US', { minimumFractionDigits: 0, maximumFractionDigits: 2 })}`
            : '<span class="text-muted">-</span>';

        const deadlineText = app.deadline
            ? formatDate(app.deadline)
            : '<span class="text-muted">No deadline</span>';

        return `
            <tr>
                <td>
                    <div class="fw-semibold text-dark">${escapeHtml(app.companyName)}</div>
                    ${app.location ? `<small class="text-muted"><i class="bi bi-geo-alt"></i> ${escapeHtml(app.location)}</small>` : ''}
                </td>
                <td>
                    <div class="fw-medium">${escapeHtml(app.jobRole)}</div>
                    <span class="badge bg-light text-secondary border" style="font-size:0.7rem;">${escapeHtml(app.workMode)}</span>
                </td>
                <td>${formatDate(app.applicationDate)}</td>
                <td>${deadlineText}</td>
                <td>${formattedStipend}</td>
                <td>
                    <span class="status-badge ${statusClass}">
                        <i class="bi bi-dot"></i>${escapeHtml(app.status)}
                    </span>
                </td>
                <td>
                    <span class="priority-badge ${priorityClass}">
                        ${escapeHtml(app.priority)}
                    </span>
                </td>
                <td class="text-end">
                    <div class="dropdown">
                        <button class="btn btn-sm btn-light border" data-bs-toggle="dropdown" aria-expanded="false">
                            <i class="bi bi-three-dots-vertical"></i>
                        </button>
                        <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0">
                            <li><a class="dropdown-item small" href="#" onclick="viewApplicationDetails(${app.id})"><i class="bi bi-eye me-2 text-primary"></i>View Details</a></li>
                            <li><a class="dropdown-item small" href="#" onclick="openEditApplicationModal(${app.id})"><i class="bi bi-pencil me-2 text-secondary"></i>Edit</a></li>
                            <li><a class="dropdown-item small" href="#" onclick="openQuickStatusModal(${app.id}, '${app.status}')"><i class="bi bi-arrow-repeat me-2 text-info"></i>Change Status</a></li>
                            <li><a class="dropdown-item small" href="#" onclick="openScheduleInterviewModal(${app.id})"><i class="bi bi-calendar-plus me-2 text-success"></i>Schedule Interview</a></li>
                            <li><hr class="dropdown-divider"></li>
                            <li><a class="dropdown-item small text-danger" href="#" onclick="confirmDeleteApplication(${app.id})"><i class="bi bi-trash me-2"></i>Delete</a></li>
                        </ul>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function renderPagination(currentPage, totalPages, totalElements) {
    const info = document.getElementById('paginationInfo');
    const list = document.getElementById('paginationList');

    if (totalElements === 0) {
        info.textContent = 'Showing 0 of 0 applications';
        list.innerHTML = '';
        return;
    }

    info.textContent = `Page ${currentPage + 1} of ${totalPages} (${totalElements} total applications)`;

    let pagesHtml = '';
    // Previous
    pagesHtml += `
        <li class="page-item ${currentPage === 0 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="loadApplications(${currentPage - 1})"><i class="bi bi-chevron-left"></i></a>
        </li>
    `;

    for (let p = 0; p < totalPages; p++) {
        if (p === 0 || p === totalPages - 1 || Math.abs(p - currentPage) <= 1) {
            pagesHtml += `
                <li class="page-item ${p === currentPage ? 'active' : ''}">
                    <a class="page-link" href="#" onclick="loadApplications(${p})">${p + 1}</a>
                </li>
            `;
        } else if (p === currentPage - 2 || p === currentPage + 2) {
            pagesHtml += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
        }
    }

    // Next
    pagesHtml += `
        <li class="page-item ${currentPage >= totalPages - 1 ? 'disabled' : ''}">
            <a class="page-link" href="#" onclick="loadApplications(${currentPage + 1})"><i class="bi bi-chevron-right"></i></a>
        </li>
    `;

    list.innerHTML = pagesHtml;
}

// ----------------- Application CRUD Modals -----------------
function openNewApplicationModal() {
    document.getElementById('applicationForm').reset();
    document.getElementById('appEditId').value = '';
    document.getElementById('applicationModalTitle').textContent = 'Add Internship Application';
    document.getElementById('formApplicationDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('formStatus').value = 'APPLIED';
    document.getElementById('formWorkMode').value = 'REMOTE';
    document.getElementById('formPriority').value = 'MEDIUM';
    appModalInstance.show();
}

async function openEditApplicationModal(id) {
    try {
        const app = await API.applications.get(id);
        if (!app) return;

        document.getElementById('appEditId').value = app.id;
        document.getElementById('applicationModalTitle').textContent = 'Edit Internship Application';
        document.getElementById('formCompany').value = app.companyName;
        document.getElementById('formRole').value = app.jobRole;
        document.getElementById('formWorkMode').value = app.workMode;
        document.getElementById('formStatus').value = app.status;
        document.getElementById('formPriority').value = app.priority;
        document.getElementById('formApplicationDate').value = app.applicationDate;
        document.getElementById('formDeadline').value = app.deadline || '';
        document.getElementById('formLocation').value = app.location || '';
        document.getElementById('formStipend').value = app.stipend || '';
        document.getElementById('formAppUrl').value = app.applicationUrl || '';
        document.getElementById('formWebsite').value = app.companyWebsite || '';
        document.getElementById('formDescription').value = app.jobDescription || '';
        document.getElementById('formNotes').value = app.notes || '';

        appModalInstance.show();
    } catch (err) {
        alert('Failed to load application: ' + err.message);
    }
}

async function handleApplicationSubmit(e) {
    e.preventDefault();
    const id = document.getElementById('appEditId').value;
    const saveBtn = document.getElementById('btnSaveApp');

    const appData = {
        companyName: document.getElementById('formCompany').value.trim(),
        jobRole: document.getElementById('formRole').value.trim(),
        workMode: document.getElementById('formWorkMode').value,
        status: document.getElementById('formStatus').value,
        priority: document.getElementById('formPriority').value,
        applicationDate: document.getElementById('formApplicationDate').value,
        deadline: document.getElementById('formDeadline').value || null,
        location: document.getElementById('formLocation').value.trim() || null,
        stipend: document.getElementById('formStipend').value ? parseFloat(document.getElementById('formStipend').value) : 0,
        applicationUrl: document.getElementById('formAppUrl').value.trim() || null,
        companyWebsite: document.getElementById('formWebsite').value.trim() || null,
        jobDescription: document.getElementById('formDescription').value.trim() || null,
        notes: document.getElementById('formNotes').value.trim() || null
    };

    // Date validation
    if (appData.deadline && appData.applicationDate && appData.deadline < appData.applicationDate) {
        alert('Application deadline cannot precede the application date.');
        return;
    }

    try {
        saveBtn.disabled = true;
        saveBtn.textContent = 'Saving...';

        if (id) {
            await API.applications.update(id, appData);
        } else {
            await API.applications.create(appData);
        }

        appModalInstance.hide();
        loadApplications(currentAppPage);
        loadDashboard();
    } catch (err) {
        alert('Error saving application: ' + err.message);
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save Application';
    }
}

async function viewApplicationDetails(id) {
    try {
        const app = await API.applications.get(id);
        const interviews = await API.interviews.list(id);
        currentDetailApp = app;

        document.getElementById('detailCompanyName').textContent = app.companyName;
        document.getElementById('detailJobRole').textContent = app.jobRole;

        const statusBadge = document.getElementById('detailStatusBadge');
        statusBadge.className = `status-badge ${getStatusClass(app.status)}`;
        statusBadge.innerHTML = `<i class="bi bi-dot"></i>${escapeHtml(app.status)}`;

        document.getElementById('detailWorkModeBadge').textContent = app.workMode;

        const priorityBadge = document.getElementById('detailPriorityBadge');
        priorityBadge.className = `priority-badge ${getPriorityClass(app.priority)}`;
        priorityBadge.textContent = app.priority;

        document.getElementById('detailAppDate').textContent = formatDate(app.applicationDate);
        document.getElementById('detailDeadline').textContent = app.deadline ? formatDate(app.deadline) : 'None';
        document.getElementById('detailStipend').textContent = app.stipend ? `$${app.stipend}` : 'N/A';
        document.getElementById('detailLocation').textContent = app.location || 'Not specified';

        const websiteSpan = document.getElementById('detailWebsite');
        websiteSpan.innerHTML = app.companyWebsite
            ? `<a href="${escapeHtml(app.companyWebsite)}" target="_blank" class="text-decoration-none"><i class="bi bi-box-arrow-up-right me-1"></i>Visit</a>`
            : '-';

        const urlSpan = document.getElementById('detailUrl');
        urlSpan.innerHTML = app.applicationUrl
            ? `<a href="${escapeHtml(app.applicationUrl)}" target="_blank" class="text-decoration-none"><i class="bi bi-link-45deg me-1"></i>Posting Link</a>`
            : '-';

        document.getElementById('detailNotes').textContent = app.notes || 'No personal notes recorded.';

        // Render Interviews
        const intContainer = document.getElementById('detailInterviewsContainer');
        if (!interviews || interviews.length === 0) {
            intContainer.innerHTML = '<div class="text-muted small text-center py-2">No interview rounds scheduled yet. Click "+ Schedule Interview" above.</div>';
        } else {
            intContainer.innerHTML = interviews.map(i => `
                <div class="p-2 mb-2 bg-white rounded border d-flex justify-content-between align-items-center">
                    <div>
                        <strong class="d-block small text-dark">${escapeHtml(i.interviewRound)}</strong>
                        <span class="text-muted" style="font-size:0.75rem;">
                            <i class="bi bi-clock me-1"></i>${formatDateTime(i.interviewDate)} • ${escapeHtml(i.interviewMode)}
                        </span>
                        ${i.interviewerName ? `<div class="text-muted" style="font-size:0.75rem;"><i class="bi bi-person me-1"></i>${escapeHtml(i.interviewerName)}</div>` : ''}
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <span class="badge ${getOutcomeBadgeClass(i.outcome)}" style="font-size:0.7rem;">${escapeHtml(i.outcome)}</span>
                        ${i.meetingLink ? `<a href="${escapeHtml(i.meetingLink)}" target="_blank" class="btn btn-sm btn-outline-primary py-0 px-2" style="font-size:0.75rem;">Join</a>` : ''}
                        <button class="btn btn-sm btn-link text-danger p-0" onclick="deleteInterview(${i.id}, ${app.id})"><i class="bi bi-trash"></i></button>
                    </div>
                </div>
            `).join('');
        }

        detailsModalInstance.show();
    } catch (err) {
        alert('Failed to load application details: ' + err.message);
    }
}

function openScheduleInterviewFromDetails() {
    if (!currentDetailApp) return;
    openScheduleInterviewModal(currentDetailApp.id);
}

function editCurrentApplication() {
    if (!currentDetailApp) return;
    detailsModalInstance.hide();
    openEditApplicationModal(currentDetailApp.id);
}

function deleteCurrentApplication() {
    if (!currentDetailApp) return;
    confirmDeleteApplication(currentDetailApp.id, true);
}

async function confirmDeleteApplication(id, fromDetails = false) {
    if (confirm('Are you sure you want to delete this application? All associated interview rounds will also be deleted.')) {
        try {
            await API.applications.delete(id);
            if (fromDetails) {
                detailsModalInstance.hide();
            }
            loadApplications(currentAppPage);
            loadDashboard();
        } catch (err) {
            alert('Error deleting application: ' + err.message);
        }
    }
}

// ----------------- Quick Status Modal -----------------
function openQuickStatusModal(id, currentStatus) {
    document.getElementById('quickStatusAppId').value = id;
    document.getElementById('quickStatusSelect').value = currentStatus;
    quickStatusModalInstance.show();
}

async function saveQuickStatus() {
    const id = document.getElementById('quickStatusAppId').value;
    const status = document.getElementById('quickStatusSelect').value;

    try {
        await API.applications.updateStatus(id, status);
        quickStatusModalInstance.hide();
        loadApplications(currentAppPage);
        loadDashboard();
    } catch (err) {
        alert('Error updating status: ' + err.message);
    }
}

// ----------------- Interview Scheduling -----------------
function openScheduleInterviewModal(appId) {
    document.getElementById('interviewForm').reset();
    document.getElementById('interviewAppId').value = appId;
    document.getElementById('interviewEditId').value = '';
    document.getElementById('interviewModalTitle').textContent = 'Schedule Interview Round';
    document.getElementById('intOutcome').value = 'PENDING';
    document.getElementById('intMode').value = 'ONLINE';

    // Default datetime to tomorrow at 10:00 AM
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);
    tomorrow.setHours(10, 0, 0, 0);
    document.getElementById('intDate').value = tomorrow.toISOString().slice(0, 16);

    interviewModalInstance.show();
}

async function handleInterviewSubmit(e) {
    e.preventDefault();
    const appId = document.getElementById('interviewAppId').value;
    const editId = document.getElementById('interviewEditId').value;
    const saveBtn = document.getElementById('btnSaveInterview');

    const intData = {
        interviewRound: document.getElementById('intRound').value.trim(),
        interviewDate: document.getElementById('intDate').value,
        interviewMode: document.getElementById('intMode').value,
        outcome: document.getElementById('intOutcome').value,
        meetingLink: document.getElementById('intLink').value.trim() || null,
        interviewerName: document.getElementById('intInterviewer').value.trim() || null,
        preparationNotes: document.getElementById('intNotes').value.trim() || null
    };

    try {
        saveBtn.disabled = true;
        saveBtn.textContent = 'Saving...';

        if (editId) {
            await API.interviews.update(editId, intData);
        } else {
            await API.interviews.schedule(appId, intData);
        }

        interviewModalInstance.hide();
        if (currentDetailApp && currentDetailApp.id == appId) {
            viewApplicationDetails(appId);
        }
        loadDashboard();
        loadAllInterviews();
    } catch (err) {
        alert('Error scheduling interview: ' + err.message);
    } finally {
        saveBtn.disabled = false;
        saveBtn.textContent = 'Save Interview';
    }
}

async function deleteInterview(id, appId) {
    if (confirm('Delete this interview record?')) {
        try {
            await API.interviews.delete(id);
            if (appId) {
                viewApplicationDetails(appId);
            }
            loadDashboard();
            loadAllInterviews();
        } catch (err) {
            alert('Error deleting interview: ' + err.message);
        }
    }
}

async function loadAllInterviews() {
    const container = document.getElementById('allInterviewsListContainer');
    if (!container) return;

    try {
        const interviews = await API.dashboard.getUpcomingInterviews();
        if (!interviews || interviews.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-5"><i class="bi bi-calendar2-check fs-1 d-block mb-2 text-secondary"></i>No interviews scheduled. Go to "My Applications" and schedule one!</div>';
            return;
        }

        container.innerHTML = `
            <div class="row g-3">
                ${interviews.map(i => `
                    <div class="col-md-6 col-lg-4">
                        <div class="border rounded p-3 h-100 bg-light d-flex flex-column justify-content-between">
                            <div>
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <h6 class="fw-bold mb-0 text-dark">${escapeHtml(i.companyName)}</h6>
                                    <span class="badge ${getOutcomeBadgeClass(i.outcome)}">${escapeHtml(i.outcome)}</span>
                                </div>
                                <div class="text-primary fw-medium small mb-2">${escapeHtml(i.interviewRound)}</div>
                                <div class="text-muted small mb-1"><i class="bi bi-clock me-1"></i>${formatDateTime(i.interviewDate)}</div>
                                <div class="text-muted small mb-2"><i class="bi bi-broadcast me-1"></i>${escapeHtml(i.interviewMode)}</div>
                                ${i.interviewerName ? `<div class="text-muted small mb-2"><i class="bi bi-person me-1"></i>${escapeHtml(i.interviewerName)}</div>` : ''}
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-3 pt-2 border-top">
                                ${i.meetingLink ? `<a href="${escapeHtml(i.meetingLink)}" target="_blank" class="btn btn-sm btn-primary"><i class="bi bi-camera-video me-1"></i>Join Call</a>` : '<span></span>'}
                                <button class="btn btn-sm btn-outline-secondary" onclick="viewApplicationDetails(${i.applicationId})">View App</button>
                            </div>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    } catch (err) {
        container.innerHTML = `<div class="text-danger text-center py-4">Error loading interviews: ${escapeHtml(err.message)}</div>`;
    }
}

// ----------------- Deadlines View -----------------
async function loadDeadlinesView() {
    try {
        const deadlines = await API.dashboard.getUpcomingDeadlines();
        const overdueContainer = document.getElementById('overdueListContainer');
        const activeContainer = document.getElementById('activeUpcomingListContainer');

        const overdue = (deadlines || []).filter(d => d.overdue);
        const upcoming = (deadlines || []).filter(d => !d.overdue);

        if (overdue.length === 0) {
            overdueContainer.innerHTML = '<div class="text-muted small text-center py-3"><i class="bi bi-check2-circle text-success me-1"></i> No overdue deadlines. Great job!</div>';
        } else {
            overdueContainer.innerHTML = overdue.map(d => `
                <div class="p-2 mb-2 bg-white rounded border border-danger-subtle d-flex justify-content-between align-items-center">
                    <div>
                        <strong class="d-block small text-dark">${escapeHtml(d.companyName)}</strong>
                        <span class="text-muted" style="font-size:0.775rem;">${escapeHtml(d.jobRole)} • Due: ${formatDate(d.deadline)}</span>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <span class="badge bg-danger">Overdue by ${Math.abs(d.daysRemaining)}d</span>
                        <button class="btn btn-sm btn-link text-primary p-0" onclick="viewApplicationDetails(${d.applicationId})"><i class="bi bi-chevron-right"></i></button>
                    </div>
                </div>
            `).join('');
        }

        if (upcoming.length === 0) {
            activeContainer.innerHTML = '<div class="text-muted small text-center py-3"><i class="bi bi-calendar-check text-primary me-1"></i> No deadlines in the next 7 days.</div>';
        } else {
            activeContainer.innerHTML = upcoming.map(d => `
                <div class="p-2 mb-2 bg-white rounded border border-warning-subtle d-flex justify-content-between align-items-center">
                    <div>
                        <strong class="d-block small text-dark">${escapeHtml(d.companyName)}</strong>
                        <span class="text-muted" style="font-size:0.775rem;">${escapeHtml(d.jobRole)} • Due: ${formatDate(d.deadline)}</span>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <span class="badge bg-warning text-dark">Due in ${d.daysRemaining}d</span>
                        <button class="btn btn-sm btn-link text-primary p-0" onclick="viewApplicationDetails(${d.applicationId})"><i class="bi bi-chevron-right"></i></button>
                    </div>
                </div>
            `).join('');
        }
    } catch (err) {
        console.error('Error loading deadlines view:', err);
    }
}

// ----------------- Helpers -----------------
function getStatusClass(status) {
    switch (status) {
        case 'WISHLIST': return 'wishlist';
        case 'APPLIED': return 'applied';
        case 'ONLINE_ASSESSMENT': return 'online-assessment';
        case 'INTERVIEW_SCHEDULED': return 'interview-scheduled';
        case 'INTERVIEW_COMPLETED': return 'interview-completed';
        case 'OFFER_RECEIVED': return 'offer-received';
        case 'REJECTED': return 'rejected';
        case 'WITHDRAWN': return 'withdrawn';
        default: return 'applied';
    }
}

function getPriorityClass(priority) {
    switch (priority) {
        case 'HIGH': return 'priority-high';
        case 'MEDIUM': return 'priority-medium';
        case 'LOW': return 'priority-low';
        default: return 'priority-medium';
    }
}

function getOutcomeBadgeClass(outcome) {
    switch (outcome) {
        case 'PASSED': return 'bg-success';
        case 'FAILED': return 'bg-danger';
        default: return 'bg-secondary';
    }
}

function formatDate(dateStr) {
    if (!dateStr) return '';
    const date = new Date(dateStr + 'T00:00:00');
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
}

function formatDateTime(dateTimeStr) {
    if (!dateTimeStr) return '';
    const date = new Date(dateTimeStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' });
}

function escapeHtml(text) {
    if (text === null || text === undefined) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
