import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/layout/app.vue'
import { useUserStore } from '@/stores/user'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/app/Login.vue')
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import('@/views/app/Register.vue')
    },
    {
        path: '/forgot-password',
        name: 'ForgotPassword',
        component: () => import('@/views/app/ForgotPassword.vue')
    },
    {
        path: '/',
        component: Layout,
        redirect: '/home',
        children: [
            {
                path: 'home',
                name: 'Home',
                component: () => import('@/views/app/Home.vue'),
                meta: { title: '首页' }
            },
            {
                path: 'notes',
                name: 'Notes',
                component: () => import('@/views/app/Notes.vue'),
                meta: { title: '笔记分享' }
            },
            {
                path: 'notes/:id',
                name: 'NoteDetail',
                component: () => import('@/views/app/NoteDetail.vue'),
                meta: { title: '笔记详情' }
            },
            {
                path: 'notifications',
                name: 'Notifications',
                component: () => import('@/views/app/Notifications.vue'),
                meta: { title: '系统通知' }
            },
            {
                path: 'friends',
                name: 'Friends',
                component: () => import('@/views/app/Friends.vue'),
                meta: { title: '我的好友' }
            },
            {
                path: 'profile/:id?',
                name: 'Profile',
                component: () => import('@/views/app/Profile.vue'),
                meta: { title: '个人资料' }
            },
            {
                path: 'change-password',
                name: 'ChangePassword',
                component: () => import('@/views/app/ChangePassword.vue'),
                meta: { title: '修改密码' }
            },
            {
                path: 'chat/:id',
                name: 'Chat',
                component: () => import('@/views/app/Chat.vue'),
                meta: { title: '聊天' }
            },
            {
                path: 'my-notes',
                name: 'MyNotes',
                component: () => import('@/views/app/MyNotes.vue'),
                meta: { title: '我的笔记' }
            },
            {
                path: 'my-collects',
                name: 'MyCollects',
                component: () => import('@/views/app/MyCollects.vue'),
                meta: { title: '我的收藏' }
            },
            {
                path: 'my-likes',
                name: 'MyLikes',
                component: () => import('@/views/app/MyLikes.vue'),
                meta: { title: '我的喜欢' }
            },
            {
                path: 'history',
                name: 'History',
                component: () => import('@/views/app/History.vue'),
                meta: { title: '浏览历史' }
            },
            {
                path: 'announcements/:id',
                name: 'AnnouncementDetail',
                component: () => import('@/views/app/AnnouncementDetail.vue'),
                meta: { title: '公告详情' }
            }
        ]
    },
    // 后台管理路由
    {
        path: '/admin',
        component: () => import('@/views/layout/LayoutAdmin.vue'),
        redirect: '/admin/dashboard',
        meta: { requiresAdmin: true },
        children: [
            {
                path: 'dashboard',
                name: 'AdminDashboard',
                component: () => import('@/views/admin/Dashboard.vue'),
                meta: { title: '运营数据' }
            },
            {
                path: 'carousels',
                name: 'AdminCarousels',
                component: () => import('@/views/admin/Carousels.vue'),
                meta: { title: '轮播图管理' }
            },
            {
                path: 'users',
                name: 'AdminUsers',
                component: () => import('@/views/admin/Users.vue'),
                meta: { title: '用户管理' }
            },
            {
                path: 'notes',
                name: 'AdminNotes',
                component: () => import('@/views/admin/NotesManage.vue'),
                meta: { title: '笔记管理' }
            },
            {
                path: 'comments',
                name: 'AdminComments',
                component: () => import('@/views/admin/Comments.vue'),
                meta: { title: '评论管理' }
            },
            {
                path: 'announcements',
                name: 'AdminAnnouncements',
                component: () => import('@/views/admin/Notifications.vue'),
                meta: { title: '公告管理' }
            },
            {
                path: 'categories',
                name: 'AdminCategories',
                component: () => import('@/views/admin/Categories.vue'),
                meta: { title: '分类管理' }
            },
            {
                path: 'tags',
                name: 'AdminTags',
                component: () => import('@/views/admin/Tags.vue'),
                meta: { title: '标签管理' }
            }
        ]
    },
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/NotFound.vue')
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

// 1. 定义白名单（不需要登录即可访问的路径）
const whiteList = ['/login', '/register', '/forgot-password']

router.beforeEach((to, from) => {
    const userStore = useUserStore()

    // 2. 检查目标路径是否在白名单中
    if (whiteList.includes(to.path)) {
        return true // 在白名单内，直接放行
    }

    // 3. 不在白名单且没有 token，则跳转到登录页
    if (!userStore.token) {
        return '/login'
    }

    return true
})

export default router