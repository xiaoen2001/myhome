<template>
  <div class="admin-notifications">
    <div class="page-header">
      <h2>通知管理</h2>
      <el-button type="primary" @click="openAddDialog">发布通知</el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-input
          v-model="keyword"
          placeholder="搜索标题或内容"
          clearable
          style="width: 250px"
          @keyup.enter="fetchList"
      />
      <el-select v-model="typeFilter" placeholder="通知类型" clearable @change="fetchList">
        <el-option label="系统通知" value="system"/>
        <el-option label="管理通知" value="admin"/>
      </el-select>
      <el-button type="primary" @click="fetchList">搜索</el-button>
      <el-button @click="resetFilter">重置</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="title" label="标题" min-width="200"/>
      <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip/>
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 'system' ? 'primary' : 'success'">
            {{ row.type === 'system' ? '系统通知' : '管理通知' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180"/>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row)">详情</el-button>
          <el-button link type="danger" @click="deleteItem(row)">删除</el-button>
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
          @size-change="fetchList"
          @current-change="fetchList"
      />
    </div>

    <!-- 发布/编辑通知弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="通知类型" required>
          <el-radio-group v-model="form.type">
            <el-radio label="system">系统通知（广播给所有用户）</el-radio>
            <el-radio label="admin">管理通知（仅管理员可见）</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="通知标题"/>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="form.content" type="textarea" rows="4" placeholder="通知简略内容"/>
        </el-form-item>
        <el-divider content-position="left">详情页内容（可选）</el-divider>
        <el-form-item label="详情标题">
          <el-input v-model="form.detail.title" placeholder="详情页标题（为空则使用通知标题）"/>
        </el-form-item>
        <el-form-item label="详情内容">
          <el-input v-model="form.detail.content" type="textarea" rows="6" placeholder="支持HTML"/>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.detail.author" placeholder="发布者名称"/>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">发布</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="通知详情" width="700px">
      <div class="detail-content" v-html="detailHtml"></div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getAdminNotifications, addNotification, deleteNotification, getNotificationDetail} from '@/api/modules/admin'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const typeFilter = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('')
const submitting = ref(false)
const form = ref({
  type: 'system',
  title: '',
  content: '',
  detail: {
    title: '',
    content: '',
    author: ''
  }
})

const detailVisible = ref(false)
const detailHtml = ref('')

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined
    }
    const res = await getAdminNotifications(params)
    list.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
  }
}

const resetFilter = () => {
  keyword.value = ''
  typeFilter.value = ''
  fetchList()
}

const openAddDialog = () => {
  dialogTitle.value = '发布通知'
  form.value = {
    type: 'system',
    title: '',
    content: '',
    detail: {title: '', content: '', author: ''}
  }
  dialogVisible.value = true
}

const viewDetail = async (row) => {
  try {
    const detail = await getNotificationDetail(row.id)
    detailHtml.value = detail.content || '暂无详情内容'
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

const submitForm = async () => {
  if (!form.value.title || !form.value.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    await addNotification(form.value)
    ElMessage.success('发布成功')
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error('发布失败')
  } finally {
    submitting.value = false
  }
}

const deleteItem = (row) => {
  ElMessageBox.confirm(`确定删除通知「${row.title}」吗？`, '提示', {type: 'warning'}).then(async () => {
    try {
      await deleteNotification(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-notifications {
  padding: 20px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(12px);
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0,0,0,0.05);
  animation: fadeInUp 0.6s ease forwards;
}

.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.detail-content {
  max-height: 60vh;
  overflow-y: auto;
  line-height: 1.6;
}
</style>