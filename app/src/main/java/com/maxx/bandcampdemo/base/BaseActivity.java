package com.maxx.bandcampdemo.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import godau.fynn.bandcampdirect.R;

public abstract class BaseActivity extends AppCompatActivity {

    protected Context mContext;
    protected ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(getLayoutResourceId());
    }

    protected abstract int getLayoutResourceId();

    public void noInternetConnectionDialog() {
        showDialog(getString(R.string.no_internet_message));
    }

    public void showDialog(String message) {
        try {
            new MaterialDialog.Builder(mContext).content(message)
                    .positiveText(getString(R.string.ok))
                    .positiveColor(ContextCompat.getColor(this, R.color.black))
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showProgressDialog() {
        try {
            pDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyleNew);
            pDialog.setCancelable(false);
            pDialog.setMessage("Please wait...");
            pDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissProgressDialog() {
        try {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
