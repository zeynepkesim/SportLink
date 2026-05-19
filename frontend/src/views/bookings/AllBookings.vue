<template>
  <div class="bookings">
    <h2>All Bookings</h2>

    <form class="filters" @submit.prevent="fetchBookings">
      <div class="row">
        <label>
          Activity ID
          <input v-model="filters.activityId" type="text" placeholder="e.g. act-200" />
        </label>
        <label>
          Sport Center ID
          <input v-model="filters.sportCenterId" type="text" placeholder="e.g. sc-100" />
        </label>
        <label>
          Booking status
          <select v-model="filters.bookingStatus">
            <option value="">(any)</option>
            <option value="PENDING">PENDING</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </label>
      </div>
      <div class="actions">
        <button type="submit" :disabled="loading">
          {{ loading ? 'Loading…' : 'Search' }}
        </button>
        <button type="button" class="secondary" @click="clearFilters">Clear</button>
        <router-link class="primary-link" to="/bookings/new">+ New Booking</router-link>
      </div>
    </form>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchBookings">Retry</button>
    </div>

    <div v-else-if="bookings.length === 0" class="empty">
      <p>No bookings match the filters. <router-link to="/bookings/new">Create one</router-link>!</p>
    </div>

    <div v-else>
      <p class="count">Found <b>{{ bookings.length }}</b> booking(s)</p>
      <div class="item" v-for="b in bookings" :key="b.bookingId">
        <h3>
          <router-link :to="'/bookings/' + b.bookingId" class="title-link">
            Booking {{ b.bookingId }}
          </router-link>
        </h3>
        <div class="meta">
          <span class="badge" :class="'status-' + b.bookingStatus.toLowerCase()">
            {{ b.bookingStatus }}
          </span>
          <span class="sport">📍 {{ b.sportCenterId }}</span>
          <span class="sport">🏃 {{ b.activityId }}</span>
          <span class="sport">🕒 {{ b.timeSlotId }}</span>
        </div>
        <p v-if="b.payment" class="payment-line">
          <b>Payment:</b>
          <span class="badge-small" :class="'pay-' + (b.payment.paymentStatus || '').toLowerCase()">
            {{ b.payment.paymentStatus }}
          </span>
          <span v-if="b.payment.money">
            {{ b.payment.money.amount }} {{ b.payment.money.currency }}
          </span>
          <span v-if="b.payment.paymentMethod">
            via {{ b.payment.paymentMethod }}
          </span>
        </p>
        <p class="created-line" v-if="b.createdAt">
          <b>Created:</b> {{ formatDate(b.createdAt) }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { bookingApi } from '../../api/client'

export default {
  name: 'AllBookings',
  data() {
    return {
      filters: {
        activityId: '',
        sportCenterId: '',
        bookingStatus: ''
      },
      bookings: [],
      loading: false,
      error: null
    }
  },
  methods: {
    fetchBookings() {
      this.loading = true
      this.error = null
      bookingApi.list({
        activityId: this.filters.activityId,
        sportCenterId: this.filters.sportCenterId,
        bookingStatus: this.filters.bookingStatus
      })
        .then(data => {
          this.bookings = Array.isArray(data) ? data : []
        })
        .catch(err => {
          if (!err.silent) this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    clearFilters() {
      this.filters.activityId = ''
      this.filters.sportCenterId = ''
      this.filters.bookingStatus = ''
      this.fetchBookings()
    },
    formatDate(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleString('en-GB', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
      })
    }
  },
  mounted() {
    this.fetchBookings()
  }
}
</script>

<style scoped>
.bookings {
  text-align: left;
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
.actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
}
button:disabled { opacity: 0.5; cursor: not-allowed; }
button.secondary { background: white; color: #2c3e50; border: 1px solid #ccc; }
.primary-link {
  background: #2c3e50;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.95em;
}
.count {
  text-align: center;
  color: #555;
}
.item {
  background: #eaf3ff;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #3498db;
}
.item h3 {
  margin: 0 0 8px;
  color: #2c3e50;
}
.title-link {
  color: #2c3e50;
  text-decoration: none;
}
.title-link:hover {
  color: #3498db;
  text-decoration: underline;
}
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
}
.status-pending   { background: #f39c12; }
.status-confirmed { background: #27ae60; }
.status-cancelled { background: #e74c3c; }
.sport {
  font-size: 0.95em;
}
.payment-line, .created-line {
  color: #555;
  margin: 4px 0 0;
  font-size: 0.95em;
}
.badge-small {
  padding: 2px 7px;
  border-radius: 10px;
  font-size: 0.75em;
  font-weight: bold;
  color: white;
  margin: 0 6px;
}
.pay-pending  { background: #f39c12; }
.pay-paid     { background: #27ae60; }
.pay-refunded { background: #8e44ad; }
.loading, .empty, .error {
  text-align: center;
  padding: 30px;
}
.error {
  color: #c0392b;
}
</style>
