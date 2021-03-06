package com.nhatran241.baseandroidlib.base.activity;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ShareCompat;

import com.nhatran241.baseandroidlib.base.BaseViewModel;
import com.nhatran241.baseandroidlib.base.fragment.BaseFragment;
import com.nhatran241.baseandroidlib.custom.dialog.LoadingDialog;

import java.util.HashMap;
import java.util.Map;


public abstract class BaseActivity<T extends BaseViewModel> extends AppCompatActivity {
    private LoadingDialog loadingDialog;
    private AlertDialog alertDialog;
    private T baseViewModel;
    private Map<String, BroadcastReceiver> broadcastReceiverList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        baseViewModel = initViewModel();
        setContentView(getLayout());
        initUI();
        initData();

    }

    public void registerBroadcast(String broadcastTag, IntentFilter intentFilter) {
        if (broadcastReceiverList == null) {
            broadcastReceiverList = new HashMap<>();
        }
        if (broadcastReceiverList.get(broadcastTag) != null) {
            unregisterReceiver(broadcastReceiverList.get(broadcastTag));
        }
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onBroadcastReceiver(intent, broadcastTag);
            }
        };
        broadcastReceiverList.put(broadcastTag, broadcastReceiver);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    public void unregisterBroadcast(String broadcastTag) {
        if (broadcastReceiverList == null) {
            return;
        }
        if (broadcastReceiverList.get(broadcastTag) != null) {
            unregisterReceiver(broadcastReceiverList.get(broadcastTag));
            broadcastReceiverList.remove(broadcastTag);
        }
    }

    public void onBroadcastReceiver(Intent intent, String broadcastTag) {
    }

    public void showLoading(boolean hidePercentView) {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
            loadingDialog.hidePercentView(hidePercentView);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void showLoading() {
        if (loadingDialog == null) {
            loadingDialog = new LoadingDialog(this);
        }
        if (!loadingDialog.isShowing()) {
            loadingDialog.show();
        }
    }

    public void showMessage(String message) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Message").setMessage(message).show();
    }

    public void updateLoadingMaxProgress(int maxProgress) {
        loadingDialog.setMaxProgress(maxProgress);
    }

    public void updateLoadingProgress(int progress) {
        if (loadingDialog != null) {
            loadingDialog.updateProgress(progress);
        }
    }

    public void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    protected void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    protected void showMessageDialog(String title, String message, String positive, String negative, String neutral) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!TextUtils.isEmpty(positive)) {
            builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onMessageDialogPositiveClick(title);
                }
            });
        }
        if (!TextUtils.isEmpty(negative)) {
            builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onMessageDialogNegativeClick(title);
                }
            });
        }
        if (!TextUtils.isEmpty(neutral)) {
            builder.setNeutralButton(neutral, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    onMessageDialogNeutralClick(title);
                }
            });
        }
        builder.setTitle(title).setMessage(message);
        alertDialog = builder.create();
        try {
            alertDialog.show();
        } catch (Exception ignored) {

        }
    }

    public void onMessageDialogNegativeClick(String title) {

    }

    public void onMessageDialogPositiveClick(String title) {

    }

    public void onMessageDialogNeutralClick(String title) {

    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    protected abstract T initViewModel();


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (baseViewModel != null) {
            baseViewModel.onDestroy();
        }
        if (loadingDialog != null) {
            loadingDialog.cancel();
            loadingDialog = null;
        }
        if (alertDialog != null) {
            alertDialog.cancel();
            alertDialog = null;
        }
        if (broadcastReceiverList != null) {
            for (Map.Entry<String, BroadcastReceiver> entry : broadcastReceiverList.entrySet()) {
                unregisterReceiver(entry.getValue());
            }
            broadcastReceiverList.clear();
            broadcastReceiverList = null;
        }
    }

    public void replaceFragment(BaseFragment baseFragment, int containerId, boolean addToBackStack) {
        if (addToBackStack) {
            getSupportFragmentManager().beginTransaction().replace(containerId, baseFragment).addToBackStack(String.valueOf(getSupportFragmentManager().getBackStackEntryCount())).commit();
        } else {
            getSupportFragmentManager().beginTransaction().replace(containerId, baseFragment).commit();
        }
    }

    public void removeFragment(BaseFragment baseFragment) {
        if (baseFragment != null) {
            getSupportFragmentManager().beginTransaction().remove(baseFragment).commit();
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("nhatnhat", "onBackPressed: " + getSupportFragmentManager().getBackStackEntryCount());
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }

    public void rateApp(){
        Uri uri = Uri.parse("market://details?id=" +getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getPackageName())));
        }
    }

    public void shareApp(){
        ShareCompat.IntentBuilder.from(this)
                .setType("text/plain")
                .setChooserTitle("Share App")
                .setText("http://play.google.com/store/apps/details?id=" + getPackageName())
                .startChooser();
    }

    protected abstract void initUI();

    protected abstract void initData();

    protected abstract int getLayout();

}
