<template>
  <div class="tags-manage">
    <div class="page-header">
      <h2>标签管理</h2>
      <el-button type="primary" @click="openAddDialog">新增标签</el-button>
    </div>

    <el-table :data="tagList" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="标签名称" min-width="150" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="enabled" label="状态" width="100">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" active-value="1" inactive-value="0" @change="toggleTagStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button link type="primary" @click="editTag(row)">编辑</el-button>
          <el-button link type="danger" @click="deleteTag(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination" v-if="total > pageSize">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchTags"
          @current-change="fetchTags"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标签名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTags as apiGetTags, addTag as apiAddTag, updateTag as apiUpdateTag, deleteTag as apiDeleteTag } from '@/api/modules/admin'

const loading = ref(false)
const tagList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref(null)
const form = ref({
  name: '',
  sortOrder: 0,
  enabled: '1'
})

const fetchTags = async () => {
  loading.value = true
  try {
    const res = await apiGetTags({
      page: currentPage.value,
      pageSize: pageSize.value
    })
    const records = res.records || []
    tagList.value = records.map(tag => ({
      ...tag,
      enabled: tag.enabled ? '1' : '0'
    }))
    total.value = res.total || 0
  } catch (error) {
    ElMessage.error('加载标签列表失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增标签'
  form.value = { name: '', sortOrder: 0, enabled: '1' }
  dialogVisible.value = true
}

const editTag = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑标签'
  form.value = { ...row }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请填写标签名称')
    return
  }
  try {
    if (isEdit.value) {
      await apiUpdateTag(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      form.value.enabled = form.value.enabled === '1';
      await apiAddTag(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchTags()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
  }
}

const toggleTagStatus = async (row) => {
  try {
    await apiUpdateTag(row.id, { enabled: row.enabled === "1"})
    ElMessage.success(`标签已${row.enabled === '1' ? '上架' : '下架'}`)
  } catch (error) {
    row.enabled = row.enabled === '1' ? '0' : '1'
    ElMessage.error('操作失败')
  }
}

const deleteTag = (row) => {
  ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await apiDeleteTag(row.id)
      ElMessage.success('删除成功')
      fetchTags()
    } catch (error) {
      ElMessage.error('删除失败，可能该标签已被使用')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchTags()
})
</script>

<style scoped>
.tags-manage {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>