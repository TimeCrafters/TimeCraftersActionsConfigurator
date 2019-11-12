package org.timecrafters.timecraftersactionconfigurator.actionSupport;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.timecrafters.timecraftersactionconfigurator.MainActivity;
import org.timecrafters.timecraftersactionconfigurator.R;
import org.timecrafters.timecraftersactionconfigurator.jsonhandler.DataStruct;
import org.timecrafters.timecraftersactionconfigurator.support.AppSync;

public class AddActionDialog extends Dialog {
    EditText actionName;
    Button   addAction;
    MainActivity mainActivity;

    public AddActionDialog(@NonNull Context context, MainActivity mainActivity) {
        super(context);
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_add_action);

        actionName = findViewById(R.id.action_name);
        addAction  = findViewById(R.id.add_action);

        addAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = actionName.getText().toString().trim();

                if (name.length() > 0) {
                    if (AppSync.actionNameIsUnique(name)) {
                        AppSync.addNewAction(name);
                        dismiss();
                    } else {
                        Toast.makeText(mainActivity, "\""+ name +"\" already exists!", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }
}
