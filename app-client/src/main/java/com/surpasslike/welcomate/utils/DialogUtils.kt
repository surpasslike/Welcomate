package com.surpasslike.welcomate.utils

import android.os.Looper
import android.util.Log
import androidx.fragment.app.FragmentManager
import com.surpasslike.welcomate.base.view.BaseDialogFragment

/**
 * 弹窗工具类
 * 负责管理应用内所有 DialogFragment 的显示、隐藏和生命周期
 * 支持多弹窗栈管理,防止弹窗重叠和内存泄漏
 */
object DialogUtils {
    private const val TAG = "DialogUtils"

    /**
     * 弹窗栈,用于管理多个弹窗的显示顺序
     * 最新显示的弹窗在列表末尾
     */
    private val dialogStack = mutableListOf<BaseDialogFragment>()

    /**
     * 显示弹窗
     *
     * @param fragmentManager
     *   1. 在 Activity 中：
     *   DialogUtils.showDialogFragment(supportFragmentManager, dialog)
     *   2. 在 Fragment 中（推荐）：
     *   DialogUtils.showDialogFragment(childFragmentManager, dialog)
     *   3. 在嵌套 Fragment 中（如果需要弹窗覆盖父 Fragment）：
     *   DialogUtils.showDialogFragment(parentFragmentManager, dialog)
     * @param dialog 要显示的 BaseDialogFragment 实例
     * @param singleInstance 是否单例模式,true 则关闭其他所有弹窗,默认 false
     * @param cancelable 是否可通过返回键或点击外部取消,默认 true
     * @param onDismiss 弹窗消失时的回调(无论是手动关闭还是自动关闭)
     * @param onCancel 弹窗被取消时的回调(仅用户主动取消时触发,如点击返回键或外部区域)
     */
    fun showDialogFragment(
        fragmentManager: FragmentManager,
        dialog: BaseDialogFragment,
        singleInstance: Boolean = false,
        cancelable: Boolean = true,
        onDismiss: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        val dialogName = dialog.javaClass.simpleName
        Log.d(TAG, "showDialogFragment: 尝试显示弹窗 [$dialogName], cancelable=$cancelable, singleInstance=$singleInstance")

        // 防止重复显示相同类型的弹窗
        // 如果弹窗栈中已存在相同类型且正在显示的弹窗,则不再显示
        val existingDialog = dialogStack.find {
            it::class.java == dialog::class.java && it.dialog?.isShowing == true
        }
        if (existingDialog != null) {
            Log.w(TAG, "showDialogFragment: 弹窗 [$dialogName] 已在显示中，忽略本次请求")
            return
        }

        // 单例模式:关闭所有已显示的弹窗
        if (singleInstance) {
            Log.i(TAG, "showDialogFragment: 单例模式，关闭所有现有弹窗")
            dismissAll()
        }

        // 设置弹窗是否可取消
        dialog.mCancelable = cancelable

        // 设置弹窗消失回调
        // 在弹窗消失时从栈中移除,并触发用户传入的回调
        dialog.dismissAction = {
            Log.d(TAG, "dismissAction: 从栈中移除弹窗 [$dialogName]")
            dialogStack.remove(dialog)
            onDismiss?.invoke()
        }

        // 设置弹窗取消回调
        dialog.cancelAction = onCancel

        // 显示弹窗,使用类的简单名称作为 tag
        dialog.show(fragmentManager, dialog.javaClass.simpleName)

        // 将弹窗添加到栈中
        dialogStack.add(dialog)
        Log.i(TAG, "showDialogFragment: 弹窗 [$dialogName] 已添加到栈, 当前栈大小=${dialogStack.size}, 栈内容=${getStackInfo()}")
    }

    /**
     * 关闭所有弹窗
     * 注意:此方法不是线程安全的,应该在主线程调用
     * 如果需要从其他线程调用,请使用 dismissAllInMainThread()
     */
    fun dismissAll() {
        if (dialogStack.isEmpty()) {
            Log.d(TAG, "dismissAll: 弹窗栈为空，无需关闭")
            return
        }

        Log.i(TAG, "dismissAll: 开始关闭所有弹窗, 当前栈大小=${dialogStack.size}, 栈内容=${getStackInfo()}")

        try {
            // 创建副本避免在遍历时修改列表导致并发问题
            val dialogList = dialogStack.toList()
            dialogList.forEach {
                val dialogName = it.javaClass.simpleName
                Log.d(TAG, "dismissAll: 关闭弹窗 [$dialogName]")
                // 先清空 dismissAction 避免在 dismiss 时触发回调,导致重复操作 dialogStack
                it.dismissAction = null
                it.dismissAllowingStateLoss()
            }
        } catch (e: Exception) {
            // 吞掉异常,避免崩溃
            Log.e(TAG, "dismissAll: 关闭弹窗时发生异常", e)
            e.printStackTrace()
        } finally {
            // 无论是否发生异常,都清空弹窗栈
            dialogStack.clear()
            Log.i(TAG, "dismissAll: 弹窗栈已清空")
        }
    }

