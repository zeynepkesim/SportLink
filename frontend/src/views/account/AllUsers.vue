<template>
  <div class="all-users">
    <h2>All Users</h2>
    <p class="subtitle">{{ total }} registered members</p>

    <div v-if="loading && users.length === 0" class="loading">Loading users…</div>
    <div v-if="error" class="error">{{ error }}</div>

    <div v-if="users.length > 0" class="grid">
      <div
        v-for="user in users"
        :key="user.userId"
        class="card"
        @click="goToProfile(user.userId)"
      >
        <div class="avatar" :style="{ background: avatarColor(user.name) }">
          {{ initials(user.name) }}
        </div>
        <div class="info">
          <div class="name">{{ user.name || 'Unnamed' }}</div>
          <div class="email">{{ user.email }}</div>
          <div class="meta">
            <span class="badge" :class="'role-' + (user.role || '').toLowerCase()">
              {{ user.role }}
            </span>
            <span class="badge" :class="'status-' + (user.status || '').toLowerCase()">
              {{ user.status }}
            </span>
            <span v-if="user.skillLevel" class="badge skill">{{ user.skillLevel }}</span>
          </div>
          <div v-if="user.city || user.district" class="location">
            📍 {{ [user.city, user.district].filter(Boolean).join(', ') }}
          </div>
          <div v-if="user.sportPreferences" class="sports">
            🏅 {{ user.sportPreferences }}
          </div>
        </div>
      </div>
    </div>

    <div v-if="users.length > 0" class="pagination">
      <button @click="prevPage" :disabled="page === 0 || loading">← Prev</button>
      <span>Page {{ page + 1 }}</span>
      <button @click="nextPage" :disabled="users.length < pageSize || loading">Next →</button>
    </div>
  </div>
</template>

<script>
import { accountApi } from '../../api/client'
import { toast } from '../../store/toastStore'

const COLORS = ['#3498db', '#2ecc71', '#9b59b6', '#e67e22', '#1abc9c', '#e74c3c', '#34495e']

export default {
  name: 'AllUsers',
  data() {
    return {
      users: [],
      page: 0,
      pageSize: 20,
      total: 0,
      loading: false,
      error: null
    }
  },
  mounted() {
    this.fetchUsers()
  },
  methods: {
    async fetchUsers() {
      this.loading = true
      this.error = null
      try {
        const data = await accountApi.listUsers(this.page, this.pageSize)
        this.users = data || []
        if (this.page === 0) this.total = this.users.length
      } catch (e) {
        this.error = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    },
    prevPage() {
      if (this.page > 0) { this.page--; this.fetchUsers() }
    },
    nextPage() {
      this.page++; this.fetchUsers()
    },
    goToProfile(id) {
      this.$router.push({ path: '/account/profile', query: { id } })
    },
    initials(name) {
      if (!name) return '?'
      return name.trim().split(/\s+/).map(w => w[0]).slice(0, 2).join('').toUpperCase()
    },
    avatarColor(name) {
      if (!name) return COLORS[0]
      const idx = name.charCodeAt(0) % COLORS.length
      return COLORS[idx]
    }
  }
}
</script>

<style scoped>
.all-users {
  max-width: 1000px;
  margin: 0 auto;
  text-align: left;
}
h2 {
  color: #2c3e50;
  margin-bottom: 4px;
}
.subtitle {
  color: #888;
  font-size: 0.9em;
  margin-bottom: 24px;
}
.loading {
  text-align: center;
  color: #888;
  padding: 40px;
}
.error {
  background: #fadbd8;
  color: #c0392b;
  padding: 12px 16px;
  border-radius: 6px;
  margin-bottom: 16px;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}
.card {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: #fff;
  border: 1px solid #e8ecef;
  border-radius: 10px;
  padding: 16px;
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.15s;
}
.card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  font-size: 1.1em;
  flex-shrink: 0;
}
.info {
  flex: 1;
  min-width: 0;
}
.name {
  font-weight: bold;
  font-size: 1em;
  color: #2c3e50;
  margin-bottom: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.email {
  font-size: 0.8em;
  color: #888;
  margin-bottom: 8px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 6px;
}
.badge {
  font-size: 0.7em;
  font-weight: bold;
  padding: 2px 8px;
  border-radius: 999px;
  text-transform: uppercase;
  letter-spacing: 0.3px;
  background: #ecf0f1;
  color: #555;
}
.badge.role-user       { background: #d6eaf8; color: #1f618d; }
.badge.role-moderator  { background: #e8daef; color: #6c3483; }
.badge.role-organizer  { background: #d5f5e3; color: #1e8449; }
.badge.status-active   { background: #d5f5e3; color: #1e8449; }
.badge.status-suspended { background: #fdebd0; color: #935116; }
.badge.status-banned   { background: #fadbd8; color: #922b21; }
.badge.skill           { background: #f4f6f7; color: #555; }
.location, .sports {
  font-size: 0.78em;
  color: #666;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 32px;
}
.pagination button {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
  font-weight: bold;
  cursor: pointer;
}
.pagination button:disabled {
  background: #bdc3c7;
  cursor: not-allowed;
}
.pagination span {
  font-weight: bold;
  color: #555;
}
</style>
