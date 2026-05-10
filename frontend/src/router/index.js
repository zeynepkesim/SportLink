import { createRouter, createWebHistory } from 'vue-router'

import AllActivities from '../views/AllActivities.vue'
import CreateActivity from '../views/CreateActivity.vue'
import AvailableSlots from '../views/AvailableSlots.vue'
import ReserveSlot from '../views/ReserveSlot.vue'

const routes = [
  {
    path: '/',
    redirect: '/activities'
  },
  {
    path: '/activities',
    name: 'AllActivities',
    component: AllActivities
  },
  {
    path: '/activities/new',
    name: 'CreateActivity',
    component: CreateActivity
  },
  {
    path: '/slots',
    name: 'AvailableSlots',
    component: AvailableSlots
  },
  {
    path: '/slots/reserve',
    name: 'ReserveSlot',
    component: ReserveSlot
  },
  {
    // fallback for unknown routes — redirect to activities
    path: '/:catchAll(.*)',
    redirect: '/activities'
  }
]

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes
})

export default router