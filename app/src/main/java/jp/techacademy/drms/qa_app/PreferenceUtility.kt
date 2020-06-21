package jp.techacademy.drms.qa_app

import android.content.Context
import android.preference.PreferenceManager

public fun getGenre(context: Context): TypeGenre{
    val genreString = PreferenceManager.getDefaultSharedPreferences(context).getString(KEY_GENRE, DEFAULT_GENRE_STRING) ?: DEFAULT_GENRE_STRING
    val typeGenre = when (genreString) {
        TypeGenre.HOBBY.string -> TypeGenre.HOBBY
        TypeGenre.LIFE.string -> TypeGenre.LIFE
        TypeGenre.HEALTH.string -> TypeGenre.HEALTH
        TypeGenre.COMPUTER.string -> TypeGenre.COMPUTER
        else -> TypeGenre.HOBBY              // default setting for unexpected case
    }
    return typeGenre
}

public fun setGenre(context: Context, genre: String){
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    editor.putString(KEY_GENRE, genre)
    editor.apply()
}