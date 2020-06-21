package jp.techacademy.drms.qa_app


data class Favorite(
    var genre: String? = "",
    var questionUid: String? = ""
){
    fun toMap(): Map<String, Any?>{
        return mapOf(
            KEY_GENRE to genre,
            KEY_QUESTION_UID to questionUid
        )
    }
}


enum class TypeFavorite{
    INVALID, DEFAULT, FAVORITE
}