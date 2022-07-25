package com.example.accountmanager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.accountmanager.adapter.MyAdapter;
import com.example.accountmanager.model.AccountInfo;
import com.example.accountmanager.source.AccountInfoData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.apache.commons.codec.binary.Hex;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
		MyAdapter.OnItemClickListener {

	private static final int REQUEST_CODE = 2432;
	private TextView textNothing;
	private RecyclerView recyclerView;
	private MyAdapter adapter;
	private FloatingActionButton faB;
	private List<AccountInfo> accountInfoList = new ArrayList<>();

	ActivityResultLauncher<Intent> startForResult = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(),
			result -> {
				if (result.getResultCode() == Activity.RESULT_OK) {
					Intent data = result.getData();
					if(data != null && data.getExtras() != null) {
						textNothing.setVisibility(View.GONE);

						AccountInfo accountInfo = (AccountInfo) data.getExtras()
								.getSerializable(AccountInfoActivity.EXTRA_ACCOUNT_INFO);
						accountInfoList.add(accountInfo);
						adapter.notifyItemInserted(accountInfoList.size() - 1);
					}
				}
			}
	);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			checkPermission();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this, AccountInfoActivity.class);
		startForResult.launch(intent);
	}

	@Override
	public void onItemClick(int position, View view, boolean isLongClick) {

	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		switch (requestCode) {
			case REQUEST_CODE:
				if (grantResults.length > 0 &&
						grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					initData();
				}
				else {
					//Explain to the user that the feature is unavailable
				}
				return;
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private void checkPermission() {
		// Check if the permission has been granted
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_GRANTED) {
			// Permission is already available
			initData();
		} else {
			// Permission is missing and must be requested.
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
		}
	}

	private void initView() {
		textNothing = findViewById(R.id.text_nothing);
		recyclerView = findViewById(R.id.my_recyclerview);
		adapter = new MyAdapter(this, accountInfoList);
		recyclerView.setAdapter(adapter);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		faB = findViewById(R.id.my_fab);
		faB.setOnClickListener(this);
	}

	private void initData() {
		accountInfoList.clear();
		String[] projection = {
				AccountInfoData.Account._ID,
				AccountInfoData.Account._TITLE,
		};
		Cursor cursor = getContentResolver().query(AccountInfoData.CONTENT_URI, projection,
				null, null, null);
		if (cursor != null) {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(cursor.getColumnIndexOrThrow(AccountInfoData.Account._ID));
				String title = cursor.getString(cursor.getColumnIndexOrThrow(AccountInfoData.Account._TITLE));

				String username = title.split(":")[0];
				String website = title.split(":")[1];

				AccountInfo accountInfo = new AccountInfo();
				accountInfo.setWebsite(website);
				accountInfo.setUsername(username);

				accountInfoList.add(accountInfo);
				adapter.notifyItemInserted(accountInfoList.size() - 1);
			}
			cursor.close();
		}

	}
}