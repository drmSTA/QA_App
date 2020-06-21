package jp.techacademy.drms.qa_app


import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.list_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {

    private var firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    private lateinit var question: Question
    private lateinit var questionDetailListAdapter: QuestionDetailListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        initializeProperties()
        initializeUI()
    }

    private fun initializeProperties(){
        // 渡ってきたQuestionのオブジェクトを保持する
        question = intent.extras!!.get(KEY_QUESTION) as Question
    }

    private fun initializeUI(){
        title = question.title

        // ListViewの準備
        questionDetailListAdapter = QuestionDetailListAdapter(this, question)
        listView.adapter = questionDetailListAdapter
        questionDetailListAdapter.notifyDataSetChanged()

        val firebaseConnection2Answer = firebaseDatabase.child(PATH_CONTENTS).child(question.genre.toString()).child(question.questionUid).child(PATH_ANSWERS)
        firebaseConnection2Answer.addChildEventListener(EventListener4AnswerInDB())
    }


    inner class ClickListener4FloatingActionButton: View.OnClickListener{
        override fun onClick(view: View?) {
            // ログインしていなければログイン画面に遷移, ログイン中であればQuestionを渡して回答作成画面を起動する
            lateinit var intent: Intent
            when(FirebaseAuth.getInstance().currentUser){
                null -> intent = Intent(applicationContext, LoginActivity::class.java)
                else ->{
                    intent = Intent(applicationContext, AnswerSendActivity::class.java)
                    intent.putExtra(KEY_QUESTION, question)
                }
            }
            startActivity(intent)
        }
    }

    inner class EventListener4AnswerInDB: ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, string: String?) {
            @Suppress("UNCHECKED_CAST") val map = dataSnapshot.value as HashMap<String, String>

            val body = map[KEY_BODY] ?: ""
            val name = map[KEY_NAME] ?: ""
            val uid = map[KEY_UID] ?: ""
            val answerUid = dataSnapshot.key ?: ""
            for (answer in question.answers) {
                // 同じAnswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) return
            }

            question.answers.add(Answer(body, name, uid, answerUid))
            questionDetailListAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {   }
        override fun onChildRemoved(dataSnapshot: DataSnapshot) {        }
        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {        }
        override fun onCancelled(databaseError: DatabaseError) {    }
    }
}