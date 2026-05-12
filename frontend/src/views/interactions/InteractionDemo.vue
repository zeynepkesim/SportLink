<template>
  <div class="interaction-demo">

    <!-- Toast notification -->
    <transition name="toast">
      <div v-if="toast.visible" class="toast">
        🔔 {{ toast.message }}
      </div>
    </transition>

    <h2>Interaction &amp; Notifications Demo</h2>

    <!-- User context -->
    <div class="card user-ctx">
      <label><b>Acting as</b></label>
      <div class="user-btns">
        <button
          v-for="u in users"
          :key="u.id"
          :class="['user-btn', currentUserId === u.id ? 'active' : '']"
          @click="switchUser(u.id)"
        >{{ u.label }}</button>
      </div>
      <small v-if="!currentUserId" class="hint">Select a user to begin</small>
    </div>

    <div class="columns">

      <!-- ── LEFT: Interaction Service ── -->
      <div class="column">
        <div class="section-header">
          <span class="service-badge interaction">Interaction Service</span>
          <small>Social graph · Comments</small>
        </div>

        <!-- Follow / Unfollow -->
        <div class="card">
          <h3>Follow a User</h3>
          <div class="user-btns">
            <button
              v-for="u in otherUsers"
              :key="u.id"
              :class="['user-btn', followTargetId === u.id ? 'active' : '']"
              @click="followTargetId = u.id"
            >{{ u.label }}</button>
          </div>
          <div class="btn-row">
            <button class="btn-primary" :disabled="!currentUserId || !followTargetId" @click="followUser">Follow</button>
            <button class="btn-danger"  :disabled="!currentUserId || !followTargetId" @click="unfollowUser">Unfollow</button>
          </div>
          <p v-if="followMsg" class="msg" :class="followErr ? 'err' : 'ok'">{{ followMsg }}</p>

          <div v-if="currentUserId" class="sub-section">
            <div class="sub-header">
              <span>Following</span>
              <button class="btn-sm" @click="loadFollowing">↻ Load</button>
            </div>
            <ul v-if="following.length">
              <li v-for="u in following" :key="u.userId">{{ labelFor(u.userId) }}</li>
            </ul>
            <p v-else class="empty">—</p>

            <div class="sub-header">
              <span>Followers</span>
              <button class="btn-sm" @click="loadFollowers">↻ Load</button>
            </div>
            <ul v-if="followers.length">
              <li v-for="u in followers" :key="u.userId">{{ labelFor(u.userId) }}</li>
            </ul>
            <p v-else class="empty">—</p>
          </div>
        </div>

        <!-- Comments -->
        <div class="card">
          <h3>Post a Comment</h3>
          <div class="user-btns">
            <button
              v-for="u in users"
              :key="u.id"
              :class="['user-btn', currentUserId === u.id ? 'active' : '']"
              @click="currentUserId = u.id"
            >{{ u.label }}</button>
          </div>
          <small v-if="!currentUserId" class="hint">Select a user to post.</small>
          <textarea v-model="commentText" placeholder="Write a comment…" rows="3" />
          <div class="btn-row">
            <button class="btn-primary" :disabled="!currentUserId || !commentText.trim()" @click="postComment">Post Comment</button>
          </div>
          <p v-if="commentMsg" class="msg" :class="commentErr ? 'err' : 'ok'">{{ commentMsg }}</p>

          <div v-if="comments.length" class="comment-list">
            <div v-for="c in comments" :key="c.commentId" class="comment-item">
              <div class="comment-top">
                <span class="comment-author">{{ labelFor(c.authorId) }}</span>
                <div class="comment-actions" v-if="c.authorId === currentUserId">
                  <button class="btn-xs" @click="startEditComment(c)">Edit</button>
                  <button class="btn-xs danger" @click="deleteComment(c.commentId)">Delete</button>
                </div>
              </div>
              <div v-if="editingCommentId === c.commentId">
                <textarea v-model="editCommentText" rows="2" />
                <div class="btn-row">
                  <button class="btn-primary btn-xs" @click="saveEditComment(c.commentId)">Save</button>
                  <button class="btn-sm btn-xs" @click="editingCommentId = null">Cancel</button>
                </div>
              </div>
              <span v-else class="comment-body">{{ c.content }}</span>
              <small class="comment-time">{{ formatTime(c.createdAt) }}</small>
            </div>
          </div>
          <p v-else-if="commentsLoaded" class="empty">No comments yet. Be the first!</p>
        </div>

      </div>


    </div>
  </div>
