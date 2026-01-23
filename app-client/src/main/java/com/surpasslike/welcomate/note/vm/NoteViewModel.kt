package com.surpasslike.welcomate.note.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.surpasslike.welcomate.note.data.Note
import com.surpasslike.welcomate.note.data.NoteRepository

class NoteViewModel : ViewModel() {
    private val TAG = "NoteViewModel"
    private val repository = NoteRepository.getInstance()

    private val _noteLiveData = MutableLiveData<List<Note>>()
    val noteLiveData: LiveData<List<Note>> = _noteLiveData

    /*
    * 加载笔记
    * */
    fun loadNotes() {
        val repositoryNotes = repository.allNotes

        // _noteLiveData 是一个容器（LiveData 对象）; _noteLiveData.value 是容器里的数据
        _noteLiveData.value = repositoryNotes
    }

    /*
    * 添加笔记
    * */
    fun addNotes(title: String, content: String) {
        Log.d(TAG, "addNotes title = $title, content = $content")
        repository.addNote(title, content)
    }
}