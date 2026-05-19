<template>
  <div class="detail">
    <router-link to="/activities" class="back">&larr; Back to all activities</router-link>

    <div v-if="loading" class="loading">Loading activity...</div>

    <div v-else-if="error && !activity" class="error">
      <p>{{ error }}</p>
      <button @click="fetchActivity">Retry</button>
    </div>

    <div v-else-if="activity">
      <h2>{{ activity.title }}</h2>

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

      <!-- ============ ORGANIZER ============ -->
      <section class="organizer-card">
        <h3>Organizer</h3>
        <div v-if="loadingOrganizer" class="muted">Loading organizer profile...</div>
        <div v-else-if="organizer">
          <div class="profile-row">
            <span class="profile-label">Name:</span>
            <span class="profile-value">{{ organizer.name || '—' }}</span>
          </div>
          <div class="profile-row">
            <span class="profile-label">Email:</span>
            <span class="profile-value">{{ organizer.email || organizer.contactInfo?.email || '—' }}</span>
          </div>
          <div class="profile-row" v-if="organizer.phone || organizer.contactInfo?.phone">
            <span class="profile-label">Phone:</span>
            <span class="profile-value">{{ organizer.phone || organizer.contactInfo?.phone }}</span>
          </div>
          <div class="profile-row" v-if="organizer.city">
            <span class="profile-label">City:</span>
            <span class="profile-value">{{ organizer.city }}</span>
          </div>
          <div class="profile-row" v-if="organizer.role">
            <span class="profile-label">Role:</span>
            <span class="profile-value">{{ organizer.role }}</span>
          </div>
        </div>
        <div v-else class="muted">
          Could not load organizer profile (Account Management Service unavailable).
        </div>
      </section>

      <!-- ============ EDIT ============ -->
      <section v-if="canEdit">
        <h3>Edit Activity</h3>
        <label>Title</label>
        <input v-model="editForm.title" />

        <label>Description</label>
        <input v-model="editForm.description" />

        <label>Max Participants</label>
        <input v-model.number="editForm.maxParticipants" type="number" min="1" />

        <label>Preferred Time Slot ID</label>
        <input v-model="editForm.preferredTimeSlotId" />

        <button @click="updateActivity" :disabled="actionLoading">
          {{ actionLoading ? 'Saving…' : 'Save Changes' }}
        </button>
      </section>

      <!-- ============ PARTICIPANTS ============ -->
      <section>
        <h3>Participants ({{ activity.participants.length }})</h3>
        <div v-if="activity.participants.length === 0">No participants yet.</div>
        <ul v-else class="participants-list">
          <li v-for="p in activity.participants" :key="p.participantId">
            <span class="role-badge" :class="'role-' + p.role.toLowerCase()">
              {{ p.role }}
            </span>
            <div class="participant-info">
              <div class="participant-name">
                {{ profileFor(p.userId)?.name || 'Loading…' }}
              </div>
              <div class="participant-email">
                {{ profileFor(p.userId)?.email
                   || profileFor(p.userId)?.contactInfo?.email
                   || '' }}
              </div>
            </div>
            <small class="joined-at">joined {{ formatDate(p.joinedAt) }}</small>
            <div class="participant-actions">
              <button
                v-if="activity.status === 'COMPLETED' && p.userId !== currentUserId"
                class="btn-secondary small"
                @click="toggleRating(p.userId)">
                {{ ratingForUserId === p.userId ? 'Cancel Rating' : 'Rate' }}
              </button>
              <button
                v-if="canRemoveParticipant(p)"
                class="btn-danger small"
                @click="removeParticipant(p.userId)">
                Remove
              </button>
            </div>
          </li>
        </ul>
      </section>

      <!-- ============ RATE PARTICIPANTS FORM ============ -->
      <section v-if="activity.status === 'COMPLETED' && ratingForUserId">
        <h3>Rate Participant: {{ profileFor(ratingForUserId)?.name || ratingForUserId }}</h3>
        <p class="muted">You are rating this user for their performance in "{{ activity.title }}"</p>

        <LeaveRating
          v-if="currentUserId"
          :activityId="activityId"
          :reviewerId="currentUserId"
          :revieweeId="ratingForUserId"
          @rating-submitted="onRatingSubmitted"
        />
        <p v-else class="error-msg">You must be logged in to leave a rating.</p>
      </section>

      <!-- ============ JOIN/LEAVE ============ -->
      <section v-if="activity.status === 'PLANNED' || activity.status === 'ACTIVE'">
        <h3>Join / Leave</h3>
        <p class="muted" v-if="!currentUserId">
          You must be logged in to join an activity.
        </p>
        <div v-else>
          <p class="muted">
            Acting as <b>{{ authState.email || currentUserId }}</b>
          </p>
          <div class="actions">
            <button
              v-if="!isParticipant"
              class="btn-primary"
              @click="joinActivity"
              :disabled="actionLoading">
              Join
            </button>
            <button
              v-if="isParticipant && !isCurrentUserOrganizer"
              class="btn-secondary"
              @click="leaveActivity"
              :disabled="actionLoading">
              Leave
            </button>
            <span v-if="isCurrentUserOrganizer" class="muted">
              You are the organizer of this activity.
            </span>
          </div>
        </div>
      </section>

      <!-- ============ CANCEL ============ -->
      <section v-if="canCancel" class="danger-zone">
        <h3>Danger Zone</h3>
        <button class="btn-danger" @click="cancelActivity" :disabled="actionLoading">
          {{ actionLoading ? 'Cancelling…' : 'Cancel This Activity' }}
        </button>
      </section>

      <!-- ============ MESSAGES ============ -->
      <div v-if="error" class="error msg">
        <p><b>Error:</b> {{ error }}</p>
      </div>
      <div v-if="success" class="success msg">
        <p>✓ {{ success }}</p>
      </div>
    </div>
  </div>
