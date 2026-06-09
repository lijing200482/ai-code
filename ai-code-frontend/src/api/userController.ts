/* eslint-disable */
import request from '@/request'

/** 用户注册 */
export async function userRegister(
  body: API.UserRegisterRequest,
  options?: { [key: string]: any },
): Promise<API.BaseResponse<number>> {
  return request.post('/user/register', body, options)
}

/** 用户登录 */
export async function userLogin(
  body: API.UserLoginRequest,
  options?: { [key: string]: any },
): Promise<API.BaseResponse<API.LoginUserVO>> {
  return request.post('/user/login', body, options)
}

/** 获取当前登录用户信息 */
export async function getLoginUser(
  options?: { [key: string]: any },
): Promise<API.BaseResponse<API.LoginUserVO>> {
  return request.get('/user/get/login', options)
}

/** 用户登出 */
export async function userLogout(
  options?: { [key: string]: any },
): Promise<API.BaseResponse<boolean>> {
  return request.post('/user/logout', {}, options)
}

/** 新增用户（管理员） */
export async function addUser(
  body: API.UserAddRequest,
  options?: { [key: string]: any },
): Promise<API.BaseResponse<number>> {
  return request.post('/user/add', body, options)
}

/** 根据 id 获取用户（管理员） */
export async function getUserById(
  id: string,
  options?: { [key: string]: any },
): Promise<API.BaseResponse<API.UserVO>> {
  return request.get('/user/get/vo', { params: { id }, ...options })
}

/** 删除用户（管理员） */
export async function deleteUser(
  body: API.DeleteRequest,
  options?: { [key: string]: any },
): Promise<API.BaseResponse<boolean>> {
  return request.post('/user/delete', body, options)
}

/** 更新用户（管理员） */
export async function updateUser(
  body: API.UserUpdateRequest,
  options?: { [key: string]: any },
): Promise<API.BaseResponse<boolean>> {
  return request.post('/user/update', body, options)
}

/** 分页获取用户列表（管理员） */
export async function listUserByPage(
  body: API.UserQueryRequest,
  options?: { [key: string]: any },
): Promise<API.BaseResponse<API.PageResult<API.UserVO>>> {
  return request.post('/user/list/page/vo', body, options)
}
