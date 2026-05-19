<template>
  <div class="form">
    <h2>Create a New Booking</h2>

    <label>Sport Center ID</label>
    <input v-model="form.sportCenterId" placeholder="sc-100" />

    <label>Activity ID</label>
    <input v-model="form.activityId" placeholder="act-500" />

    <label>Time Slot ID (reserved synchronously via Scheduling Service)</label>
    <input v-model="form.timeSlotId" placeholder="ts-900" />

    <fieldset>
      <legend>Payment (optional)</legend>

      <label>Amount</label>
      <input v-model.number="form.payment.amount" type="number" min="0" step="0.01" placeholder="60.00" />

      <label>Currency</label>
      <input v-model="form.payment.currency" type="text" maxlength="3" placeholder="EUR" />

      <label>Payment method</label>
      <select v-model="form.payment.paymentMethod">
        <option value="">(none)</option>
        <option value="CARD">CARD</option>
        <option value="PAYPAL">PAYPAL</option>
        <option value="CASH">CASH</option>
      </select>
    </fieldset>

    <button @click="submit" :disabled="loading">
      {{ loading ? 'Creating…' : 'Create Booking' }}
    </button>

    <div v-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
    </div>

    <div v-if="success" class="success">
      <p>✓ Booking created successfully! Redirecting…</p>
    </div>
  </div>
</template>

<script>
import { bookingApi } from '../../api/client'

export default {
  name: 'CreateBooking',
  data() {
    return {
      form: {
        sportCenterId: '',
        activityId: '',
        timeSlotId: '',
        payment: {
          amount: null,
          currency: 'EUR',
          paymentMethod: 'CARD'
        }
      },
      loading: false,
      error: null,
      success: false
    }
  },
  methods: {
    submit() {
      this.error = null
      this.success = false
      this.loading = true

      const p = this.form.payment
      const hasPayment = p.amount || p.currency || p.paymentMethod
      const body = {
        sportCenterId: this.form.sportCenterId,
        activityId: this.form.activityId,
        timeSlotId: this.form.timeSlotId
      }
      if (hasPayment) body.payment = { ...p }

      bookingApi.create(body)
        .then(created => {
          this.success = true
          const id = created && created.bookingId ? created.bookingId : null
          setTimeout(() => {
            this.$router.push(id ? '/bookings/' + id : '/bookings')
          }, 1200)
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
</style>
