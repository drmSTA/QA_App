package jp.techacademy.drms.qa_app

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.database.DatabaseReference
import com.google.firebase.auth.FirebaseAuth
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private var firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    private lateinit var inputMethodManager: InputMethodManager
    private lateinit var view: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        initializeProperties()
        initializeUI()
    }

    private fun initializeProperties(){
        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view = findViewById<View>(android.R.id.content)
    }

    private fun initializeUI(){
        // UIの初期設定
        title = TITLE_SETTING

        // Preferenceから表示名を取得してEditTextに反映させる
        val name = PreferenceManager.getDefaultSharedPreferences(this).getString(KEY_NAME, "")
        nameText.setText(name)

        changeButton.setOnClickListener(ClickListener4ChangeButton())

        logoutButton.setOnClickListener(ClickListener4LogoutButton())
    }

    inner class ClickListener4ChangeButton: View.OnClickListener{
        override fun onClick(view: View) {
            UiUtility.hideWindowKeyboard(view, inputMethodManager)

            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            when(user){
                null -> UiUtility.showSnackbar(view, "ログインしていません")
                else ->{
                    val name = nameText.text.toString()
                    // 変更した表示名をPreferenceに保存する
                    setPreference(this@SettingActivity, KEY_NAME, name)

                    // 変更した表示名をFirebaseに保存する
                    val userInDB = firebaseDatabase.child(PATH_USERS).child(user.uid)
                    val data = HashMap<String, String>()
                    data[KEY_NAME] = name
                    userInDB.setValue(data)


                    UiUtility.showSnackbar(view, "表示名を変更しました")
                }
            }
        }
    }

    inner class ClickListener4LogoutButton: View.OnClickListener{
        override fun onClick(view: View) {
//            logout in firebase
            FirebaseAuth.getInstance().signOut()

//            logout in app preference
            setPreference(this@SettingActivity, KEY_NAME, null)
//            お気に入り一覧　→　ログアウト　→　メイン画面　表示の場合に、Genre選択 が favirute の場合はデフォルト値に再設定
            if(getGenre() == TypeGenre.FAVORITE) setGenre(DEFAULT_GENRE_STRING)

//            UI operation
            nameText.setText("")
            UiUtility.hideWindowKeyboard(view, inputMethodManager)
            UiUtility.showSnackbar(view, "ログアウトしました")
        }
    }


    private fun setGenre(genre: String){
        setGenre(this, genre)
    }

    private fun getGenre(): TypeGenre{
        return jp.techacademy.drms.qa_app.getGenre(this)
    }

}