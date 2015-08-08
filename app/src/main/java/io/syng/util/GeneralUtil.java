package io.syng.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public final class GeneralUtil {

    public static void hideKeyBoard(View view, Context context) {
        if (view == null)
            return;
        InputMethodManager imm = (InputMethodManager)
                context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
