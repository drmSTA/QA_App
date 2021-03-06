package jp.techacademy.drms.qa_app

/*
入力フォームの validation クラスとして作成
 */

class StringUtility {

    companion object {
        public fun isValidFormat4login(email: String, password: String, name: String): Boolean {
            val LOWER_LENGTH_FOR_PASSWORD: Int = 6
            return if (email.length > 0 && password.length >= LOWER_LENGTH_FOR_PASSWORD && name.length > 0) true else false
        }
    }
}