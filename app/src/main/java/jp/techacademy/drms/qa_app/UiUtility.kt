package jp.techacademy.drms.qa_app

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.view.inputmethod.InputMethodManager

class UiUtility {

    companion object {
        public fun showSnackbar(view: View, text: String){
            Snackbar.make(view, text, Snackbar.LENGTH_LONG).show()
        }


        public fun hideWindowKeyboard(view: View, inputMedhodManager: InputMethodManager){
            inputMedhodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
       }

    }
}