package com.surpasslike.welcomate.note.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.surpasslike.welcomate.R
import com.surpasslike.welcomate.constants.AppConstants
import com.surpasslike.welcomate.note.fragment.NoteListFragment

class NoteActivity : AppCompatActivity() {

    /** 日志标签  */
    private val TAG: String = "NoteActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        // 获取传递的用户名
        val username = intent.getStringExtra(AppConstants.IntentExtra.USERNAME)
        // 初始化页面
        initViews(username)
    }

    // 初始化页面
    private fun initViews(userName: String?) {
        Log.d(TAG, "initViews  userName = $userName")
        val transaction = supportFragmentManager.beginTransaction()
        val fragment = NoteListFragment.newInstance(userName)
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
}