// Thin fetch wrapper that injects the JWT, normalises errors (including
// Spring's `details` array on validation failures), and on 401 clears the
// session and bounces the user to the login screen with a toast.
//
// Exception: a 401 from /api/auth/login means "wrong credentials", not
// "expired session". The login screen displays its own error, so we skip
// the global toast/redirect for that path.

import { authState, clearAuth } from '../store/authStore'
import { toast } from '../store/toastStore'
import router from '../router'

const ACCOUNT_BASE = 'http://localhost:8081'
const MODERATION_BASE = 'http://localhost:8090'
const INTERACTION_BASE = 'http://localhost:8084'
const NOTIFICATION_BASE = 'http://localhost:8085'
const RATING_BASE = 'http://localhost:8082'
const MATCHING_BASE = 'http://localhost:8087'
const ACTIVITY_BASE = 'http://localhost:8083'
const SCHEDULING_BASE = 'http://localhost:8089'
const BOOKING_BASE = 'http://localhost:8088'
const SEARCH_BASE = 'http://localhost:8086'

async function request(baseUrl, path, options = {}) {
// ... rest of request function
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (authState.token) {
    headers.Authorization = `Bearer ${authState.token}`
  }

  let response
  try {
    response = await fetch(baseUrl + path, { ...options, headers })
  } catch (networkErr) {
    throw new Error('Network error: ' + networkErr.message)
  }

  const isLoginAttempt = path.startsWith('/api/auth/login')

  if (response.status === 401 && !isLoginAttempt) {
    clearAuth()
    toast.error('Session expired. Please sign in again.')
    if (router.currentRoute.value.path !== '/account/login') {
      router.push('/account/login')
    }
    const err = new Error('Session expired. Please sign in again.')
    err.silent = true
    throw err
  }

  const text = await response.text()
  const body = text ? safeJson(text) : null

  if (!response.ok && response.status !== 202) {
    throw new Error(extractMessage(body, response.status))
  }
  return body
}

// Spring's error responses look like:
//   { status, error, message, details: ["field: must not be blank", ...] }
// We surface the most specific information we have.
function extractMessage(body, status) {
  if (!body) {
    if (status === 401) return 'Invalid email or password.'
    if (status === 403) return 'You do not have permission to perform this action.'
    if (status === 503) return 'A required peer service is unavailable. Try again shortly.'
    return `HTTP ${status}`
  }
  if (Array.isArray(body.details) && body.details.length) {
    return body.details.join(' • ')
  }
  return body.message || body.error || `HTTP ${status}`
}

function safeJson(text) {
  try {
    return JSON.parse(text)
  } catch (e) {
    return null
  }
}

function buildQuery(params) {
  const q = new URLSearchParams()
  for (const [k, v] of Object.entries(params || {})) {
    if (v === null || v === undefined) continue
    const s = typeof v === 'string' ? v.trim() : String(v)
    if (s === '') continue
    q.append(k, s)
  }
  const str = q.toString()
  return str ? '?' + str : ''
}

