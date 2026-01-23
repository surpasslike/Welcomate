package com.surpasslike.welcomate.note.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.surpasslike.welcomate.note.data.NoteRepository
import com.surpasslike.welcomate.note.entity.NoteBean

class NoteViewModel : ViewModel() {
    private val TAG = "NoteViewModel"
    private val repository = NoteRepository.getInstance()

    private val _noteLiveData = MutableLiveData<List<NoteBean>>()
    val noteLiveData: LiveData<List<NoteBean>> = _noteLiveData

    /*
    * 加载笔记
    * */
    fun loadNotes() {
        val repositoryNotes = repository.allNotes
        // 这个是数据库里面的note格式,也就是List<Note>, 而不是List<NoteBean>
        // 因此我们需要转换成List<NoteBean>的格式,也就是下面的noteBeans
        // 目的是为了供前台fragment使用, 毕竟fragment使用的是adapter

        // Kotlin 的 map 函数，用于转换列表: List<Note> →  map 转换  → List<NoteBean>
        val noteBeans = repositoryNotes.map { note ->
            NoteBean(
                id = note.id,
                title = note.title,
                content = note.content,
                createTime = note.createTime,
                updateTime = note.updateTime
            )
        }
        // _noteLiveData 是一个容器（LiveData 对象）; _noteLiveData.value 是容器里的数据
        _noteLiveData.value = noteBeans
    }

    /*
    * 添加笔记
    * */
    fun addNotes(title: String, content: String) {
        Log.d(TAG, "addNotes title = $title, content = $content")
        repository.addNote(title, content)
    }
}