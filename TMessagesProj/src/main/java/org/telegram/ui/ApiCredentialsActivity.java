package org.telegram.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.telegram.messenger.BuildVars;

import java.util.Locale;

public class ApiCredentialsActivity extends Activity {

    private EditText apiIdField;
    private EditText apiHashField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BuildVars.loadApiCredentials(this);
        if (BuildVars.hasValidApiCredentials()) {
            openTelegram();
            return;
        }

        buildForm();
    }

    private void buildForm() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(48), dp(24), dp(32));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));

        TextView title = new TextView(this);
        title.setText("تنظیم API تلگرام");
        title.setTextSize(24);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView description = new TextView(this);
        description.setText("برای ورود، ابتدا API ID و API Hash حساب توسعه‌دهنده تلگرام را وارد کنید. این اطلاعات فقط داخل همین برنامه ذخیره می‌شود.");
        description.setTextSize(15);
        description.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams descriptionParams = matchWrap();
        descriptionParams.topMargin = dp(14);
        root.addView(description, descriptionParams);

        apiIdField = new EditText(this);
        apiIdField.setHint("API ID");
        apiIdField.setSingleLine(true);
        apiIdField.setInputType(InputType.TYPE_CLASS_NUMBER);
        LinearLayout.LayoutParams idParams = matchWrap();
        idParams.topMargin = dp(28);
        root.addView(apiIdField, idParams);

        apiHashField = new EditText(this);
        apiHashField.setHint("API Hash");
        apiHashField.setSingleLine(true);
        apiHashField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        LinearLayout.LayoutParams hashParams = matchWrap();
        hashParams.topMargin = dp(12);
        root.addView(apiHashField, hashParams);

        TextView note = new TextView(this);
        note.setText("API Hash باید ۳۲ کاراکتر هگزادسیمال باشد. اعتبار واقعی API هنگام ارسال درخواست کد ورود توسط سرور تلگرام بررسی می‌شود.");
        note.setTextSize(13);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams noteParams = matchWrap();
        noteParams.topMargin = dp(14);
        root.addView(note, noteParams);

        Button continueButton = new Button(this);
        continueButton.setText("ذخیره و ادامه");
        continueButton.setAllCaps(false);
        continueButton.setOnClickListener(v -> saveAndContinue());
        LinearLayout.LayoutParams buttonParams = matchWrap();
        buttonParams.topMargin = dp(24);
        root.addView(continueButton, buttonParams);

        setContentView(scrollView);
    }

    private void saveAndContinue() {
        String idText = apiIdField.getText().toString().trim();
        String hash = apiHashField.getText().toString().trim().toLowerCase(Locale.US);

        if (idText.isEmpty()) {
            apiIdField.setError("API ID را وارد کنید");
            apiIdField.requestFocus();
            return;
        }

        final int apiId;
        try {
            apiId = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            apiIdField.setError("API ID معتبر نیست");
            apiIdField.requestFocus();
            return;
        }

        if (apiId <= 0) {
            apiIdField.setError("API ID باید عدد مثبت باشد");
            apiIdField.requestFocus();
            return;
        }

        if (!hash.matches("(?i)^[0-9a-f]{32}$")) {
            apiHashField.setError("API Hash باید دقیقاً ۳۲ کاراکتر هگزادسیمال باشد");
            apiHashField.requestFocus();
            return;
        }

        try {
            BuildVars.setApiCredentials(this, apiId, hash);
            openTelegram();
        } catch (Throwable e) {
            Toast.makeText(this, "ذخیره اطلاعات API انجام نشد", Toast.LENGTH_LONG).show();
        }
    }

    private void openTelegram() {
        Intent source = getIntent();
        Intent intent = new Intent(this, LaunchActivity.class);
        if (source != null) {
            intent.setAction(source.getAction());
            intent.setData(source.getData());
            if (source.getExtras() != null) {
                intent.putExtras(source.getExtras());
            }
        }
        startActivity(intent);
        finish();
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private int dp(float value) {
        return (int) Math.ceil(value * getResources().getDisplayMetrics().density);
    }
}
