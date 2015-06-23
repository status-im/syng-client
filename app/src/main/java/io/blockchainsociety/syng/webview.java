package io.blockchainsociety.syng;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;

public class webview extends BaseActivity implements ObservableScrollViewCallbacks {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.dapp);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {

        super.onPostCreate(savedInstanceState);
    }


    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onScrollChanged(int i, boolean b, boolean b1) {

    }

    @Override
    public void onDownMotionEvent() {

    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {

        if (getSupportActionBar() == null) {
            Log.d("MyTag", "Null");

            return;
        }
        if (scrollState == ScrollState.UP) {

            if (getSupportActionBar().isShowing()) {
                Log.d("MyTag", "Hide");
                getSupportActionBar().hide();

            }
        } else if (scrollState == ScrollState.DOWN) {
            if (!getSupportActionBar().isShowing()) {
                Log.d("MyTag", "Show");
                getSupportActionBar().show();
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }
}
