<template>
  <div class="form">
    <h2>Create a New Activity</h2>

    <div v-if="!authState.token" class="error">
      <p>You must be <router-link to="/account/login">logged in</router-link> to create an activity.</p>
    </div>

    <template v-else>
      <div class="organizer-info">
        <small><b>Organizer:</b> {{ authState.email || authState.accountId }}</small>
      </div>

      <label>Title</label>
      <input v-model="form.title" placeholder="Saturday Football Match" />

      <label>Sport Type</label>
      <input v-model="form.sportType" placeholder="football" />

      <label>Max Participants</label>
      <input v-model.number="form.maxParticipants" type="number" min="1" />

      <label>Description (optional)</label>
      <input v-model="form.description" placeholder="Casual weekend game" />

      <label>Preferred Time Slot (optional — reserves it via Scheduling)</label>
      <select v-model="form.preferredTimeSlotId" :disabled="loadingSlots">
        <option value="">— No time slot —</option>
        <option v-for="s in availableSlots" :key="s.slotId" :value="s.slotId">
          {{ formatSlotLabel(s) }}
        </option>
      </select>
      <p v-if="loadingSlots" class="muted">Loading available slots…</p>
      <p v-else-if="availableSlots.length === 0" class="muted">
        No available slots yet — create one via the
        <router-link to="/slots/reserve">Scheduling page</router-link>.
      </p>

      <button @click="submit" :disabled="loading || !canSubmit">
        {{ loading ? 'Creating…' : 'Create Activity' }}
      </button>

      <div v-if="error" class="error">
        <p><b>Error:</b> {{ error }}</p>
      </div>

      <div v-if="success" class="success">
        <p>✓ Activity created successfully! Redirecting…</p>
      </div>
    </template>
  </div>
</template>

<script>
import { activityApi, schedulingApi } from '../../api/client'
import { authState } from '../../store/authStore'

export default {
  name: 'CreateActivity',
  data() {
    return {
      authState,
      form: {
        title: '',
        sportType: '',
        maxParticipants: 10,
        description: '',
        preferredTimeSlotId: ''
      },
      availableSlots: [],
      loadingSlots: false,
      loading: false,
      error: null,
      success: false
    }
  },
  computed: {
    canSubmit() {
      return this.form.title && this.form.sportType && this.form.maxParticipants > 0
    }
  },
  methods: {
    async fetchSlots() {
      this.loadingSlots = true
      try {
        this.availableSlots = await schedulingApi.listAvailable()
      } catch (err) {
        // No fatal — user can still create activity without a slot
        this.availableSlots = []
      } finally {
        this.loadingSlots = false
      }
    },

    formatSlotLabel(slot) {
      const start = new Date(slot.startTime).toLocaleString('en-GB', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
      })
      const end = new Date(slot.endTime).toLocaleString('en-GB', {
        hour: '2-digit', minute: '2-digit'
      })
      return `${start} → ${end}`
    },

    async submit() {
      this.error = null
      this.success = false
      this.loading = true

      const body = {
        organizerId: this.authState.accountId,
        title: this.form.title,
        sportType: this.form.sportType,
        maxParticipants: this.form.maxParticipants,
        description: this.form.description || null
      }

      if (this.form.preferredTimeSlotId) {
        body.preferredTimeSlotId = this.form.preferredTimeSlotId
      }

      try {
        await activityApi.create(body)
        this.success = true
        setTimeout(() => this.$router.push('/activities'), 1200)
      } catch (err) {
        if (!err.silent) this.error = err.message
      } finally {
        this.loading = false
      }
    }
  },
  mounted() {
    if (this.authState.token) this.fetchSlots()
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
.organizer-info {
  background: #eaf3fa;
  color: #1f618d;
  padding: 8px 12px;
  border-radius: 6px;
  margin-bottom: 10px;
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
  border-color: #42b983;
}
.muted {
  color: #999;
  font-size: 0.85em;
  margin: 6px 0 0;
  font-style: italic;
}
button {
  background: #42b983;
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