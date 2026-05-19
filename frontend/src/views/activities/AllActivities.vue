<template>
  <div class="activities">
    <h2 class="feed-title">My Feed</h2>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="load">Retry</button>
    </div>

    <template v-else>
      <div v-if="!isLoggedIn" class="empty">
        <p>Sign in to see your personalised feed.</p>
      </div>

      <div v-else-if="displayed.length === 0" class="empty">
        <p>Nothing here yet. Follow some users and their activities will appear here.</p>
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
import { activityApi, accountApi, interactionApi } from '../../api/client'
import { authState } from '../../store/authStore'

export default {
  name: 'AllActivities',
  data() {
    return {
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
      return this.activities.filter(a =>
        this.followingIds.has(a.organizerId) || a.organizerId === authState.accountId
      )
    }
  },
  methods: {
    async load() {
      this.loading = true
      this.error = null
      try {
        this.activities = await activityApi.list()
        const uniqueOrganizers = [...new Set(this.activities.map(a => a.organizerId))]
        uniqueOrganizers.forEach(id => this.fetchProfile(id))
        if (this.isLoggedIn) await this.loadFollowing()
      } catch (err) {
        if (!err.silent) this.error = err.message
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
    async fetchProfile(userId) {
      if (this.profiles[userId]) return
      try {
        const profile = await accountApi.getUser(userId)
        this.profiles = { ...this.profiles, [userId]: profile }
      } catch { /* show fallback name */ }
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
.feed-title { color: #2c3e50; margin-bottom: 20px; }

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
