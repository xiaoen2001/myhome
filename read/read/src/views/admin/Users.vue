<template>
  <div class="admin-users">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="search-bar">
        <el-input
            v-model="keyword"
            placeholder="搜索用户名/昵称/邮箱"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <el-table :data="userList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="头像" width="80" align="center">
        <template #default="{ row }">
          <el-avatar
              :size="40"
              :src="getAvatarUrl(row.avatar)"
          >
            <img :src="defaultAvatar" alt="默认头像"/>
          </el-avatar>
        </template>
      </el-table-column>
      <el-table-column prop="username" label="用户名" min-width="120" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="email" label="邮箱" min-width="200" />
      <el-table-column prop="createdAt" label="注册时间" width="180" />
      <el-table-column prop="role" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.role === 'admin' ? 'danger' : 'info'">
            {{ row.role === 'admin' ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.status"  active-value="active" :disabled="row.id===userId" inactive-value="disabled" @change="toggleUserStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="editUser(row)">编辑</el-button>
          <el-button link type="primary" @click="resetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
      />
    </div>

    <!-- 编辑用户弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width: 100%">
            <el-option label="普通用户" value="user" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUserInfo">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { getAvatarUrl } from '@/utils/avatar'
import {ref, onMounted, computed} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminUsers, resetUserPassword, toggleUserStatus as apiToggleUserStatus, updateUserInfo } from '@/api/modules/admin'
import {useUserStore} from "@/stores/user.js";

const userStore = useUserStore()
const userId = computed(()=>userStore.userId)
const loading = ref(false)
const userList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
// const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const defaultAvatar = '/default-avatar.png'

// 编辑相关
const editDialogVisible = ref(false)
const editForm = ref({ id: null, nickname: '', email: '', role: '' })

const fetchUsers = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined
    }
    const res = await getAdminUsers(params)
    userList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchUsers()
}

const resetSearch = () => {
  keyword.value = ''
  handleSearch()
}

const resetPassword = (row) => {
  ElMessageBox.confirm(`确定将用户「${row.nickname || row.username}」的密码重置为 123456 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await resetUserPassword(row.id)
      ElMessage.success('密码已重置为 123456')
    } catch (error) {
      ElMessage.error('重置失败')
    }
  }).catch(() => {})
}

const toggleUserStatus = async (row) => {
  try {
    await apiToggleUserStatus(row.id, row.status)
    ElMessage.success(`用户已${row.status === 'active' ? '启用' : '禁用'}`)
  } catch (error) {
    row.status = row.status === 'active' ? 'disabled' : 'active'
    ElMessage.error('操作失败')
  }
}

const editUser = (row) => {
  editForm.value = {
    id: row.id,
    nickname: row.nickname,
    email: row.email,
    role: row.role === 'admin' ? 'admin' : 'user'
  }
  editDialogVisible.value = true
}

const saveUserInfo = async () => {
  try {
    await updateUserInfo(editForm.value.id, {
      nickname: editForm.value.nickname,
      email: editForm.value.email,
      role: editForm.value.role
    })
    ElMessage.success('更新成功')
    editDialogVisible.value = false
    fetchUsers()
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.admin-users {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>