package com.example.androidx_example.fragments.chat

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.androidx_example.R
import com.example.androidx_example.data.ChatMessage
import com.example.androidx_example.entity.MessageSaveData
import com.example.androidx_example.fragments.BaseFragment
import com.example.androidx_example.until.ChatMessageBus
import com.example.androidx_example.until.RoomUntil
import com.example.androidx_example.works.MessageSendWorker
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment : BaseFragment() {

    private var mChatAdapter: ChatAdapter? = null
    private var mChatRows: ArrayList<ChatMessage> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initRecyclerView()
        initMsgAction()
    }

    private fun loadLocalMsgData() {
        Thread(Runnable {
            val msgSaveRows = RoomUntil.db.msgSaveDataDao().getPageMessage(1, 1)
            debugLog("数据量", msgSaveRows.size.toString())
        }).start()
//        val msgRows = msgSaveRows.map {
//            ChatMessage.createFromString(it.msgData)!!
//        }
//        mChatRows.addAll(msgRows)
    }

    private fun initRecyclerView() {
        loadLocalMsgData()
        val messageDisposable = ChatMessageBus.obsOnMainThread {
            val size = mChatRows.size
            mChatRows.add(it)
            mChatAdapter?.notifyItemRangeChanged(size, 1)
            chat_recycler_view.scrollToPosition(size)
        }
        addDisposableToCompositeDisposable(messageDisposable)
        mChatAdapter = mChatAdapter ?: ChatAdapter(mChatRows, "cool1024")
        chat_recycler_view.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = mChatAdapter
        }
    }

    private fun initMsgAction() {
        msg_edit.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                if (v.text.isNotEmpty()) {
                    val msg = ChatMessage.MessageData(
                        fromUid = "cool1024",
                        toUid = "cool1024",
                        type = "TEXT",
                        content = v.text.toString()
                    )
                    v.text = ""
                    v.clearFocus()
                    MessageSendWorker.send(context!!, "cool1024", Gson().toJson(msg))
                } else {
                    showToast("不能发空消息哦～")
                }
            }
            true
        }
        btn_send.setOnClickListener { msg_edit.onEditorAction(EditorInfo.IME_ACTION_SEND) }
    }
}