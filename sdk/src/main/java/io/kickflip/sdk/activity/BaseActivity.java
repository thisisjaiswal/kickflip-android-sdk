package io.kickflip.sdk.activity;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

public abstract class BaseActivity extends Activity {

    protected static final String TAG = "KICKFLIP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout());
        setUi();
        init();
        populate();
        setListeners();
    }

    protected abstract int layout();

    protected abstract void setUi();

    protected abstract void setListeners();

    protected abstract void populate();

    protected abstract void init();

    protected void showToast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    protected void showToast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    protected void replaceFragment(int resId, Fragment f) {
        getFragmentManager()
                .beginTransaction()
                .replace(resId, f)
                .commit();
    }

    protected void replaceFragment(int resId, Fragment f, String tag) {
        getFragmentManager()
                .beginTransaction()
                .replace(resId, f, tag)
                .commit();
    }

    public void addFragment(int resId, Fragment f) {
        getFragmentManager()
                .beginTransaction()
                .add(resId, f)
                .commit();
    }

    public void addFragment(int resId, Fragment f, String tag) {
        getFragmentManager()
                .beginTransaction()
                .add(resId, f, tag)
                .commit();
    }

    public void removeFragment(Fragment f) {
        getFragmentManager()
                .beginTransaction()
                .remove(f)
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}