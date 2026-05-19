<template>
  <div class="search-page">
    <h2>Activity Search</h2>
    <p class="muted">
      Search indexed activities via <code>/api/search/activities</code>.
    </p>

    <form class="filters" @submit.prevent="search">
      <div class="row">
        <label>
          Activity title
          <input v-model="filters.activityTitle" type="text" placeholder="e.g. football" />
        </label>
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Activity status
          <select v-model="filters.activityStatus">
            <option value="">(any)</option>
            <option value="OPEN">OPEN</option>
            <option value="CLOSED">CLOSED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
        <label>
          Max participants
          <input v-model.number="filters.activityMaxParticipants" type="number" min="0" placeholder="e.g. 10" />
        </label>
        <label>
          Time slots
          <input v-model="filters.activityTimeSlots" type="text" placeholder="e.g. ts-300" />
        </label>
      </div>
      <div class="actions">
        <button type="submit" :disabled="loading">
          {{ loading ? 'Searching…' : 'Search' }}
        </button>
        <button type="button" class="secondary" @click="clearFilters">Clear</button>
      </div>
    </form>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="search">Retry</button>
    </div>

    <div v-else-if="activities.length === 0 && hasSearched" class="empty">
      <p>No activities match the filters.</p>
    </div>

    <div v-else-if="activities.length > 0">
      <p class="count">Found <b>{{ activities.length }}</b> activity(ies)</p>
      <div class="item" v-for="a in activities" :key="a.indexId || a.entityId">
        <h3>{{ a.displayName || a.activityTitle || '(no title)' }}</h3>
        <div class="meta">
          <span class="badge">ACTIVITY</span>
          <span v-if="a.sportType" class="sport">🏀 {{ a.sportType }}</span>
          <span
            v-if="a.activityStatus"
            class="status-tag"
            :class="'status-' + a.activityStatus.toLowerCase()">
            {{ a.activityStatus }}
          </span>
          <span v-if="a.location" class="sport">📍 {{ a.location }}</span>
          <span v-if="a.activityMaxParticipants" class="sport">👥 max {{ a.activityMaxParticipants }}</span>
        </div>
        <p v-if="a.activityTimeSlots" class="slots-line">
          <b>Time slots:</b> {{ a.activityTimeSlots }}
        </p>
        <p v-if="a.keywords && a.keywords.length" class="keywords-line">
          <b>Keywords:</b>
          <span v-for="kw in a.keywords" :key="kw" class="keyword">{{ kw }}</span>
        </p>
        <p class="ids-line">
          <b>Entity ID:</b> {{ a.entityId || '—' }}
          &middot; <b>Index ID:</b> {{ a.indexId || '—' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { searchApi } from '../../api/client'

export default {
  name: 'ActivitySearch',
  data() {
    return {
      filters: {
        activityTitle: '',
        sportType: '',
        activityStatus: '',
        location: '',
        activityMaxParticipants: null,
        activityTimeSlots: ''
      },
      activities: [],
      loading: false,
      error: null,
      hasSearched: false
    }
  },
  methods: {
    search() {
      this.loading = true
      this.error = null
      this.hasSearched = true
      searchApi.activities(this.filters)
        .then(data => {
          this.activities = Array.isArray(data) ? data : []
        })
        .catch(err => {
          if (!err.silent) this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    },
    clearFilters() {
      this.filters.activityTitle = ''
      this.filters.sportType = ''
      this.filters.activityStatus = ''
      this.filters.location = ''
      this.filters.activityMaxParticipants = null
      this.filters.activityTimeSlots = ''
      this.error = null
      this.search()
    }
  },
  mounted() {
    this.search()
  }
}
</script>

<style scoped>
.search-page { text-align: left; }
h2 { color: #2c3e50; }
.muted {
  color: #777;
  font-size: 0.9em;
  margin-top: -8px;
  margin-bottom: 20px;
}
.muted code {
  background: #eaf3ff;
  padding: 1px 5px;
  border-radius: 3px;
  font-family: monospace;
}
.filters {
  background: #f4f4f4;
  padding: 15px 20px;
  border-radius: 10px;
  margin-bottom: 16px;
}
.row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.row label {
  display: flex;
  flex-direction: column;
  font-size: 0.9em;
  color: #2c3e50;
}
.row input, .row select {
  margin-top: 4px;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95em;
}
.actions { display: flex; gap: 8px; }
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
}
button:disabled { opacity: 0.6; cursor: not-allowed; }
button.secondary { background: white; color: #2c3e50; border: 1px solid #ccc; }
.count { text-align: center; color: #555; }
.item {
  background: #ecf3ee;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #42b983;
}
.item h3 { margin: 0 0 8px; color: #2c3e50; }
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.badge {
  background: #42b983;
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}
.status-tag {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
}
.status-open      { background: #3498db; }
.status-closed    { background: #95a5a6; }
.status-cancelled { background: #e74c3c; }
.sport { font-size: 0.95em; }
.slots-line, .keywords-line, .ids-line {
  color: #555;
  margin: 4px 0 0;
  font-size: 0.9em;
}
.keyword {
  display: inline-block;
  background: #d4efdf;
  color: #1d6f42;
  padding: 2px 7px;
  border-radius: 3px;
  font-size: 0.85em;
  margin: 0 4px 0 4px;
}
.loading, .empty, .error {
  text-align: center;
  padding: 30px;
}
.error { color: #c0392b; }
</style>
