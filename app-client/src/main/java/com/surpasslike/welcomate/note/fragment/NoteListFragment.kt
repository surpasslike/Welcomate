package com.surpasslike.welcomate.note.fragment

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.surpasslike.welcomate.R
import com.surpasslike.welcomate.base.view.BaseFragment
import com.surpasslike.welcomate.constants.AppConstants
import com.surpasslike.welcomate.databinding.FragmentNoteListBinding
import com.surpasslike.welcomate.note.adapter.NoteAdapter
import com.surpasslike.welcomate.note.entity.NoteBean
import com.surpasslike.welcomate.note.vm.NoteViewModel

class NoteListFragment : BaseFragment<FragmentNoteListBinding>() {

    private val noteAdapter by lazy { NoteAdapter() }
    private val viewModel: NoteViewModel by lazy { ViewModelProvider(requireActivity())[NoteViewModel::class.java] }

    companion object {
        fun newInstance(userName: String?) = NoteListFragment().apply {
            arguments = Bundle().apply {
                putString(AppConstants.IntentExtra.USERNAME, userName)
            }
        }
    }

    private val userName by lazy { arguments?.getString(AppConstants.IntentExtra.USERNAME) ?: "" }

    override fun initObserve() {
        super.initObserve()
        // 如果笔记发生变化, 则执行refreshView
        viewModel.noteLiveData.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "initObserve noteLiveData data:${data}")
            refreshView(data)
        }
    }

    override fun initRequestData() {
        super.initRequestData()
        // 加载笔记的数据
        viewModel.loadNotes()
    }

    override fun FragmentNoteListBinding.initView() {
        // 标题
        tvNoteListTitle.text = getString(R.string.tv_note_list_title_text, userName);

        // 笔记列表
        rvNoteList.adapter = noteAdapter
        rvNoteList.layoutManager = LinearLayoutManager(requireContext())

        noteAdapter.onRootClickAction = {
            val bean = noteAdapter.currentList[it]
            // todo show这个笔记的修改弹窗
        }

        btnAddNote.setOnClickListener {
            // todo show一个添加笔记的弹窗
        }
    }

    private fun refreshView(noteBeanList: List<NoteBean>) {
        noteAdapter.submitList(noteBeanList)
    }
}