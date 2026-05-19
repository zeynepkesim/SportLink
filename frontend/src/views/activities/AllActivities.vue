<template>
  <div class="activities">
    <div class="tab-bar">
      <button :class="['tab', { active: tab === 'all' }]" @click="switchTab('all')">All Activities</button>
      <button :class="['tab', { active: tab === 'feed' }]" @click="switchTab('feed')">My Feed</button>
    </div>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="load">Retry</button>
    </div>

    <template v-else>
      <div v-if="tab === 'feed' && !isLoggedIn" class="empty">
        <p>Sign in to see your personalised feed.</p>
      </div>

      <div v-else-if="displayed.length === 0" class="empty">
        <template v-if="tab === 'feed'">
          <p>Nothing here yet. Follow some users and their activities will appear here.</p>
        </template>
        <template v-else>
          <p>No activities yet. <router-link to="/activities/new">Create the first one</router-link>!</p>
        </template>
      </div>

      <div v-else>
        <p class="count">{{ displayed.length }} activity(ies)</p>
        <div class="item" v-for="activity in displayed" :key="activity.activityId">
          <h3>
            <router-link :to="'/activities/' + activity.activityId" class="title-link">
              {{ activity.title }}
            </router-link>
          </h3>
          <div class="meta">
            <span class="badge" :class="'status-' + activity.status.toLowerCase()">
              {{ activity.status }}
            </span>
            <span class="sport">🏃 {{ activity.sportType }}</span>
            <span class="participants">
              👥 {{ activity.participants.length }} / {{ activity.maxParticipants }}
            </span>
          </div>
          <p v-if="activity.description" class="description">{{ activity.description }}</p>
          <p class="organizer-line">
            <b>Organized by:</b>
            {{ organizerName(activity.organizerId) || 'Loading…' }}
          </p>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
import { interactionApi } from '../../api/client'
import { authState } from '../../store/authStore'

export default {
  name: 'AllActivities',
  data() {
    return {
      tab: 'all',
      activities: [],
      profiles: {},
      followingIds: new Set(),
      loading: false,
      error: null,
      authState
    }
  },
  computed: {
    isLoggedIn() {
      return !!authState.token && authState.accountType === 'USER'
    },
    displayed() {
      if (this.tab === 'feed') {
        return this.activities.filter(a => this.followingIds.has(a.organizerId))
      }
      return this.activities
    }
  },
  methods: {
    switchTab(t) {
      this.tab = t
      if (t === 'feed' && this.isLoggedIn) {
        this.loadFollowing()
      }
    },
    async load() {
      this.loading = true
      this.error = null
      try {
        const data = await fetch('http://localhost:8083/api/activities').then(r => r.json())
        this.activities = data
        const uniqueOrganizers = [...new Set(data.map(a => a.organizerId))]
        uniqueOrganizers.forEach(id => this.fetchProfile(id))
        if (this.isLoggedIn) this.loadFollowing()
      } catch (err) {
        this.error = err.message
      } finally {
        this.loading = false
      }
    },
    async loadFollowing() {
      if (!authState.accountId) return
      try {
        const page = await interactionApi.getFollowing(authState.accountId, 500)
        const ids = (page?.content ?? []).map(u => u.userId)
        this.followingIds = new Set(ids)
      } catch { /* non-fatal */ }
    },
    fetchProfile(userId) {
      if (this.profiles[userId]) return
      fetch(`http://localhost:8081/api/users/${userId}`)
        .then(r => r.ok ? r.json() : null)
        .then(profile => {
          if (profile) this.profiles = { ...this.profiles, [userId]: profile }
        })
        .catch(() => {})
    },
    organizerName(userId) {
      return this.profiles[userId]?.name
    }
  },
  mounted() {
    this.load()
  }
}
</script>

<style scoped>
.activities { text-align: left; }

.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 2px solid #e8ecef;
  padding-bottom: 0;
}
.tab {
  background: none;
  border: none;
  padding: 8px 20px;
  font-size: 0.95em;
  font-weight: bold;
  color: #888;
  cursor: pointer;
  border-bottom: 3px solid transparent;
  margin-bottom: -2px;
  transition: color 0.15s, border-color 0.15s;
}
.tab.active { color: #42b983; border-bottom-color: #42b983; }
.tab:hover:not(.active) { color: #555; }

.count { text-align: center; color: #555; }
.item {
  background: #ecf3ee;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #42b983;
}
.item h3 { margin: 0 0 8px; color: #2c3e50; }
.title-link { color: #2c3e50; text-decoration: none; }
.title-link:hover { color: #42b983; text-decoration: underline; }
.meta { display: flex; gap: 12px; margin-bottom: 10px; flex-wrap: wrap; }
.badge {
  padding: 3px 10px; border-radius: 12px;
  font-size: 0.8em; font-weight: bold; color: white;
}
.status-planned   { background: #3498db; }
.status-active    { background: #27ae60; }
.status-completed { background: #95a5a6; }
.status-cancelled { background: #e74c3c; }
.sport, .participants { font-size: 0.95em; }
.description { font-style: italic; color: #555; margin: 8px 0; }
.organizer-line { color: #555; margin: 8px 0 0; font-size: 0.95em; }
.loading, .empty, .error { text-align: center; padding: 30px; }
.error { color: #c0392b; }
button { background: #42b983; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }
</style>
