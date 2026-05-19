<template>
  <div class="search-page">
    <h2>Facility Search</h2>
    <p class="muted">
      Search indexed sport facilities via <code>/api/search/facilities</code>.
    </p>

    <form class="filters" @submit.prevent="searchFacilities">
      <div class="row">
        <label>
          Facility name
          <input v-model="filters.facilityName" type="text" placeholder="e.g. Tartu" />
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
          Sport Center ID
          <input v-model="filters.sportCenterId" type="text" placeholder="e.g. sc-100" />
          <small class="hint">triggers Booking Service call</small>
        </label>
      </div>
      <div class="actions">
        <button type="submit" :disabled="loading">
          {{ loading ? 'Searching…' : 'Search' }}
        </button>
        <button type="button" class="secondary" @click="clearFilters">Clear</button>
      </div>
    </form>

    <div v-if="bookingCallTriggered" class="integration-note">
      <b>Cross-service call active.</b>
      The Search Service is calling
      <code>GET /api/bookings?sportCenterId={{ filters.sportCenterId }}</code>
      on the Booking Service for this request.
    </div>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="searchFacilities">Retry</button>
    </div>

    <div v-else-if="facilities.length === 0" class="empty">
      <p>No facilities match the filters.</p>
    </div>

    <div v-else>
      <p class="count">Found <b>{{ facilities.length }}</b> facility(ies)</p>
      <div class="item" v-for="f in facilities" :key="f.indexId || f.entityId">
        <h3>{{ f.displayName || f.facilityName || '(no name)' }}</h3>
        <div class="meta">
          <span class="badge">FACILITY</span>
          <span v-if="f.sportType" class="sport">🏀 {{ f.sportType }}</span>
          <span v-if="f.location" class="sport">📍 {{ f.location }}</span>
        </div>
        <p v-if="f.keywords && f.keywords.length" class="keywords-line">
          <b>Keywords:</b>
          <span v-for="kw in f.keywords" :key="kw" class="keyword">{{ kw }}</span>
        </p>
        <p class="ids-line">
          <b>Entity ID:</b> {{ f.entityId || '—' }}
          &middot; <b>Index ID:</b> {{ f.indexId || '—' }}
          <span v-if="f.lastIndexedAt">
            &middot; <b>Indexed:</b> {{ formatTime(f.lastIndexedAt) }}
          </span>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { searchApi } from '../../api/client'

export default {
  name: 'FacilitySearch',
  data() {
    return {
      filters: {
        facilityName: '',
        sportType: '',
        location: '',
        sportCenterId: ''
      },
      facilities: [],
      loading: false,
      error: null,
      bookingCallTriggered: false
    }
  },
  methods: {
    searchFacilities() {
      this.loading = true
      this.error = null
      this.bookingCallTriggered = Boolean(
        this.filters.sportCenterId && this.filters.sportCenterId.trim()
      )
      searchApi.facilities(this.filters)
        .then(data => {
          this.facilities = Array.isArray(data) ? data : []
        })
        .catch(err => {
          if (!err.silent) this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    },
    clearFilters() {
      this.filters.facilityName = ''
      this.filters.sportType = ''
      this.filters.location = ''
      this.filters.sportCenterId = ''
      this.error = null
      this.bookingCallTriggered = false
      this.searchFacilities()
    },
    formatTime(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleString('en-GB', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
      })
    }
  },
  mounted() {
    this.searchFacilities()
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
.row input {
  margin-top: 4px;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95em;
}
.hint {
  color: #d68910;
  font-size: 0.8em;
  margin-top: 3px;
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
.integration-note {
  background: #fff7e6;
  border-left: 4px solid #f39c12;
  padding: 10px 14px;
  border-radius: 0 8px 8px 0;
  margin-bottom: 14px;
  color: #7a4a00;
  font-size: 0.9em;
}
.integration-note code {
  background: #fdebc8;
  padding: 1px 5px;
  border-radius: 3px;
  font-family: monospace;
}
.count { text-align: center; color: #555; }
.item {
  background: #eaf3ff;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #3498db;
}
.item h3 { margin: 0 0 8px; color: #2c3e50; }
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.badge {
  background: #3498db;
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}
.sport { font-size: 0.95em; }
.keywords-line, .ids-line {
  color: #555;
  margin: 4px 0 0;
  font-size: 0.9em;
}
.keyword {
  display: inline-block;
  background: #d6e9ff;
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
