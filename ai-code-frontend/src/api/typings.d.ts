/// <reference types="vite/client" />

declare namespace API {
  /** 统一响应格式 */
  interface BaseResponse<T = unknown> {
    code: number
    data: T
    message: string
  }

  /** 分页结果 */
  interface PageResult<T> {
    records: T[]
    totalRow: number
    pageNum: number
    pageSize: number
    totalPage: number
  }

  /** 脱敏用户信息（公开） */
  interface UserVO {
    id: string
    userAccount: string
    userName: string
    userAvatar?: string
    userProfile?: string
    userRole: string
    createTime: string
  }

  /** 登录用户信息（当前登录用户） */
  interface LoginUserVO {
    id: string
    userAccount: string
    userName: string
    userAvatar?: string
    userProfile?: string
    userRole: string
    createTime: string
    updateTime: string
  }

  /** 注册请求 */
  interface UserRegisterRequest {
    userAccount: string
    userPassword: string
    checkPassword: string
  }

  /** 登录请求 */
  interface UserLoginRequest {
    userAccount: string
    userPassword: string
  }

  /** 新增用户请求 */
  interface UserAddRequest {
    userName: string
    userAccount: string
    userAvatar?: string
    userProfile?: string
    userRole: string
  }

  /** 更新用户请求 */
  interface UserUpdateRequest {
    id: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  /** 查询用户请求 */
  interface UserQueryRequest {
    id?: string
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
    pageNum: number
    pageSize: number
    sortField?: string
    sortOrder?: string
  }

  /** 删除请求 */
  interface DeleteRequest {
    id: string
  }
}
