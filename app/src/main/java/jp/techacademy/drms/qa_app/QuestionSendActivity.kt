package jp.techacademy.drms.qa_app

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*
import kotlinx.android.synthetic.main.activity_question_send.*

import java.io.ByteArrayOutputStream
import java.util.HashMap

class QuestionSendActivity : AppCompatActivity() {

    private var firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    //private var genre: Int = DEFAULT_GENRE_STRING
    private var pictureUri: Uri? = null

    private lateinit var inputMethodManager: InputMethodManager

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 100
        private const val CHOOSER_REQUEST_CODE = 100
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_send)

        initializeProperties()
        initializeUI()
    }

    private fun initializeProperties(){

        inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private fun initializeUI(){
        // UIの準備
        title = TITLE_MAKE_QUESTION

        sendButton.setOnClickListener(ClickListener4SendButton())
        imageView.setOnClickListener(ClickListener4ImageView())
    }

    inner class ClickListener4SendButton: View.OnClickListener{
        override fun onClick(view: View) {
            UiUtility.hideWindowKeyboard(view, inputMethodManager)

            // タイトルを取得する
            val title = titleText.text.toString()
            if (title.isEmpty()) {
                // タイトルが入力されていない時はエラーを表示するだけ
                Snackbar.make(view, "タイトルを入力して下さい", Snackbar.LENGTH_LONG).show()
                return
            }

            // 本文を取得する
            val body = bodyText.text.toString()
            if (body.isEmpty()) {
                // 質問が入力されていない時はエラーを表示するだけ
                Snackbar.make(view, "質問を入力して下さい", Snackbar.LENGTH_LONG).show()
                return
            }

            // 添付画像を取得する
            var bitmapString: String? = null
            val drawable = imageView.drawable as? BitmapDrawable
            // 添付画像が設定されていれば画像を取り出してBASE64エンコードする
            if (drawable != null) {
                val bitmap = drawable.bitmap
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos)
                bitmapString = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
            }

            val name  = PreferenceManager.getDefaultSharedPreferences(this@QuestionSendActivity).getString(KEY_NAME, "") ?: ""

            val data : HashMap<String, String> = HashMap<String, String>()
            data[KEY_UID] = FirebaseAuth.getInstance().currentUser!!.uid
            data[KEY_TITLE] = title
            data[KEY_BODY] = body
            data[KEY_NAME] = name
            if(bitmapString != null) data[KEY_IMAGE] = bitmapString

            val sendingQuestionInDB = firebaseDatabase.child(PATH_CONTENTS).child(getGenre(this@QuestionSendActivity).string)
            progressBar.visibility = View.VISIBLE

            sendingQuestionInDB.push().setValue(data) { databaseError, databaseReference ->
                progressBar.visibility = View.GONE
                when(databaseError){
                    null -> finish()
                    else -> UiUtility.showSnackbar(view, "投稿に失敗しました")
                }
            }
        }
    }

    inner class ClickListener4ImageView: View.OnClickListener{
        override fun onClick(view: View?) {
            // パーミッションの許可状態を確認する
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // 許可されている
                    showChooser()
                } else {
                    // 許可されていないので許可ダイアログを表示する
                    requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                    return
                }
            } else {
                showChooser()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if(requestCode == PERMISSIONS_REQUEST_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            showChooser()
        }
    }

    private fun showChooser() {
        // ギャラリーから選択するIntent
        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
        galleryIntent.type = "image/*"
        galleryIntent.addCategory(Intent.CATEGORY_OPENABLE)

        // カメラで撮影するIntent
        val filename = System.currentTimeMillis().toString() + ".jpg"
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, filename)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        pictureUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, pictureUri)

        // ギャラリー選択のIntentを与えてcreateChooserメソッドを呼ぶ
        val chooserIntent = Intent.createChooser(galleryIntent, "画像を取得")

        // EXTRA_INITIAL_INTENTS にカメラ撮影のIntentを追加
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))

        startActivityForResult(chooserIntent, CHOOSER_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSER_REQUEST_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                if (pictureUri != null) {
                    contentResolver.delete(pictureUri!!, null, null)
                    pictureUri = null
                }
                return
            }

            // 画像を取得
            val uri = if (data == null || data.data == null) pictureUri else data.data

            // URIからBitmapを取得する
            val image: Bitmap
            try {
                val contentResolver = contentResolver
                val inputStream = contentResolver.openInputStream(uri!!)
                image = BitmapFactory.decodeStream(inputStream)
                inputStream!!.close()
            } catch (e: Exception) {
                return
            }

            // 取得したBimapの長辺を500ピクセルにリサイズする
            val imageWidth = image.width
            val imageHeight = image.height
            val scale = Math.min(500.toFloat() / imageWidth, 500.toFloat() / imageHeight) // (1)

            val matrix = Matrix()
            matrix.postScale(scale, scale)

            val resizedImage = Bitmap.createBitmap(image, 0, 0, imageWidth, imageHeight, matrix, true)

            // BitmapをImageViewに設定する
            imageView.setImageBitmap(resizedImage)

            pictureUri = null
        }
    }
}