</template>

<script>
const ACCOUNT_BASE      = 'http://localhost:8081'
const INTERACTION_BASE  = 'http://localhost:8084'
const NOTIFICATION_BASE = 'http://localhost:8085'
const DEMO_ACTIVITY_ID  = 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee'

export default {
  name: 'InteractionDemo',
  data() {
    return {
      users: [],
      currentUserId: '',
      toast: { visible: false, message: '' },
      seenIds: new Set(),
      pollTimer: null,
      // follow
      followTargetId: '',
      followMsg: '',
      followErr: false,
      following: [],
      followers: [],
      // comments
      commentText: '',
      commentMsg: '',
      commentErr: false,
      comments: [],
      commentsLoaded: false,
      editingCommentId: null,
      editCommentText: '',
      // notifications
      notifications: [],
      notifsLoaded: false,
      notifMsg: '',
      notifErr: false,
    }
  },
  computed: {
    otherUsers() {
      return this.users.filter(u => u.id !== this.currentUserId)
    },
    unreadCount() {
      return this.notifications.filter(n => !n.read).length
    }
  },
  mounted() {
    this.loadUsers()
  },
  beforeUnmount() {
    this.stopPolling()
  },
  methods: {
    async loadUsers() {
      try {
        const r = await fetch(`${ACCOUNT_BASE}/api/users`)
        if (r.ok) {
          const data = await r.json()
          this.users = data.map(u => ({ id: u.userId, label: u.name }))
        }
      } catch (_) { /* silent */ }
    },

    async switchUser(id) {
      this.currentUserId = id
      this.stopPolling()
      this.seenIds = new Set()
      this.notifications = []
      this.notifsLoaded = false
      this.following = []
      this.followers = []
      this.followMsg = ''
      // Pre-load existing notifications and mark them seen so we don't re-toast old ones
      try {
        const r = await fetch(`${NOTIFICATION_BASE}/api/notifications`, {
          headers: { 'X-User-Id': id }
        })
        if (r.ok) {
          const data = await r.json()
          const items = data.content || data
          this.notifications = items
          this.notifsLoaded = true
          items.forEach(n => this.seenIds.add(n.id))
        }
      } catch (_) { /* silent */ }
      this.startPolling()
    },

    startPolling() {
      if (!this.currentUserId) return
      this.pollTimer = setInterval(async () => {
        try {
          const r = await fetch(`${NOTIFICATION_BASE}/api/notifications`, {
            headers: { 'X-User-Id': this.currentUserId }
          })
          if (!r.ok) return
          const data = await r.json()
          const items = data.content || data
          this.notifications = items
          this.notifsLoaded = true
          items.forEach(n => {
            if (!this.seenIds.has(n.id)) {
              this.seenIds.add(n.id)
              if (!n.read) this.showToast(this.parsePayload(n.payload))
            }
          })
        } catch (_) { /* silent — network errors during polling are expected */ }
      }, 3000)
    },

    stopPolling() {
      if (this.pollTimer) { clearInterval(this.pollTimer); this.pollTimer = null }
    },

    async pollForOwnerNotification(ownerId, snapshotIds) {
      const ownerName = this.labelFor(ownerId)
      const commenterName = this.labelFor(this.currentUserId)
      let attempts = 0
      const check = setInterval(async () => {
        attempts++
        if (attempts > 15) { clearInterval(check); return }
        try {
          const r = await fetch(`${NOTIFICATION_BASE}/api/notifications`, {
            headers: { 'X-User-Id': ownerId }
          })
          if (!r.ok) return
          const data = await r.json()
          const items = data.content || data
          const newOne = items.find(n => !snapshotIds.has(n.id))
          if (newOne) {
            clearInterval(check)
            this.showToast(`📬 ${ownerName} received a notification: ${commenterName} commented on their activity`)
          }
        } catch (_) { /* silent */ }
      }, 1000)
    },

    async pollForFolloweeNotification(followeeId, snapshotIds) {
      const followeeName = this.labelFor(followeeId)
      let attempts = 0
      const check = setInterval(async () => {
        attempts++
        if (attempts > 15) { clearInterval(check); return }
        try {
          const r = await fetch(`${NOTIFICATION_BASE}/api/notifications`, {
            headers: { 'X-User-Id': followeeId }
          })
          if (!r.ok) return
          const data = await r.json()
          const items = data.content || data
          const newOne = items.find(n => !snapshotIds.has(n.id))
          if (newOne) {
            clearInterval(check)
            this.showToast(`📬 ${followeeName} received: "${this.parsePayload(newOne.payload)}"`)
          }
        } catch (_) { /* silent */ }
      }, 1000)
    },

    showToast(message, duration = 4000) {
      this.toast.message = message
      this.toast.visible = true
      setTimeout(() => { this.toast.visible = false }, duration)
    },

    userHeaders() {
      return { 'X-User-Id': this.currentUserId, 'Content-Type': 'application/json' }
    },

    async apiErrorMessage(response) {
      try {
        const body = await response.json()
        return body.message || body.error || `HTTP ${response.status}`
      } catch {
        return `HTTP ${response.status}`
      }
    },

    async followUser() {
      if (!this.currentUserId || !this.followTargetId) return
      const snapshotIds = new Set()
      try {
        const snap = await fetch(`${NOTIFICATION_BASE}/api/notifications`, {
          headers: { 'X-User-Id': this.followTargetId }
        })
        if (snap.ok) {
          const snapData = await snap.json()
          ;(snapData.content || snapData).forEach(n => snapshotIds.add(n.id))
        }
      } catch (_) { /* silent */ }

      const r = await fetch(`${INTERACTION_BASE}/api/follows`, {
        method: 'POST',
        headers: this.userHeaders(),
        body: JSON.stringify({ targetUserId: this.followTargetId })
      })
      if (r.ok) {
        this.followMsg = `Now following ${this.labelFor(this.followTargetId)}`
        this.followErr = false
        this.loadFollowing()
        this.pollForFolloweeNotification(this.followTargetId, snapshotIds)
      } else {
        this.followMsg = await this.apiErrorMessage(r)
        this.followErr = true
      }
    },

    async unfollowUser() {
      if (!this.currentUserId || !this.followTargetId) return
      const r = await fetch(`${INTERACTION_BASE}/api/follows/${this.followTargetId}`, {
        method: 'DELETE',
        headers: this.userHeaders()
      })
      if (r.ok) {
        const name = this.labelFor(this.followTargetId)
        this.followMsg = `Unfollowed ${name}`
        this.followErr = false
        this.loadFollowing()
        this.showToast(`👋 You unfollowed ${name}`)
      } else {
        this.followMsg = await this.apiErrorMessage(r)
        this.followErr = true
      }
    },

    async loadFollowing() {
      if (!this.currentUserId) return
      try {
        const r = await fetch(`${INTERACTION_BASE}/api/users/${this.currentUserId}/following`)
        const data = await r.json()
        this.following = data.content || data
      } catch (_) { this.following = [] }
    },

    async loadFollowers() {
      if (!this.currentUserId) return
      try {
        const r = await fetch(`${INTERACTION_BASE}/api/users/${this.currentUserId}/followers`)
        const data = await r.json()
        this.followers = data.content || data
      } catch (_) { this.followers = [] }
    },

    async postComment() {
      if (!this.currentUserId || !this.commentText.trim()) {
        this.commentMsg = 'Select a user and write a comment.'
        this.commentErr = true; return
      }
      const activityOwnerId = this.users.find(u => u.id !== this.currentUserId)?.id || null

      // snapshot owner's current notifications before posting
      const snapshotIds = new Set()
      if (activityOwnerId) {
        try {
          const snap = await fetch(`${NOTIFICATION_BASE}/api/notifications`, {
            headers: { 'X-User-Id': activityOwnerId }
          })
          if (snap.ok) {
            const snapData = await snap.json()
            ;(snapData.content || snapData).forEach(n => snapshotIds.add(n.id))
          }
        } catch (_) { /* silent */ }
      }

      try {
        const r = await fetch(`${INTERACTION_BASE}/api/activities/${DEMO_ACTIVITY_ID}/comments`, {
          method: 'POST',
          headers: this.userHeaders(),
          body: JSON.stringify({ body: this.commentText, activityOwnerId })
        })
        if (!r.ok) throw new Error(await this.apiErrorMessage(r))
        this.commentMsg = 'Comment posted!'
        this.commentErr = false
        this.commentText = ''
        this.loadComments()
        if (activityOwnerId) this.pollForOwnerNotification(activityOwnerId, snapshotIds)
      } catch (e) {
        this.commentMsg = e.message; this.commentErr = true
      }
    },

    async loadComments() {
      try {
        const r = await fetch(`${INTERACTION_BASE}/api/activities/${DEMO_ACTIVITY_ID}/comments`)
        const data = await r.json()
        this.comments = data.content || data
        this.commentsLoaded = true
      } catch (_) { this.comments = []; this.commentsLoaded = true }
    },

    startEditComment(c) {
      this.editingCommentId = c.commentId
      this.editCommentText = c.content
    },

    async saveEditComment(commentId) {
      try {
        const r = await fetch(`${INTERACTION_BASE}/api/comments/${commentId}`, {
          method: 'PUT',
          headers: this.userHeaders(),
          body: JSON.stringify({ body: this.editCommentText })
        })
        if (!r.ok) throw new Error(await this.apiErrorMessage(r))
        this.editingCommentId = null
        this.loadComments()
      } catch (e) {
        this.commentMsg = e.message; this.commentErr = true
      }
    },

    async deleteComment(commentId) {
      try {
        const r = await fetch(`${INTERACTION_BASE}/api/comments/${commentId}`, {
          method: 'DELETE',
          headers: this.userHeaders()
        })
        if (!r.ok) throw new Error(await this.apiErrorMessage(r))
        this.comments = this.comments.filter(c => c.commentId !== commentId)
        this.commentMsg = 'Comment deleted.'
        this.commentErr = false
      } catch (e) {
        this.commentMsg = e.message; this.commentErr = true
      }
    },

    async loadNotifications() {
      if (!this.currentUserId) {
        this.notifMsg = 'Select a user first.'
        this.notifErr = true; return
      }
      try {
        const r = await fetch(`${NOTIFICATION_BASE}/api/notifications`, {
          headers: { 'X-User-Id': this.currentUserId }
        })
        if (!r.ok) throw new Error(`HTTP ${r.status}`)
        const data = await r.json()
        this.notifications = data.content || data
        this.notifsLoaded = true
        this.notifMsg = ''; this.notifErr = false
      } catch (e) {
        this.notifMsg = e.message; this.notifErr = true
      }
    },

    async markRead(notificationId) {
      try {
        await fetch(`${NOTIFICATION_BASE}/api/notifications/${notificationId}/read`, {
          method: 'PATCH',
          headers: { 'X-User-Id': this.currentUserId }
        })
        const n = this.notifications.find(x => x.id === notificationId)
        if (n) n.read = true
      } catch (e) {
        this.notifMsg = e.message; this.notifErr = true
      }
    },

    async markAllRead() {
      if (!this.currentUserId) return
      try {
        await fetch(`${NOTIFICATION_BASE}/api/notifications/read-all`, {
          method: 'POST',
          headers: { 'X-User-Id': this.currentUserId }
        })
        this.notifications.forEach(n => (n.read = true))
        this.notifMsg = 'All marked as read.'
        this.notifErr = false
      } catch (e) {
        this.notifMsg = e.message; this.notifErr = true
      }
    },

    labelFor(userId) {
      const u = this.users.find(x => x.id === userId)
      return u ? u.label : userId
    },

    parsePayload(payload) {
      if (!payload) return ''
      try {
        const obj = JSON.parse(payload)
        let msg = obj.message || obj.content || payload
        this.users.forEach(u => { msg = msg.replaceAll(u.id, u.label) })
        return msg
      } catch {
        return payload
      }
    },

    typeClass(type) {
      return type ? type.toUpperCase() : ''
    },

    formatTime(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleString('en-GB', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
/* ── Toast ── */
.toast {
  position: fixed;
  top: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: #2c3e50;
  color: #fff;
  padding: 12px 28px;
  border-radius: 8px;
  font-size: 0.95em;
  font-weight: bold;
  z-index: 9999;
  box-shadow: 0 4px 16px rgba(0,0,0,0.3);
  white-space: nowrap;
  max-width: 90vw;
  white-space: normal;
  text-align: center;
}
.toast-enter-active { animation: slideDown 0.3s ease; }
.toast-leave-active { animation: fadeOut 0.3s ease; }
@keyframes slideDown {
  from { opacity: 0; top: 0; }
  to   { opacity: 1; top: 20px; }
}
@keyframes fadeOut {
  from { opacity: 1; }
  to   { opacity: 0; }
}

.interaction-demo { text-align: left; }

.columns { display: flex; gap: 20px; }
.column  { flex: 1; display: flex; flex-direction: column; gap: 16px; }

.section-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 2px;
}
.section-header small { color: #888; }

.service-badge {
  font-size: 0.78em;
  font-weight: bold;
  padding: 3px 10px;
  border-radius: 12px;
  color: white;
}
.service-badge.interaction { background: #42b983; }
.service-badge.notification { background: #3498db; }

.card {
  background: #f9f9f9;
  border-radius: 10px;
  padding: 16px 18px;
  border: 1px solid #e0e0e0;
}
.card h3 { margin: 0 0 12px; color: #2c3e50; font-size: 1em; display: flex; align-items: center; gap: 8px; }

.user-ctx {
  margin-bottom: 10px;
  background: #eaf6f0;
  border-color: #42b983;
}
.user-ctx label { display: block; margin-bottom: 8px; }

.user-btns { display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px; }
.user-btn  { background: #ddd; color: #333; }
.user-btn.active { background: #42b983; color: white; }

.activity-picker { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 6px; }

.or-row { text-align: center; color: #aaa; font-size: 0.8em; margin: 4px 0; }

.field-label { display: block; font-size: 0.8em; font-weight: bold; color: #555; margin: 8px 0 4px; }

input, textarea {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.9em;
  margin-bottom: 8px;
}
textarea { resize: vertical; }

.btn-row { display: flex; gap: 8px; margin-bottom: 8px; flex-wrap: wrap; }

button {
  padding: 7px 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.88em;
  font-weight: bold;
}
button:disabled { opacity: 0.45; cursor: not-allowed; }
.btn-primary { background: #42b983; color: white; }
.btn-danger  { background: #e74c3c; color: white; }
.btn-sm      { background: #ddd; color: #333; padding: 4px 10px; }
.btn-xs      { background: #eee; color: #333; padding: 2px 8px; font-size: 0.8em; }
.btn-xs.danger { background: #fde; color: #c0392b; }

.msg  { font-size: 0.88em; margin: 4px 0; }
.ok   { color: #27ae60; }
.err  { color: #c0392b; }
.empty { color: #aaa; font-size: 0.88em; }
.hint  { color: #aaa; font-size: 0.82em; display: block; margin-top: 4px; }

.sub-section { margin-top: 14px; }
.sub-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  font-size: 0.85em;
  color: #555;
  margin-bottom: 4px;
}
ul { margin: 0 0 10px; padding-left: 18px; font-size: 0.9em; }

/* Comments */
.comment-list { margin-top: 10px; }
.comment-item {
  background: #fff;
  border-radius: 6px;
  padding: 8px 12px;
  margin-bottom: 6px;
  border-left: 3px solid #42b983;
  font-size: 0.9em;
}
.comment-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2px; }
.comment-author { font-weight: bold; color: #2c3e50; font-size: 0.82em; }
.comment-actions { display: flex; gap: 4px; }
.comment-body   { color: #333; }
.comment-time   { color: #bbb; font-size: 0.78em; display: block; margin-top: 2px; }

/* Notifications */
.badge-count {
  background: #e74c3c;
  color: white;
  border-radius: 10px;
  font-size: 0.72em;
  padding: 1px 7px;
  font-weight: bold;
}

.notif-list { margin-top: 10px; }
.notif-item {
  background: #fff;
  border-radius: 6px;
  padding: 10px 12px;
  margin-bottom: 8px;
  border-left: 3px solid #bdc3c7;
  font-size: 0.9em;
}
.notif-item.unread { border-left-color: #f39c12; background: #fffbf0; }
.notif-top    { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.notif-actions { display: flex; gap: 4px; }
.notif-body   { margin: 4px 0 2px; color: #333; }
.notif-time   { color: #bbb; font-size: 0.8em; }

.badge-type {
  color: white;
  font-size: 0.72em;
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: bold;
  background: #95a5a6;
}
.badge-type.FOLLOW   { background: #42b983; }
.badge-type.COMMENT  { background: #9b59b6; }
.badge-type.REMINDER { background: #e67e22; }
.badge-type.CANCEL   { background: #e74c3c; }
.badge-type.JOIN     { background: #3498db; }
.badge-type.LEAVE    { background: #7f8c8d; }

/* Legend */
.legend-card { padding: 14px 16px; }
.legend-list { display: flex; flex-direction: column; gap: 8px; margin-top: 6px; }
.legend-item { display: flex; align-items: center; gap: 10px; font-size: 0.88em; color: #555; }
</style>
