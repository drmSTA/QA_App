package jp.techacademy.drms.qa_app

enum  class TypeLogin{
    NEW_USER, CURRENT_USER
}

enum class TypeGenre(val string:String){
    // この実装では、activity_main_drawer.xml, enum, onNavigationItemSelected 関数内 及び
    // firebase 内の階層と整合性を確認する必要がある
    // genre の項目は いろいろなところにハードコーティングするのではなく、何らかの形で一元管理し、
    // 動的にmenuの項目や分岐を実装する方ほうが好ましいと考える
    FAVORITE("0"),
    HOBBY("1"),
    LIFE("2"),
    HEALTH("3"),
    COMPUTER("4")
}

enum class TypeContent(val intValue:Int){
    QUESTION(0),
    ANSWER(1)
}

// path in firebase
const val PATH_USERS = "users"       // Firebaseにユーザの表示名を保存するパス
const val PATH_CONTENTS = "contents" // Firebaseに質問を保存するバス
const val PATH_ANSWERS = "answers"   // Firebaseに解答を保存するパス
const val PATH_FAVORITES = "favorites"   // Firebaseにお気に入りを保存するパス


// keys used in HashMap, intent etc
const val KEY_QUESTION  = "question"
const val KEY_UID       = "uid"
const val KEY_NAME = "name"
const val KEY_BODY = "body"
const val KEY_GENRE = "genre"
const val KEY_TITLE = "title"
const val KEY_IMAGE = "image"
const val KEY_ANSWER = "answer"
const val KEY_QUESTION_UID = "questionUid"
const val KEY_ANSWER_UID = "answerUid"


// titles
const val TITLE_LOGIN = "ログイン"
const val TITLE_MAKE_QUESTION = "質問作成"
const val TITLE_SETTING = "設定"
const val TITLE_FAVORITE = "お気に入り"


// text for favorite expression
const val TEXT_THIS_IS_FAVORITE = "お気に入り:ON"
const val TEXT_ADD_TO_FAVORITE  = "お気に入り:OFF"


const val DEFAULT_GENRE_STRING = "1"

