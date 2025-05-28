import { defineRouter } from '#q-app/wrappers'
import { createRouter, createMemoryHistory, createWebHistory, createWebHashHistory } from 'vue-router'
import { useAuthStore } from 'src/stores/auth.js';
import routes from './routes'
import { Notify } from 'quasar';

export default defineRouter(function (/* { store, ssrContext } */) {
  const createHistory = process.env.SERVER
    ? createMemoryHistory
    : (process.env.VUE_ROUTER_MODE === 'history' ? createWebHistory : createWebHashHistory)

  const Router = createRouter({
    scrollBehavior: () => ({ left: 0, top: 0 }),
    routes,
    history: createHistory(process.env.VUE_ROUTER_BASE)
  })

  Router.beforeEach((to, from, next) => {
    const authStore = useAuthStore()

    if (to.matched.some(record => record.meta.requiresAuth)) {
      if (!authStore.isAuthenticated) {
        next('/login')
        return
      }

      const requiredRoles = to.matched.find(record => record.meta.requiredRoles)?.meta.requiredRoles

      if (requiredRoles && !requiredRoles.some(role => authStore.hasPermission(role))) {
        // Benutzer hat keine der erforderlichen Rollen
        Notify.create({
          type: 'negative',
          message: 'Sie haben keine Berechtigung, auf diese Seite zuzugreifen'
        })
        return
      }
      // Benutzer ist angemeldet und hat die erforderliche Rolle (falls vorhanden)
      next()
    } else {
      // Keine Authentifizierung erforderlich
      next()
    }
  })

  return Router
})
