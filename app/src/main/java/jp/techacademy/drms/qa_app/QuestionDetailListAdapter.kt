package jp.techacademy.drms.qa_app


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.list_question_detail.view.*

class QuestionDetailListAdapter(context: Context, private val question: Question) : BaseAdapter() {

    private var firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference

    private var favoriteType: TypeFavorite = TypeFavorite.INVALID
    private var layoutInflater: LayoutInflater? = null

    init {
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + question.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return question
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var currentView = when(getItemViewType(position)){
            TypeContent.QUESTION.intValue -> makeView4Question(position, convertView, parent)
            TypeContent.ANSWER.intValue   -> makeView4Answer(position, convertView, parent)
            else                          -> makeView4Answer(position, convertView, parent)
        }
        return currentView
    }

    private fun makeView4Question(position: Int, view: View?, parent: ViewGroup): View{
        val currentView: View = view?: layoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
        currentView.favoriteButton.setOnClickListener(ClickListener4FavoriteButton())

        applyFavoriteTypeInDBToUI(currentView)

        val bodyTextView = currentView.findViewById<View>(R.id.bodyTextView) as TextView
        bodyTextView.text = question.body
        val nameTextView = currentView.findViewById<View>(R.id.nameTextView) as TextView
        nameTextView.text = question.name

        val bytes = question.imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = currentView.findViewById<View>(R.id.imageView) as ImageView
            imageView.setImageBitmap(image)
        }

        return currentView
    }

    private fun makeView4Answer(position: Int, view: View?, parent: ViewGroup): View{
        val currentView = view?: layoutInflater!!.inflate(R.layout.list_answer, parent, false)!!

        val answer = question.answers[position - 1]

        val bodyTextView = currentView.findViewById<View>(R.id.bodyTextView) as TextView
        bodyTextView.text = answer.body

        val nameTextView = currentView.findViewById<View>(R.id.nameTextView) as TextView
        nameTextView.text = answer.name

        return currentView
    }

    inner class ClickListener4FavoriteButton: View.OnClickListener{
        override fun onClick(view: View) {
            favoriteType = switchFavorite()
            applyFavoriteSwitchToUI(view)
            applyFavoriteSwitchToDB(view)
        }
    }

    private fun switchFavorite(): TypeFavorite{
        // perform favorite switching when button clicked
        when(favoriteType) {
            TypeFavorite.DEFAULT -> return TypeFavorite.FAVORITE
            TypeFavorite.FAVORITE -> return TypeFavorite.DEFAULT
            else -> return TypeFavorite.INVALID
        }
    }

    private fun applyFavoriteSwitchToDB(handlingView: View){
        when(favoriteType){
            TypeFavorite.DEFAULT-> removeFavoriteFromDB(handlingView)
            TypeFavorite.FAVORITE-> putFavoriteIntoDB(handlingView)
            else -> {}
        }
    }

    private fun putFavoriteIntoDB(handlingView: View){
        val data : HashMap<String, String> = HashMap<String, String>()
        data[KEY_QUESTION_UID] = question.questionUid
        data[KEY_GENRE] = question.genre.toString()

        val uid : String = FirebaseAuth.getInstance().currentUser!!.uid
        val handlingItemInDB = firebaseDatabase.child(PATH_FAVORITES).child(uid).child(question.questionUid)

        handlingItemInDB.push().setValue(data) { databaseError, databaseReference ->
            when(databaseError){
                null -> {}
                else -> UiUtility.showSnackbar(handlingView, "お気に入りの更新に失敗しました")
            }
        }
    }

    private fun removeFavoriteFromDB(handlingView: View){
        val uid : String = FirebaseAuth.getInstance().currentUser!!.uid
        val handlingItemInDB = firebaseDatabase.child(PATH_FAVORITES).child(uid).child(question.questionUid)

        handlingItemInDB.removeValue(){ databaseError, databaseReference ->
            when(databaseError){
                null -> {}
                else -> UiUtility.showSnackbar(handlingView, "お気に入りの更新に失敗しました")
            }
        }
    }

    private fun applyFavoriteTypeInDBToUI(currentView : View) {
        val user = FirebaseAuth.getInstance().currentUser
        when(user){
            null -> {applyFavoriteSwitchToUI(currentView)}
            else -> {
                val handlingItemInDB = firebaseDatabase.child(PATH_FAVORITES).child(user.uid).child(question.questionUid)

                handlingItemInDB.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var data  = snapshot.value as Map<String, *>?
                        // if there is data in DB => register as favorite, no data in DB => default
                        if(data != null )   favoriteType = TypeFavorite.FAVORITE
                        else                favoriteType = TypeFavorite.DEFAULT
                        applyFavoriteSwitchToUI(currentView)
                    }
                    override fun onCancelled(firebaseError: DatabaseError) {}
                })
            }
        }
    }

    private fun applyFavoriteSwitchToUI(handlingView: View){
        var button = handlingView.favoriteButton!!

        when(favoriteType){
            TypeFavorite.INVALID-> handlingView.favoriteButton.visibility = View.GONE
            TypeFavorite.DEFAULT->{
                button.text = TEXT_ADD_TO_FAVORITE
                button.setBackgroundColor(handlingView.resources.getColor(R.color.colorButtonInactivate))
            }
            TypeFavorite.FAVORITE->{
                button.text = TEXT_THIS_IS_FAVORITE
                button.setBackgroundColor(handlingView.resources.getColor(R.color.colorButtonActivate))
            }
        }
    }

}