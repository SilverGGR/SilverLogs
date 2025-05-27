const routes = [
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        component: () => import('pages/IndexPage.vue')
      },
      {
        path: '/profile',
        component: () => import('pages/ProfilePage.vue')
      },
      {
        path: '/report',
        component: () => import('pages/ReportPage.vue')
      },
      {
        path: '/manage-connections',
        component: () => import('pages/ManageConnectionsPage.vue'),
        meta: { requiredRole: 'ADMIN' }
      },
      {
        path: '/user',
        component: () => import('pages/UserManagementPage.vue'),
        meta: { requiredRole: 'ADMIN' }
      }
    ]
  },
  {
    path: '/login',
    component: () => import('pages/LoginPage.vue')
  },
  {
    path: '/register',
    component: () => import('pages/RegisterPage.vue')
  },

  // Always leave this as last one,
  // but you can also remove it
  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue')
  }
]

export default routes
