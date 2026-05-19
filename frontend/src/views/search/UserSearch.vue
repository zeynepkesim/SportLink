<template>
  <div class="search-page">
    <h2>User Search</h2>
    <p class="muted">
      Search indexed user profiles via <code>/api/search/users</code>.
    </p>

    <form class="filters" @submit.prevent="search">
      <div class="row">
        <label>
          User name
          <input v-model="filters.userName" type="text" placeholder="e.g. Olena" />
        </label>
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Skill level
          <input v-model="filters.skillLevel" type="text" placeholder="e.g. INTERMEDIATE" />
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
        <label>
          User rating score
          <input v-model.number="filters.userRatingScore" type="number" step="0.1" min="0" max="5" placeholder="e.g. 4.7" />
        </label>
        <label>
          Sport preferences
          <input v-model="filters.userSportPreferences" type="text" placeholder="e.g. BASKETBALL,VOLLEYBALL" />
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

    <div v-else-if="users.length === 0 && hasSearched" class="empty">
      <p>No users match the filters.</p>
    </div>

    <div v-else-if="users.length > 0">
      <p class="count">Found <b>{{ users.length }}</b> user(s)</p>
      <div class="item" v-for="u in users" :key="u.indexId || u.entityId">
        <h3>{{ u.displayName || u.userName || '(no name)' }}</h3>
        <div class="meta">
          <span class="badge">USER</span>
          <span v-if="u.sportType" class="sport">🏀 {{ u.sportType }}</span>
          <span v-if="u.skillLevel" class="sport">🎯 {{ u.skillLevel }}</span>
          <span v-if="u.location" class="sport">📍 {{ u.location }}</span>
          <span v-if="u.userRatingScore" class="sport">⭐ {{ u.userRatingScore }}</span>
        </div>
        <p v-if="u.userSportPreferences" class="prefs-line">
          <b>Prefers:</b> {{ u.userSportPreferences }}
        </p>
        <p v-if="u.keywords && u.keywords.length" class="keywords-line">
          <b>Keywords:</b>
          <span v-for="kw in u.keywords" :key="kw" class="keyword">{{ kw }}</span>
        </p>
        <p class="ids-line">
          <b>Entity ID:</b> {{ u.entityId || '—' }}
          &middot; <b>Index ID:</b> {{ u.indexId || '—' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { searchApi } from '../../api/client'

export default {
  name: 'UserSearch',
  data() {
    return {
      filters: {
        userName: '',
        sportType: '',
        skillLevel: '',
        location: '',
        userRatingScore: null,
        userSportPreferences: ''
      },
      users: [],
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
      searchApi.users(this.filters)
        .then(data => {
          this.users = Array.isArray(data) ? data : []
        })
        .catch(err => {
          if (!err.silent) this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    },
    clearFilters() {
      this.filters.userName = ''
      this.filters.sportType = ''
      this.filters.skillLevel = ''
      this.filters.location = ''
      this.filters.userRatingScore = null
      this.filters.userSportPreferences = ''
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
.row input {
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
  background: #f4ecf7;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #8e44ad;
}
.item h3 { margin: 0 0 8px; color: #2c3e50; }
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.badge {
  background: #8e44ad;
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}
.sport { font-size: 0.95em; }
.prefs-line, .keywords-line, .ids-line {
  color: #555;
  margin: 4px 0 0;
  font-size: 0.9em;
}
.keyword {
  display: inline-block;
  background: #e8daef;
  color: #4a235a;
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
