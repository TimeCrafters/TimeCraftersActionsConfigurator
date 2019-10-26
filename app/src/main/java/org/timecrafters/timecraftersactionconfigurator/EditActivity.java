package org.timecrafters.timecraftersactionconfigurator;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.timecrafters.timecraftersactionconfigurator.actionSupport.RenameActionDialog;
import org.timecrafters.timecraftersactionconfigurator.editSupport.EditDialog;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

public class EditActivity extends AppCompatActivity {
  static public EditActivity instance;

  private static final int MAGIC_NUM = 1010;
  private LinearLayout container;
  private MainActivity mainActivity;
  private ArrayList<DataStruct> dataStructs;
  private EditText variableName;
  private Spinner variableType;
  private Button addVariable;
  private TextView title;
  private Button renameAction;
  private DataStruct activeDataStruct;

  private int currentIndex = 0;

  public TextView actionName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    this.instance = this;

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_edit);

    mainActivity = MainActivity.instance;
    dataStructs  = mainActivity.dataStructs;

    title        = (TextView) findViewById(R.id.actionName);
    renameAction = (Button) findViewById(R.id.rename_action);
    container    = (LinearLayout) findViewById(R.id.container);
    variableName = (EditText) findViewById(R.id.variableName);
    variableType = (Spinner) findViewById(R.id.variableType);
    addVariable  = (Button) findViewById(R.id.add);

    activeDataStruct = mainActivity.currentDataStruct;

    for (Entry<String, String> variable : activeDataStruct.variables().entrySet()) {
      addToList(variable.getKey(), variable.getValue(), currentIndex);
      currentIndex++;
    }

    title.setText("Editing "+activeDataStruct.name());

    renameAction.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showRenameActionDialog();
      }
    });

    addVariable.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String name = variableName.getText().toString();
        String type = variableType.getSelectedItem().toString();

        if (name.length() > 0) {
          if (uniqueName(name)) {
            String value = DataStruct.encodeValue(type, defaultValue(type));
            activeDataStruct.variables().put(name, value);
            addToList(name, value, currentIndex);
            currentIndex++;

            mainActivity.saveJSON(container, "Added \"" + name + "\".");

          } else {
            Snackbar.make(view, "Name \""+name+"\" is already taken!", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
          }
        } else {
          Snackbar.make(view, "Can not add empty variables!", Snackbar.LENGTH_LONG)
                  .setAction("Action", null).show();
        }
      }
    });
  }

  private void showRenameActionDialog() {
    actionName = title;
    RenameActionDialog renameActionDialog = new RenameActionDialog(this);
    renameActionDialog.show();
  }

  private boolean uniqueName(String text) {
    boolean unique = true;

    for (Entry<String, String> variable : activeDataStruct.variables().entrySet()) {
      if (variable.getKey().equals(text)) {
        unique = false;
        break;
      }
    }

    return unique;
  }

  private String defaultValue(String type) {
    switch (type) {
      case "Boolean": {
        return "Bx"+false;
      }
      case "Double": {
        return "Dx"+0.0;
      }
      case "Float": {
        return "Fx"+0.0;
      }
      case "Integer": {
        return "Ix"+0;
      }
      case "Long": {
        return "Lx"+0;
      }
      case "String": {
        return "Sx"+"string";
      }
      default: {
        return "=!UNKNOWN!=";
      }
    }
  }

  private void addToList(final String variableName, String variableValue, int index) {
    final int finalIndex = index;
    final LinearLayout parent = new LinearLayout(this);
    parent.setId(index*MAGIC_NUM);
    parent.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    parent.setOrientation(LinearLayout.VERTICAL);
    if ((index % 2) == 0) {
      parent.setBackgroundResource(R.color.even);
    } else {
      parent.setBackgroundResource(R.color.odd);
    }

    final LinearLayout firstRow = new LinearLayout(this);
    firstRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    firstRow.setOrientation(LinearLayout.HORIZONTAL);

    final Button deleteButton = new Button(this);
    deleteButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    deleteButton.setText("Delete");
    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Runnable data = new Runnable() {
          public void run() {
          LinearLayout parent = ((LinearLayout) findViewById(finalIndex * MAGIC_NUM));
          LinearLayout container = ((LinearLayout) findViewById(R.id.container));
          container.removeView(parent);

          activeDataStruct.variables().remove(variableName);
          mainActivity.saveJSON(container, "Deleted \"" + variableName + "\".");
          recolorParents();
        }};

        showConfirmation(data, "Delete \""+variableName+"\"?", "Are you sure you want to delete \""+variableName+"\"?");
      }
    });

    final TextView variableNameText = new TextView(this);
    variableNameText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    variableNameText.setText("Name: "+variableName);
    variableNameText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    variableNameText.setTextSize(18);


    final LinearLayout secondRow = new LinearLayout(this);
    secondRow.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    secondRow.setOrientation(LinearLayout.HORIZONTAL);

    final Button editButton = new Button(this);
    editButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    editButton.setText("Edit");

    final TextView variableValueText = new TextView(this);
    variableValueText.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    variableValueText.setText("Value: "+DataStruct.valueOf(variableValue).toString());
    variableValueText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
    variableValueText.setTextSize(18);

    final Context context = this;
    editButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        EditDialog editDialog = new EditDialog(context, mainActivity);
        editDialog.setVariable(variableName, activeDataStruct.variables());
        editDialog.setView(variableValueText);
        editDialog.show();
      }
    });

    firstRow.addView(deleteButton);
    firstRow.addView(variableNameText);

    secondRow.addView(editButton);
    secondRow.addView(variableValueText);

    parent.addView(firstRow);
    parent.addView(secondRow);
    container.addView(parent);
  }

  private void recolorParents() {
    LinearLayout container = (LinearLayout) findViewById(R.id.container);
    for (int i = 0; i < container.getChildCount(); i++) {
      LinearLayout child = (LinearLayout) container.getChildAt(i);
      if ((i % 2) == 0) {
        child.setBackgroundResource(R.color.even);
      } else {
        child.setBackgroundResource(R.color.odd);
      }
    }
  }

  private void showConfirmation(Runnable data, String title, String message) {
    final Runnable finalData = data;

    AlertDialog.Builder confirmation = new AlertDialog.Builder(this);
    confirmation.setTitle(title);
    confirmation.setMessage(message);
    confirmation.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        finalData.run();
      }
    });
    confirmation.setNegativeButton("Cancel", null);
    confirmation.show();
  }
}