export const accountApi = {
  login: (email, password) => request(ACCOUNT_BASE, '/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password })
  }),
  registerUser: (body) => request(ACCOUNT_BASE, '/api/users', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  registerFacility: (body) => request(ACCOUNT_BASE, '/api/sport-centers', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  listUsers: (page = 0, size = 20) => request(ACCOUNT_BASE, `/api/users?page=${page}&size=${size}`),
  getUser: (id) => request(ACCOUNT_BASE, `/api/users/${id}`),
  updateUser: (id, body) => request(ACCOUNT_BASE, `/api/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body)
  }),
  updateUserStatus: (id, status) => request(ACCOUNT_BASE, `/api/users/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status })
  }),
  getFacility: (id) => request(ACCOUNT_BASE, `/api/sport-centers/${id}`),
  updateFacility: (id, body) => request(ACCOUNT_BASE, `/api/sport-centers/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body)
  })
}

// Interaction service uses X-User-Id header (not JWT) for identity
function interactionRequest(path, options = {}, userId) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (userId) headers['X-User-Id'] = userId
  return fetch(INTERACTION_BASE + path, { ...options, headers })
    .then(async r => {
      const text = await r.text()
      const body = text ? safeJson(text) : null
      if (!r.ok && r.status !== 204) throw new Error(body?.message || `HTTP ${r.status}`)
      return body
    })
}

export const interactionApi = {
  followUser: (myId, targetUserId) =>
    interactionRequest('/api/follows', {
      method: 'POST',
      body: JSON.stringify({ targetUserId })
    }, myId),
  unfollowUser: (myId, targetUserId) =>
    interactionRequest(`/api/follows/${targetUserId}`, { method: 'DELETE' }, myId),
  isFollowing: (myId, targetUserId) =>
    interactionRequest(`/api/follows/check/${targetUserId}`, {}, myId),
  getFollowing: (userId, size = 500) =>
    interactionRequest(`/api/users/${userId}/following?size=${size}`, {})
}

function notificationRequest(path, options = {}, userId) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (userId) headers['X-User-Id'] = userId
  return fetch(NOTIFICATION_BASE + path, { ...options, headers })
    .then(async r => {
      const text = await r.text()
      const body = text ? safeJson(text) : null
      if (!r.ok && r.status !== 204) throw new Error(body?.message || `HTTP ${r.status}`)
      return body
    })
}

export const notificationApi = {
  getInbox: (userId, page = 0, size = 20) =>
    notificationRequest(`/api/notifications?page=${page}&size=${size}`, {}, userId),
  markRead: (userId, notificationId) =>
    notificationRequest(`/api/notifications/${notificationId}/read`, { method: 'PATCH' }, userId),
  markAllRead: (userId) =>
    notificationRequest('/api/notifications/read-all', { method: 'POST' }, userId)
}

export const moderationApi = {
  listCases: (params = {}) => {
    const query = new URLSearchParams()
    if (params.contentType) query.append('contentType', params.contentType)
    if (params.verdict) query.append('verdict', params.verdict)
    query.append('size', params.size || '20')
    query.append('sort', 'createdAt,desc')
    return request(MODERATION_BASE, '/api/cases?' + query.toString())
  },
  getCase: (id) => request(MODERATION_BASE, `/api/cases/${id}`),
  createCase: (body) => request(MODERATION_BASE, '/api/cases', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  submitVerdict: (id, body) => request(MODERATION_BASE, `/api/cases/${id}/verdict`, {
    method: 'PUT',
    body: JSON.stringify(body)
  }),
  applySanction: (body) => request(MODERATION_BASE, '/api/sanctions', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  getSanctionHistory: (userId) => request(MODERATION_BASE, `/api/sanctions/${userId}`),
  removeContent: (contentType, contentId) => request(MODERATION_BASE, `/api/content/${contentType}/${contentId}`, {
    method: 'DELETE'
  })
}

export const ratingApi = {
  getRatings: (params) => {
    const query = new URLSearchParams(params).toString()
    return request(RATING_BASE, `/api/ratings${query ? `?${query}` : ''}`)
  },
  getRating: (id) => request(RATING_BASE, `/api/ratings/${id}`),
  createRating: (body) => request(RATING_BASE, '/api/ratings', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  deleteRating: (id) => request(RATING_BASE, `/api/ratings/${id}`, {
    method: 'DELETE'
  }),
  getReputation: (userId) => request(RATING_BASE, `/api/reputations/${userId}`),
  recomputeReputation: (userId) => request(RATING_BASE, `/api/reputations/${userId}/recompute`, {
    method: 'POST'
  })
}

export const matchingApi = {
  getUserSuggestions: (userId, params) => {
    const query = new URLSearchParams(params).toString()
    return request(MATCHING_BASE, `/api/matching/users/${userId}/suggestions?${query}`)
  },
  recomputeUserSuggestions: (userId, params) => {
    const query = new URLSearchParams(params).toString()
    return request(MATCHING_BASE, `/api/matching/users/${userId}/suggestions?${query}`, {
      method: 'POST'
    })
  },
  getActivitySuggestions: (userId, params) => {
    const query = new URLSearchParams(params).toString()
    return request(MATCHING_BASE, `/api/matching/activities/${userId}/suggestions?${query}`)
  },
  recomputeActivitySuggestions: (userId, params) => {
    const query = new URLSearchParams(params).toString()
    return request(MATCHING_BASE, `/api/matching/activities/${userId}/suggestions?${query}`, {
      method: 'POST'
    })
  },
  checkCompatibility: (userAId, userBId) => {
    const query = new URLSearchParams({ userAId, userBId }).toString()
    return request(MATCHING_BASE, `/api/matching/compatibility?${query}`)
  },
  invalidateSuggestion: (suggestionId) => request(MATCHING_BASE, `/api/matching/suggestions/${suggestionId}`, {
    method: 'DELETE'
  })
}

export const activityApi = {
  list: () => request(ACTIVITY_BASE, '/api/activities'),
  get: (id) => request(ACTIVITY_BASE, `/api/activities/${id}`),
  create: (body) => request(ACTIVITY_BASE, '/api/activities', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  update: (id, body) => request(ACTIVITY_BASE, `/api/activities/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body)
  }),
  cancel: (id) => request(ACTIVITY_BASE, `/api/activities/${id}`, {
    method: 'DELETE'
  }),
  join: (id, userId) => request(ACTIVITY_BASE, `/api/activities/${id}/join`, {
    method: 'POST',
    body: JSON.stringify({ userId })
  }),
  leave: (id, userId) => request(ACTIVITY_BASE, `/api/activities/${id}/leave`, {
    method: 'POST',
    body: JSON.stringify({ userId })
  }),
  participants: (id) => request(ACTIVITY_BASE, `/api/activities/${id}/participants`),
  removeParticipant: (activityId, userId) =>
    request(ACTIVITY_BASE, `/api/activities/${activityId}/participants/${userId}`, {
      method: 'DELETE'
    })
}

