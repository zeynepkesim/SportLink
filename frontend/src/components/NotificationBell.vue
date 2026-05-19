<template>
  <div class="bell-wrap" ref="wrap">
    <button class="bell-btn" @click="toggleDropdown" title="Notifications">
      🔔
      <span v-if="unreadCount > 0" class="badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</span>
    </button>

    <div v-if="open" class="dropdown">
      <div class="dropdown-header">
        <span class="dropdown-title">Notifications</span>
        <button v-if="unreadCount > 0" class="mark-all" @click="markAll">Mark all read</button>
      </div>

      <div v-if="loading" class="dd-empty">Loading…</div>
      <div v-else-if="items.length === 0" class="dd-empty">No notifications yet.</div>

      <ul v-else class="list">
        <li
          v-for="n in items"
          :key="n.id"
          class="item"
          :class="{ unread: !n.read }"
          @click="markOne(n)"
        >
          <div class="item-icon">{{ iconFor(n.type) }}</div>
          <div class="item-body">
            <p class="item-msg">{{ messageFor(n) }}</p>
            <p class="item-time">{{ timeAgo(n.createdAt) }}</p>
          </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
import { notificationApi, accountApi } from '../api/client'
import { authState } from '../store/authStore'

const POLL_MS = 5000

export default {
  name: 'NotificationBell',
  data() {
    return {
      open: false,
      items: [],
      names: {},   // followerId → name cache
      loading: false,
      pollTimer: null
    }
  },
  computed: {
    userId() { return authState.accountId },
    isUser() { return !!authState.token && authState.accountType === 'USER' },
    unreadCount() { return this.items.filter(n => !n.read).length }
  },
  watch: {
    isUser(val) {
      if (val) this.startPolling()
      else this.stopPolling()
    }
  },
  mounted() {
    if (this.isUser) this.startPolling()
    document.addEventListener('click', this.onOutsideClick)
  },
  beforeUnmount() {
    this.stopPolling()
    document.removeEventListener('click', this.onOutsideClick)
  },
  methods: {
    startPolling() {
      this.fetchInbox()
      this.pollTimer = setInterval(this.fetchInbox, POLL_MS)
    },
    stopPolling() {
      clearInterval(this.pollTimer)
      this.pollTimer = null
      this.items = []
    },
    async fetchInbox() {
      if (!this.userId) return
      try {
        const page = await notificationApi.getInbox(this.userId, 0, 20)
        this.items = page?.content ?? []
        this.resolveNames()
      } catch { /* silent — polling failures shouldn't interrupt the user */ }
    },
    async resolveNames() {
      const ids = this.items
        .filter(n => n.type === 'FOLLOWER_UPDATE')
        .map(n => {
          try { return JSON.parse(n.payload).followerId } catch { return null }
        })
        .filter(id => id && !this.names[id])

      for (const id of ids) {
        try {
          const profile = await accountApi.getUser(id)
          this.names = { ...this.names, [id]: profile.name || 'Someone' }
        } catch {
          this.names = { ...this.names, [id]: 'Someone' }
        }
      }
    },
    toggleDropdown() {
      this.open = !this.open
    },
    onOutsideClick(e) {
      if (this.$refs.wrap && !this.$refs.wrap.contains(e.target)) {
        this.open = false
      }
    },
    async markOne(n) {
      if (n.read) return
      try {
        await notificationApi.markRead(this.userId, n.id)
        n.read = true
      } catch { /* ignore */ }
    },
    async markAll() {
      try {
        await notificationApi.markAllRead(this.userId)
        this.items.forEach(n => { n.read = true })
      } catch { /* ignore */ }
    },
    messageFor(n) {
      try {
        const p = JSON.parse(n.payload)
        if (n.type === 'FOLLOWER_UPDATE') {
          const name = this.names[p.followerId] || 'Someone'
          return `${name} started following you`
        }
        if (n.type === 'COMMENT_POSTED') return 'New comment on your activity'
        return p.message || n.type
      } catch {
        return n.type
      }
    },
    iconFor(type) {
      if (type === 'FOLLOWER_UPDATE') return '👤'
      if (type === 'COMMENT_POSTED') return '💬'
      return '🔔'
    },
    timeAgo(isoStr) {
      if (!isoStr) return ''
      const diff = Date.now() - new Date(isoStr).getTime()
      const m = Math.floor(diff / 60000)
      if (m < 1) return 'just now'
      if (m < 60) return `${m}m ago`
      const h = Math.floor(m / 60)
      if (h < 24) return `${h}h ago`
      return `${Math.floor(h / 24)}d ago`
    }
  }
}
</script>

<style scoped>
.bell-wrap {
  position: relative;
  display: inline-block;
}
.bell-btn {
  background: none;
  border: none;
  font-size: 1.3em;
  cursor: pointer;
  position: relative;
  padding: 4px 6px;
  line-height: 1;
}
.badge {
  position: absolute;
  top: -2px;
  right: -4px;
  background: #e74c3c;
  color: white;
  font-size: 0.55em;
  font-weight: bold;
  border-radius: 999px;
  padding: 1px 5px;
  line-height: 1.4;
  min-width: 16px;
  text-align: center;
}
.dropdown {
  position: absolute;
  top: calc(100% + 8px);
  right: 0;
  width: 320px;
  background: white;
  border: 1px solid #e0e4e8;
  border-radius: 10px;
  box-shadow: 0 8px 24px rgba(0,0,0,0.12);
  z-index: 1000;
  overflow: hidden;
}
.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}
.dropdown-title {
  font-weight: bold;
  font-size: 0.95em;
  color: #2c3e50;
}
.mark-all {
  background: none;
  border: none;
  color: #3498db;
  font-size: 0.8em;
  cursor: pointer;
  font-weight: bold;
  padding: 0;
}
.mark-all:hover { text-decoration: underline; }

.dd-empty {
  padding: 24px;
  text-align: center;
  color: #aaa;
  font-size: 0.9em;
}
.list {
  list-style: none;
  margin: 0;
  padding: 0;
  max-height: 360px;
  overflow-y: auto;
}
.item {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  border-bottom: 1px solid #f7f7f7;
  cursor: pointer;
  transition: background 0.1s;
  align-items: flex-start;
}
.item:last-child { border-bottom: none; }
.item:hover { background: #f8f9fa; }
.item.unread { background: #eaf3fb; }
.item.unread:hover { background: #ddeef8; }
.item-icon {
  font-size: 1.2em;
  flex-shrink: 0;
  margin-top: 2px;
}
.item-body { flex: 1; min-width: 0; }
.item-msg {
  margin: 0 0 3px;
  font-size: 0.88em;
  color: #2c3e50;
  line-height: 1.4;
}
.item-time {
  margin: 0;
  font-size: 0.75em;
  color: #aaa;
}
</style>
