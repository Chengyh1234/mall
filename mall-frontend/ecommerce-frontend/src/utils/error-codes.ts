/**
 * 后端业务错误码定义
 *
 * 后端统一响应格式：{ code: number, msg: string, data: T }
 * HTTP 状态码与业务码分离：
 *   - 业务规则错误 → HTTP 200 + 业务码 40000
 *   - 认证相关 → HTTP 401 + 业务码 40101/40102/40103
 *   - 其他错误 → HTTP 状态码与业务码一致
 */
export enum ErrorCode {
  /** 请求参数格式错误 */
  BAD_REQUEST = 400,
  /** 未登录，请先登录 */
  NOT_LOGGED_IN = 40101,
  /** 用户名或密码错误 */
  AUTH_FAILED = 40102,
  /** 登录已失效，请重新登录 */
  LOGIN_EXPIRED = 40103,
  /** 权限不足 */
  FORBIDDEN = 403,
  /** 资源不存在 */
  NOT_FOUND = 404,
  /** 业务规则错误（HTTP 200 但业务失败） */
  BUSINESS_ERROR = 40000,
  /** 系统异常 */
  SYSTEM_ERROR = 500,
  /** 验证码已过期或无效 */
  CAPTCHA_EXPIRED = 40104,
}

/**
 * 错误分类
 * 用于决定拦截器中如何处理各类错误
 */
export enum ErrorCategory {
  /** 认证类 — 需清除登录态并跳转登录页 */
  AUTH,
  /** 用户可修正 — 仅弹提示，不操作登录态 */
  USER_ACTION,
  /** 系统异常 — 弹错误提示，可扩展上报 */
  SYSTEM,
  /** 纯通知 — 仅提示无需特殊处理 */
  NOTIFY,
}

/** 业务码 → 分类映射 */
export const ERROR_CATEGORY_MAP: Record<number, ErrorCategory> = {
  [ErrorCode.BAD_REQUEST]: ErrorCategory.USER_ACTION,
  [ErrorCode.NOT_LOGGED_IN]: ErrorCategory.AUTH,
  [ErrorCode.AUTH_FAILED]: ErrorCategory.USER_ACTION,
  [ErrorCode.LOGIN_EXPIRED]: ErrorCategory.AUTH,
  [ErrorCode.FORBIDDEN]: ErrorCategory.USER_ACTION,
  [ErrorCode.NOT_FOUND]: ErrorCategory.NOTIFY,
  [ErrorCode.BUSINESS_ERROR]: ErrorCategory.USER_ACTION,
  [ErrorCode.SYSTEM_ERROR]: ErrorCategory.SYSTEM,
  [ErrorCode.CAPTCHA_EXPIRED]: ErrorCategory.USER_ACTION,
}

/** 业务码 → 默认提示文案 */
export const ERROR_DEFAULT_MSG: Record<number, string> = {
  [ErrorCode.BAD_REQUEST]: '请求参数格式错误',
  [ErrorCode.NOT_LOGGED_IN]: '未登录，请先登录',
  [ErrorCode.AUTH_FAILED]: '用户名或密码错误',
  [ErrorCode.LOGIN_EXPIRED]: '登录已失效，请重新登录',
  [ErrorCode.FORBIDDEN]: '权限不足',
  [ErrorCode.NOT_FOUND]: '请求的资源不存在',
  [ErrorCode.BUSINESS_ERROR]: '操作失败',
  [ErrorCode.SYSTEM_ERROR]: '系统异常，请稍后重试',
}