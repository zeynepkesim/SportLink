<template>
  <div class="form">
    <h2>Reserve a Time Slot</h2>

    <p class="hint">
      Reserve a custom time window for one of your activities. Usually slots
      are reserved automatically when you create an activity — this page is
      for additional reservations or testing.
    </p>

    <div v-if="!authState.token" class="error">
      <p>You must be <router-link to="/account/login">logged in</router-link> to reserve a slot.</p>
    </div>

    <template v-else>
      <div class="organizer-info">
        <small><b>Organizer:</b> {{ authState.email || authState.accountId }}</small>
      </div>

      <label>Start Time (ISO format)</label>
      <input v-model="form.startTime" placeholder="2026-05-20T14:00:00" />

      <label>End Time (ISO format)</label>
      <input v-model="form.endTime" placeholder="2026-05-20T15:30:00" />

      <label>Reserved For (one of your activities)</label>
      <select v-model="form.reservedForId" :disabled="loadingActivities">
        <option value="">— Select an activity —</option>
        <option v-for="a in myActivities" :key="a.activityId" :value="a.activityId">
          {{ a.title }} ({{ a.sportType }}, {{ a.status }})
        </option>
      </select>
      <p v-if="loadingActivities" class="muted">Loading your activities…</p>
      <p v-else-if="myActivities.length === 0" class="muted">
        You don't have any activities yet —
        <router-link to="/activities/new">create one</router-link> first.
      </p>

      <button @click="submit" :disabled="loading || !canSubmit">
        {{ loading ? 'Reserving…' : 'Reserve Slot' }}
      </button>

      <div v-if="error" class="error">
        <p><b>Error:</b> {{ error }}</p>
      </div>

      <div v-if="result" class="success">
        <p>✓ Slot reserved!</p>
        <p><small><b>Slot ID:</b> {{ result.slotId }}</small></p>
      </div>
    </template>
  </div>
</template>

<script>
import { schedulingApi, activityApi } from '../../api/client'
import { authState } from '../../store/authStore'

export default {
  name: 'ReserveSlot',
  data() {
    return {
      authState,
      form: {
        startTime: '',
        endTime: '',
        reservedForId: ''
      },
      myActivities: [],
      loadingActivities: false,
      loading: false,
      error: null,
      result: null
    }
  },
  computed: {
    canSubmit() {
      return this.form.startTime && this.form.endTime && this.form.reservedForId
    }
  },
  methods: {
    async fetchMyActivities() {
      this.loadingActivities = true
      try {
        const all = await activityApi.list()
        // Filter to activities where the logged-in user is the organizer
        // and the activity is still active (PLANNED or ACTIVE)
        this.myActivities = all.filter(a =>
          a.organizerId === this.authState.accountId &&
          (a.status === 'PLANNED' || a.status === 'ACTIVE')
        )
      } catch (err) {
        this.myActivities = []
      } finally {
        this.loadingActivities = false
      }
    },

    async submit() {
      this.error = null
      this.result = null
      this.loading = true

      const body = {
        startTime: this.form.startTime,
        endTime: this.form.endTime,
        organizerId: this.authState.accountId,
        reservedForId: this.form.reservedForId
      }

      try {
        this.result = await schedulingApi.reserve(body)
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.loading = false
      }
    }
  },
  mounted() {
    if (this.authState.token) this.fetchMyActivities()
  }
}
</script>

<style scoped>
.form {
  max-width: 500px;
  margin: 0 auto;
  background: #fff;
  padding: 30px;
  border-radius: 10px;
  text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 {
  color: #2c3e50;
  text-align: center;
}
.hint {
  background: #fff7e6;
  color: #7a5a00;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 0.85em;
  border-left: 3px solid #f39c12;
}
.organizer-info {
  background: #eaf3fa;
  color: #1f618d;
  padding: 8px 12px;
  border-radius: 6px;
  margin-top: 10px;
}
label {
  display: block;
  margin: 18px 0 6px;
  font-weight: bold;
  font-size: 0.85em;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
input, select {
  display: block;
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
  border: 1px solid #d5dde0;
  border-radius: 6px;
  font-size: 0.95em;
  background: white;
}
input:focus, select:focus {
  outline: none;
  border-color: #f39c12;
}
.muted {
  color: #999;
  font-size: 0.85em;
  margin: 6px 0 0;
  font-style: italic;
}
button {
  background: #f39c12;
  color: white;
  border: none;
  padding: 12px 24px;
  margin-top: 25px;
  border-radius: 6px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
}
button:disabled {
  background: #aaa;
  cursor: not-allowed;
}
.error {
  background: #fadbd8;
  color: #c0392b;
  padding: 10px 15px;
  margin-top: 20px;
  border-radius: 6px;
}
.success {
  background: #d4efdf;
  color: #196f3d;
  padding: 10px 15px;
  margin-top: 20px;
  border-radius: 6px;
}
</style>