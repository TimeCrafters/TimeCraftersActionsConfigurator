package org.timecrafters.timecraftersactionconfigurator.support;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.util.Log;

import org.timecrafters.timecraftersactionconfigurator.R;

import java.util.TimerTask;

public class UIController extends TimerTask {
  private static final String TAG = "TACNET|UIController";
  private boolean shownClientConnected = false;
  private ActionBar actionBar;

  @Override
  public void run() {
    if (AppSync.getMainActivity() != null) {
      actionBar = AppSync.getMainActivity().getSupportActionBar();

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
        setupActionBarColor(AppSync.getMainActivity().getResources().getColor(R.color.colorServerActiveClient));

      } else {
        setupActionBarColor(AppSync.getMainActivity().getResources().getColor(R.color.colorServerActive));
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
      if (AppSync.getConnection().hasConnected()) {
        setupActionBarColor(AppSync.getMainActivity().getResources().getColor(R.color.colorConnectionActive));

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
        setupActionBarColor(AppSync.getMainActivity().getResources().getColor(R.color.colorConnectionPending));
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

    setupActionBarColor(AppSync.getMainActivity().getResources().getColor(R.color.colorPrimary));
  }

  private void setupActionBarColor(final int color) {
    AppSync.getMainActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        actionBar.setBackgroundDrawable(new ColorDrawable(color));
      }
    });
  }
}
