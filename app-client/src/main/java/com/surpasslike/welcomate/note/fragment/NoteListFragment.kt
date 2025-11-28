package com.surpasslike.welcomate.note.fragment

import android.util.Log
import android.widget.LinearLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.VERTICAL
import androidx.recyclerview.widget.RecyclerView
import com.surpasslike.welcomate.base.view.BaseFragment
import com.surpasslike.welcomate.databinding.FragmentNoteListBinding
import com.surpasslike.welcomate.note.adapter.NoteAdapter
import com.surpasslike.welcomate.note.entity.NoteBean
import com.surpasslike.welcomate.note.vm.NoteViewModel

class NoteListFragment : BaseFragment<FragmentNoteListBinding>() {

    private val noteAdapter by lazy { NoteAdapter() }
    private val viewModel: NoteViewModel by lazy { ViewModelProvider(requireActivity())[NoteViewModel::class.java] }

    override fun initObserve() {
        super.initObserve()
        // 如果笔记发生变化, 则执行refreshView
        viewModel.noteLiveData.observe(viewLifecycleOwner) { data ->
            Log.i(TAG, "initObserve noteLiveData data:${data}")
            refreshView(data)
        }

        // 加载笔记的数据
        viewModel.loadNotes()
    }

    override fun FragmentNoteListBinding.initView() {
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