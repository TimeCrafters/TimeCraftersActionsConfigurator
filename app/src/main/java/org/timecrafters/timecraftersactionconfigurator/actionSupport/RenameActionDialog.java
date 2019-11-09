package org.timecrafters.timecraftersactionconfigurator.actionSupport;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.timecrafters.timecraftersactionconfigurator.EditActivity;
import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.R;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;

public class RenameActionDialog extends Dialog {
  EditText actionName;
  Button   updateAction;
  MainActivity mainActivity;
  EditActivity editActivity;

  public RenameActionDialog(@NonNull Context context) {
    super(context);
    this.mainActivity = MainActivity.instance;
    this.editActivity = EditActivity.instance;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dialog_rename_action);

    actionName = findViewById(R.id.action_name);
    updateAction  = findViewById(R.id.update_action);

    actionName.setText(mainActivity.currentDataStruct.name());

    updateAction.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (actionName.getText().length() > 0) {
          if (mainActivity.actionNameIsUnique(actionName.getText().toString())) {
            mainActivity.currentActionName.setText(actionName.getText().toString());
            mainActivity.currentActionName.setTextOn(actionName.getText().toString());
            mainActivity.currentActionName.setTextOff(actionName.getText().toString());

            editActivity.actionName.setText(actionName.getText().toString());

            mainActivity.currentDataStruct.setName(actionName.getText().toString());

            mainActivity.saveJSON(editActivity.actionName);

            dismiss();
          } else {
            Toast.makeText(mainActivity, "\""+actionName.getText().toString()+"\" already exists!", Toast.LENGTH_LONG).show();
          }
        }
      }
    });
  }
}