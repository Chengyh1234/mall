<template>
  <div class="seller-profile">
    <div class="page-header">
      <h1>店铺设置</h1>
    </div>

    <div class="profile-card">
      <div class="avatar-section">
        <div class="avatar">
          <img :src="shopLogoUrl" alt="店铺Logo" />
        </div>
        <div class="avatar-actions">
          <input 
            type="file" 
            id="logo-upload" 
            class="logo-upload-input" 
            accept="image/*"
            @change="handleLogoUpload"
          />
          <label for="logo-upload" class="upload-btn">更换Logo</label>
        </div>
      </div>
      
      <el-form :model="profileForm" label-width="120px">
        <el-form-item label="店铺名称">
          <el-input v-model="profileForm.name" placeholder="请输入店铺名称" />
        </el-form-item>
        <el-form-item label="店铺描述">
          <el-input v-model="profileForm.description" type="textarea" :rows="4" placeholder="请输入店铺描述" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="profileForm.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="店铺地址">
          <el-input v-model="profileForm.address" placeholder="请输入店铺地址" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">保存设置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyStore, updateStore, type Store, type UpdateStoreRequest } from '@/api/shop'
import { getStoreLogoUrl } from '@/utils/resource'

const saving = ref(false)
const loading = ref(true)
const storeId = ref(0)
const shopLogo = ref('')
const logoFile = ref<File | null>(null)

const profileForm = reactive({
  name: '',
  description: '',
  phone: '',
  address: ''
})

const shopLogoUrl = computed(() => {
  if (shopLogo.value) {
    return getStoreLogoUrl(shopLogo.value)
  }
  return 'https://via.placeholder.com/120x120/f5f5f5/999999?text=店铺Logo'
})

// 加载店铺信息
const loadShopInfo = async () => {
  try {
    loading.value = true
    const store = await getMyStore()
    storeId.value = store.id
    profileForm.name = store.name || ''
    profileForm.description = store.description || ''
    profileForm.phone = store.phone || ''
    profileForm.address = store.address || ''
    shopLogo.value = store.logo || ''
  } catch {
    ElMessage.error('获取店铺信息失败')
  } finally {
    loading.value = false
  }
}

// 处理Logo上传
const handleLogoUpload = (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return
  
  logoFile.value = file
  
  // 预览图片
  const reader = new FileReader()
  reader.onload = (e) => {
    shopLogo.value = e.target?.result as string
  }
  reader.readAsDataURL(file)
  
  target.value = ''
}

// 保存店铺设置
const saveProfile = async () => {
  try {
    saving.value = true
    
    const updateData: UpdateStoreRequest = {
      id: storeId.value,
      name: profileForm.name,
      description: profileForm.description,
      phone: profileForm.phone,
      address: profileForm.address
    }
    
    await updateStore(updateData, logoFile.value || undefined)
    ElMessage.success('店铺设置已保存')
    logoFile.value = null
  } catch {
    ElMessage.error('保存店铺设置失败')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadShopInfo()
})
</script>

<style scoped>
.seller-profile {
  padding: 20px;
  max-width: 600px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h1 {
  font-size: 24px;
  margin: 0;
  color: #333;
}

.profile-card {
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
}

.avatar {
  width: 120px;
  height: 120px;
  margin: 0 auto 15px;
  border-radius: 8px;
  overflow: hidden;
  border: 3px solid #eee;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-actions {
  position: relative;
}

.logo-upload-input {
  display: none;
}

.upload-btn {
  padding: 8px 20px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  color: #666;
  display: inline-block;
}

.upload-btn:hover {
  background: #eef2f7;
}

.el-form {
  margin-top: 20px;
}

.el-form-item {
  margin-bottom: 20px;
}

.el-button {
  width: 100%;
}
</style>