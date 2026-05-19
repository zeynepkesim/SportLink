<template>
  <div class="rating-detail">
    <button class="back-button" @click="goBack">← Back</button>

    <h2>Rating Detail</h2>

    <div v-if="loading" class="status">Loading rating…</div>
    <div v-else-if="error" class="status error">{{ error }}</div>

    <div v-else-if="rating" class="detail-card">
      <div class="detail-header">
        <div class="detail-id">
          <span class="label-muted">Rating ID</span>
          <span class="mono">{{ rating.ratingId }}</span>
        </div>
        <span class="created-at">{{ formatDate(rating.createdAt) }}</span>
      </div>

      <div class="detail-section">
        <div class="detail-row">
          <span class="field-label">Reviewer</span>
          <span>{{ rating.reviewerName || rating.reviewerId }}</span>
        </div>
        <div class="detail-row">
          <span class="field-label">Reviewee</span>
          <span>{{ rating.revieweeName || rating.revieweeId }}</span>
        </div>
        <div class="detail-row">
          <span class="field-label">Activity</span>
          <span class="mono">{{ rating.activityId }}</span>
        </div>
      </div>

      <div class="detail-section">
        <h3 class="section-title">Scores</h3>
        <div class="scores-grid">
          <div class="score-block">
            <span class="score-name">Behavior</span>
            <div class="stars">
              <span
                v-for="n in 5"
                :key="'b' + n"
                class="star"
                :class="{ filled: n <= rating.behaviorValue }"
              >★</span>
            </div>
            <span class="score-value">{{ rating.behaviorValue }} / 5</span>
            <span class="score-label">{{ rating.behaviorLabel }}</span>
          </div>

          <div class="score-block">
            <span class="score-name">Skill</span>
            <div class="stars">
              <span
                v-for="n in 5"
                :key="'s' + n"
                class="star"
                :class="{ filled: n <= rating.skillValue }"
              >★</span>
            </div>
            <span class="score-value">{{ rating.skillValue }} / 5</span>
            <span class="score-label">{{ rating.skillLabel }}</span>
          </div>
        </div>
      </div>

      <div v-if="rating.comment" class="detail-section">
        <h3 class="section-title">Comment</h3>
        <p class="comment-text">{{ rating.comment }}</p>
      </div>

      <div class="detail-actions">
        <button class="delete-button" @click="confirmDelete">Delete Rating</button>
      </div>
    </div>

    <div v-else class="status">
      <p>No rating found with ID <strong>{{ ratingId }}</strong>.</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RatingDetail',
  data() {
    return {
      ratingId: '',
      rating: null,
      loading: false,
      error: null
    }
  },
  watch: {
    '$route.params.ratingId': {
      immediate: true,
      handler(value) {
        this.ratingId = value || ''
        this.rating = null
        this.error = null
        if (this.ratingId) {
          this.fetchRating()
        } else {
          this.error = 'No rating ID provided.'
        }
      }
    }
  },
  methods: {
    fetchRating() {
      this.loading = true
      this.error = null

      fetch(`http://localhost:8082/api/ratings/${this.ratingId}`)
        .then(response => {
          if (response.status === 404) {
            throw new Error('Rating not found.')
          }
          if (!response.ok) {
            throw new Error('Failed to load rating (HTTP ' + response.status + ')')
          }
          return response.json()
        })
        .then(data => {
          this.rating = data
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    confirmDelete() {
      if (!window.confirm('Delete rating ' + this.ratingId + '? This cannot be undone.')) {
        return
      }
      this.deleteRating()
    },
    deleteRating() {
      this.loading = true
      this.error = null

      fetch(`http://localhost:8082/api/ratings/${this.ratingId}`, {
        method: 'DELETE'
      })
        .then(response => {
          if (!response.ok) {
            throw new Error('Failed to delete rating (HTTP ' + response.status + ')')
          }
          return response.text()
        })
        .then(() => {
          this.goBack()
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
          this.loading = false
        })
    },
    goBack() {
      if (window.history.length > 1) {
        this.$router.back()
      } else {
        this.$router.push('/ratings')
      }
    },
    formatDate(value) {
      if (!value) return ''
      return new Date(value).toLocaleString()
    }
  }
}
</script>

<style scoped>
.rating-detail {
  max-width: 700px;
  margin: 0 auto;
  text-align: left;
}

.back-button {
  background: none;
  border: none;
  color: #42b983;
  font-size: 0.95em;
  cursor: pointer;
  padding: 0;
  margin-bottom: 16px;
}

.back-button:hover {
  text-decoration: underline;
}

.status {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 10px;
  color: #333;
}

.status.error {
  background: #fdecea;
  color: #c0392b;
}

.detail-card {
  border: 1px solid #dde2e6;
  border-radius: 10px;
  padding: 24px;
  background: #fff;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.detail-id {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.label-muted {
  font-size: 0.8em;
  color: #999;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.mono {
  font-family: monospace;
  font-size: 0.9em;
  color: #444;
}

.created-at {
  font-size: 0.9em;
  color: #777;
  white-space: nowrap;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-section:last-of-type {
  border-bottom: none;
}

.section-title {
  font-size: 0.95em;
  font-weight: 600;
  color: #555;
  margin: 0 0 12px 0;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.detail-row {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  align-items: baseline;
}

.field-label {
  font-weight: 600;
  min-width: 90px;
  color: #444;
}

.scores-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.score-block {
  background: #f9fafb;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.score-name {
  font-weight: 600;
  font-size: 0.9em;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}

.stars {
  display: flex;
  gap: 2px;
}

.star {
  font-size: 1.3em;
  color: #ddd;
}

.star.filled {
  color: #f5a623;
}

.score-value {
  font-size: 1.1em;
  font-weight: 700;
  color: #333;
}

.score-label {
  font-size: 0.85em;
  color: #777;
}

.comment-text {
  color: #444;
  line-height: 1.6;
  margin: 0;
  background: #f9fafb;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid #eee;
}

.detail-actions {
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #eee;
}

.delete-button {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
}

.delete-button:hover {
  background: #c0392b;
}

@media (max-width: 500px) {
  .scores-grid {
    grid-template-columns: 1fr;
  }
  .detail-header {
    flex-direction: column;
  }
}
</style>