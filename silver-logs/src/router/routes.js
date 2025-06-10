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
        path: '/report-watch',
        component: () => import('pages/ReportWatchPage.vue'),
        meta: { requiredRoles: ['SUPERVISOR', 'ADMIN'] }
      },
      {
        path: '/manage-connections',
        component: () => import('pages/ManageConnectionsPage.vue'),
        meta: { requiredRoles: ['ADMIN'] }
      },
      {
        path: '/user',
        component: () => import('pages/UserManagementPage.vue'),
        meta: { requiredRoles: ['ADMIN'] }
      },
      {
        path: '/documents',
        component: () => import('pages/DokumentPage.vue'),
      },
      {
        path: '/documents-watch',
        component: () => import('pages/DokumentWatchPage.vue'),
        meta: { requiredRoles: ['SUPERVISOR', 'ADMIN'] }
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
