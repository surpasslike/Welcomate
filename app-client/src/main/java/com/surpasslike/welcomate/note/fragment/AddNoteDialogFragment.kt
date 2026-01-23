package com.surpasslike.welcomate.note.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.surpasslike.welcomate.base.view.BaseDialogFragment
import com.surpasslike.welcomate.databinding.FragmentAddNoteDialogBinding
import com.surpasslike.welcomate.note.vm.NoteViewModel

/**
 * 增加笔记弹窗
 */
class AddNoteDialogFragment : BaseDialogFragment() {

    private lateinit var mBinding: FragmentAddNoteDialogBinding
    private val mViewModel by lazy {
        ViewModelProvider(requireParentFragment())[NoteViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        // 初始化binding,使用 Binding inflate 布局
        mBinding = FragmentAddNoteDialogBinding.inflate(inflater, container, false)
        return mBinding.root  // 返回 Binding 的根视图
    }

    // 在 onViewCreated 中设置监听器,各个按钮的点击事件
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.ivReturnBack.setOnClickListener {
            // 点击返回按钮，关闭弹窗
            dismiss()
        }

        mBinding.ivSave.setOnClickListener {
            // 点击保存按钮
            val title = mBinding.etTitle.text.toString()
            val context = mBinding.etContext.text.toString()
            mViewModel.addNotes(title, context)
            dismiss()
        }
    }
}