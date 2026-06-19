<template>
  <div class="admin-categories">
    <div class="page-header">
      <h2>笔记分类管理</h2>
      <el-button type="primary" @click="openAddDialog">新增分类</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名称" min-width="150" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="enabled" label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" @change="updateCategorys(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
          <el-button link type="danger" @click="deleteCategory(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
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
import { getCategories, addCategory, updateCategory as apiUpdateCategorys, deleteCategory as apiDeleteCategory } from '@/api/modules/admin'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const editId = ref(null)
const form = ref({ name: '', sort: 0, enabled: true, id: null })

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getCategories()
    list.value = res || []
  } catch (error) {
    ElMessage.error('加载分类失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增分类'
  form.value = { name: '', sort: 0, enabled: true }
  dialogVisible.value = true
}

const openEditDialog = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑分类'
  form.value = { name: row.name, sort: row.sort, enabled: row.enabled }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.value.name) {
    ElMessage.warning('请填写分类名称')
    return
  }
  try {
    if (isEdit.value) {
      form.value.id = editId.value
      await updateCategorys(form.value)
      ElMessage.success('更新成功')
    } else {
      await addCategory(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
  }
}

const updateCategorys = async (row) => {
  try {
    await apiUpdateCategorys(row.id, row)
  } catch (error) {
    row.enabled = !row.enabled
    ElMessage.error('状态修改失败')
  }
}

const deleteCategory = (row) => {
  ElMessageBox.confirm(`确定删除分类「${row.name}」吗？删除后该分类下的笔记会变成未分类。`, '提示', { type: 'warning' }).then(async () => {
    try {
      await apiDeleteCategory(row.id)
      ElMessage.success('删除成功')
      fetchList()
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.admin-categories {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}
</style>