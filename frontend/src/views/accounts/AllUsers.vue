<template>
  <div>
    <h2>Users</h2>
    <p v-if="loading">Loading...</p>
    <p v-else-if="error" style="color:red">{{ error }}</p>
    <table v-else>
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          <th>Role</th>
          <th>Status</th>
          <th>City</th>
          <th>Skill Level</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.userId">
          <td>{{ user.name }}</td>
          <td>{{ user.email }}</td>
          <td>{{ user.role }}</td>
          <td>{{ user.status }}</td>
          <td>{{ user.city || '-' }}</td>
          <td>{{ user.skillLevel || '-' }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
export default {
  name: 'AllUsers',
  data() {
    return {
      users: [],
      loading: true,
      error: null
    }
  },
  async created() {
    try {
      const res = await fetch('http://localhost:8081/api/users')
      if (!res.ok) throw new Error(`HTTP ${res.status}`)
      this.users = await res.json()
    } catch (e) {
      this.error = 'Failed to load users: ' + e.message
    } finally {
      this.loading = false
    }
  }
}
</script>

<style scoped>
table {
  width: 100%;
  border-collapse: collapse;
}
th, td {
  border: 1px solid #ddd;
  padding: 10px;
  text-align: left;
}
th {
  background: #42b983;
  color: white;
}
tr:nth-child(even) {
  background: #f9f9f9;
}
</style>