export const schedulingApi = {
  listAll: () => request(SCHEDULING_BASE, '/api/timeslots'),
  listAvailable: () => request(SCHEDULING_BASE, '/api/timeslots/available'),
  reserve: (body) => request(SCHEDULING_BASE, '/api/timeslots/reserve', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  release: (slotId) => request(SCHEDULING_BASE, `/api/timeslots/${slotId}/release`, {
    method: 'DELETE'
  }),
  getUserAvailability: (userId) =>
    request(SCHEDULING_BASE, `/api/timeslots/users/${userId}/availability`),
  updateUserAvailability: (userId, body) =>
    request(SCHEDULING_BASE, `/api/timeslots/users/${userId}/availability`, {
      method: 'PUT',
      body: JSON.stringify(body)
    }),
  resolve: (userIds) => request(SCHEDULING_BASE, '/api/timeslots/resolve', {
    method: 'POST',
    body: JSON.stringify({ userIds })
  })
}

export const bookingApi = {
  list: (filters = {}) => request(BOOKING_BASE, '/api/bookings' + buildQuery(filters)),
  get: (id) => request(BOOKING_BASE, `/api/bookings/${encodeURIComponent(id)}`),
  create: (body) => request(BOOKING_BASE, '/api/bookings', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  confirm: (id) => request(BOOKING_BASE, `/api/bookings/${encodeURIComponent(id)}/confirm`, {
    method: 'PUT'
  }),
  cancel: (id) => request(BOOKING_BASE, `/api/bookings/${encodeURIComponent(id)}/cancel`, {
    method: 'PUT'
  }),
  refund: (id) => request(BOOKING_BASE, `/api/bookings/${encodeURIComponent(id)}/refund`, {
    method: 'POST'
  }),
  searchFacilities: (filters = {}) => request(
    BOOKING_BASE, '/api/bookings/facilities/search' + buildQuery(filters)
  )
}

export const searchApi = {
  all: (filters = {}) => request(SEARCH_BASE, '/api/search' + buildQuery(filters)),
  users: (filters = {}) => request(SEARCH_BASE, '/api/search/users' + buildQuery(filters)),
  activities: (filters = {}) => request(SEARCH_BASE, '/api/search/activities' + buildQuery(filters)),
  facilities: (filters = {}) => request(SEARCH_BASE, '/api/search/facilities' + buildQuery(filters)),
  indexEntity: (body) => request(SEARCH_BASE, '/api/search/index', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  getIndex: (entityType, entityId) => request(
    SEARCH_BASE, `/api/search/index/${encodeURIComponent(entityType)}/${encodeURIComponent(entityId)}`
  ),
  deleteIndex: (entityType, entityId) => request(
    SEARCH_BASE, `/api/search/index/${encodeURIComponent(entityType)}/${encodeURIComponent(entityId)}`,
    { method: 'DELETE' }
  )
}
