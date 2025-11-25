package com.surpasslike.welcomate.note.fragment

import com.surpasslike.welcomate.base.view.BaseFragment
import com.surpasslike.welcomate.databinding.FragmentNoteListBinding
import com.surpasslike.welcomate.note.adapter.NoteAdapter
import com.surpasslike.welcomate.note.entity.NoteBean

class NoteListFragment : BaseFragment<FragmentNoteListBinding>() {

    private val noteAdapter by lazy { NoteAdapter() }

    override fun initObserve() {
        super.initObserve()
        // 如果笔记发生变化, 则执行refreshView
    }

    override fun FragmentNoteListBinding.initView() {
        mBinding.rvNoteList.adapter = noteAdapter

    }

    private fun refreshView(noteBeanList: List<NoteBean>) {
        noteAdapter.submitList(noteBeanList)
    }
}