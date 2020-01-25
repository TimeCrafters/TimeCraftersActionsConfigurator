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
import org.timecrafters.timecraftersactionconfigurator.support.AppSync;

public class SearchFilterDialog extends Dialog {
  private MainActivity mainActivity;
  private Button applyButton;
  private EditText searchFilter;

  public SearchFilterDialog(@NonNull Context context, MainActivity mainActivity){
    super(context);
    this.mainActivity = mainActivity;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dialog_search_filter);

    applyButton = findViewById(R.id.apply_button);
    searchFilter  = findViewById(R.id.search_filter);
    if (AppSync.instance.searchFilter != null) {
      searchFilter.setText( AppSync.instance.searchFilter );
    }

    applyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        String name = searchFilter.getText().toString().toLowerCase().trim();

        if (name.length() == 0) {
          AppSync.instance.searchFilter = null;
        } else {
          AppSync.instance.searchFilter = name;
        }

        mainActivity.reloadConfig();
        dismiss();
      }
    });
  }
}
