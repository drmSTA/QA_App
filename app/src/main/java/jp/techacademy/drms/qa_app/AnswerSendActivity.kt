package jp.techacademy.drms.qa_app

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_answer_send.*
import java.util.HashMap

class AnswerSendActivity : AppCompatActivity(){

    private val firebaseDatabase = FirebaseDatabase.getInstance().reference

    private lateinit var question: Question
    private lateinit var inputMethodManager: InputMethodManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initializeProperties()
        initializeUI()
    }

    private fun initializeProperties(){
        question = intent.extras.get(KEY_QUESTION) as Question
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initializeUI(){
        setContentView(R.layout.activity_answer_send)
        sendButton.setOnClickListener(ClickListener4SendButton())
    }

    inner class ClickListener4SendButton: View.OnClickListener{
        override fun onClick(view: View) {
            UiUtility.hideWindowKeyboard(view, inputMethodManager)

            // handling on "answer" input
            val answer = answerEditText.text.toString()
            when(answer.isEmpty()){
                true -> UiUtility.showSnackbar(view, "回答を入力して下さい")
                else -> pushAnserIntoDB(view, answer)
            }
        }
    }

    private fun pushAnserIntoDB(view: View, answer: String){
        val answerInDB = firebaseDatabase.child(PATH_CONTENTS).child(question.genre.toString()).child(question.questionUid).child(PATH_ANSWERS)

        // make contents which put into DB
        val data = HashMap<String, String>()
        data[KEY_UID] = FirebaseAuth.getInstance().currentUser!!.uid
        data[KEY_NAME] = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_NAME, "")?: """"""
        data[KEY_BODY] = answer

        progressBar.visibility = View.VISIBLE
        answerInDB.push().setValue(data, CompletionListener4Database(view))
    }

    inner class CompletionListener4Database (view: View) : DatabaseReference.CompletionListener{
        private val handlingView: View = view

        override fun onComplete(databaseError: DatabaseError?, databaseReference: DatabaseReference) {
            // this method will be called when accomplished DB handling to verify errors
            progressBar.visibility = View.GONE
            when(databaseError){
                null -> finish()
                else -> UiUtility.showSnackbar(this.handlingView, "投稿に失敗しました")
            }
        }
    }

}