</template>

<script>
import LeaveRating from '@/views/ratings/LeaveRating.vue'
import { activityApi, accountApi } from '../../api/client'
import { authState } from '../../store/authStore'

export default {
  name: 'ActivityDetail',
  components: { LeaveRating },
  data() {
    return {
      authState,
      activityId: this.$route.params.id,
      activity: null,
      organizer: null,
      profiles: {},          // cache: userId -> profile
      loading: false,
      loadingOrganizer: false,
      actionLoading: false,
      error: null,
      success: null,
      ratingForUserId: null, // which user is being rated
      editForm: {
        title: '',
        description: '',
        maxParticipants: 0,
        preferredTimeSlotId: ''
      }
    }
  },
  computed: {
    currentUserId() {
      return this.authState.accountId
    },
    isModerator() {
      return this.authState.role === 'MODERATOR'
    },
    isCurrentUserOrganizer() {
      return this.activity && this.activity.organizerId === this.currentUserId
    },
    isParticipant() {
      if (!this.activity || !this.currentUserId) return false
      return this.activity.participants.some(p => p.userId === this.currentUserId)
    },
    canEdit() {
      if (!this.activity) return false
      if (this.activity.status !== 'PLANNED' && this.activity.status !== 'ACTIVE') return false
      return this.isCurrentUserOrganizer || this.isModerator
    },
    canCancel() {
      if (!this.activity) return false
      if (this.activity.status === 'CANCELLED' || this.activity.status === 'COMPLETED') return false
      return this.isCurrentUserOrganizer || this.isModerator
    }
  },
  methods: {
    canRemoveParticipant(p) {
      if (!this.activity) return false
      if (this.activity.status === 'COMPLETED') return false
      if (p.role === 'ORGANIZER') return false
      return this.isCurrentUserOrganizer || this.isModerator
    },

    toggleRating(userId) {
      this.ratingForUserId = this.ratingForUserId === userId ? null : userId
    },

    onRatingSubmitted() {
      this.success = 'Rating submitted successfully!'
      this.ratingForUserId = null
    },

    async fetchActivity() {
      this.loading = true
      this.error = null
      try {
        const data = await activityApi.get(this.activityId)
        this.activity = data
        this.editForm.title = data.title || ''
        this.editForm.description = data.description || ''
        this.editForm.maxParticipants = data.maxParticipants || 1
        this.editForm.preferredTimeSlotId = data.preferredTimeSlotId || ''

        // Fetch organizer + participants profiles in parallel
        this.fetchOrganizer(data.organizerId)
        for (const p of data.participants) {
          this.fetchProfile(p.userId)
        }
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.loading = false
      }
    },

    async fetchOrganizer(userId) {
      this.loadingOrganizer = true
      try {
        this.organizer = await this.fetchProfile(userId)
      } finally {
        this.loadingOrganizer = false
      }
    },

    async fetchProfile(userId) {
      if (this.profiles[userId]) return this.profiles[userId]
      try {
        const profile = await accountApi.getUser(userId)
        if (profile) {
          this.profiles = { ...this.profiles, [userId]: profile }
        }
        return profile
      } catch (err) {
        return null
      }
    },

    profileFor(userId) {
      return this.profiles[userId]
    },

    async updateActivity() {
      this.actionLoading = true
      this.error = null
      this.success = null
      const body = {
        title: this.editForm.title,
        description: this.editForm.description,
        maxParticipants: this.editForm.maxParticipants
      }
      if (this.editForm.preferredTimeSlotId) {
        body.preferredTimeSlotId = this.editForm.preferredTimeSlotId
      }
      try {
        await activityApi.update(this.activityId, body)
        this.success = 'Activity updated.'
        await this.fetchActivity()
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.actionLoading = false
      }
    },

    async cancelActivity() {
      if (!confirm('Are you sure you want to cancel this activity?')) return
      this.actionLoading = true
      this.error = null
      this.success = null
      try {
        await activityApi.cancel(this.activityId)
        this.success = 'Activity cancelled. Reserved slot will be released via Kafka.'
        await this.fetchActivity()
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.actionLoading = false
      }
    },

    async joinActivity() {
      if (!this.currentUserId) {
        this.error = 'You must be logged in.'
        return
      }
      this.actionLoading = true
      this.error = null
      this.success = null
      try {
        await activityApi.join(this.activityId, this.currentUserId)
        this.success = `Joined successfully.`
        await this.fetchActivity()
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.actionLoading = false
      }
    },

    async leaveActivity() {
      if (!this.currentUserId) {
        this.error = 'You must be logged in.'
        return
      }
      this.actionLoading = true
      this.error = null
      this.success = null
      try {
        await activityApi.leave(this.activityId, this.currentUserId)
        this.success = `Left successfully.`
        await this.fetchActivity()
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.actionLoading = false
      }
    },

    async removeParticipant(userId) {
      const name = this.profileFor(userId)?.name || userId
      if (!confirm(`Remove participant ${name}?`)) return
      this.actionLoading = true
      this.error = null
      this.success = null
      try {
        await activityApi.removeParticipant(this.activityId, userId)
        this.success = `Participant removed.`
        await this.fetchActivity()
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.actionLoading = false
      }
    },

    formatDate(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleString('en-GB', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
      })
    }
  },
  mounted() {
    this.fetchActivity()
  }
}
</script>

