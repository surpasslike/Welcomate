package com.surpasslike.welcomate.note.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.surpasslike.welcomate.note.data.NoteRepository
import com.surpasslike.welcomate.note.entity.NoteBean

class NoteViewModel : ViewModel() {
    val TAG = "NoteViewModel"

    private val repository = NoteRepository.getInstance()

    private val _noteLiveData = MutableLiveData<List<NoteBean>>()
    val noteLiveData: LiveData<List<NoteBean>> = _noteLiveData

    fun loadNotes() {
        val notes = repository.allNotes

        // Kotlin 的 map 函数，用于转换列表: List<Note> →  map 转换  → List<NoteBean>
        val noteBeans = notes.map { note ->
            NoteBean(
                id = note.id,
                title = note.title,
                content = note.content,
                createTime = note.createTime,
                updateTime = note.updateTime
            )
        }

        _noteLiveData.value = noteBeans // _noteLiveData 是一个容器（LiveData 对象）; _noteLiveData.value 是容器里的数据
    }





}