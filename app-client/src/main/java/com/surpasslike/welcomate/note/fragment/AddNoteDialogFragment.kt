package com.surpasslike.welcomate.note.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.surpasslike.welcomate.R
import com.surpasslike.welcomate.base.view.BaseDialogFragment

/**
 * 增加笔记弹窗
 */
class AddNoteDialogFragment : BaseDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_note_dialog, container, false)
    }



}