<style scoped>
.detail {
  text-align: left;
  max-width: 700px;
  margin: 0 auto;
}
.back {
  display: inline-block;
  margin-bottom: 20px;
  color: #42b983;
  text-decoration: none;
}
h2 { color: #2c3e50; }
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}
.badge, .role-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
}
.status-planned   { background: #3498db; }
.status-active    { background: #27ae60; }
.status-completed { background: #95a5a6; }
.status-cancelled { background: #e74c3c; }
.role-organizer { background: #e67e22; }
.role-player    { background: #16a085; }
.description {
  font-style: italic;
  color: #555;
  margin-bottom: 15px;
}
section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  margin-top: 20px;
}
section.organizer-card {
  background: #ecf3ee;
  border-left: 4px solid #42b983;
}
section.danger-zone {
  background: #fef0ef;
  border-left: 4px solid #e74c3c;
}
h3 {
  margin-top: 0;
  color: #2c3e50;
}
.profile-row {
  display: flex;
  gap: 10px;
  padding: 4px 0;
}
.profile-label {
  font-weight: bold;
  color: #555;
  min-width: 70px;
}
.profile-value { color: #2c3e50; }
.muted { color: #999; font-style: italic; }
label {
  display: block;
  margin: 12px 0 4px;
  font-weight: bold;
  font-size: 0.8em;
  text-transform: uppercase;
  color: #555;
}
input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #d5dde0;
  border-radius: 6px;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
  align-items: center;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 12px;
}
button:disabled { background: #aaa; cursor: not-allowed; }
.btn-primary { background: #42b983; }
.btn-secondary { background: #95a5a6; }
.btn-danger { background: #e74c3c; }
button.small { padding: 4px 10px; font-size: 0.8em; margin-top: 0; }
.participants-list {
  list-style: none;
  padding: 0;
}
.participants-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
}
.participants-list li:last-child { border-bottom: none; }
.participant-info { flex: 1; }
.participant-name { font-weight: bold; color: #2c3e50; }
.participant-email { font-size: 0.85em; color: #777; }
.joined-at { color: #999; font-size: 0.8em; }
.msg { padding: 10px 15px; margin-top: 20px; border-radius: 6px; }
.error { background: #fadbd8; color: #c0392b; }
.success { background: #d4efdf; color: #196f3d; }
.loading { text-align: center; padding: 30px; }
.participant-actions {
  display: flex;
  gap: 8px;
}
.error-msg {
  color: #e74c3c;
  font-weight: bold;
  margin-top: 10px;
}
</style>