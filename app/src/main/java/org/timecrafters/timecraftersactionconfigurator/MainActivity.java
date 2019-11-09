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

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
  static public MainActivity instance;
  private static final int REQUEST_WRITE_PERMISSION = 70;

  protected Reader jsonReader;
  protected ArrayList<DataStruct> dataStructs;
  protected FloatingActionButton addActionButton;

  private Switch toggleSwitch;
  private LinearLayout primaryLayout;

  public DataStruct currentDataStruct = null;
  public Switch currentActionName;

  public boolean serverRunning, permitDestructiveEditing = false;
  private Server server;
  private Connection connection;

  public String HOSTNAME = "192.168.1.3";
  public int    PORT     = 8962;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    MainActivity.instance = this;
    this.dataStructs = new ArrayList<>();

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    this.addActionButton = (FloatingActionButton) findViewById(R.id.fab);

    addActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        newActionDialog();
      }
    });
    if (!permitDestructiveEditing)
      addActionButton.setVisibility(View.INVISIBLE);

    primaryLayout = (LinearLayout) findViewById(R.id.primary_layout);

    checkPermissions();
    // dataStructs should be populated from checkPermissions()->handleReader()
  }

  private void newActionDialog() {
    AddActionDialog addActionDialog = new AddActionDialog(this, this);
    addActionDialog.show();
  }

  public void addNewAction(String name) {
    DataStruct dataStruct = new DataStruct();
    dataStruct.setName(name);
    dataStructs.add(dataStruct);

    addAction(dataStruct);
    saveJSON(addActionButton);
  }

  public boolean actionNameIsUnique(String name) {
    boolean unique = true;

    for (DataStruct dataStruct : dataStructs) {
      if (dataStruct.name().equals(name)) { unique = false; }
    }

    return unique;
  }

  private void addAction(final DataStruct dataStruct) {
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
        if (!permitDestructiveEditing) {
          Snackbar.make(view, "Destructive Editing is disabled, can't delete action.", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
          return;
        }

        showDeleteConfirmation(new Runnable() {
          @Override
          public void run() {
            dataStructs.remove(dataStruct);
            primaryLayout.removeView(parent);
            recolor();

            saveJSON(addActionButton);
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
        saveJSON(addActionButton);
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

  public void saveJSON(View view) {
    saveJSON(view, "");
  }

  public void saveJSON(View view, String message) {
    Writer writer = new Writer(dataStructs);

    if (writer.writeSucceeded()) {
      Snackbar.make(view, message+" JSON Saved.", Snackbar.LENGTH_SHORT)
              .setAction("Action", null).show();
    } else {
      Snackbar.make(view, "Failed to write JSON!", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
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
    for(DataStruct item : dataStructs) {
      addAction(item);
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
      if (permitDestructiveEditing) {
        addActionButton.setVisibility(View.VISIBLE);
        addActionButton.setEnabled(true);
      }

      handleReader();

      populateLayout();
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_WRITE_PERMISSION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          if (permitDestructiveEditing) {
            addActionButton.setVisibility(View.VISIBLE);
            addActionButton.setEnabled(true);
          }

          Snackbar.make(addActionButton, "Thank you for permission, have a nice day!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
          handleReader();

          populateLayout();

        } else {
          populateLayoutWithErrorMessage();
          Snackbar.make(addActionButton, "Read/Write Permission is required!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
        }
      }
    }
  }

  private void handleReader() {
    this.jsonReader = new Reader(this);

    if (jsonReader.getLoadSuccess()) {
      this.dataStructs = jsonReader.dataStructs();

      Snackbar.make(addActionButton, "Loaded from JSON.", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    } else {
      Snackbar.make(addActionButton, "Failed to load JSON.", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    if (id == R.id.action_server) {
      serverRunning = !serverRunning;
      item.setChecked(serverRunning);

      if (serverRunning) {
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#f50000")));
        try {
          this.server = new Server(PORT);
          this.server.start();
        } catch (IOException e) {
        }
        // TODO: Lock GUI

      } else {
        try {
          this.server.stop();
        } catch (IOException e) {
        }

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#008000")));
        this.server = null;
      }

      Toast.makeText(this, "Server is " + item.isChecked(), Toast.LENGTH_SHORT).show();
      return true;
    }

    if (id == R.id.action_destructive_editing) {
      permitDestructiveEditing = !permitDestructiveEditing;
      item.setChecked(permitDestructiveEditing);

      if (permitDestructiveEditing) {
        addActionButton.setVisibility(View.VISIBLE);
      } else {
        addActionButton.setVisibility(View.INVISIBLE);
      }

      Toast.makeText(this, "Destructive editing is " + item.isChecked(), Toast.LENGTH_SHORT).show();
      return true;
    }

    if (id == R.id.action_restart) {
      finish();
      startActivity(new Intent(getApplicationContext(), MainActivity.class));
      return true;
    }

    if (id == R.id.action_connection) {
      if (server == null) {
        if (connection == null) {
          item.setTitle("Disconnect");
          this.connection = new Connection(HOSTNAME, PORT);
          this.connection.connect();

          if (!this.connection.socketError()) {
            item.setTitle("Connect");
            Snackbar.make(addActionButton, this.connection.lastError(), Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            this.connection = null;
          }

        } else {
          try {
            if (this.connection.isClosed()) {
              this.connection.close();
              this.connection = null;
              item.setTitle("Connect");
            }
          } catch (IOException e) {
            Toast.makeText(this, "Failed to disconnect from server!", Toast.LENGTH_LONG).show();
          }
        }

      } else {
        Toast.makeText(this, "Can't connect to self!", Toast.LENGTH_SHORT).show();
      }
    }

    return super.onOptionsItemSelected(item);
  }
}
