import request from '@/utils/request'

/**
 * 文件上传响应
 */
export interface UploadResponse {
  url: string
  fullUrl: string
  fileName?: string
  fileSize?: number
  fileType?: string
}

// 上传文件
export function uploadFile(file: File, onProgress?: (progressEvent: any) => void): Promise<UploadResponse> {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

// 上传图片
export function uploadImage(file: File, onProgress?: (progressEvent: any) => void): Promise<UploadResponse> {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/file/upload-image',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress: onProgress
  })
}

// 批量上传文件
export function uploadFiles(files: File[]): Promise<UploadResponse[]> {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: '/file/upload-batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

// 删除文件
export function deleteFile(filePath: string): Promise<void> {
  return request({
    url: '/file/delete',
    method: 'delete',
    params: { filePath }
  })
}

// 下载文件
export function downloadFile(filePath: string): Promise<Blob> {
  return request({
    url: '/file/download',
    method: 'get',
    params: { filePath },
    responseType: 'blob'
  })
}

// 预览图片
export function previewImage(filePath: string): Promise<Blob> {
  return request({
    url: '/file/preview',
    method: 'get',
    params: { filePath },
    responseType: 'blob'
  })
}

// 获取文件URL
export function getFileUrl(filePath: string): Promise<{ url: string; fullUrl: string }> {
  return request({
    url: '/file/url',
    method: 'get',
    params: { filePath }
  })
}