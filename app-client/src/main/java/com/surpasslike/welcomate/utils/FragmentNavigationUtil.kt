package com.surpasslike.welcomate.utils

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.surpasslike.welcomate.R

object FragmentNavigationUtil {
    fun navigateToFragment(
        fragmentManager: FragmentManager,
        targetFragment: Fragment,
        containerId: Int = R.id.fragment_container,
        args: Bundle? = null,
        addToBackStack: Boolean = true,
        tag: String? = null
    ) {
        args?.let { targetFragment.arguments = it }
        fragmentManager.beginTransaction()
            .replace(containerId, targetFragment, tag ?: targetFragment::class.java.simpleName)
            .apply { if (addToBackStack) addToBackStack(null) }.commit()
    }

    // 为 Java 调用添加简化方法
    @JvmStatic
    fun goToFragment(fragmentManager: FragmentManager, targetFragment: Fragment) {
        navigateToFragment(fragmentManager, targetFragment)
    }
}