<template>
  <div class="form">
    <h2>User Profile</h2>

    <label>User ID (UUID)</label>
    <div class="row">
      <input v-model="userId" placeholder="Enter user UUID" />
      <button
        v-if="canUseMyId"
        @click="useMyId"
        type="button"
        class="ghost"
        title="Fill with the user ID you're signed in as"
      >Use mine</button>
    </div>

    <div class="actions">
      <button @click="fetchUser" :disabled="loading || !userId" class="secondary">
        <Spinner v-if="loading && !profile" :size="14" inline />
        <span>Load profile</span>
      </button>
    </div>

    <div v-if="loadError" class="error"><p><b>Error:</b> {{ loadError }}</p></div>

    <template v-if="profile">
      <hr />

      <!-- ============ REPUTATION (shown when viewing any profile) ============ -->
      <div class="profile-header">
        <p class="readonly">
          <small>
            <b>Name:</b> {{ profile.name }}<br />
            <b>Email:</b> {{ profile.email }}<br />
            <b>Role:</b> {{ profile.role }}<br />
            <b>Status:</b> {{ profile.status }}
          </small>
        </p>

        <div v-if="reputation" class="reputation-card">
          <div class="reputation-score">
            <span class="score-value">{{ reputation.averageScore.toFixed(1) }}</span>
            <span class="score-label">{{ reputation.userLabel }}</span>
          </div>
          <div class="reputation-meta">
            <span>{{ reputation.totalRatings }} ratings</span>
            <button
              v-if="isModerator"
              @click="recomputeReputation"
              class="ghost small"
              :disabled="loading"
            >Recompute</button>
          </div>
        </div>
        <div v-else class="reputation-card empty">
          <p>No reputation data available yet.</p>
          <button
            v-if="isModerator"
            @click="recomputeReputation"
            class="ghost small"
            :disabled="loading"
          >Initialize</button>
        </div>
      </div>

      <!-- ── Read-only profile card (viewing someone else) ── -->
      <div v-if="!isOwnProfile" class="profile-card">
        <div class="pc-avatar" :style="{ background: avatarColor(profile.name) }">
          {{ initials(profile.name) }}
        </div>
        <div class="pc-name">{{ profile.name || 'Unnamed' }}</div>
        <div class="pc-email">{{ profile.email }}</div>
        <div class="pc-badges">
          <span class="badge" :class="'role-' + (profile.role || '').toLowerCase()">{{ profile.role }}</span>
          <span class="badge" :class="'status-' + (profile.status || '').toLowerCase()">{{ profile.status }}</span>
          <span v-if="profile.skillLevel" class="badge skill">{{ profile.skillLevel }}</span>
        </div>
        <button
          class="follow-btn"
          :class="{ following }"
          :disabled="followLoading"
          @click="toggleFollow"
        >
          <span v-if="followLoading">…</span>
          <span v-else-if="following">✓ Following</span>
          <span v-else>+ Follow</span>
        </button>
        <div class="pc-grid">
          <div v-if="profile.language" class="pc-item">
            <span class="pc-icon">🌐</span>
            <span class="pc-label">Language</span>
            <span class="pc-value">{{ profile.language }}</span>
          </div>
          <div v-if="profile.city || profile.district" class="pc-item">
            <span class="pc-icon">📍</span>
            <span class="pc-label">Location</span>
            <span class="pc-value">{{ [profile.city, profile.district].filter(Boolean).join(', ') }}</span>
          </div>
          <div v-if="profile.sportPreferences" class="pc-item pc-item--full">
            <span class="pc-icon">🏅</span>
            <span class="pc-label">Sport preferences</span>
            <span class="pc-value">{{ profile.sportPreferences }}</span>
          </div>
        </div>
      </div>

      <!-- ── Own profile: compact summary + edit form ── -->
      <template v-else>
        <p class="readonly">
          <small>
            <b>Name:</b> {{ profile.name }}&ensp;·&ensp;
            <b>Email:</b> {{ profile.email }}&ensp;·&ensp;
            <b>Role:</b> {{ profile.role }}&ensp;·&ensp;
            <b>Status:</b> {{ profile.status }}
          </small>
        </p>

        <h3>Update fields</h3>

        <label>Name</label>
        <input v-model="updates.name" />

        <label>Phone</label>
        <input v-model="updates.phone" />

        <label>Other contact</label>
        <input v-model="updates.other" />

        <label>Skill level</label>
        <select v-model="updates.skillLevel">
          <option :value="null">— Keep current —</option>
          <option value="BEGINNER">BEGINNER</option>
          <option value="INTERMEDIATE">INTERMEDIATE</option>
          <option value="ADVANCED">ADVANCED</option>
          <option value="PROFESSIONAL">PROFESSIONAL</option>
        </select>

        <label>Language</label>
        <input v-model="updates.language" />

        <label>Sport preferences (comma separated)</label>
        <input v-model="updates.sportPreferences" />

        <label>City</label>
        <input v-model="updates.city" />

        <label>District</label>
        <input v-model="updates.district" />

        <button @click="saveProfile" :disabled="loading">
          <Spinner v-if="loading && profile" :size="14" inline />
          <span>{{ loading ? 'Saving…' : 'Save changes' }}</span>
        </button>

        <div v-if="saveError" class="error"><p><b>Error:</b> {{ saveError }}</p></div>
      </template>
    </template>
  </div>
