const baseUrl = '/api'

const resourcePaths = {
  spu: '/uploads/images/spu',
  sku: '/uploads/images/sku',
  brands: '/uploads/images/brands',
  stores: '/uploads/images/stores',
  storeLogo: '/uploads/images/stores/logo',
  storeBanner: '/uploads/images/stores/banner',
  avatars: '/uploads/images/user/avatars',
  banners: '/uploads/images/banners'
}

export type ResourceType = keyof typeof resourcePaths

function isFullUrl(path: string): boolean {
  return path.startsWith('http://') || path.startsWith('https://')
}

function isDataUri(path: string): boolean {
  return path.startsWith('data:image/')
}

function isApiPath(path: string): boolean {
  return path.startsWith('/api/')
}

function encodePathSegment(path: string): string {
  const segments = path.split('/')
  return segments.map(segment => {
    try {
      if (decodeURIComponent(segment) !== segment) {
        return segment
      }
      return encodeURIComponent(segment)
    } catch {
      return encodeURIComponent(segment)
    }
  }).join('/')
}

export function getResourceUrl(type: ResourceType, dbPath: string): string {
  if (!dbPath) return ''
  if (isFullUrl(dbPath)) return dbPath
  if (isDataUri(dbPath)) return dbPath
  if (isApiPath(dbPath)) return dbPath
  const encodedPath = encodePathSegment(dbPath)
  return `${baseUrl}${resourcePaths[type]}/${encodedPath}`
}

export function getSpuImageUrl(dbPath: string): string {
  return getResourceUrl('spu', dbPath)
}

export function getSkuImageUrl(dbPath: string): string {
  return getResourceUrl('sku', dbPath)
}

export function getBrandLogoUrl(dbPath: string): string {
  return getResourceUrl('brands', dbPath)
}

export function getStoreLogoUrl(dbPath: string): string {
  return getResourceUrl('storeLogo', dbPath)
}

export function getStoreBannerUrl(dbPath: string): string {
  return getResourceUrl('storeBanner', dbPath)
}

export function getAvatarUrl(dbPath: string): string {
  return getResourceUrl('avatars', dbPath)
}

export function getBannerUrl(dbPath: string): string {
  return getResourceUrl('banners', dbPath)
}