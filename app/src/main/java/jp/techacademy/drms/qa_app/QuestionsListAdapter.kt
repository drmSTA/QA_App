package jp.techacademy.drms.qa_app


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList

class QuestionsListAdapter(context: Context) : BaseAdapter() {
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private var questionArrayList = ArrayList<Question>()

    override fun getCount(): Int {
        return questionArrayList.size
    }

    override fun getItem(position: Int): Any {
        return questionArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val returnView = convertView ?: layoutInflater.inflate(R.layout.list_questions, parent, false)

        val titleText = returnView!!.findViewById<View>(R.id.titleTextView) as TextView
        titleText.text = questionArrayList[position].title

        val nameText = returnView.findViewById<View>(R.id.nameTextView) as TextView
        nameText.text = questionArrayList[position].name

        val resText = returnView.findViewById<View>(R.id.resTextView) as TextView
        val resNum = questionArrayList[position].answers.size
        resText.text = resNum.toString()

        val bytes = questionArrayList[position].imageBytes
        if (bytes.isNotEmpty()) {
            val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
            val imageView = returnView.findViewById<View>(R.id.imageView) as ImageView
            imageView.setImageBitmap(image)
        }

        return returnView
    }

    fun setQuestionArrayList(questionArrayList: ArrayList<Question>) {
        this.questionArrayList = questionArrayList
    }
}