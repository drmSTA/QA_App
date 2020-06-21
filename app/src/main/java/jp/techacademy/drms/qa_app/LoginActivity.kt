package jp.techacademy.drms.qa_app

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*
import java.util.HashMap
import com.google.android.gms.tasks.Task


class LoginActivity : AppCompatActivity() {

    private var firebaseAutherication: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    private lateinit var inputMethodManager: InputMethodManager

    private var loginType: TypeLogin = TypeLogin.CURRENT_USER

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeProperties()
        initializeUI()
    }

    private fun initializeProperties(){
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initializeUI(){
        title = TITLE_LOGIN

        createButton.setOnClickListener(ClickListener4CreateButton())
        loginButton.setOnClickListener(ClickListener4LoginButton())
    }

    inner class ClickListener4CreateButton: View.OnClickListener{
        override fun onClick(view: View) {
            UiUtility.hideWindowKeyboard(view, inputMethodManager)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()
            val name = nameText.text.toString()

            when(StringUtility.isValid4login(email, password, name)){
                true -> {
                    // ログイン時に表示名を保存するようにフラグを立てる
                    loginType = TypeLogin.NEW_USER
                    createAccount(email, password)
                }
                else -> UiUtility.showSnackbar(view, "正しく入力してください")
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        // アカウントを作成する
        firebaseAutherication.createUserWithEmailAndPassword(email, password).addOnCompleteListener(Listener4CreateAccount())

        // perform View.GONE by callback in OnCompleteListener
    }

    inner class Listener4CreateAccount: OnCompleteListener<AuthResult>{
        override fun onComplete(task: Task<AuthResult>) {
            progressBar.visibility = View.GONE
            val view = findViewById<View>(android.R.id.content)

            when(task.isSuccessful){
                false -> UiUtility.showSnackbar(view, "アカウント作成に失敗しました")
                true -> {
                    // 成功した場合ログインを行う
                    val email = emailText.text.toString()
                    val password = passwordText.text.toString()
                    performLogin(email, password)
                }
            }
        }
    }

    inner class ClickListener4LoginButton: View.OnClickListener{
        override fun onClick(view: View) {
            UiUtility.hideWindowKeyboard(view, inputMethodManager)

            val email = emailText.text.toString()
            val password = passwordText.text.toString()

            when(StringUtility.isValid4login(email, password, " ")){
                true ->{
                    loginType = TypeLogin.CURRENT_USER
                    performLogin(email, password)
                }
                else -> UiUtility.showSnackbar(view, "正しく入力してください")
            }
        }
    }

    private fun performLogin(email: String, password: String) {
        progressBar.visibility = View.VISIBLE

        // ログインする
        firebaseAutherication.signInWithEmailAndPassword(email, password).addOnCompleteListener(Listener4Login())
    }

    inner class Listener4Login: OnCompleteListener<AuthResult>{
        override fun onComplete(task: Task<AuthResult>) {
            progressBar.visibility = View.GONE
            val view = findViewById<View>(android.R.id.content)

            when (task.isSuccessful){
                false -> UiUtility.showSnackbar(view, "ログインに失敗しました")
                true ->{
                    val user = firebaseAutherication.currentUser
                    val handlingUserInformationInDB = firebaseDatabase.child(PATH_USERS).child(user!!.uid)
                    var data: HashMap<String, String> = HashMap<String, String>()

                    when(loginType){
                        TypeLogin.NEW_USER ->{
                            // アカウント作成の時は表示名をFirebaseに保存する
                            data[KEY_NAME] = nameText.text.toString()
                            handlingUserInformationInDB.setValue(data)
                            // 表示名をPrefarenceに保存する
                            setCurrentUserName2Preference(data!![KEY_NAME] as String)
                        }

                        TypeLogin.CURRENT_USER ->{
                            handlingUserInformationInDB.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    data = snapshot.value as HashMap<String, String>
                                    // 表示名をPrefarenceに保存する
                                    setCurrentUserName2Preference(data!![KEY_NAME] as String)
                                }
                                override fun onCancelled(firebaseError: DatabaseError) {}
                            })
                        }
                    }

                    // Activityを閉じる
                    finish()
                }
            }
        }
    }

    private fun setCurrentUserName2Preference(name: String) {
        // Preferenceに保存する
        val editor = PreferenceManager.getDefaultSharedPreferences(this).edit()
        editor.putString(KEY_NAME, name)
        editor.apply()
    }


}