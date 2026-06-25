import { ErrorCode, ERROR_DEFAULT_MSG } from './error-codes'

/**
 * 可捕获的业务异常
 *
 * 当 API 调用方需要针对特定业务码做差异化处理时，catch 此异常。
 * 例如登录页面区分「密码错误」和「账号不存在」。
 *
 * @example
 * ```ts
 * try {
 *   await login(data)
 * } catch (err) {
 *   if (err instanceof BusinessError && err.code === ErrorCode.UNAUTHORIZED_PASSWORD) {
 *     // 密码错误次数 +1
 *   }
 * }
 * ```
 */
export class BusinessError extends Error {
  /** 后端返回的业务码 */
  public readonly code: number

  constructor(code: number, msg?: string) {
    super(msg || ERROR_DEFAULT_MSG[code] || '未知错误')
    this.name = 'BusinessError'
    this.code = code
  }
}