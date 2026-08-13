import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/home/index.vue'),
      children: [
        {
          path: '/',
          name: 'market',
          component: () => import('@/views/market/index.vue')
        },
        {
          path: '/detail/:id',
          name: 'detail',
          component: () => import('@/views/detail/index.vue')
        },
        {
          path: '/create-order',
          name: 'createOrder',
          component: () => import('@/views/create-order/index.vue')
        },
        {
          path: '/category/:id',
          name: 'category',
          component: () => import('@/views/category/index.vue')
        },
        {
          path: '/chat',
          name: 'chat',
          component: () => import('@/views/chat/index.vue')
        },
        {
          path: '/search',
          name: 'search',
          component: () => import('@/views/search/index.vue')
        },
        {
          path: '/payment',
          name: 'payment',
          component: () => import('@/views/payment/index.vue')
        },
        {
          path: '/personal-center',
          name: 'personalCenter',
          component: () => import('@/views/personal-center/index.vue'),
          children: [
            {
              path: '/personal-center',
              redirect: '/personal-center/personal'
            },
            {
              path: 'personal',
              name: 'personal',
              redirect: '/personal-center/personal/my-community',
              component: () => import('@/views/personal-center/personal/index.vue'),
              children: [
                {
                  path: 'my-community',
                  name: 'myCommunity',
                  component: () => import('@/views/personal-center/personal/components/Community.vue')
                },
                {
                  path: 'browse-history',
                  name: 'personalBrowseHistory',
                  component: () => import('@/views/personal-center/personal/components/BrowseHistory.vue')
                }
              ]
            },
            {
              path: 'published',
              name: 'published',
              component: () => import('@/views/personal-center/published/index.vue')
            },
            {
              path: 'buy',
              name: 'buy',
              component: () => import('@/views/personal-center/buy/index.vue')
            },
            {
              path: 'shipment',
              name: 'shipment',
              component: () => import('@/views/personal-center/shipment/index.vue')
            },
            {
              path: 'collection',
              name: 'collection',
              component: () => import('@/views/personal-center/collection/index.vue')
            },
            {
              path: 'setting',
              name: 'setting',
              redirect: '/personal-center/setting/foundation',
              component: () => import('@/views/personal-center/setting/index.vue'),
              children: [
                {
                  path: 'foundation',
                  name: 'foundation',
                  component: () => import('@/views/personal-center/setting/components/Foundation.vue')
                },
                {
                  path: 'password',
                  name: 'password',
                  component: () => import('@/views/personal-center/setting/components/Password.vue')
                }
              ]
            },
            {
              path: 'release-idle',
              name: 'release-idle',
              component: () => import('@/views/personal-center/release-idle/index.vue')
            },
            {
              path: 'receiving-address',
              name: 'receivingAddress',
              component: () => import('@/views/personal-center/receiving-address/index.vue')
            }
          ]
        }
      ]
    },
    {
      path: '/login',
      component: () => import('@/views/login/index.vue'),
      children: [
        {
          path: '',
          name: 'login',
          component: () => import('@/views/login/components/Login.vue')
        }
      ]
    },
    {
      path: '/register',
      component: () => import('@/views/login/index.vue'),
      children: [
        {
          path: '',
          name: 'register',
          component: () => import('@/views/login/components/Register.vue')
        }
      ]
    },
    {
      path: '/forget',
      component: () => import('@/views/login/index.vue'),
      children: [
        {
          path: '',
          name: 'forget',
          component: () => import('@/views/login/components/Forget.vue')
        }
      ]
    },
    {
      path: '/admin',
      component: () => import('@/views/admin/layout/AdminLayout.vue'),
      meta: { requiresAdmin: true },
      redirect: '/admin/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'adminDashboard',
          component: () => import('@/views/admin/dashboard/index.vue'),
          meta: { requiresAdmin: true }
        },
        {
          path: 'user/account',
          name: 'adminUserAccount',
          component: () => import('@/views/admin/user/account.vue'),
          meta: { requiresAdmin: true }
        },
        {
          path: 'user/role',
          name: 'adminUserRole',
          component: () => import('@/views/admin/user/role.vue'),
          meta: { requiresAdmin: true }
        },
        {
          path: 'commodity/list',
          name: 'adminCommodityList',
          component: () => import('@/views/admin/commodity/list.vue'),
          meta: { requiresAdmin: true }
        },
        {
          path: 'commodity/category',
          name: 'adminCommodityCategory',
          component: () => import('@/views/admin/commodity/category.vue'),
          meta: { requiresAdmin: true }
        },
        {
          path: 'knowledge',
          name: 'adminKnowledge',
          component: () => import('@/views/admin/knowledge/index.vue'),
          meta: { requiresAdmin: true }
        },
        {
          path: 'order',
          name: 'adminOrder',
          component: () => import('@/views/admin/order/index.vue'),
          meta: { requiresAdmin: true }
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      component: () => import('@/components/404.vue')
    }
  ]
})

export function setupRouterGuards(router) {
  router.beforeEach(async (to, from) => {
    const userStore = useUserStore()

    const authPages = ['/login', '/register', '/forget']
    const isAuthPage = authPages.some(p => to.path.startsWith(p))

    const token = localStorage.getItem('token')
    if (token && !userStore.loginUser) {
      try {
        await userStore.getCurrLoginUser()
      } catch (e) {
        localStorage.removeItem('token')
      }
    }

    if (isAuthPage && userStore.isLoggedIn()) {
      if (userStore.isAdmin()) {
        return { path: '/admin' }
      } else {
        return { path: '/' }
      }
    }

    const requiresAdmin = to.matched.some(record => record.meta.requiresAdmin)

    if (requiresAdmin) {
      if (!userStore.isLoggedIn()) {
        return {
          path: '/login',
          query: { redirect: to.fullPath }
        }
      }
      if (!userStore.isAdmin()) {
        return { path: '/' }
      }
      return
    }

    const publicPaths = ['/login', '/register', '/forget', '/detail', '/category', '/search']
    const isPublic = publicPaths.some(p => to.path.startsWith(p)) || to.path === '/' || to.name === '404'

    if (isPublic) {
      if (userStore.isLoggedIn() && userStore.isAdmin() && to.path === '/') {
        return { path: '/admin' }
      }
      return
    }

    if (!userStore.isLoggedIn()) {
      return {
        path: '/login',
        query: { redirect: to.fullPath }
      }
    }

    if (userStore.isAdmin()) {
      return { path: '/admin' }
    }
  })
}

export default router
