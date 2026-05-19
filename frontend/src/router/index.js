import { createRouter, createWebHistory } from 'vue-router'

import AllActivities from '../views/activities/AllActivities.vue'
import CreateActivity from '../views/activities/CreateActivity.vue'
import AvailableSlots from '../views/scheduling/AvailableSlots.vue'
import ReserveSlot from '../views/scheduling/ReserveSlot.vue'
import AccountLogin from '../views/account/Login.vue'
import AccountRegister from '../views/account/Register.vue'
import UserProfile from '../views/account/UserProfile.vue'
import UpdateUserStatus from '../views/account/UpdateUserStatus.vue'
import SportCenter from '../views/account/SportCenter.vue'
import ModerationCases from '../views/moderation/Cases.vue'
import CreateCase from '../views/moderation/CreateCase.vue'
import CaseDetail from '../views/moderation/CaseDetail.vue'
import ApplySanction from '../views/moderation/ApplySanction.vue'
import SanctionHistory from '../views/moderation/SanctionHistory.vue'
import UserRatings from '../views/ratings/UserRatings.vue'
import FacilitySearch from '../views/search/FacilitySearch.vue'

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
    path: '/account/login',
    name: 'AccountLogin',
    component: AccountLogin
  },
  {
    path: '/account/register',
    name: 'AccountRegister',
    component: AccountRegister
  },
  {
    path: '/account/profile',
    name: 'UserProfile',
    component: UserProfile
  },
  {
    path: '/account/status',
    name: 'UpdateUserStatus',
    component: UpdateUserStatus
  },
  {
    path: '/account/sport-center',
    name: 'SportCenter',
    component: SportCenter
  },
  {
    path: '/moderation/cases',
    name: 'ModerationCases',
    component: ModerationCases
  },
  {
    path: '/moderation/cases/new',
    name: 'CreateCase',
    component: CreateCase
  },
  {
    path: '/moderation/cases/:id',
    name: 'CaseDetail',
    component: CaseDetail
  },
  {
    path: '/moderation/sanctions/new',
    name: 'ApplySanction',
    component: ApplySanction
  },
  {
    path: '/moderation/sanctions/history',
    name: 'SanctionHistory',
    component: SanctionHistory
  },
  {
    path: '/ratings',
    name: 'UserRatings',
    component: UserRatings
  },
  {
    path: '/search/facilities',
    name: 'FacilitySearch',
    component: FacilitySearch
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