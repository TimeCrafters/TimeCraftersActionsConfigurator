package org.timecrafters.timecraftersactionconfigurator;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.timecrafters.timecraftersactionconfigurator.actionSupport.AddActionDialog;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Reader;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Writer;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;
import org.timecrafters.timecraftersactionconfigurator.server.Connection;
import org.timecrafters.timecraftersactionconfigurator.server.Server;
import org.timecrafters.timecraftersactionconfigurator.support.AppSync;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_WRITE_PERMISSION = 70;

  private Switch toggleSwitch;
  public LinearLayout primaryLayout;

  public DataStruct currentDataStruct = null;
  public Switch currentActionName;

  private Menu menu;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    if (AppSync.instance == null) {
      new AppSync();
    }

    AppSync.instance.mainActivity = this;

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton addActionButton = findViewById(R.id.fab);

    addActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (AppSync.instance.serverEnabled) {
          Snackbar.make(view, "Can not add Actions while server is running!", Snackbar.LENGTH_LONG).show();

        } else if (!AppSync.instance.allowDestructiveEditing) {
          Snackbar.make(view, "Enable Destructive Editing to add Actions", Snackbar.LENGTH_LONG).show();

        } else {
          newActionDialog();
        }
      }
    });

    primaryLayout = findViewById(R.id.primary_layout);

    // dataStructs should be populated from checkPermissions()->handleReader()
    if (AppSync.getServer() == null) {
      checkPermissions();
    }
  }

  private void newActionDialog() {
    AddActionDialog addActionDialog = new AddActionDialog(this, this);
    addActionDialog.show();
  }

  public void addActionToLayout(final DataStruct dataStruct) {
    // Create items main container <-->
    final LinearLayout parent = new LinearLayout(this);
    parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    parent.setOrientation(LinearLayout.HORIZONTAL);

    final LinearLayout modifiers = new LinearLayout(this);
    modifiers.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    modifiers.setOrientation(LinearLayout.VERTICAL);

    Button delete = new Button(this);
    Button edit = new Button(this);
    final Switch toggle = new Switch(this);

    // delete button
    delete.setText("delete");
    delete.setTextColor(getResources().getColor(R.color.deleteButton));
    delete.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
    delete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (!AppSync.instance.allowDestructiveEditing) {
          Snackbar.make(view, "Destructive Editing is disabled, can't delete action.", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
          return;
        }

        showDeleteConfirmation(new Runnable() {
          @Override
          public void run() {
            AppSync.getDataStructs().remove(dataStruct);
            primaryLayout.removeView(parent);
            recolor();

            AppSync.saveJSON();
          }
        }, "Are you ABSOLUTELY sure?", "Destroy action \"" + dataStruct.name() + "\" and ALL of its variables?");
      }
    });

    // Edit button
    edit.setText("Edit");
    edit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
    edit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        currentDataStruct = dataStruct;
        currentActionName = toggle;
        Intent intent = new Intent(getBaseContext(), EditActivity.class);
        startActivity(intent);
      }
    });

    // Toggle Button
    toggle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
    toggle.setChecked(dataStruct.enabled());

    toggle.setTag("toggle");
    toggle.setText(dataStruct.name());
    toggle.setTextOn(dataStruct.name());
    toggle.setTextOff(dataStruct.name());
    toggle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    toggle.setTextSize(18);
    toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        recolor();

        dataStruct.setEnabled(b);
        AppSync.saveJSON();
      }
    });

    modifiers.addView(delete);
    modifiers.addView(edit);
    parent.addView(modifiers);

    parent.addView(toggle);

    primaryLayout.addView(parent);

    recolor();
  }

  private void recolor() {
    LinearLayout container = (LinearLayout) findViewById(R.id.primary_layout);
    for (int i = 0; i < container.getChildCount(); i++) {
      LinearLayout child = (LinearLayout) container.getChildAt(i);

      if (child.getChildCount() <= 1) { continue; } // Don't recolor primary_layout
      boolean toggleFound = false;

      for (int j = 0; j < child.getChildCount(); j++) {
        View view = child.getChildAt(j);

        if (view.getTag() != null && ((String) view.getTag()).equals("toggle")) {
          toggleFound = true;
          toggleSwitch = (Switch) view;
          break;
        }
      }

      if (toggleFound && toggleSwitch.isChecked()) {
        if ((i % 2) == 0) {
          child.setBackgroundResource(R.color.checked_even);
        } else {
          child.setBackgroundResource(R.color.checked_odd);
        }
      } else {
        if ((i % 2) == 0) {
          child.setBackgroundResource(R.color.even);
        } else {
          child.setBackgroundResource(R.color.odd);
        }
      }
    }
  }

  private void showDeleteConfirmation(final Runnable runner, String title, String message) {
    TextView titleView = new TextView(this);
    titleView.setText(title);
    titleView.setTextColor(getResources().getColor(R.color.deleteButton));
    titleView.setTextSize(24);
    titleView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

    AlertDialog.Builder confirmation = new AlertDialog.Builder(this);
    confirmation.setCustomTitle(titleView);
    confirmation.setMessage(message);
    confirmation.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        runner.run();
      }
    });
    confirmation.setNegativeButton("Cancel", null);
    confirmation.show();
  }

  private void populateLayout() {
    for(DataStruct item : AppSync.getDataStructs()) {
      addActionToLayout(item);
    }
  }

  private void populateLayoutWithErrorMessage() {
    LinearLayout parent = new LinearLayout(this);
    parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    parent.setOrientation(LinearLayout.VERTICAL);

    TextView message = new TextView(this);
    message.setText("Read/Write Permission is required!");
    message.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    message.setTextSize(32);
    message.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    parent.addView(message);

    Button button = new Button(this);
    button.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    button.setTextSize(28);
    button.setText("Ask Again?");

    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        finish();
        startActivity(new Intent(getBaseContext(), MainActivity.class));
      }
    });
    parent.addView(button);

    LinearLayout primaryLayout = (LinearLayout) findViewById(R.id.primary_layout);
    primaryLayout.setBackgroundResource(R.color.colorAccent);
    primaryLayout.addView(parent);

  }

  private void checkPermissions() {
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              REQUEST_WRITE_PERMISSION);
    } else {
      handleReader();

      populateLayout();
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_WRITE_PERMISSION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          Snackbar.make(primaryLayout, "Thank you for permission, have a nice day!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
          handleReader();

          populateLayout();

        } else {
          populateLayoutWithErrorMessage();
          Snackbar.make(primaryLayout, "Read/Write Permission is required!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
        }
      }
    }
  }

  private void handleReader() {
    Reader jsonReader = new Reader(this);

    if (jsonReader.getLoadSuccess()) {
      AppSync.instance.dataStructs = jsonReader.dataStructs();

      Snackbar.make(primaryLayout, "Loaded from JSON.", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    } else {
      Snackbar.make(primaryLayout, "Failed to load JSON.", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    }
  }

  public void reloadConfig() {
    ((LinearLayout) findViewById(R.id.primary_layout)).removeAllViews();

    checkPermissions();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    this.menu = menu;
    getMenuInflater().inflate(R.menu.menu_main, menu);

    if (AppSync.instance.serverEnabled) {
      this.menu.findItem(R.id.action_server).setChecked(true);
    }

    if (AppSync.instance.allowDestructiveEditing) {
      this.menu.findItem(R.id.action_destructive_editing).setChecked(true);
    }

    if (AppSync.getConnection() != null) {
      this.menu.findItem(R.id.action_connection).setTitle("Disconnect");
    }

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.action_server) {
      AppSync.instance.serverEnabled = !AppSync.instance.serverEnabled;
      item.setChecked(AppSync.instance.serverEnabled);

      if (AppSync.instance.serverEnabled) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f50000")));
        try {
          AppSync.instance.server = new Server(AppSync.PORT);
          AppSync.instance.server.start();
          primaryLayout.removeAllViews();

          if (AppSync.instance.allowDestructiveEditing) {
            AppSync.instance.allowDestructiveEditing = false;

            menu.findItem(R.id.action_destructive_editing).setChecked(AppSync.instance.allowDestructiveEditing);
          }

          Snackbar.make(primaryLayout, "Server running, local editing is disabled!", Snackbar.LENGTH_LONG).show();

        } catch (IOException e) {
          AppSync.instance.server = null;
          AppSync.instance.serverEnabled = false;
          item.setChecked(AppSync.instance.serverEnabled);

          getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));

          Snackbar.make(primaryLayout, "Server failed to start: " + e.getMessage(), Snackbar.LENGTH_LONG).show();

          reloadConfig();
        }

      } else {
        try {
          AppSync.getServer().stop();
        } catch (IOException e) {
        }

        reloadConfig();

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorPrimary)));
        AppSync.instance.server = null;
      }

      return true;
    }

    if (id == R.id.action_destructive_editing) {
      AppSync.instance.allowDestructiveEditing = !AppSync.instance.allowDestructiveEditing;

      if (AppSync.getServer() != null) { AppSync.instance.allowDestructiveEditing = false; }

      item.setChecked(AppSync.instance.allowDestructiveEditing);

      Snackbar.make(primaryLayout, "Destructive editing is " + (item.isChecked() ? "On" : "Off"), Snackbar.LENGTH_LONG).show();
      return true;
    }

    if (id == R.id.action_reload) {
      if (AppSync.instance.server == null) {
        reloadConfig();
      } else {
        Snackbar.make(primaryLayout, "Can not reload config while server is running!", Snackbar.LENGTH_LONG).show();
      }
      return true;
    }

    if (id == R.id.action_connection) {
      if (AppSync.getServer() == null) {
        if (AppSync.getConnection() == null) {
          item.setTitle("Disconnect");
          AppSync.instance.connection = new Connection(AppSync.HOSTNAME, AppSync.PORT);
          AppSync.instance.connection.connect(new Runnable() {
            @Override
            public void run() {
              runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  if (AppSync.instance.connection.socketError()) {

                    item.setTitle("Connect");
                    Snackbar.make(primaryLayout, AppSync.getConnection().lastError(), Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();

                    AppSync.instance.connection = null;
                  }
                }
              });
            }
          });

        } else {
          try {
            AppSync.getConnection().close();
            AppSync.instance.connection = null;
            item.setTitle("Connect");
            Snackbar.make(primaryLayout, "Disconnected from server.", Snackbar.LENGTH_SHORT).show();

          } catch (IOException e) {
            Snackbar.make(primaryLayout, "Failed to disconnect from server!", Snackbar.LENGTH_LONG).show();
          }
        }

      } else {
        Snackbar.make(primaryLayout, "Can't connect to self!", Snackbar.LENGTH_LONG).show();
      }
    }

    return super.onOptionsItemSelected(item);
  }
}
