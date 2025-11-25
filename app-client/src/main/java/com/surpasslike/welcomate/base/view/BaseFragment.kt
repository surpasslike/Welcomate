package com.surpasslike.welcomate.base.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewbinding.ViewBinding
import com.surpasslike.welcomate.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.ParameterizedType
import java.util.Objects
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Fragment基类，封装了ViewBinding、生命周期管理、日志打印及Flow收集的简易实现。
 * 建议作为所有业务Fragment的直接父类
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    // 假设 FrameView 接口的方法被直接移入这个抽象类
    abstract fun VB.initView()
    open fun initObserve() {}
    open fun initRequestData() {}

    open val TAG: String by lazy { javaClass.simpleName }

    private var _binding: VB? = null

    /** 视图绑定实例，非空断言，确保只在 onViewCreated 和其后访问 */
    protected val mBinding get() = _binding!!

    private var isViewCreated = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // 使用反射工具类获取并实例化 ViewBinding
        _binding = reflexViewBinding(javaClass, layoutInflater)
        if (savedInstanceState != null) {
            isViewCreated = savedInstanceState.getBoolean("isViewCreated", false)
        }
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isViewCreated = true

        // 初始化视图
        _binding?.initView()
        // 初始化数据观察
        initObserve()
        // 初始化数据请求
        initRequestData()
    }

    // --- Flow 收集与生命周期管理功能 ---

    /**
     * UI更新相关flow收集。
     * 使用 viewLifecycleOwner.repeatOnLifecycle(State) 安全地收集数据流，防止内存泄漏。
     */
    protected fun <T> Flow<T>.collectWithViewLife(
        context: CoroutineContext = EmptyCoroutineContext,
        state: Lifecycle.State = Lifecycle.State.STARTED,
        block: suspend CoroutineScope.(T) -> Unit
    ) {
        viewLifecycleOwner.lifecycleScope.launch(context) {
            viewLifecycleOwner.repeatOnLifecycle(state) {
                collect { value ->
                    block(value)
                }
            }
        }
    }

    /** 简化的调试日志 */
    fun dbg(log: Any?) = Log.d(TAG, log.toString())

    fun getBinding() = mBinding

    // 生命周期清理与状态保存
    override fun onDestroyView() {
        super.onDestroyView()
        // 清理 ViewBinding 引用，避免内存泄漏
        _binding = null
        isViewCreated = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isViewCreated", isViewCreated)
    }

    /**
     * 切换到指定的Fragment
     *
     * @param T 要切换到的Fragment类型，必须是BaseFragment的子类
     * @param fm Fragment实例，默认通过无参构造函数创建
     * @param init 应用于Fragment的初始化lambda
     */
    inline fun <reified T : BaseFragment<*>> switchFragment(
        fm: T = T::class.java.getDeclaredConstructor().newInstance(),
        noinline init: T.() -> Unit = {}
    ) {
        dbg("goToFragment ${T::class.simpleName}")
        fm.init()
        parentFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.setting_frag_in, R.anim.setting_frag_out)
            .replace(R.id.fragment_container, fm).commit()
    }

    /**
     * 反射获取ViewBinding
     *
     * @param <V>    ViewBinding 实现类
     * @param aClass 当前类
     * @param from   layouinflater
     * @return viewBinding实例
    </V> */
    private fun <V : ViewBinding> reflexViewBinding(aClass: Class<*>, from: LayoutInflater?): V {
        try {
            val actualTypeArguments =
                (Objects.requireNonNull(aClass.genericSuperclass) as ParameterizedType).actualTypeArguments
            for (i in actualTypeArguments.indices) {
                val tClass: Class<Any>
                try {
                    tClass = actualTypeArguments[i] as Class<Any>
                } catch (e: Exception) {
                    continue
                }
                if (ViewBinding::class.java.isAssignableFrom(tClass)) {
                    val inflate = tClass.getMethod("inflate", LayoutInflater::class.java)
                    return inflate.invoke(null, from) as V
                }
            }
            return reflexViewBinding<V>(aClass.superclass, from)
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
            Log.e("error", "ViewBinding reflex", e)
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
            Log.e("error", "ViewBinding reflex", e)
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
            Log.e("error", "ViewBinding reflex", e)
        }
        throw RuntimeException("ViewBinding初始化失败")
    }
}