    /**
     * 在主线程中关闭所有弹窗
     * 如果当前已在主线程,直接执行;否则切换到主线程执行
     */
    fun dismissAllInMainThread() {
        if (Looper.getMainLooper().isCurrentThread) {
            Log.d(TAG, "dismissAllInMainThread: 当前在主线程，直接执行")
            dismissAll()
        } else {
            // 使用 Handler 切换到主线程
            Log.d(TAG, "dismissAllInMainThread: 当前在子线程，切换到主线程执行")
            android.os.Handler(Looper.getMainLooper()).post {
                dismissAll()
            }
        }
    }

    /**
     * 关闭栈顶弹窗(最后显示的弹窗)
     * 常用于处理返回键事件
     *
     * @return true 表示已处理返回事件(弹窗被关闭或弹窗不可取消)
     *         false 表示没有弹窗或关闭失败,应该执行默认返回行为
     */
    fun dismissTopDialog(): Boolean {
        // 弹窗栈为空,返回 false 让系统处理返回事件
        if (dialogStack.isEmpty()) {
            Log.d(TAG, "dismissTopDialog: 弹窗栈为空，返回 false")
            return false
        }

        val topDialog = dialogStack.last()
        val dialogName = topDialog.javaClass.simpleName

        // 如果顶层弹窗不可取消,返回 true 拦截返回事件(不关闭弹窗也不执行默认返回)
        if (!topDialog.mCancelable) {
            Log.w(TAG, "dismissTopDialog: 栈顶弹窗 [$dialogName] 不可取消，拦截返回键")
            return true
        }

        Log.i(TAG, "dismissTopDialog: 关闭栈顶弹窗 [$dialogName], 当前栈大小=${dialogStack.size}")

        try {
            // 先清空 dismissAction 避免重复移除
            topDialog.dismissAction = null
            // 关闭弹窗并从栈中移除
            topDialog.dismissAllowingStateLoss()
            dialogStack.remove(topDialog)
            Log.i(TAG, "dismissTopDialog: 栈顶弹窗 [$dialogName] 已关闭, 剩余栈大小=${dialogStack.size}")
        } catch (e: Exception) {
            Log.e(TAG, "dismissTopDialog: 关闭栈顶弹窗 [$dialogName] 时发生异常", e)
            e.printStackTrace()
            return false
        }

        return true
    }

    /**
     * 判断当前是否有弹窗正在显示
     *
     * @return true 表示至少有一个弹窗正在显示, false 表示没有弹窗显示
     */
    fun isShowing(): Boolean {
        if (dialogStack.isEmpty()) {
            Log.d(TAG, "isShowing: 弹窗栈为空，返回 false")
            return false
        }

        try {
            // 遍历弹窗栈,检查是否有正在显示的弹窗
            val hasShowing = dialogStack.any { it.dialog?.isShowing == true }
            Log.d(TAG, "isShowing: $hasShowing, 栈大小=${dialogStack.size}")
            return hasShowing
        } catch (e: Exception) {
            Log.e(TAG, "isShowing: 检查弹窗显示状态时发生异常", e)
            e.printStackTrace()
            return false
        }
    }

    /**
     * 获取当前弹窗栈的大小
     * 可用于调试或判断当前有多少个弹窗
     *
     * @return 弹窗栈中的弹窗数量
     */
    fun getDialogCount(): Int {
        val count = dialogStack.size
        Log.d(TAG, "getDialogCount: $count")
        return count
    }

    /**
     * 获取栈顶弹窗(最后显示的弹窗)
     *
     * @return 栈顶的 BaseDialogFragment 实例,如果栈为空则返回 null
     */
    fun getTopDialog(): BaseDialogFragment? {
        val topDialog = dialogStack.lastOrNull()
        val dialogName = topDialog?.javaClass?.simpleName ?: "null"
        Log.d(TAG, "getTopDialog: [$dialogName]")
        return topDialog
    }

    /**
     * 获取弹窗栈信息（用于日志输出）
     * @return 弹窗栈的简要信息，格式：[Dialog1, Dialog2, Dialog3]
     */
    private fun getStackInfo(): String {
        return dialogStack.joinToString(", ", "[", "]") { it.javaClass.simpleName }
    }
}