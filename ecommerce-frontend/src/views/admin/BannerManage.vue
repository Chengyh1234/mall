<template>
  <div class="banner-manage">
    <div class="page-header">
      <h1>轮播图管理</h1>
      <el-button type="primary" @click="openAddDialog">新增轮播图</el-button>
    </div>

    <div class="search-bar">
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable @change="loadList">
        <el-option label="全部" :value="undefined" />
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" @click="loadList">刷新</el-button>
    </div>

    <el-table :data="bannerList" border stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="图片" width="120">
        <template #default="scope">
          <el-image
            :src="getImageUrl(scope.row.imageUrl)"
            style="width: 80px; height: 40px"
            fit="cover"
            :preview-src-list="[getImageUrl(scope.row.imageUrl)]"
          />
        </template>
      </el-table-column>
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column prop="linkUrl" label="跳转链接" min-width="200" show-overflow-tooltip />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="openEditDialog(scope.row)">编辑</el-button>
          <el-button
            size="small"
            :type="scope.row.status === 1 ? 'warning' : 'success'"
            @click="handleToggleStatus(scope.row)"
          >
            {{ scope.row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑轮播图' : '新增轮播图'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入轮播图标题" maxlength="50" />
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="可选，点击轮播图跳转的链接" />
        </el-form-item>
        <el-form-item label="轮播图" prop="imageFile">
          <div class="upload-wrapper">
            <el-upload
              ref="uploadRef"
              :auto-upload="false"
              :show-file-list="false"
              accept="image/jpeg,image/png,image/gif,image/webp"
              :on-change="handleFileChange"
            >
              <template #trigger>
                <el-button type="primary">选择图片</el-button>
              </template>
            </el-upload>
            <div v-if="form.imageFile" class="file-name">{{ form.imageFile.name }}</div>
            <div v-else-if="form.previewUrl" class="file-name current-img">
              <el-image :src="form.previewUrl" style="width: 120px; height: 60px" fit="cover" />
            </div>
            <div v-else class="file-name placeholder">未选择图片</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules, type UploadInstance, type UploadRawFile } from 'element-plus'
import { getBannerList, addBanner, updateBanner, deleteBanner, updateBannerStatus, type BannerItem } from '@/api/banner'
import { getBannerUrl } from '@/utils/resource'

const loading = ref(false)
const saving = ref(false)
const bannerList = ref<BannerItem[]>([])
const statusFilter = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()

const form = ref({
  title: '',
  linkUrl: '',
  imageFile: null as File | null,
  previewUrl: ''
})

const formRules: FormRules = {
  title: [
    { required: true, message: '请输入轮播图标题', trigger: 'blur' }
  ],
  imageFile: [
    {
      validator: (_rule: any, _value: any, callback: any) => {
        if (!isEdit.value && !form.value.imageFile) {
          callback(new Error('请选择轮播图图片'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

const getImageUrl = (imageUrl: string) => {
  return getBannerUrl(imageUrl)
}

const loadList = async () => {
  loading.value = true
  try {
    bannerList.value = await getBannerList(statusFilter.value)
  } catch {
    // 拦截器已处理
  } finally {
    loading.value = false
  }
}

const openAddDialog = () => {
  isEdit.value = false
  editId.value = null
  form.value = { title: '', linkUrl: '', imageFile: null, previewUrl: '' }
  dialogVisible.value = true
}

const openEditDialog = (row: BannerItem) => {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    title: row.title,
    linkUrl: row.linkUrl || '',
    imageFile: null,
    previewUrl: getImageUrl(row.imageUrl)
  }
  dialogVisible.value = true
}

const handleFileChange = (file: { raw: UploadRawFile; name: string }) => {
  const allowedTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.raw.type)) {
    ElMessage.warning('仅支持 jpg、png、gif、webp 格式')
    return false
  }
  form.value.imageFile = file.raw
  form.value.previewUrl = URL.createObjectURL(file.raw)
}

const handleSave = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  if (!form.value.title) return

  saving.value = true
  try {
    const fd = new FormData()
    fd.append('title', form.value.title)
    if (form.value.linkUrl) fd.append('linkUrl', form.value.linkUrl)

    if (isEdit.value && editId.value !== null) {
      fd.append('id', String(editId.value))
      if (form.value.imageFile) {
        fd.append('imageFile', form.value.imageFile)
      }
      await updateBanner(fd)
      ElMessage.success('更新成功')
    } else {
      if (!form.value.imageFile) {
        ElMessage.warning('请选择轮播图图片')
        return
      }
      fd.append('imageFile', form.value.imageFile)
      await addBanner(fd)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    await loadList()
  } catch {
    // 拦截器已处理
  } finally {
    saving.value = false
  }
}

const handleToggleStatus = async (row: BannerItem) => {
  const newStatus = row.status === 1 ? 0 : 1
  const label = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}该轮播图吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateBannerStatus(row.id, newStatus)
    ElMessage.success(`${label}成功`)
    await loadList()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 拦截器已处理
    }
  }
}

const handleDelete = async (row: BannerItem) => {
  try {
    await ElMessageBox.confirm(`确定删除轮播图「${row.title}」吗？删除后不可恢复。`, '警告', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'error'
    })
    await deleteBanner(row.id)
    ElMessage.success('删除成功')
    await loadList()
  } catch (e: any) {
    if (e !== 'cancel') {
      // 拦截器已处理
    }
  }
}

onMounted(() => {
  loadList()
})
</script>

<style scoped>
.banner-manage {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: center;
}

.upload-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-name {
  font-size: 13px;
  color: #666;
}

.file-name.current-img {
  display: flex;
  align-items: center;
}

.file-name.placeholder {
  color: #999;
}
</style>