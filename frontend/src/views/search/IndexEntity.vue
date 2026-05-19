<template>
  <div class="form">
    <h2>Index an Entity</h2>
    <p class="muted">
      Upsert a SearchIndex entry via <code>POST /api/search/index</code>.
      Creates new; updates if <code>entityType + entityId</code> already exists.
    </p>

    <label>Entity Type</label>
    <select v-model="form.entityType">
      <option value="">Select…</option>
      <option value="USER">USER</option>
      <option value="ACTIVITY">ACTIVITY</option>
      <option value="FACILITY">FACILITY</option>
    </select>

    <label>Entity ID</label>
    <input v-model="form.entityId" placeholder="e.g. sc-100" />

    <label>Display Name</label>
    <input v-model="form.displayName" placeholder="e.g. Tartu Sport Hall" />

    <label>Keywords (comma-separated)</label>
    <input v-model="form.keywordsRaw" placeholder="e.g. basketball, tartu, indoor" />

    <fieldset>
      <legend>Filters</legend>

      <label>Sport type</label>
      <input v-model="form.filters.sportType" placeholder="e.g. BASKETBALL" />

      <label>Location</label>
      <input v-model="form.filters.location" placeholder="e.g. Tartu" />

      <label>Skill level</label>
      <input v-model="form.filters.skillLevel" placeholder="e.g. INTERMEDIATE" />

      <label>Facility name</label>
      <input v-model="form.filters.facilityName" placeholder="e.g. Tartu Sport Hall" />

      <label>User name</label>
      <input v-model="form.filters.userName" placeholder="e.g. Olena K." />

      <label>User rating score</label>
      <input v-model.number="form.filters.userRatingScore" type="number" step="0.1" min="0" max="5" placeholder="e.g. 4.6" />

      <label>User sport preferences</label>
      <input v-model="form.filters.userSportPreferences" placeholder="e.g. BASKETBALL,VOLLEYBALL" />

      <label>Activity title</label>
      <input v-model="form.filters.activityTitle" placeholder="e.g. Sunday Pickup Basketball" />

      <label>Activity status</label>
      <input v-model="form.filters.activityStatus" placeholder="e.g. OPEN" />

      <label>Activity max participants</label>
      <input v-model.number="form.filters.activityMaxParticipants" type="number" min="0" placeholder="e.g. 10" />

      <label>Activity time slots</label>
      <input v-model="form.filters.activityTimeSlots" placeholder="e.g. ts-300" />
    </fieldset>

    <button @click="submit" :disabled="loading || !canSubmit">
      {{ loading ? 'Indexing…' : 'Index Entity' }}
    </button>

    <div v-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
    </div>

    <div v-if="success" class="success">
      <p>✓ Index entry saved.</p>
      <pre>{{ JSON.stringify(result, null, 2) }}</pre>
    </div>
  </div>
</template>

<script>
import { searchApi } from '../../api/client'

export default {
  name: 'IndexEntity',
  data() {
    return {
      form: {
        entityType: '',
        entityId: '',
        displayName: '',
        keywordsRaw: '',
        filters: {
          sportType: '',
          location: '',
          skillLevel: '',
          facilityName: '',
          userName: '',
          userRatingScore: null,
          userSportPreferences: '',
          activityTitle: '',
          activityStatus: '',
          activityMaxParticipants: null,
          activityTimeSlots: ''
        }
      },
      loading: false,
      error: null,
      success: false,
      result: null
    }
  },
  computed: {
    canSubmit() {
      return this.form.entityType
        && this.form.entityId.trim()
        && this.form.displayName.trim()
    }
  },
  methods: {
    submit() {
      this.error = null
      this.success = false
      this.result = null
      this.loading = true

      const keywords = (this.form.keywordsRaw || '')
        .split(',')
        .map(s => s.trim())
        .filter(Boolean)

      const filters = {}
      for (const [k, v] of Object.entries(this.form.filters)) {
        if (v === null || v === undefined) continue
        const s = typeof v === 'string' ? v.trim() : v
        if (s === '') continue
        filters[k] = s
      }

      const body = {
        entityType: this.form.entityType,
        entityId: this.form.entityId.trim(),
        displayName: this.form.displayName.trim(),
        keywords,
        filters
      }

      searchApi.indexEntity(body)
        .then(data => {
          this.result = data
          this.success = true
        })
        .catch(err => {
          if (!err.silent) this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    }
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
.muted {
  color: #777;
  font-size: 0.9em;
  text-align: center;
  margin-bottom: 20px;
}
.muted code {
  background: #eaf3ff;
  padding: 1px 5px;
  border-radius: 3px;
  font-family: monospace;
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
}
input:focus, select:focus {
  outline: none;
  border-color: #42b983;
}
fieldset {
  border: 1px solid #d5dde0;
  border-radius: 6px;
  padding: 6px 18px 18px;
  margin-top: 25px;
}
legend {
  font-weight: bold;
  color: #2c3e50;
  font-size: 0.9em;
  padding: 0 6px;
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
.success pre {
  background: white;
  padding: 10px;
  border-radius: 4px;
  overflow-x: auto;
  font-size: 0.85em;
}
</style>
