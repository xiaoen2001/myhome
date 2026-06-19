<template>
  <div class="my-notes-page">
    <div class="page-header">
      <h2>我的笔记</h2>
      <el-button type="primary" @click="openAddDialog">+ 新建笔记</el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-bar">
      <el-select v-model="statusFilter" placeholder="全部状态" clearable @change="fetchNotes">
        <el-option label="全部" value=""/>
        <el-option label="草稿" value="draft"/>
        <el-option label="审核中" value="reviewing"/>
        <el-option label="已通过" value="published"/>
        <el-option label="未通过" value="rejected"/>
        <el-option label="已下架" value="offline"/>
        <el-option label="已封禁" value="banned"/>
      </el-select>
      <el-input
          v-model="keyword"
          placeholder="搜索标题或书名"
          clearable
          style="width: 250px"
          @clear="fetchNotes"
          @keyup.enter="fetchNotes"
      />
      <el-button type="primary" @click="fetchNotes">搜索</el-button>
    </div>

    <!-- 笔记列表 -->
    <div v-loading="loading" class="notes-list">
      <el-card v-for="note in notes" :key="note.id" class="note-card" shadow="hover">
        <div class="note-cover">
          <img :src="note.cover || 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg'"/>
        </div>
        <div class="note-info">
          <h3>{{ note.title }}</h3>
          <p class="book">{{ note.bookName }} · {{ note.author }}</p>
          <div class="status">
            <el-tag :type="statusTag(note.status)" size="small">{{ statusText(note.status) }}</el-tag>
            <el-tag v-if="note.visibility === 'private'" type="info" size="small" style="margin-left:6px;">私密</el-tag>
          </div>
          <div class="meta">
            <span>浏览 {{ note.viewCount }}</span>
            <span>点赞 {{ note.likeCount }}</span>
            <span>评论 {{ note.commentCount }}</span>
          </div>
          <div class="actions">
            <el-button link type="primary" @click="editNote(note)">编辑</el-button>
            <el-button link type="danger" @click="deleteNote(note)">删除</el-button>
            <el-button v-if="note.status === 'draft' && note.visibility === 'public'" link type="success" @click="submitReview(note)">
              提交审核
            </el-button>
            <!-- <el-button v-if="note.status === 'draft' && note.visibility === 'private'" link type="primary" @click="submitReview(note)">
              直接发布（免审）
            </el-button> -->
            <el-button v-if="note.status === 'offline'" link type="success" @click="submitReview(note)">上架
            </el-button>
            <el-button v-if="note.status === 'published'" link type="warning" @click="offlineNote(note)">下架
            </el-button>
            <div class="visibility-switch" v-if="note.status === 'draft'">
              <span style="font-size:12px; margin-right:4px;">
                <span v-if="note.visibility === 'public'">公开</span>
                <span v-else>私密</span>
              </span>
              <el-switch
                  v-model="note.visibility"
                  active-value="public"
                  inactive-value="private"
                  active-text=""
                  inactive-text=""
                  @change="toggleVisibility(note)"
              />
              <span style="font-size:11px; color:#E6A23C; margin-left:4px;">私密提交后免审仅自己可见，且无法改回公开</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <div class="pagination" v-if="total > pageSize">
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

    <!-- 新增/编辑笔记弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" top="5vh">
      <el-form :model="form" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" placeholder="笔记标题"/>
        </el-form-item>
        <el-form-item label="书籍名称" required>
          <el-input v-model="form.bookName" placeholder="书名"/>
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="form.author" placeholder="书籍作者"/>
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleCoverUpload"
              accept="image/*"
          >
            <img v-if="form.cover" :src="form.cover" class="cover-preview"/>
            <el-button v-else>上传图片</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option v-for="cat in categories" :key="cat.id" :label="cat.name" :value="cat.id"/>
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tags" multiple filterable allow-create default-first-option
                     placeholder="选择或输入标签">
            <el-option v-for="tag in tags" :key="tag.id" :label="tag.name" :value="tag.name"/>
          </el-select>
        </el-form-item>
        <el-form-item label="可见性">
          <el-radio-group v-model="form.visibility">
            <el-radio value="public">公开（提交后需审核）</el-radio>
            <el-radio value="private">私密（免审核，仅自己可见）</el-radio>
          </el-radio-group>
          <div v-if="form.visibility === 'private'" style="color:#E6A23C; font-size:12px; margin-top:4px;">
            设为私密后无法改为公开，如需公开请删除重建
          </div>
        </el-form-item>
        <el-form-item label="内容" required>
          <div style="border: 1px solid #DCDFE6; border-radius: 4px;">
            <Toolbar
                :editor="editor"
                :defaultConfig="toolbarConfig"
                mode="default"
            />
            <Editor
                v-model="form.content"
                :defaultConfig="editorConfig"
                mode="default"
                @onCreated="onEditorCreated"
                style="height: 400px; overflow-y: auto;"
            />
          </div>
        </el-form-item>