</template>

<script>
import { accountApi, interactionApi, ratingApi } from '../../api/client'
import { authState } from '../../store/authStore'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'UserProfileView',
  components: { Spinner },
  data() {
    return {
      userId: '',
      profile: null,
      reputation: null,
      updates: {
        name: '', phone: '', other: '', skillLevel: null,
        language: '', sportPreferences: '', city: '', district: ''
      },
      loading: false,
      loadError: null,
      saveError: null,
      following: false,
      followLoading: false,
      authState
    }
  },
  computed: {
    canUseMyId() {
      return authState.token && authState.accountType === 'USER' && authState.accountId
    },
    isOwnProfile() {
      return authState.accountId && this.userId === authState.accountId
    },
    isModerator() {
      return authState.accountType === 'MODERATOR'
    }
  },
  mounted() {
    if (this.$route.query.id) {
      this.userId = this.$route.query.id
      this.fetchUser()
    } else if (this.canUseMyId) {
      this.userId = authState.accountId
    }
  },
  methods: {
    useMyId() {
      this.userId = authState.accountId
    },
    initials(name) {
      if (!name) return '?'
      return name.trim().split(/\s+/).map(w => w[0]).slice(0, 2).join('').toUpperCase()
    },
    avatarColor(name) {
      const palette = ['#3498db','#2ecc71','#9b59b6','#e67e22','#1abc9c','#e74c3c','#34495e']
      if (!name) return palette[0]
      return palette[name.charCodeAt(0) % palette.length]
    },
    async checkFollowing() {
      if (!authState.accountId || this.isOwnProfile) return
      try {
        const res = await interactionApi.isFollowing(authState.accountId, this.userId)
        this.following = res?.following ?? false
      } catch { /* ignore */ }
    },
    async toggleFollow() {
      if (!authState.accountId) { toast.error('Sign in to follow users.'); return }
      this.followLoading = true
      try {
        if (this.following) {
          await interactionApi.unfollowUser(authState.accountId, this.userId)
          this.following = false
          toast.info('Unfollowed.')
        } else {
          await interactionApi.followUser(authState.accountId, this.userId)
          this.following = true
          toast.success('Following!')
        }
      } catch (e) {
        toast.error(e.message)
      } finally {
        this.followLoading = false
      }
    },
    async fetchUser() {
      this.loading = true; this.loadError = null; this.profile = null; this.reputation = null
      try {
        const [profile, reputation] = await Promise.all([
          accountApi.getUser(this.userId),
          ratingApi.getReputation(this.userId).catch(() => null)
        ])
        this.profile = profile
        this.reputation = reputation
        this.checkFollowing()
        this.updates = {
          name: this.profile.name || '',
          phone: this.profile.phone || '',
          other: this.profile.other || '',
          skillLevel: null,
          language: this.profile.language || '',
          sportPreferences: this.profile.sportPreferences || '',
          city: this.profile.city || '',
          district: this.profile.district || ''
        }
      } catch (e) {
        this.loadError = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    },
    async recomputeReputation() {
      this.loading = true
      try {
        this.reputation = await ratingApi.recomputeReputation(this.userId)
        toast.success('Reputation recomputed.')
      } catch (e) {
        toast.error('Failed to recompute: ' + e.message)
      } finally {
        this.loading = false
      }
    },
    async saveProfile() {
      this.loading = true; this.saveError = null
      const body = Object.fromEntries(
        Object.entries(this.updates).filter(([, v]) => v !== '' && v !== null)
      )
      try {
        this.profile = await accountApi.updateUser(this.userId, body)
        toast.success('Profile updated.')
      } catch (e) {
        this.saveError = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 540px; margin: 0 auto; background: #fff;
  padding: 28px 30px; border-radius: 10px; text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2, h3 { color: #2c3e50; }
h2 { text-align: center; margin-top: 0; }
h3 { margin-top: 24px; font-size: 1em; }
label {
  display: block; margin: 14px 0 6px; font-weight: bold;
  font-size: 0.85em; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
}
.row { display: flex; gap: 8px; align-items: stretch; }
.row input { flex: 1; }
.actions { margin-top: 12px; }
input, select {
  display: block; width: 100%; padding: 10px; box-sizing: border-box;
  border: 1px solid #d5dde0; border-radius: 6px; font-size: 0.95em;
}
input:focus, select:focus { outline: none; border-color: #3498db; }
button {
  background: #3498db; color: white; border: none;
  padding: 11px 22px; margin-top: 8px; border-radius: 6px;
  font-size: 0.95em; font-weight: bold; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  min-width: 160px;
}
button.secondary { background: #ecf0f1; color: #2c3e50; }
button.ghost {
  background: transparent; color: #3498db; border: 1px solid #3498db;
  padding: 0 12px; min-width: 0; margin: 0; font-size: 0.85em;
}
button.ghost:hover { background: #eaf3fa; }
button:disabled { background: #aaa; cursor: not-allowed; }
.readonly { background: #eaf3fa; padding: 10px 14px; border-radius: 6px; }
.error { background: #fadbd8; color: #c0392b; padding: 10px 14px; margin-top: 14px; border-radius: 6px; font-size: 0.9em; }
hr { border: none; border-top: 1px solid #eee; margin: 20px 0; }

/* ── Profile card (read-only view) ── */
.profile-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 8px 0 4px;
}
.pc-avatar {
  width: 80px; height: 80px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  color: white; font-size: 1.8em; font-weight: bold;
  margin-bottom: 14px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.pc-name {
  font-size: 1.3em; font-weight: bold; color: #2c3e50; margin-bottom: 4px;
}
.pc-email {
  font-size: 0.85em; color: #888; margin-bottom: 12px;
}
.pc-badges {
  display: flex; flex-wrap: wrap; justify-content: center; gap: 6px; margin-bottom: 20px;
}
.badge {
  font-size: 0.72em; font-weight: bold; padding: 3px 10px;
  border-radius: 999px; text-transform: uppercase; letter-spacing: 0.3px;
  background: #ecf0f1; color: #555;
}
.badge.role-user       { background: #d6eaf8; color: #1f618d; }
.badge.role-moderator  { background: #e8daef; color: #6c3483; }
.badge.role-organizer  { background: #d5f5e3; color: #1e8449; }
.badge.status-active   { background: #d5f5e3; color: #1e8449; }
.badge.status-suspended { background: #fdebd0; color: #935116; }
.badge.status-banned   { background: #fadbd8; color: #922b21; }
.badge.skill           { background: #f4f6f7; color: #555; }

.follow-btn {
  margin-bottom: 20px;
  padding: 8px 28px;
  border-radius: 999px;
  border: 2px solid #3498db;
  background: #3498db;
  color: white;
  font-weight: bold;
  font-size: 0.9em;
  cursor: pointer;
  min-width: 120px;
  transition: background 0.15s, color 0.15s;
}
.follow-btn.following {
  background: white;
  color: #3498db;
}
.follow-btn:disabled { opacity: 0.6; cursor: not-allowed; }
.pc-grid {
  width: 100%;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  text-align: left;
}
.pc-item {
  background: #f8f9fa;
  border: 1px solid #eaecef;
  border-radius: 8px;
  padding: 10px 14px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.pc-item--full { grid-column: 1 / -1; }
.pc-icon { font-size: 1em; margin-bottom: 2px; }
.pc-label {
  font-size: 0.7em; text-transform: uppercase; letter-spacing: 0.5px;
  color: #aaa; font-weight: bold;
}
.pc-value { font-size: 0.9em; color: #2c3e50; font-weight: 500; }

.profile-header {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  flex-wrap: wrap;
}
.profile-header > p { flex: 1; min-width: 200px; }

.reputation-card {
  background: #f8f9fa;
  border: 1px solid #e9ecef;
  border-radius: 10px;
  padding: 16px;
  min-width: 180px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.reputation-card.empty {
  color: #6c757d;
  font-size: 0.85em;
  text-align: center;
}
.reputation-score {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.score-value {
  font-size: 2em;
  font-weight: bold;
  color: #2c3e50;
  line-height: 1;
}
.score-label {
  font-size: 0.75em;
  font-weight: bold;
  text-transform: uppercase;
  color: #3498db;
  letter-spacing: 1px;
}
.reputation-meta {
  font-size: 0.8em;
  color: #6c757d;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}
button.small {
  padding: 4px 8px;
  min-width: 0;
  font-size: 0.8em;
  margin-top: 4px;
}
</style>
