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
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
  private static final int REQUEST_WRITE_PERMISSION = 70;
  private static final int ACTIONS_LIST_SIZE = 10;

  protected Reader jsonReader;
  protected ArrayList<DataStruct> dataStructs;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    this.dataStructs = new ArrayList<>();

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        new Writer(dataStructs);

        Snackbar.make(view, "JSON Saved.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
      }
    });

    checkPermissions();
    // dataStructs should be populated from checkPermissions()->handleReader()
  }

  private void populateLayout() {
    int i = 0;
    for(final DataStruct item : dataStructs) {
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
      toggle.setChecked(item.enabled());

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
          item.setEnabled(b);
        }
      });
      parent.addView(toggle);


      TextView itemName = new TextView(this);
      itemName.setText(item.name());
      itemName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
      itemName.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
      parent.addView(itemName);


      LinearLayout primaryLayout = (LinearLayout) findViewById(R.id.primary_layout);
      primaryLayout.addView(parent);

      i++;
    }
  }

  private void populateDataStructs() {
   if (dataStructs.size() == 0) {
     System.out.println(dataStructs);
     for (int i = 0; i < ACTIONS_LIST_SIZE; i++) {
       System.out.println("populating "+i);

       DataStruct dataStruct = new DataStruct();
       dataStruct.setName("Action 00"+i);
       dataStructs.add(dataStruct);
     }
   }
  }

  private void checkPermissions() {
    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(MainActivity.this,
              new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
              REQUEST_WRITE_PERMISSION);
    } else {
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
          FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
          Snackbar.make(fab, "Thank you for permission, have a nice day!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
          handleReader();

          populateDataStructs();
          populateLayout();

        } else {
          FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
          Snackbar.make(fab, "Read/Write Permission is required!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
        }
      }
    }
  }

  private void handleReader() {
    this.jsonReader = new Reader(this);

    this.dataStructs = jsonReader.dataStructs();

    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
    Snackbar.make(fab, "Loaded from JSON.", Snackbar.LENGTH_LONG)
            .setAction("Action", null).show();
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
