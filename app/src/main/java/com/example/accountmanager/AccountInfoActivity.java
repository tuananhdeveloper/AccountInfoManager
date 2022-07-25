package com.example.accountmanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.example.accountmanager.dialog.MyPasswordDialog;
import com.example.accountmanager.model.AccountInfo;
import com.example.accountmanager.model.Password;
import com.example.accountmanager.source.AccountInfoData;
import com.example.accountmanager.source.MyEncryptSharedPref;
import com.google.android.material.textfield.TextInputLayout;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

public class AccountInfoActivity extends AppCompatActivity implements MyPasswordDialog.OnButtonClickListener {
    public static final String EXTRA_ACCOUNT_INFO = "extra_account_info";
    private int pos=0;
    private TextInputLayout inputWebsite, inputUsername;
    private TextInputLayout inputNameOnCard, inputCardNumber;
    private TextInputLayout inputExpiryDate, inputCVV;

    public final String parentPath = "storage/0/emulated";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_info);

        initView();
    }

    private void initView() {
        toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        inputWebsite = findViewById(R.id.input_website);
        inputUsername = findViewById(R.id.input_username);
        inputNameOnCard = findViewById(R.id.input_placeholder);
        inputCardNumber = findViewById(R.id.input_card_number);
        inputExpiryDate = findViewById(R.id.input_expiry_date);
        inputCVV = findViewById(R.id.input_security_code);

        EditText editExpiryDate = inputExpiryDate.getEditText();
        if (editExpiryDate != null) {
            editExpiryDate.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    if(editExpiryDate.getText().length()==2 && pos!=3)
                    {   editExpiryDate.setText(editExpiryDate.getText().toString()+"/");
                        editExpiryDate.setSelection(3);
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
                    pos = editExpiryDate.getText().length();
                }

                @Override
                public void afterTextChanged(Editable arg0) {
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_check:
                save();
                break;
        }
        return true;
    }

    @Override
    public void onClick(Password password, boolean isPositiveButtonClick, DialogInterface dialog, int id) {
        if(isPositiveButtonClick) {
            saveAccountInfo(password);
        }
        else {
            dialog.dismiss();
        }
    }

    private void save() {
        if (inputWebsite.getEditText() != null && inputUsername.getEditText() != null
                && inputNameOnCard.getEditText() != null && inputCardNumber.getEditText() != null
                && inputExpiryDate.getEditText() != null && inputCVV.getEditText() != null) {

            String website = inputWebsite.getEditText().getText().toString();
            String username = inputUsername.getEditText().getText().toString();
            String nameOnCard = inputNameOnCard.getEditText().getText().toString();
            String cardNumber = inputCardNumber.getEditText().getText().toString();
            String expiryDate = inputExpiryDate.getEditText().getText().toString();
            String cvv = inputCVV.getEditText().getText().toString();
            if(!TextUtils.isEmpty(website) && !TextUtils.isEmpty(username)
                && !TextUtils.isEmpty(nameOnCard) && !TextUtils.isEmpty(cardNumber)
                && !TextUtils.isEmpty(expiryDate) && !TextUtils.isEmpty(cvv)) {

                MyPasswordDialog passwordDialog = new MyPasswordDialog(this);
                passwordDialog.show(getSupportFragmentManager(), null);
            }
        }

    }

    private void saveAccountInfo(Password password) {
        if (inputWebsite.getEditText() != null && inputUsername.getEditText() != null
        && inputNameOnCard.getEditText() != null && inputCardNumber.getEditText() != null
        && inputExpiryDate.getEditText() != null && inputCVV.getEditText() != null) {
            String website = inputWebsite.getEditText().getText().toString();
            String username = inputUsername.getEditText().getText().toString();
            String nameOnCard = inputNameOnCard.getEditText().getText().toString();
            String cardNumber = inputCardNumber.getEditText().getText().toString();
            String expiryDate = inputExpiryDate.getEditText().getText().toString();
            String cvv = inputCVV.getEditText().getText().toString();

            AccountInfo accountInfo = new AccountInfo(
                    website,
                    username,
                    nameOnCard,
                    Long.parseLong(cardNumber),
                    expiryDate,
                    Integer.parseInt(cvv));

            saveFile(accountInfo, password);
        }
    }

    private void saveFile(AccountInfo accountInfo, Password password) {
        if (password.getPassword().equals(password.getConfirmPassword())) {
            String fileName = "account_info_" + accountInfo.getUsername() + "_"  + accountInfo.getWebsite();
            MyEncryptSharedPref.getInstance()
                    .writeFile(getBaseContext(), parentPath, fileName, accountInfo);

            addAccountToProvider(accountInfo, password, parentPath + "/" + fileName);
        }
    }

    private void addAccountToProvider(AccountInfo accountInfo, Password password , String filePath) {
        ContentValues values = new ContentValues();
        values.put(AccountInfoData.Account._TITLE, accountInfo.getUsername()+ ":" + accountInfo.getWebsite());
        values.put(AccountInfoData.Account._CONTENT, filePath);
        values.put(AccountInfoData.Account._PASSWORD, new String(Hex.encodeHex(password.getPassword().getBytes())));
        getContentResolver().insert(AccountInfoData.CONTENT_URI, values);
        navigateBack(accountInfo);
    }

    private void navigateBack(AccountInfo accountInfo) {
        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_ACCOUNT_INFO, accountInfo);
        returnIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}