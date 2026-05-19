<template>
  <div id="app">
    <header>
      <h1>SportLink</h1>
      <nav>
        <router-link to="/activities">Activities</router-link> |
        <router-link to="/activities/new">Create Activity</router-link> |
        <router-link to="/slots">Available Slots</router-link> |
        <router-link to="/slots/reserve">Reserve a Slot</router-link> |
        <router-link to="/slots/availability">User Availability</router-link> |
        <router-link to="/slots/resolve">Find Common Time</router-link> |
        <router-link to="/bookings">Bookings</router-link> |
        <router-link to="/bookings/new">New Booking</router-link> |
        <router-link to="/bookings/facilities">Find Facility (Booking)</router-link> |
        <router-link to="/account/login">Login</router-link> |
        <router-link to="/moderation/cases">Cases</router-link> |
        <router-link to="/moderation/sanctions/new">Apply Sanction</router-link> |
        <router-link to="/ratings">User Ratings</router-link> |
        <router-link to="/admin/ratings">Admin Ratings</router-link> |
        <router-link to="/matching/users">User Suggestions</router-link> |
        <router-link to="/matching/activities">Activity Suggestions</router-link> |
        <router-link to="/search/facilities">Facility Search</router-link>
        <router-link to="/search">All</router-link> |
        <router-link to="/search/users">Users</router-link> |
        <router-link to="/search/activities">Activities</router-link> |
        <router-link to="/search/index">Index Maintenance</router-link>
      </nav>
    </header>

    <main>
      <router-view />
    </main>
  </div>
</template>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  max-width: 960px;
  margin: 0 auto;
  padding: 20px;
}

header h1 {
  color: #42b983;
  margin-bottom: 10px;
}

nav {
  padding: 20px;
  background: #f4f4f4;
  border-radius: 8px;
  margin-bottom: 14px;
  line-height: 2;
}

nav.search-nav {
  background: #eaf3ff;
  border: 1px solid #d6e9ff;
  margin-bottom: 30px;
  padding: 12px 20px;
}

nav.search-nav .nav-label {
  font-weight: bold;
  color: #1f3a5f;
  margin-right: 8px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  margin: 0 5px;
}

nav a.router-link-exact-active {
  color: #42b983;
}

nav.search-nav a.router-link-exact-active {
  color: #1f3a5f;
  background: #d6e9ff;
  padding: 2px 8px;
  border-radius: 4px;
}
</style>
