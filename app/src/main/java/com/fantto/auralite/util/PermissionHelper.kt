package com.fantto.auralite.util

import android.app.Activity
import android.content.Context
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission

/**
 * 权限申请工具类，基于 XXPermissions 框架封装。
 *
 * 提供权限检查、请求、跳转设置页等通用能力，
 * 业务层无需直接依赖 XXPermissions 的具体 API。
 */
object PermissionHelper {


    /**
     * 检查麦克风权限是否已授予
     */
    fun isRecordAudioGranted(context: Context): Boolean {
        return XXPermissions.isGrantedPermission(context, PermissionLists.getRecordAudioPermission())
    }

    /**
     * 检查单个权限是否已授予
     *
     * @param context  上下文
     * @param permission 要检查的权限
     */
    fun isPermissionGranted(context: Context, permission: IPermission): Boolean {
        return XXPermissions.isGrantedPermission(context, permission)
    }

    /**
     * 检查多个权限是否全部已授予
     *
     * @param context     上下文
     * @param permissions 要检查的权限列表
     */
    fun isPermissionsGranted(context: Context, vararg permissions: IPermission): Boolean {
        return XXPermissions.isGrantedPermissions(context, permissions.toList())
    }


    /**
     * 请求单个权限（通用方法）
     *
     * @param context        上下文
     * @param permission     要申请的权限
     * @param onGranted      权限全部授予后的回调
     * @param onDenied       权限被拒绝后的回调，参数表示是否勾选了"不再询问"
     */
    fun requestPermission(
        context: Context,
        permission: IPermission,
        onGranted: () -> Unit,
        onDenied: (doNotAskAgain: Boolean) -> Unit
    ) {
        val activity = context as? Activity
        if (activity == null) {
            onDenied(false)
            return
        }

        XXPermissions.with(activity)
            .permission(permission)
            .request { _, deniedList ->
                if (deniedList.isEmpty()) {
                    onGranted()
                } else {
                    val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(activity, deniedList)
                    onDenied(doNotAskAgain)
                }
            }
    }

    /**
     * 请求麦克风权限的单个方法
     *
     * @param context    上下文
     * @param onGranted  权限授予后的回调
     * @param onDenied   权限被拒绝后的回调，参数表示是否勾选了"不再询问"
     */
    fun requestRecordAudioPermission(
        context: Context,
        onGranted: () -> Unit,
        onDenied: (doNotAskAgain: Boolean) -> Unit
    ) {
        requestPermission(
            context = context,
            permission = PermissionLists.getRecordAudioPermission(),
            onGranted = onGranted,
            onDenied = onDenied
        )
    }

    /**
     * 同时请求多个权限
     *
     * @param context        上下文
     * @param permissions    要申请的权限列表
     * @param onAllGranted   所有权限全部授予后的回调
     * @param onDenied       有权限被拒绝后的回调，参数表示被拒绝的权限是否勾选了"不再询问"
     */
    fun requestPermissions(
        context: Context,
        vararg permissions: IPermission,
        onAllGranted: () -> Unit,
        onDenied: (doNotAskAgain: Boolean) -> Unit
    ) {
        val activity = context as? Activity
        if (activity == null) {
            onDenied(false)
            return
        }

        XXPermissions.with(activity)
            .permissions(permissions)
            .request { _, deniedList ->
                if (deniedList.isEmpty()) {
                    onAllGranted()
                } else {
                    val doNotAskAgain = XXPermissions.isDoNotAskAgainPermissions(activity, deniedList)
                    onDenied(doNotAskAgain)
                }
            }
    }


    /**
     * 跳转到应用权限设置页
     *
     * @param context 上下文
     */
    fun startPermissionActivity(context: Context) {
        XXPermissions.startPermissionActivity(context)
    }

    /**
     * 跳转到应用权限设置页，并指定要开启的权限
     *
     * @param context     上下文
     * @param permissions 需要引导用户开启的权限
     */
    fun startPermissionActivity(context: Context, vararg permissions: IPermission) {
        XXPermissions.startPermissionActivity(context, *permissions)
    }
}