<!--        <el-form-item label="状态">-->
<!--          <el-select v-model="form.status">-->
<!--            <el-option label="草稿" value="draft"/>-->
<!--            <el-option label="提交审核" value="reviewing"/>-->
<!--          </el-select>-->
<!--        </el-form-item>-->
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveNote">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {useUserStore} from '@/stores/user'
import {onBeforeUnmount, onMounted, ref, shallowRef} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {
  createNote,
  deleteNote as apiDeleteNote,
  getCategories,
  getMyNotes,
  getTags,
  offlineNote as apiOfflineNote,
  submitForReview,
  updateNote,
  updateNoteVisibility
} from '@/api/modules/note'
import '@wangeditor/editor/dist/css/style.css'
import {Editor, Toolbar} from '@wangeditor/editor-for-vue'

const userStore = useUserStore()

// ==================== 自定义轻量哈希函数 ====================
const simpleHash = (str) => {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash  // 转换为32位整数
  }
  return hash.toString(16)
}

// 生成表单哈希值（用于比对是否有变化）
const generateFormHash = (formData) => {
  const compareData = {
    title: formData.title || '',
    bookName: formData.bookName || '',
    author: formData.author || '',
    cover: formData.cover || '',
    categoryId: formData.categoryId || null,
    tags: [...(formData.tags || [])].sort(),  // 排序保证数组顺序一致
    content: formData.content || '',
    visibility: formData.visibility || 'public'
  }
  const compareStr = JSON.stringify(compareData)
  return simpleHash(compareStr)
}
// ========================================================

// 编辑器实例
const editor = shallowRef()
const editorConfig = {
  placeholder: '请输入笔记内容...',
  MENU_CONF: {
    uploadImage: {
      async customUpload(file, insertFn) {
        const reader = new FileReader()
        reader.onload = (e) => insertFn(e.target.result, file.name || '图片')
        reader.readAsDataURL(file)
      }
    }
  }
}
const toolbarConfig = {}

const onEditorCreated = (editorInstance) => {
  editor.value = editorInstance
}

onBeforeUnmount(() => {
  if (editor.value) editor.value.destroy()
})

// 响应式数据
const loading = ref(false)
const notes = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const statusFilter = ref('')
const keyword = ref('')
const categories = ref([])
const tags = ref([])

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref(null)

// 存储原始表单的哈希值（用于比对）
const originalFormHash = ref('')

const form = ref({
  title: '',
  bookName: '',
  author: '',
  cover: '',
  categoryId: null,
  tags: [],
  content: '',
  status: 'draft',
  visibility: 'public'
})

// 状态文本映射
const statusText = (status) => {
  const map = {
    draft: '草稿',
    reviewing: '审核中',
    published: '已通过',
    rejected: '未通过',
    offline: '已下架',
    banned: '已封禁'
  }
  return map[status] || status
}

// 状态标签颜色映射
const statusTag = (status) => {
  const map = {
    draft: 'info',
    reviewing: 'warning',
    published: 'success',
    rejected: 'danger',
    offline: 'info',
    banned: 'danger'
  }
  return map[status] || 'info'
}

