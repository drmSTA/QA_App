package jp.techacademy.drms.qa_app

import android.content.Context
import android.preference.PreferenceManager

/*
お気に入り一覧の表示方法が 他のジャンル選択・表示機能と類似していたため、
お気に入りをジャンルの一つとして実装し、機能の多くを転用した。
この為、genre の取扱を下記の通りに変更した
　①genre が intent 間で共有できるよう PreferenceManager 上にて管理するよう変更した
　②お気に入り一覧　→　ログアウト　→　メイン画面表示　の画面推移で
　　メイン画面を再表示する際に、お気に入り一覧ではなく default 値で指定された genre を
　　表示するようにした
各コードの中で呼び出し処理を毎回記述するのでは可読性の低下を感じたため、
Utilityシリーズの一つとして切り出したのがこのクラスである
 */


public fun getPreference(context: Context, key: String): String?{
    return PreferenceManager.getDefaultSharedPreferences(context).getString(key, "")
}


public fun setPreference(context: Context, key: String, value: String?){
    val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
    editor.putString(key, value)
    editor.apply()
}

public fun getGenre(context: Context): TypeGenre{
    val genreString = getPreference(context, KEY_GENRE) ?: DEFAULT_GENRE_STRING
    val typeGenre = when (genreString) {
        TypeGenre.FAVORITE.string -> TypeGenre.FAVORITE
        TypeGenre.HOBBY.string -> TypeGenre.HOBBY
        TypeGenre.LIFE.string -> TypeGenre.LIFE
        TypeGenre.HEALTH.string -> TypeGenre.HEALTH
        TypeGenre.COMPUTER.string -> TypeGenre.COMPUTER
        else -> TypeGenre.HOBBY              // default setting for unexpected case
    }
    return typeGenre
}

public fun setGenre(context: Context, genre: String){
    setPreference(context, KEY_GENRE, genre)
}
