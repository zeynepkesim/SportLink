<template>
  <div class="search-page">
    <h2>Cross-Entity Search</h2>
    <p class="muted">
      Unified search across users, activities, and facilities via
      <code>/api/search</code>.
    </p>

    <form class="filters" @submit.prevent="search">
      <div class="row">
        <label>
          Query (q)
          <input v-model="filters.q" type="text" placeholder="e.g. basketball" />
        </label>
        <label>
          Entity type
          <select v-model="filters.entityType">
            <option value="">(any)</option>
            <option value="USER">USER</option>
            <option value="ACTIVITY">ACTIVITY</option>
            <option value="FACILITY">FACILITY</option>
          </select>
        </label>
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
        <label>
          Skill level
          <input v-model="filters.skillLevel" type="text" placeholder="e.g. INTERMEDIATE" />
        </label>
        <label>
          Activity status
          <input v-model="filters.activityStatus" type="text" placeholder="e.g. OPEN" />
        </label>
        <label>
          Min rating
          <input v-model.number="filters.minRating" type="number" step="0.1" min="0" max="5" placeholder="e.g. 4.5" />
        </label>
        <label>
          Facility name
          <input v-model="filters.facilityName" type="text" placeholder="e.g. Tartu" />
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

    <div v-else-if="results.length === 0 && hasSearched" class="empty">
      <p>No results match the filters.</p>
    </div>

    <div v-else-if="results.length > 0">
      <p class="count">Found <b>{{ results.length }}</b> result(s)</p>
      <div
        class="item"
        v-for="r in results"
        :key="r.indexId || r.entityId"
        :class="'type-' + (r.entityType || '').toLowerCase()">
        <h3>{{ r.displayName || '(no name)' }}</h3>
        <div class="meta">
          <span class="badge" :class="'badge-' + (r.entityType || '').toLowerCase()">
            {{ r.entityType }}
          </span>
          <span v-if="r.sportType" class="sport">🏀 {{ r.sportType }}</span>
          <span v-if="r.location" class="sport">📍 {{ r.location }}</span>
          <span v-if="r.skillLevel" class="sport">🎯 {{ r.skillLevel }}</span>
          <span v-if="r.activityStatus" class="sport">📌 {{ r.activityStatus }}</span>
          <span v-if="r.userRatingScore" class="sport">⭐ {{ r.userRatingScore }}</span>
        </div>
        <p v-if="r.keywords && r.keywords.length" class="keywords-line">
          <b>Keywords:</b>
          <span v-for="kw in r.keywords" :key="kw" class="keyword">{{ kw }}</span>
        </p>
        <p class="ids-line">
          <b>Entity ID:</b> {{ r.entityId || '—' }}
          &middot; <b>Index ID:</b> {{ r.indexId || '—' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { searchApi } from '../../api/client'

export default {
  name: 'CrossEntitySearch',
  data() {
    return {
      filters: {
        q: '',
        entityType: '',
        sportType: '',
        location: '',
        skillLevel: '',
        activityStatus: '',
        minRating: null,
        facilityName: ''
      },
      results: [],
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
      searchApi.all(this.filters)
        .then(data => {
          this.results = Array.isArray(data) ? data : []
        })
        .catch(err => {
          if (!err.silent) this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    },
    clearFilters() {
      this.filters.q = ''
      this.filters.entityType = ''
      this.filters.sportType = ''
      this.filters.location = ''
      this.filters.skillLevel = ''
      this.filters.activityStatus = ''
      this.filters.minRating = null
      this.filters.facilityName = ''
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
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  background: #eaf3ff;
  border-left: 4px solid #3498db;
}
.item.type-user {
  background: #f4ecf7;
  border-left-color: #8e44ad;
}
.item.type-activity {
  background: #ecf3ee;
  border-left-color: #42b983;
}
.item h3 { margin: 0 0 8px; color: #2c3e50; }
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
}
.badge-facility { background: #3498db; }
.badge-user     { background: #8e44ad; }
.badge-activity { background: #42b983; }
.sport { font-size: 0.95em; }
.keywords-line, .ids-line {
  color: #555;
  margin: 4px 0 0;
  font-size: 0.9em;
}
.keyword {
  display: inline-block;
  background: rgba(255, 255, 255, 0.6);
  color: #1f3a5f;
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
