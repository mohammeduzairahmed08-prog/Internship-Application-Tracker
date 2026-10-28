/**
 * Internship Application Tracker - API Client Layer
 */

const API_BASE = '/api';

async function apiRequest(endpoint, options = {}) {
    const defaultHeaders = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    };

    const config = {
        ...options,
        headers: {
            ...defaultHeaders,
            ...options.headers
        },
        credentials: 'include' // ensures session cookies (INTERN_SESSION_ID) are transmitted
    };

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);

        // If unauthorized and not on auth pages, redirect to login
        if (response.status === 401) {
            const currentPath = window.location.pathname;
            if (!currentPath.includes('login.html') && !currentPath.includes('register.html')) {
                window.location.href = '/login.html';
                return null;
            }
        }

        const data = await response.json().catch(() => null);

        if (!response.ok) {
            let errorMsg = 'An error occurred';
            if (data && data.message) {
                errorMsg = data.message;
            } else if (data && data.error) {
                errorMsg = data.error;
            }
            if (data && data.validationErrors) {
                const details = Object.entries(data.validationErrors)
                    .map(([field, msg]) => `${field}: ${msg}`)
                    .join('\n');
                errorMsg += `\n${details}`;
            }
            throw new Error(errorMsg);
        }

        return data ? data.data : null;
    } catch (err) {
        throw err;
    }
}

const API = {
    auth: {
        register: (userData) => apiRequest('/auth/register', { method: 'POST', body: JSON.stringify(userData) }),
        login: (credentials) => apiRequest('/auth/login', { method: 'POST', body: JSON.stringify(credentials) }),
        logout: () => apiRequest('/auth/logout', { method: 'POST' }),
        me: () => apiRequest('/auth/me')
    },
    dashboard: {
        getSummary: () => apiRequest('/dashboard/summary'),
        getStatusDistribution: () => apiRequest('/dashboard/status-distribution'),
        getApplicationTrends: () => apiRequest('/dashboard/application-trends'),
        getUpcomingDeadlines: () => apiRequest('/dashboard/upcoming-deadlines'),
        getUpcomingInterviews: () => apiRequest('/dashboard/upcoming-interviews')
    },
    applications: {
        list: (params = {}) => {
            const query = new URLSearchParams();
            Object.entries(params).forEach(([key, val]) => {
                if (val !== undefined && val !== null && val !== '') {
                    query.append(key, val);
                }
            });
            const qs = query.toString();
            return apiRequest(`/applications${qs ? '?' + qs : ''}`);
        },
        get: (id) => apiRequest(`/applications/${id}`),
        create: (appData) => apiRequest('/applications', { method: 'POST', body: JSON.stringify(appData) }),
        update: (id, appData) => apiRequest(`/applications/${id}`, { method: 'PUT', body: JSON.stringify(appData) }),
        updateStatus: (id, status) => apiRequest(`/applications/${id}/status`, { method: 'PATCH', body: JSON.stringify({ status }) }),
        delete: (id) => apiRequest(`/applications/${id}`, { method: 'DELETE' })
    },
    interviews: {
        list: (applicationId) => apiRequest(`/applications/${applicationId}/interviews`),
        schedule: (applicationId, data) => apiRequest(`/applications/${applicationId}/interviews`, { method: 'POST', body: JSON.stringify(data) }),
        update: (id, data) => apiRequest(`/interviews/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
        delete: (id) => apiRequest(`/interviews/${id}`, { method: 'DELETE' })
    }
};

window.API = API;
