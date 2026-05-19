<template>
  <div class="availability">
    <h2>User Availability Schedule</h2>

    <section class="user-picker">
      <label>User ID</label>
      <div class="picker-row">
        <input v-model="userIdInput" placeholder="UUID of user" />
          <button @click="loadUser" :disabled="loading || !userIdInput">
            {{ loading ? 'Loading…' : 'Load' }}
          </button>
          <button
            v-if="authState.accountId"
            class="btn-mini"
            @click="loadMyself"
            :disabled="loading"
            title="Load my own schedule">
            My
          </button>
      </div>
      <p v-if="userProfile" class="user-info">
        {{ userProfile.name }}
        <span v-if="userProfile.email || userProfile.contactInfo?.email">
          — {{ userProfile.email || userProfile.contactInfo?.email }}
        </span>
      </p>
    </section>

    <section v-if="loaded">
      <h3>Weekly Availability Windows</h3>
      <p class="hint">
        Each window is a recurring time range on a specific day of the week
        (e.g. "Mondays 18:00–20:00"). Saving replaces the user's entire schedule.
      </p>

      <div v-if="windows.length === 0" class="empty-inline">
        No windows yet — add one below.
      </div>

      <div class="window" v-for="(w, idx) in windows" :key="idx">
        <div class="window-header">
          <span class="window-number">Window {{ idx + 1 }}</span>
          <button class="btn-remove" @click="removeWindow(idx)">Remove</button>
        </div>
        <div class="window-fields">
          <div>
            <label>Day of Week</label>
            <select v-model="w.dayOfWeek">
              <option v-for="d in days" :key="d" :value="d">{{ d }}</option>
            </select>
          </div>
          <div>
            <label>From</label>
            <input v-model="w.timeRangeFrom" placeholder="2026-05-25T18:00:00" />
          </div>
          <div>
            <label>To</label>
            <input v-model="w.timeRangeTo" placeholder="2026-05-25T20:00:00" />
          </div>
        </div>
      </div>

      <button class="btn-secondary" @click="addWindow">+ Add window</button>

      <div class="save-row">
        <button class="btn-primary" @click="saveAvailability" :disabled="saving">
          {{ saving ? 'Saving…' : 'Save Schedule' }}
        </button>
      </div>
    </section>

    <div v-if="error" class="error msg">
      <p><b>Error:</b> {{ error }}</p>
    </div>
    <div v-if="success" class="success msg">
      <p>✓ {{ success }}</p>
    </div>
  </div>
</template>

<script>
import { schedulingApi, accountApi } from '../../api/client'
import { authState } from '../../store/authStore'

export default {
  name: 'UserAvailability',
  data() {
    return {
      authState,
      userIdInput: '',
      currentUserId: null,
      userProfile: null,
      windows: [],
      loaded: false,
      loading: false,
      saving: false,
      error: null,
      success: null,
      days: ['MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN']
    }
  },
  methods: {
    async loadUser() {
      if (!this.userIdInput) return
      this.loading = true
      this.error = null
      this.success = null
      this.userProfile = null
      this.loaded = false

      const userId = this.userIdInput.trim()

      try {
        // Fetch profile and availability in parallel
        const [profile, windows] = await Promise.all([
          accountApi.getUser(userId).catch(() => null),
          schedulingApi.getUserAvailability(userId)
        ])

        this.userProfile = profile
        this.windows = windows.map(w => ({
          windowId: w.windowId,
          dayOfWeek: w.dayOfWeek,
          timeRangeFrom: w.timeRangeFrom,
          timeRangeTo: w.timeRangeTo
        }))
        this.currentUserId = userId
        this.loaded = true
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.loading = false
      }
    },

    loadMyself() {
      if (this.authState.accountId) {
        this.userIdInput = this.authState.accountId
        this.loadUser()
      }
    },

    addWindow() {
      this.windows.push({
        dayOfWeek: 'MON',
        timeRangeFrom: '',
        timeRangeTo: ''
      })
    },

    removeWindow(idx) {
      this.windows.splice(idx, 1)
    },

    async saveAvailability() {
      // Basic client-side validation
      for (const [idx, w] of this.windows.entries()) {
        if (!w.timeRangeFrom || !w.timeRangeTo) {
          this.error = `Window ${idx + 1}: both 'from' and 'to' are required.`
          return
        }
      }

      this.saving = true
      this.error = null
      this.success = null

      const body = {
        windows: this.windows.map(w => ({
          timeRangeFrom: w.timeRangeFrom,
          timeRangeTo: w.timeRangeTo,
          dayOfWeek: w.dayOfWeek
        }))
      }

      try {
        const saved = await schedulingApi.updateUserAvailability(this.currentUserId, body)
        // Re-sync from server response (so windowIds are populated)
        this.windows = saved.map(w => ({
          windowId: w.windowId,
          dayOfWeek: w.dayOfWeek,
          timeRangeFrom: w.timeRangeFrom,
          timeRangeTo: w.timeRangeTo
        }))
        this.success = `Schedule saved (${saved.length} window(s)).`
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.availability {
  text-align: left;
  max-width: 700px;
  margin: 0 auto;
}
h2 { color: #2c3e50; }
section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}
.user-picker { background: #ecf3ee; border-left: 4px solid #42b983; }
.picker-row {
  display: flex;
  gap: 10px;
}
.picker-row input { flex: 1; }
.user-info {
  margin: 12px 0 0;
  color: #2c3e50;
  font-weight: bold;
}
h3 {
  margin-top: 0;
  color: #2c3e50;
}
.hint {
  color: #777;
  font-size: 0.9em;
  margin: 0 0 15px;
}
.empty-inline {
  background: #fff;
  padding: 15px;
  border-radius: 6px;
  color: #999;
  text-align: center;
  margin-bottom: 12px;
}
.window {
  background: white;
  border-radius: 8px;
  padding: 12px 15px;
  margin-bottom: 10px;
  border-left: 3px solid #3498db;
}
.window-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.window-number {
  font-weight: bold;
  color: #2c3e50;
}
.btn-remove {
  background: transparent;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  padding: 3px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8em;
  margin: 0;
}
.window-fields {
  display: grid;
  grid-template-columns: 100px 1fr 1fr;
  gap: 10px;
  align-items: end;
}
label {
  display: block;
  margin: 0 0 4px;
  font-size: 0.75em;
  font-weight: bold;
  text-transform: uppercase;
  color: #555;
}
input, select {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #d5dde0;
  border-radius: 6px;
  font-size: 0.95em;
}
.save-row {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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
.msg {
  padding: 10px 15px;
  margin-top: 15px;
  border-radius: 6px;
}
.error { background: #fadbd8; color: #c0392b; }
.success { background: #d4efdf; color: #196f3d; }

.btn-mini {
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  font-size: 0.9em;
  cursor: pointer;
  font-weight: bold;
  margin: 0;
}
.btn-mini:disabled { background: #aaa; cursor: not-allowed; }
</style>