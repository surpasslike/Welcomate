package com.surpasslike.welcomate.base.view

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.surpasslike.welcomate.R

open class BaseDialogFragment : DialogFragment() {
    val TAG = javaClass.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: 弹窗实例创建")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView: 创建弹窗视图")
        val view = inflater.inflate(R.layout.fragment_base_dialog, container, false)
        view.findViewById<View>(R.id.root_view)?.setOnClickListener {
            Log.d(TAG, "点击外部区域，关闭弹窗")
            dismiss()
        }
        return view
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart: 弹窗开始显示, cancelable=$mCancelable")

        // 设置弹窗大小
        setupDialogSize()
    }

    /**
     * 设置弹窗大小
     * 子类可以重写 widthPercent 和 heightPercent 来自定义大小
     */
    private fun setupDialogSize() {
        dialog?.window?.let { window ->
            val displayMetrics = resources.displayMetrics
            val screenWidth = displayMetrics.widthPixels
            val screenHeight = displayMetrics.heightPixels

            // 计算弹窗宽高（使用百分比）
            val width = (screenWidth * widthPercent).toInt()
            val height = (screenHeight * heightPercent).toInt()

            window.setLayout(width, height)
            Log.d(TAG, "setupDialogSize: 设置弹窗大小 width=$width(${widthPercent * 100}%), height=$height(${heightPercent * 100}%)")
        }
    }

    /**
     * 弹窗宽度占屏幕宽度的百分比，默认 0.9（90%）
     * 子类可以重写此属性来自定义宽度
     */
    protected open val widthPercent: Float = 0.9f

    /**
     * 弹窗高度占屏幕高度的百分比，默认 0.7（70%）
     * 子类可以重写此属性来自定义高度
     */
    protected open val heightPercent: Float = 0.7f

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: 弹窗可交互")
    }

    override fun getTheme(): Int {
        return R.style.custom_dialog_style
    }

    // 添加回调属性
    var dismissAction: (() -> Unit)? = null
    var cancelAction: (() -> Unit)? = null

    // 自定义可取消属性，同步设置 DialogFragment 的 isCancelable
    var mCancelable: Boolean = true
        set(value) {
            Log.i(TAG, "设置 mCancelable: $field -> $value")
            field = value
            isCancelable = value  // 同步设置内置属性，真正控制弹窗行为
        }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        Log.i(TAG, "onDismiss: 弹窗已关闭")
        dismissAction?.invoke()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        Log.w(TAG, "onCancel: 用户主动取消弹窗（按返回键或点击外部）")
        cancelAction?.invoke()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: 弹窗失去焦点")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: 弹窗停止显示")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: 弹窗销毁")
    }
}