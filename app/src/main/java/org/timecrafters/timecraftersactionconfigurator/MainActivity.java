package org.timecrafters.timecraftersactionconfigurator;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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
  private ArrayList<String> actionsList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Snackbar.make(view, "JSON Saved.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
      }
    });

    checkPermissions();

    this.actionsList = new ArrayList<>();
    this.dataStructs = new ArrayList<>();

    populateActionsList();
    populateLayout();
  }

  private void populateLayout() {
    int i = 0;
    for(final String item : actionsList) {
      final int indexID = i;
      // Create items main container <-->
      LinearLayout parent = new LinearLayout(this);
      parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      parent.setOrientation(LinearLayout.HORIZONTAL);
      if ((i % 2) == 0) {
        parent.setBackgroundResource(R.color.even);
      } else {
        parent.setBackgroundResource(R.color.odd);
      }

      // Toggle Button
      ToggleButton toggle = new ToggleButton(this);
      toggle.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      if (toggle.isChecked()) {
        toggle.setText("Enabled");
      } else {
        toggle.setText("Disabled");
      }

      toggle.setTextOn("Enabled");
      toggle.setTextOff("Disabled");
      toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
          Toast.makeText(getApplicationContext(), "index: "+indexID+" item:"+item+" is "+b, Toast.LENGTH_SHORT).show();
        }
      });
      parent.addView(toggle);


      TextView itemName = new TextView(this);
      itemName.setText(item);
      itemName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      itemName.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
      parent.addView(itemName);


      LinearLayout primaryLayout = (LinearLayout) findViewById(R.id.primary_layout);
      primaryLayout.addView(parent);

      i++;
    }
  }

  private void populateActionsList() {
    this.actionsList.add("One");
    this.actionsList.add("Two");
    this.actionsList.add("Three");
  }

  private void checkPermissions() {
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              REQUEST_WRITE_PERMISSION);
    } else {
      this.jsonReader = new Reader(this);
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    switch (requestCode) {
      case REQUEST_WRITE_PERMISSION: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
          Snackbar.make(fab, "Thank you for permission, have a nice day!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
          this.jsonReader = new Reader(this);

        } else {
          FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
          Snackbar.make(fab, "Read/Write Permission is required!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
        }
      }
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

    return super.onOptionsItemSelected(item);
  }
}
