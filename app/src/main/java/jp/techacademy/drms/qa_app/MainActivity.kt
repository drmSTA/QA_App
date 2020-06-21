package jp.techacademy.drms.qa_app


import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.ListView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.app_bar_main.*


class MainActivity : AppCompatActivity() {

    //private lateinit var genreType : TypeGenre // default selection of genre is defined in resume()

    private val firebaseDatabase: DatabaseReference = FirebaseDatabase.getInstance().reference
    private lateinit var currentWatchingNodeInDB : DatabaseReference
    private lateinit var eventListener4currentWatchingNode: ChildEventListener
    private lateinit var listener4navigationItemSelection:  Listener4NavigationItemSelection
    private lateinit var questionsListAdapter: QuestionsListAdapter

    private lateinit var toolbar: Toolbar
    private lateinit var listView: ListView
    private lateinit var navigationView: NavigationView
    private var questionList: ArrayList<Question> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeProperties()
        initializeUI()
    }

    private fun initializeProperties(){
        setGenre(DEFAULT_GENRE_STRING)

        currentWatchingNodeInDB = firebaseDatabase.child(PATH_CONTENTS).child(getGenre().string)
        eventListener4currentWatchingNode = EventListener4GenreInDB()
        listener4navigationItemSelection   = Listener4NavigationItemSelection()
        currentWatchingNodeInDB.addChildEventListener(eventListener4currentWatchingNode)

        questionsListAdapter = QuestionsListAdapter(this)
        questionsListAdapter.notifyDataSetChanged()

    }

    private fun setGenre(genre: String){
        setGenre(this, genre)
    }

    private fun getGenre(): TypeGenre{
        return getGenre(this)
    }


    private fun initializeUI(){
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // ナビゲーションドロワーの設定
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.app_name, R.string.app_name)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(listener4navigationItemSelection)

        // ListViewの準備
        listView = findViewById(R.id.listView)
        listView.setOnItemClickListener { _, _, position, _ ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra(KEY_QUESTION, questionList[position])
            startActivity(intent)
        }

        fab.setOnClickListener (ClickListener4FloatingActionButton())
        applyUserLoginStatusToNavigationView()

    }

    override fun onResume() {
        super.onResume()
        applyUserLoginStatusToNavigationView()
    }

    private fun applyUserLoginStatusToNavigationView(){
//        お気に入り一覧の表示切り替え（Loginなし＝非表示、Login＝表示）
        when(FirebaseAuth.getInstance().currentUser){
            null -> navigationView.menu.findItem(R.id.nav_favorite).isVisible = false
            else -> navigationView.menu.findItem(R.id.nav_favorite).isVisible = true
        }
//         menu.getItemで指定する番号は、TypeGenre(enum) と activity_main_drawer.xml で指定一致させている
        listener4navigationItemSelection.onNavigationItemSelected(navigationView.menu.getItem(getGenre().string.toInt()))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    inner class ClickListener4FloatingActionButton: View.OnClickListener{
        override fun onClick(view: View?) {
            // ログインしていなければログイン画面に遷移, ログイン中であればジャンルを渡して質問作成画面を起動する
            lateinit var intent: Intent
            when(FirebaseAuth.getInstance().currentUser){
                null -> intent = Intent(applicationContext, LoginActivity::class.java)
                else -> intent = Intent(applicationContext, QuestionSendActivity::class.java)
            }
            startActivity(intent)
        }
    }

    inner class Listener4NavigationItemSelection: NavigationView.OnNavigationItemSelectedListener{
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when(item.itemId){
                R.id.nav_favorite   -> setTitleAndGenreType("お気に入り", TypeGenre.FAVORITE)
                R.id.nav_hobby      -> setTitleAndGenreType("趣味", TypeGenre.HOBBY)
                R.id.nav_life       -> setTitleAndGenreType("生活", TypeGenre.LIFE)
                R.id.nav_health     -> setTitleAndGenreType("健康", TypeGenre.HEALTH)
                R.id.nav_compter    -> setTitleAndGenreType("コンピューター", TypeGenre.COMPUTER)
            }

            val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
            drawer.closeDrawer(GravityCompat.START)

            // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            questionList.clear()
            questionsListAdapter.setQuestionArrayList(questionList)
            listView.adapter = questionsListAdapter

            // 選択していたジャンルのリスナーを取り除き、新たに選択したノードのリスナーを
            // 再インスタンス化し、新たにリスナーへ登録する
            currentWatchingNodeInDB.removeEventListener(eventListener4currentWatchingNode)
            when (getGenre()){
                TypeGenre.FAVORITE ->{
                    val uid : String = FirebaseAuth.getInstance().currentUser!!.uid
                    currentWatchingNodeInDB = firebaseDatabase.child(PATH_FAVORITES).child(uid)
                    eventListener4currentWatchingNode = EventListener4FavoriteInDB()

//                    お気に入り一覧の機能をジャンルの一つとして実装したため
//                    genre が お気に入りのときには質問を追加させないように fab を非表示にしている
                    fab.visibility = FloatingActionButton.INVISIBLE
                }
                else ->{
                    currentWatchingNodeInDB = firebaseDatabase.child(PATH_CONTENTS).child(getGenre().string)
                    eventListener4currentWatchingNode = EventListener4GenreInDB()

                    fab.visibility = FloatingActionButton.VISIBLE
                    fab.setOnClickListener(ClickListener4FloatingActionButton())

                }
            }
            currentWatchingNodeInDB.addChildEventListener(eventListener4currentWatchingNode)

            return true
        }

        private fun setTitleAndGenreType(title: String, genreType: TypeGenre){
            toolbar.title = title
            setGenre(genreType.string)
        }
    }

    inner class EventListener4GenreInDB : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, string: String?) {
            updateQuestionList(dataSnapshot)
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            @Suppress("UNCHECKED_CAST") val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in questionList) {
                if (dataSnapshot.key.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.answers.clear()
                    @Suppress("UNCHECKED_CAST") val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            @Suppress("UNCHECKED_CAST") val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp[KEY_BODY] ?: ""
                            val answerName = temp[KEY_NAME] ?: ""
                            val answerUid = temp[KEY_UID] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                    questionsListAdapter.notifyDataSetChanged()
                }
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {        }
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {        }
        override fun onCancelled(p0: DatabaseError) {        }
    }

    inner class EventListener4FavoriteInDB : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, string: String?) {
            updateQuestionListInFavoriteItems(dataSnapshot)
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
            updateQuestionListInFavoriteItems(dataSnapshot)
        }

        private fun updateQuestionListInFavoriteItems(dataSnapshot: DataSnapshot){
            @Suppress("UNCHECKED_CAST") val map = dataSnapshot.value as Map<String, String>
            lateinit var dataInMap: Map<String, String>
//             Favorite 一覧のデータを処理する部分 firebase 内に保存した question_uid と genre 番号を取得
            for(key in map.keys){
                @Suppress("UNCHECKED_CAST") dataInMap = map[key] as Map<String, String>
                val genreIntVal = dataInMap[KEY_GENRE].toString()
                val questionUid = dataInMap[KEY_QUESTION_UID].toString()

//                firebase 内の階層 content - genre - question_uid を参照し,
//                取得したQuestionより, questionList の更新を行っている
//                従来の genre 系列の処理と共通する処理なので、DataSnapshot を引数にした関数に切り出している (updateQuestionList(dataSnapshot)
                val handlingQuestionInDB = firebaseDatabase.child(PATH_CONTENTS).child(genreIntVal!!).child(questionUid!!)
                handlingQuestionInDB.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        updateQuestionList(dataSnapshot)
                    }

                    override fun onCancelled(firebaseError: DatabaseError) {}
                })
            }

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
        override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
        override fun onCancelled(p0: DatabaseError) {}
    }

    private fun updateQuestionList(dataSnapshot: DataSnapshot){
        @Suppress("UNCHECKED_CAST") val map = dataSnapshot.value as Map<String, String>

        val title = map[KEY_TITLE] ?: ""
        val body = map[KEY_BODY] ?: ""
        val name = map[KEY_NAME] ?: ""
        val uid = map[KEY_UID] ?: ""
        val imageString = map[KEY_IMAGE] ?: ""
        val bytes: ByteArray = makeImageBytes(imageString)
        val answerArrayList = ArrayList<Answer>()
        @Suppress("UNCHECKED_CAST") val answerMap = map[KEY_ANSWER] as Map<String, String>?
        if (answerMap != null) {
            for (key in answerMap.keys) {
                @Suppress("UNCHECKED_CAST") val temp = answerMap[key] as Map<String, String>
                val answerBody = temp[KEY_BODY] ?: ""
                val answerName = temp[KEY_NAME] ?: ""
                val answerUid = temp[KEY_UID] ?: ""
                answerArrayList.add(Answer(answerBody, answerName, answerUid, key))
            }
        }
        val question = Question(title, body, name, uid, dataSnapshot.key ?: "",  getGenre().string, bytes, answerArrayList)
        questionList.add(question)
        questionsListAdapter.notifyDataSetChanged()
    }

    private fun makeImageBytes(imageString: String) : ByteArray{
        if(imageString.isNotEmpty()) {
            return Base64.decode(imageString, Base64.DEFAULT)
        }else {
            return byteArrayOf()
        }
    }

}