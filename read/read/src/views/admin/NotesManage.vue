<template>
  <div class="admin-notes">
    <div class="page-header">
      <h2>笔记管理</h2>
      <div class="search-bar">
        <el-input
            v-model="keyword"
            placeholder="搜索标题/用户名/书名"
            clearable
            style="width: 250px"
            @keyup.enter="handleSearch"
        />
        <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="审核中" value="reviewing" />
          <el-option label="已通过" value="published" />
          <el-option label="未通过" value="rejected" />
          <el-option label="已下架" value="offline" />
          <el-option label="已封禁" value="banned" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <el-table :data="noteList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="封面" width="80">
        <template #default="{ row }">
          <el-image :src="row.cover || 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'" style="width: 60px; height: 40px; object-fit: cover;" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="150" show-overflow-tooltip />
      <el-table-column prop="bookName" label="书名" width="120" />
      <el-table-column prop="author" label="作者" width="100" />
      <el-table-column prop="nickname" label="发布者" width="120" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="发布时间" width="160" />
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewDetail(row)">查看</el-button>
          <template v-if="row.status === 'reviewing'">
            <el-button link type="success" @click="approveNote(row)">通过</el-button>
            <el-button link type="danger" @click="rejectNote(row)">驳回</el-button>
          </template>
          <template v-if="row.status === 'published'">
            <el-button link type="warning" @click="offlineNote(row)">下架</el-button>
          </template>
          <template v-if="row.status !== 'banned' && row.status !== 'rejected'">
            <el-button link type="danger" @click="banNote(row)">封禁</el-button>
          </template>
          <template v-if="row.status === 'banned' ||  row.status === 'rejected'">
            <el-button link type="danger" @click="deleteNote(row)">删除</el-button>
          </template>
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
          @size-change="fetchNotes"
          @current-change="fetchNotes"
      />
    </div>

    <!-- 查看详情弹窗（不跳转前台） -->
    <el-dialog v-model="detailVisible" title="笔记详情" width="800px" top="5vh">
      <div class="note-detail">
        <div class="detail-header">
          <h3>{{ currentNote.title }}</h3>
          <p>作者：{{ currentNote.nickname }} | 状态：{{ statusText(currentNote.status) }}</p>
        </div>
        <div class="detail-cover" v-if="currentNote.cover">
          <img :src="currentNote.cover" />
        </div>
        <div class="detail-content" v-html="currentNote.content"></div>
        <div class="detail-meta">
          <span>书籍：《{{ currentNote.bookName }}》</span>
          <span>作者：{{ currentNote.author }}</span>
          <span>分类：{{ currentNote.categoryName }}</span>
        </div>
      </div>
    </el-dialog>

    <!-- 驳回原因弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="驳回原因" width="400px">
      <el-input
          v-model="rejectReason"
          type="textarea"
          :rows="3"
          placeholder="请输入驳回原因，该原因会通知用户"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmReject">确定驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminNotes, approveNote as apiApproveNote, rejectNote as apiRejectNote, offlineNote as apiOfflineNote, banNote as apiBanNote } from '@/api/modules/admin'
import {deleteNote as apiDeleteNote} from "@/api/modules/note.js";

const loading = ref(false)
const noteList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const statusFilter = ref('')

const detailVisible = ref(false)
const currentNote = ref({})
const rejectDialogVisible = ref(false)
const rejectReason = ref('')
const currentNoteId = ref(null)

const statusText = (status) => {
  const map = {
    reviewing: '审核中',
    published: '已通过',
    rejected: '未通过',
    offline: '已下架',
    banned: '已封禁'
  }
  return map[status] || status
}
const statusTag = (status) => {
  const map = {
    reviewing: 'warning',
    published: 'success',
    rejected: 'danger',
    offline: 'info',
    banned: 'danger'
  }
  return map[status] || 'info'
}

const fetchNotes = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value || undefined
    }
    const res = await getAdminNotes(params)
    noteList.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载笔记列表失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  currentPage.value = 1
  fetchNotes()
}
const resetSearch = () => {
  keyword.value = ''
  statusFilter.value = ''
  handleSearch()
}

const viewDetail = (note) => {
  currentNote.value = note
  detailVisible.value = true
}

const approveNote = (note) => {
  ElMessageBox.confirm(`确定通过笔记「${note.title}」的审核吗？`, '提示', { type: 'info' }).then(async () => {
    try {
      await apiApproveNote(note.id)
      ElMessage.success('已通过审核')
      fetchNotes()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const rejectNote = (note) => {
  currentNoteId.value = note.id
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请输入驳回原因')
    return
  }
  try {
    await apiRejectNote(currentNoteId.value, rejectReason.value)
    ElMessage.success('已驳回，原因已通知用户')
    rejectDialogVisible.value = false
    fetchNotes()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const offlineNote = (note) => {
  ElMessageBox.confirm(`确定下架笔记「${note.title}」吗？下架后用户将无法查看。`, '提示', { type: 'warning' }).then(async () => {
    try {
      await apiOfflineNote(note.id)
      ElMessage.success('已下架')
      fetchNotes()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const banNote = (note) => {
  ElMessageBox.confirm(`确定封禁笔记「${note.title}」吗？封禁后用户无法查看，且用户不可编辑。`, '提示', { type: 'warning' }).then(async () => {
    try {
      await apiBanNote(note.id)
      ElMessage.success('已封禁')
      fetchNotes()
    } catch (error) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const deleteNote = (row) => {
  ElMessageBox.confirm(`确定永久删除记录「${row.title}」吗？删除后不可恢复！`, '提示', { type: 'warning' }).then(async () => {
    try {
      await apiDeleteNote(row.id)
      ElMessage.success('删除成功')
      fetchNotes()   // 刷新列表
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchNotes()
})
</script>

<style scoped>
.admin-notes {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.search-bar {
  display: flex;
  gap: 12px;
  align-items: center;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.note-detail {
  max-height: 60vh;
  overflow-y: auto;
}
.detail-header {
  margin-bottom: 16px;
}
.detail-cover {
  text-align: center;
  margin-bottom: 16px;
}
.detail-cover img {
  max-width: 100%;
  max-height: 300px;
  object-fit: cover;
}
.detail-content {
  line-height: 1.6;
  margin-bottom: 16px;
}
.detail-meta {
  display: flex;
  gap: 16px;
  color: #8c9aa6;
  font-size: 13px;
}
</style>