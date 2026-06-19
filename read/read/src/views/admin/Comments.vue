<template>
  <div class="admin-comments">
    <div class="page-header">
      <h2>评论管理</h2>
      <div class="search-bar">
        <el-input
            v-model="keyword"
            placeholder="评论内容关键词"
            clearable
            style="width: 200px"
            @keyup.enter="handleSearch"
        />
        <el-input
            v-model="username"
            placeholder="用户名"
            clearable
            style="width: 150px"
            @keyup.enter="handleSearch"
        />
        <el-input
            v-model="noteTitle"
            placeholder="笔记标题"
            clearable
            style="width: 180px"
            @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <el-table :data="commentList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80"/>
      <el-table-column prop="content" label="评论内容" min-width="200" show-overflow-tooltip/>
      <el-table-column prop="username" label="用户名" width="120"/>
      <el-table-column prop="noteTitle" label="所属笔记" width="180" show-overflow-tooltip/>
      <el-table-column prop="createdAt" label="评论时间" width="160"/>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="danger" @click="openDeleteDialog(row)">删除</el-button>
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
          @size-change="fetchComments"
          @current-change="fetchComments"
      />
    </div>

    <!-- 删除评论弹窗（填写原因） -->
    <el-dialog v-model="deleteDialogVisible" title="删除评论" width="500px">
      <el-form :model="deleteForm" label-width="100px">
        <el-form-item label="删除原因" required>
          <el-input
              v-model="deleteForm.reason"
              type="textarea"
              rows="3"
              placeholder="请输入删除原因（将通知用户）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete">确认删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {ref, onMounted} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {getAdminComments, deleteComment} from '@/api/modules/admin'

const loading = ref(false)
const commentList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const username = ref('')
const noteTitle = ref('')

const deleteDialogVisible = ref(false)
const deleteForm = ref({id: null, reason: ''})

const fetchComments = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      username: username.value || undefined,
      noteTitle: noteTitle.value || undefined
    }
    const res = await getAdminComments(params)
    console.log(res)
    commentList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载评论列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchComments()
}

const resetSearch = () => {
  keyword.value = ''
  username.value = ''
  noteTitle.value = ''
  handleSearch()
}

const openDeleteDialog = (row) => {
  deleteForm.value = {id: row.id, reason: ''}
  deleteDialogVisible.value = true
}

const confirmDelete = async () => {
  if (!deleteForm.value.reason.trim()) {
    ElMessage.warning('请输入删除原因')
    return
  }
  try {
    await deleteComment(deleteForm.value.id, {reason: deleteForm.value.reason})
    ElMessage.success('删除成功')
    deleteDialogVisible.value = false
    fetchComments()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}

onMounted(() => {
  fetchComments()
})
</script>

<style scoped>
.admin-comments {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>