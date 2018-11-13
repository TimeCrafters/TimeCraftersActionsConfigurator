package org.timecrafters.timecraftersactionconfigurator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ToggleButton;

import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Reader;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.Writer;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_WRITE_PERMISSION = 70;

  protected Reader jsonReader;
  protected ArrayList<DataStruct> dataStructs;
  protected FloatingActionButton saveButton;
  private ArrayList<String> actionsList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    this.dataStructs = new ArrayList<>();
    this.actionsList = new ArrayList<>();
    this.actionsList.add("RunDropRobot");
    this.actionsList.add("RunPostDropUTurn");
    this.actionsList.add("RunDriveToDetect");
    this.actionsList.add("RunMineralDetect");
    this.actionsList.add("RunMineralKick");
    this.actionsList.add("RunTeamMarkerDrive");
    this.actionsList.add("RunTeamMarkerPlace");
    this.actionsList.add("RunDriveToPark_fromTMP");
    this.actionsList.add("RunDriveToPark_fromMK");

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    this.saveButton = (FloatingActionButton) findViewById(R.id.fab);
    saveButton.setVisibility(View.GONE);
    saveButton.setEnabled(false);

    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        saveJSON();
      }
    });

    checkPermissions();
    // dataStructs should be populated from checkPermissions()->handleReader()
  }

  private void saveJSON() {
    Writer writer = new Writer(dataStructs);

    if (writer.writeSucceeded()) {
      Snackbar.make(saveButton, "JSON Saved.", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    } else {
      Snackbar.make(saveButton, "Failed to write JSON.", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    }
  }

  private void populateLayout() {
    int i = 0;
    for(final DataStruct item : dataStructs) {
      // Create items main container <-->
      LinearLayout parent = new LinearLayout(this);
      parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      parent.setOrientation(LinearLayout.HORIZONTAL);
      if ((i % 2) == 0) {
        parent.setBackgroundResource(R.color.even);
      } else {
        parent.setBackgroundResource(R.color.odd);
      }

      // Edit button
      Button edit = new Button(this);
      edit.setText("Edit");
      edit.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
      edit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          startActivity(new Intent(getBaseContext(), EditActivity.class));
        }
      });

      // Toggle Button
      Switch toggle = new Switch(this);
      toggle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
      toggle.setChecked(item.enabled());

      if (toggle.isChecked()) {
        toggle.setText(item.name());
      } else {
        toggle.setText(item.name());
      }

      toggle.setTextOn(item.name());
      toggle.setTextOff(item.name());
      toggle.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
      toggle.setTextSize(18);
      toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
          item.setEnabled(b);
          saveJSON();
        }
      });

      parent.addView(edit);
      parent.addView(toggle);

      LinearLayout primaryLayout = (LinearLayout) findViewById(R.id.primary_layout);
      primaryLayout.addView(parent);

      i++;
    }

    LinearLayout padding = new LinearLayout(this);
    padding.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 350));
    padding.setOrientation(LinearLayout.HORIZONTAL);
//    padding.setBackgroundColor(99550055);
    LinearLayout primaryLayout = (LinearLayout) findViewById(R.id.primary_layout);
    primaryLayout.addView(padding);
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

  private void populateDataStructs() {
   if (dataStructs.size() == 0) {
     for (int i = 0; i < actionsList.size(); i++) {
       DataStruct data = new DataStruct();
       data.setName(actionsList.get(i));

       dataStructs.add(data);
     }

     // auto create file //
     saveJSON();
   }
  }

  private void checkPermissions() {
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              REQUEST_WRITE_PERMISSION);
    } else {
      saveButton.setVisibility(View.VISIBLE);
      saveButton.setEnabled(true);

      handleReader();

      populateDataStructs();
      populateLayout();
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_WRITE_PERMISSION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          saveButton.setVisibility(View.VISIBLE);
          saveButton.setEnabled(true);

          Snackbar.make(saveButton, "Thank you for permission, have a nice day!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
          handleReader();

          populateDataStructs();
          populateLayout();

        } else {
          populateLayoutWithErrorMessage();
          Snackbar.make(saveButton, "Read/Write Permission is required!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
        }
      }
    }
  }

  private void handleReader() {
    this.jsonReader = new Reader(this);

    if (jsonReader.getLoadSuccess()) {
      this.dataStructs = jsonReader.dataStructs();

      Snackbar.make(saveButton, "Loaded from JSON.", Snackbar.LENGTH_LONG)
              .setAction("Action", null).show();
    } else {
      Snackbar.make(saveButton, "Failed to load JSON.", Snackbar.LENGTH_LONG)
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

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      Toast.makeText(this, "Hello", Toast.LENGTH_SHORT).show();
      return true;
    }
    if (id == R.id.action_restart) {
      finish();
      startActivity(new Intent(getApplicationContext(), MainActivity.class));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}