// 切换可见性
const toggleVisibility = async (note) => {
  try {
    await updateNoteVisibility(note.id, note.visibility)
    ElMessage.success(`笔记已设为${note.visibility === 'public' ? '公开' : '私密'}`)
  } catch (error) {
    note.visibility = note.visibility === 'public' ? 'private' : 'public'
    ElMessage.error('操作失败')
  }
}

// 获取分类列表
const fetchCategories = async () => {
  try {
    categories.value = await getCategories()
  } catch (error) {
    console.error('加载分类失败', error)
  }
}

// 获取标签列表
const fetchTags = async () => {
  try {
    tags.value = await getTags()
  } catch (error) {
    console.error('加载标签失败', error)
  }
}

// 获取我的笔记列表
const fetchNotes = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value,
      status: statusFilter.value || undefined,
      keyword: keyword.value || undefined
    }
    const res = await getMyNotes(params)
    notes.value = res.records || []
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载笔记失败')
  } finally {
    loading.value = false
  }
}

// 打开新增弹窗
const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新建笔记'
  form.value = {
    title: '',
    bookName: '',
    author: '',
    cover: '',
    categoryId: null,
    tags: [],
    content: '',
    status: 'draft',
    visibility: 'public'
  }
  originalFormHash.value = ''  // 新增时清空哈希
  dialogVisible.value = true
}

// 编辑笔记
const editNote = (note) => {
  isEdit.value = true
  editId.value = note.id
  dialogTitle.value = '编辑笔记'
  const formData = { ...note, tags: note.tags || [] }
  form.value = formData
  // 保存原始表单的哈希值
  originalFormHash.value = generateFormHash(formData)
  dialogVisible.value = true
}

// 处理封面上传
const handleCoverUpload = (file) => {
  const reader = new FileReader()
  reader.onload = (e) => {
    form.value.cover = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 保存笔记
const saveNote = async () => {
  // 表单校验
  if (!form.value.title || !form.value.bookName || !form.value.content) {
    ElMessage.warning('请填写标题、书名和内容')
    return
  }

  // 编辑模式：检查哈希值是否变化
  if (isEdit.value) {
    const currentHash = generateFormHash(form.value)
    if (currentHash === originalFormHash.value) {
      ElMessage.info('内容未发生变化')
      dialogVisible.value = false
      return
    }
  }

  try {
    if (isEdit.value) {
      await updateNote(editId.value, form.value)
      ElMessage.success('更新成功，已提交重新审核')
    } else {
      await createNote(form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchNotes()
  } catch (error) {
    ElMessage.error('保存失败')
  }
}

// 删除笔记
const deleteNote = (note) => {
  ElMessageBox.confirm(`确定删除笔记「${note.title}」吗？`, '提示', { type: 'warning' })
      .then(async () => {
        try {
          await apiDeleteNote(note.id)
          ElMessage.success('删除成功')
          fetchNotes()
        } catch (error) {
          ElMessage.error('删除失败')
        }
      })
      .catch(() => {})
}

// 提交审核
const submitReview = async (note) => {
  try {
    await submitForReview(note.id)
    ElMessage.success('已提交审核')
    fetchNotes()
  } catch (error) {
    ElMessage.error('提交失败')
  }
}

// 下架笔记
const offlineNote = async (note) => {
  try {
    await apiOfflineNote(note.id)
    ElMessage.success('已下架')
    fetchNotes()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  fetchCategories()
  fetchTags()
  fetchNotes()
})
</script>

<style scoped>
.my-notes-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.notes-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 24px;
}

.note-card {
  display: flex;
  flex-direction: column;
  border-radius: 12px;
  overflow: hidden;
}

.note-cover {
  height: 160px;
  overflow: hidden;
}

.note-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.note-info {
  padding: 16px;
}

.note-info h3 {
  font-size: 18px;
  margin-bottom: 6px;
}

.book {
  font-size: 14px;
  color: #6b7a86;
  margin-bottom: 8px;
}

.status {
  margin-bottom: 8px;
}

.meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #8c9aa6;
  margin-bottom: 12px;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.cover-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}

.pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>