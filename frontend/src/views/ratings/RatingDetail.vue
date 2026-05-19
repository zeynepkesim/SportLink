<template>
  <div class="rating-detail">
    <router-link :to="backLink" class="back-link">&larr; Back to ratings</router-link>

    <div v-if="loading" class="status">Loading rating details…</div>
    <div v-else-if="error" class="status error">
      <p><b>Error:</b> {{ error }}</p>
      <button @click="fetchRating" class="retry-btn">Retry</button>
    </div>

    <div v-else-if="rating" class="detail-card">
      <div class="header">
        <h2>Rating Details</h2>
        <span class="timestamp">{{ formatDate(rating.createdAt) }}</span>
      </div>

      <div class="id-section">
        <code>ID: {{ rating.ratingId }}</code>
      </div>

      <div class="info-grid">
        <div class="info-group">
          <label>Reviewer</label>
          <div class="val">{{ rating.reviewerName || 'Unknown User' }}</div>
          <small class="uuid">{{ rating.reviewerId }}</small>
        </div>

        <div class="info-group">
          <label>Reviewee</label>
          <div class="val">{{ rating.revieweeName || 'Unknown User' }}</div>
          <small class="uuid">{{ rating.revieweeId }}</small>
        </div>

        <div class="info-group wide">
          <label>Activity ID</label>
          <div class="val">{{ rating.activityId }}</div>
        </div>

        <div class="score-card behavior">
          <label>Behavior</label>
          <div class="score">
            <span class="stars">{{ '★'.repeat(rating.behaviorValue) }}{{ '☆'.repeat(5 - rating.behaviorValue) }}</span>
            <span class="num">{{ rating.behaviorValue }} / 5</span>
          </div>
          <div class="label-badge">{{ rating.behaviorLabel }}</div>
        </div>

        <div class="score-card skill">
          <label>Skill</label>
          <div class="score">
            <span class="stars">{{ '★'.repeat(rating.skillValue) }}{{ '☆'.repeat(5 - rating.skillValue) }}</span>
            <span class="num">{{ rating.skillValue }} / 5</span>
          </div>
          <div class="label-badge">{{ rating.skillLabel }}</div>
        </div>
      </div>
      
      <div v-if="isModerator" class="admin-actions">
        <hr />
        <div class="action-buttons">
          <button @click="recomputeReputation" class="recompute-btn" :disabled="recomputing">
            {{ recomputing ? 'Recomputing…' : 'Recompute Reviewee Reputation' }}
          </button>
          <button @click="confirmDelete" class="delete-btn" :disabled="deleting">
            {{ deleting ? 'Deleting…' : 'Delete Rating' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { authState } from '../../store/authStore'
import { toast } from '../../store/toastStore'
import { ratingApi } from '../../api/client'

export default {
  name: 'RatingDetail',
  data() {
    return {
      ratingId: this.$route.params.ratingId,
      rating: null,
      loading: false,
      deleting: false,
      recomputing: false,
      error: null
    }
  },
  computed: {
    isModerator() {
      return authState.accountType === 'MODERATOR'
    },
    backLink() {
      return this.isModerator ? '/admin/ratings' : '/ratings'
    }
  },
  methods: {
    async fetchRating() {
      this.loading = true
      this.error = null
      try {
        this.rating = await ratingApi.getRating(this.ratingId)
      } catch (err) {
        this.error = err.message
        console.error(err)
      } finally {
        this.loading = false
      }
    },
    async recomputeReputation() {
      if (!this.rating) return
      this.recomputing = true
      try {
        await ratingApi.recomputeReputation(this.rating.revieweeId)
        toast.success('Reviewee reputation recomputed.')
      } catch (err) {
        toast.error('Recompute failed: ' + err.message)
      } finally {
        this.recomputing = false
      }
    },
    confirmDelete() {
      if (!confirm('Are you sure you want to permanently delete this rating?')) return
      this.deleteRating()
    },
    async deleteRating() {
      this.deleting = true
      try {
        await ratingApi.deleteRating(this.ratingId)
        toast.success('Rating deleted.')
        this.$router.push(this.backLink)
      } catch (err) {
        toast.error('Delete failed: ' + err.message)
      } finally {
        this.deleting = false
      }
    },
    formatDate(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleString()
    }
  },
  mounted() {
    this.fetchRating()
  }
}
</script>

<style scoped>
.rating-detail {
  max-width: 640px;
  margin: 0 auto;
  text-align: left;
}

.back-link {
  display: inline-block;
  margin-bottom: 20px;
  color: #42b983;
  text-decoration: none;
  font-weight: 500;
}

.status {
  padding: 40px;
  background: #f8f9fa;
  border-radius: 12px;
  text-align: center;
}

.status.error {
  background: #fef1f0;
  color: #e74c3c;
}

.detail-card {
  background: white;
  padding: 32px;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  border: 1px solid #eee;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

h2 {
  margin: 0;
  color: #2c3e50;
}

.timestamp {
  color: #7f8c8d;
  font-size: 0.9em;
}

.id-section {
  margin-bottom: 24px;
}

code {
  background: #f1f3f5;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  color: #495057;
}

.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.info-group.wide {
  grid-column: span 2;
}

label {
  display: block;
  font-size: 0.75em;
  font-weight: bold;
  text-transform: uppercase;
  color: #95a5a6;
  letter-spacing: 0.5px;
  margin-bottom: 4px;
}

.val {
  font-size: 1.1em;
  font-weight: 500;
  color: #2c3e50;
}

.uuid {
  display: block;
  font-size: 0.8em;
  color: #bdc3c7;
  word-break: break-all;
}

.score-card {
  padding: 16px;
  border-radius: 12px;
  background: #f8f9fa;
}

.score-card.behavior { border-left: 4px solid #3498db; }
.score-card.skill { border-left: 4px solid #27ae60; }

.score {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}

.stars {
  font-size: 1.4em;
  color: #f1c40f;
  letter-spacing: 2px;
}

.num {
  font-weight: bold;
  color: #7f8c8d;
}

.label-badge {
  display: inline-block;
  padding: 4px 10px;
  background: white;
  border-radius: 20px;
  font-size: 0.8em;
  font-weight: bold;
  color: #34495e;
  border: 1px solid #ddd;
}

.admin-actions {
  margin-top: 32px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

hr {
  border: none;
  border-top: 1px solid #eee;
  margin: 24px 0;
}

.recompute-btn {
  background: #3498db;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
}

.recompute-btn:hover:not(:disabled) {
  background: #2980b9;
}

.delete-btn {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 12px 20px;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
}

.delete-btn:hover:not(:disabled) {
  background: #c0392b;
}

.delete-btn:disabled, .recompute-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.retry-btn {
  margin-top: 12px;
  background: #3498db;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}
</style>
