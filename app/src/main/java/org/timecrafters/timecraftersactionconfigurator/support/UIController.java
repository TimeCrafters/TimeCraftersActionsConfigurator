package org.timecrafters.timecraftersactionconfigurator.support;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.util.Log;

import org.timecrafters.timecraftersactionconfigurator.EditActivity;
import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.R;

import java.util.TimerTask;

public class UIController extends TimerTask {
  private static final String TAG = "TACNET|UIController";
  private boolean shownClientConnected = false;

  @Override
  public void run() {
    if (AppSync.getMainActivity() != null) {
      if (AppSync.getServer() != null) {
        controlServerMode();

      } else if (AppSync.getConnection() != null) {
        controlConnectionMode();

      } else {
        relinquishControl();
      }
    }
  }

  private void controlServerMode() {
    if (!AppSync.getServer().isClosed()) {
      if (AppSync.getServer().hasActiveClient()) {
        setupActionBarColor(AppSync.getMainActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorServerActiveClient));

      } else {
        setupActionBarColor(AppSync.getMainActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorServerActive));
      }

      AppSync.getMainActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          AppSync.getMainActivity().updateServerDataLayout();
        }
      });
    }
  }

  private void controlConnectionMode() {
    if (!AppSync.getConnection().isClosed()) {
      if (AppSync.getConnection().getClient().getPacketsReceived() > 1) {
        setupActionBarColor(AppSync.getMainActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorConnectionActive));

        if (AppSync.getEditActivity() != null) {
          setupActionBarColor(AppSync.getEditActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorConnectionActive));
        }

        if (!shownClientConnected) {
          AppSync.getMainActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              AppSync.getMainActivity().connectionConnected();
            }
          });
        }

        shownClientConnected = true;

      } else {
        setupActionBarColor(AppSync.getMainActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorConnectionPending));

        if (AppSync.getEditActivity() != null) {
          setupActionBarColor(AppSync.getEditActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorConnectionPending));
        }
      }
    } else {

      AppSync.getMainActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          AppSync.getMainActivity().connectionDisconnected();
        }
      });

    }
  }

  private void relinquishControl() {
    shownClientConnected = false;

    setupActionBarColor(AppSync.getMainActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorPrimary));
    if (AppSync.getEditActivity() != null) {
      setupActionBarColor(AppSync.getEditActivity(), AppSync.getMainActivity().getResources().getColor(R.color.colorPrimary));
    }
  }

  private void setupActionBarColor(final MainActivity activity, final int color) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
      }
    });
  }

  private void setupActionBarColor(final EditActivity activity, final int color) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        activity.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));
      }
    });
  }
}
