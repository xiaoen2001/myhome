<template>
  <div class="carousels-manage">
    <div class="page-header">
      <h2>轮播图管理</h2>
      <el-button type="primary" @click="openAddDialog">新增轮播图</el-button>
    </div>

    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column label="图片" width="120">
        <template #default="{ row }">
          <el-image :src="row.imageUrl"
                    style="width: 80px;
                    height: 60px;
                    object-fit: cover;"
                    :preview-src-list="row.imageUrl.split(',')"
                    :preview-teleported="true" />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="150" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="enabled" label="状态" width="80">
        <template #default="{ row }">
          <el-switch v-model="row.enabled" @change="updateCarousel(row)" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="editCarousel(row)">编辑</el-button>
          <el-button link type="danger" @click="deleteCarousel(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="图片" required>
          <el-upload
              class="avatar-uploader"
              action="/api/admin/carousels/upload"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :before-upload="beforeUpload"
              :with-credentials="true"
          >
            <img v-if="form.imageUrl" :src="form.imageUrl" class="upload-preview"  alt="图片"/>
            <el-icon v-else class="upload-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.enabled" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="closeForm">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { getCarousels, addCarousel, updateCarousel as apiUpdateCarousel, deleteCarousel as apiDeleteCarousel,deleteCarouselNewImage } from '@/api/modules/admin'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const newImg = ref(null)
const isEdit = ref(false)
const editId = ref(null)
const form = ref({
  title: '',
  imageUrl: '',
  sortOrder: 0,
  enabled: true
})

const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`
}))

const fetchCarousels = async () => {
  loading.value = true
  try {
    const res = await getCarousels()
    list.value = res || []
  } catch (error) {
    ElMessage.error('加载轮播图失败')
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增轮播图'
  form.value = { title: '', imageUrl: '', sortOrder: 0, enabled: true }
  dialogVisible.value = true
}

const editCarousel = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑轮播图'
  form.value = { ...row }
  dialogVisible.value = true
}

const handleUploadSuccess = (response) => {
  if (response.code === 200) {
    form.value.imageUrl = response.data
    newImg.value = response.data
    ElMessage.success('上传成功')
  } else {
    ElMessage.error(response.msg || '上传失败')
  }
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const closeForm = async () => {
  dialogVisible.value = false;
  if (newImg.value) {
    try {
      await deleteCarouselNewImage(newImg.value);
      console.log('临时图片已删除');
    } catch (error) {
      console.error('删除临时图片失败', error);
    } finally {
      newImg.value = null;
    }
  }
}

const submitForm = async () => {
  if (!form.value.title || !form.value.imageUrl) {
    ElMessage.warning('请填写标题并上传图片')
    return
  }
  try {
    if (isEdit.value) {
      await apiUpdateCarousel(editId.value, form.value)
      ElMessage.success('更新成功')
    } else {
      await addCarousel(form.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchCarousels()
  } catch (error) {
    ElMessage.error(isEdit.value ? '更新失败' : '新增失败')
  }
}

const updateCarousel = async (row) => {
  try {
    await apiUpdateCarousel(row.id, row)
    ElMessage.success('状态已更新')
  } catch (error) {
    row.enabled = !row.enabled
    ElMessage.error('更新失败')
  }
}

const deleteCarousel = (row) => {
  ElMessageBox.confirm(`确定删除轮播图「${row.title}」吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await apiDeleteCarousel(row.id)
      ElMessage.success('删除成功')
      fetchCarousels()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchCarousels()
})
</script>


<style scoped>
.carousels-manage {
  padding: 20px;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}
.upload-preview {
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 8px;
}
.upload-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  text-align: center;
  line-height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
}
</style>