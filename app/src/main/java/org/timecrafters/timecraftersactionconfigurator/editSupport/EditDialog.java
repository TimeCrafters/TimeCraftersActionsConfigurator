package org.timecrafters.timecraftersactionconfigurator.editSupport;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.R;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;

import java.util.HashMap;
import java.util.Map;

public class EditDialog extends Dialog {
  private String variableName;
  private HashMap<String, String> variables;
  Spinner booleanValue;
  EditText decimalValue;
  EditText integerValue;
  EditText stringValue;
  TextView textView;

  public EditDialog(@NonNull Context context) {
    super(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dialog_edit_variable);

    Log.i("EditDialog", "Variables: " + variables);
    final LinearLayout booleanEditor = (LinearLayout) findViewById(R.id.booleanEditor);
    final LinearLayout decimalEditor = (LinearLayout) findViewById(R.id.decimalEditor);
    final LinearLayout integerEditor = (LinearLayout) findViewById(R.id.integerEditor);
    final LinearLayout stringEditor = (LinearLayout) findViewById(R.id.stringEditor);

    final TextView title = (TextView) findViewById(R.id.variableName);

    booleanValue = (Spinner) findViewById(R.id.booleanValue);
    decimalValue= (EditText) findViewById(R.id.decimalValue);
    integerValue= (EditText) findViewById(R.id.integerValue);
    stringValue = (EditText) findViewById(R.id.stringValue);

    final Button cancel = (Button) findViewById(R.id.cancel);
    cancel.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        dismiss();
      }
    });
    final Button save   = (Button) findViewById(R.id.save);
    save.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        saveVariable();
        textView.setText(DataStruct.valueOf(variables.get(variableName)).toString());
        MainActivity.mainActivity.saveJSON(textView, "Updated \""+variableName+"\".");
        dismiss();
      }
    });

    String type  = DataStruct.typeOf(variables.get(variableName));
    String value = DataStruct.valueOf(variables.get(variableName)).toString();

    title.setText("Editing "+ variableName);

    switch (type) {
      case "Boolean": {
        if (value.equals("true")) {
          booleanValue.setSelection(0);
        } else {
          booleanValue.setSelection(1);
        }

        decimalEditor.setVisibility(View.GONE);
        integerEditor.setVisibility(View.GONE);
        stringEditor.setVisibility(View.GONE);
        break;
      }
      case "Double":
      case "Float": {
        decimalValue.setText(value);

        booleanEditor.setVisibility(View.GONE);
        integerEditor.setVisibility(View.GONE);
        stringEditor.setVisibility(View.GONE);
        break;
      }
      case "Integer": {
        integerValue.setText(value);

        booleanEditor.setVisibility(View.GONE);
        decimalEditor.setVisibility(View.GONE);
        stringEditor.setVisibility(View.GONE);
        break;
      }
      case "String": {
        stringValue.setText(value);

        booleanEditor.setVisibility(View.GONE);
        decimalEditor.setVisibility(View.GONE);
        integerEditor.setVisibility(View.GONE);
        break;
      }
      default: {
        // Hmm?
        break;
      }
    }
  }

  private void saveVariable() {
    String type  = DataStruct.typeOf(variables.get(variableName));
    String value;

    switch (type) {
      case "Boolean": {
        value = (booleanValue.getItemAtPosition(booleanValue.getSelectedItemPosition()).toString());
        break;
      }
      case "Double":{
        value = decimalValue.getText().toString();
        break;
      }
      case "Float": {
        value = decimalValue.getText().toString();
        break;
      }
      case "Integer": {
        value = integerValue.getText().toString();
        break;
      }
      case "String": {
        value = stringValue.getText().toString();
        break;
      }
      default: {
        // Hmm?
        value = "";
        break;
      }
    }

    variables.put(variableName, DataStruct.encodeValue(type, value));
  }

  public void setVariable(String variableName, HashMap<String, String> variables) {
    this.variableName = variableName;
    this.variables    = variables;
  }

  public void setView(TextView textView) {
    this.textView = textView;
  }
}
