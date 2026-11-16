import { createRouter, createWebHashHistory } from 'vue-router'
import { auth } from './auth'

const routes = [
  { path: '/login', component: () => import('./views/LoginView.vue'), meta: { public: true } },
  {
    path: '/hitss',
    component: () => import('./components/AppShell.vue'),
    meta: { role: 'HITSS_MANAGER' },
    children: [
      { path: '', component: () => import('./views/hitss/DashboardView.vue') },
      { path: 'devs', component: () => import('./views/hitss/DevelopersView.vue') },
      { path: 'devs/:id', component: () => import('./views/hitss/DeveloperDetailView.vue') },
      { path: 'alocacoes', component: () => import('./views/hitss/AllocationsView.vue') },
      { path: 'gestores', component: () => import('./views/hitss/ClientManagersView.vue') },
      { path: 'feedbacks', component: () => import('./views/hitss/FeedbacksView.vue') }
    ]
  },
  {
    path: '/cliente',
    component: () => import('./components/AppShell.vue'),
    meta: { role: 'CLIENT_MANAGER' },
    children: [
      { path: '', component: () => import('./views/client/TeamView.vue') },
      { path: 'feedback/novo/:allocationId', component: () => import('./views/client/FeedbackFormView.vue') },
      { path: 'feedback/:id/editar', component: () => import('./views/client/FeedbackFormView.vue') },
      { path: 'historico', component: () => import('./views/client/HistoryView.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: () => auth.home }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach((to) => {
  if (to.meta.public) {
    return auth.user && to.path === '/login' ? auth.home : true
  }
  if (!auth.token || !auth.user) return '/login'
  const required = to.matched.map((r) => r.meta.role).find(Boolean)
  if (required && required !== auth.user.role) return auth.home
  return true
})

export default router
