/*
 * FreeOTP
 *
 * Authors: Nathaniel McCallum <npmccallum@redhat.com>
 *
 * Copyright (C) 2013  Nathaniel McCallum, Red Hat
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Portions Copyright 2009 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.juanjcsr.cloudotp;

import org.juanjcsr.cloudotp.add.AddActivity;
import org.juanjcsr.cloudotp.add.ScanActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.GridView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnMenuItemClickListener {
    private static final int REQUEST_CAMERA_PERMISSION = 1;
    private TokenAdapter mTokenAdapter;
    private DataSetObserver mDataSetObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
        setContentView(R.layout.main);

        mTokenAdapter = new TokenAdapter(this);
        ((GridView) findViewById(R.id.grid)).setAdapter(mTokenAdapter);

        // Don't permit screenshots since these might contain OTP codes.
        getWindow().setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE);

        mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mTokenAdapter.getCount() == 0)
                    findViewById(android.R.id.empty).setVisibility(View.VISIBLE);
                else
                    findViewById(android.R.id.empty).setVisibility(View.GONE);
            }
        };
        mTokenAdapter.registerDataSetObserver(mDataSetObserver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mTokenAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mTokenAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTokenAdapter.unregisterDataSetObserver(mDataSetObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_scan).setVisible(ScanActivity.haveCamera());
        menu.findItem(R.id.action_scan).setOnMenuItemClickListener(this);
        menu.findItem(R.id.action_add).setOnMenuItemClickListener(this);
        menu.findItem(R.id.action_about).setOnMenuItemClickListener(this);
        menu.findItem(R.id.action_dropbox).setOnMenuItemClickListener(this);
        return true;
    }

    public void requestCameraPermission(Activity thisActivity, String permission, int code) {
        if (ContextCompat.checkSelfPermission(thisActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, permission)) {

            } else {
                ActivityCompat.requestPermissions(thisActivity, new String[]{permission}, code);
            }
        }
    }

    public boolean checkCameraPermission(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {
        switch (permsRequestCode) {
            case REQUEST_CAMERA_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivity(new Intent(this, org.juanjcsr.cloudotp.add.ScanActivity.class));
                } else {
                    Toast.makeText(this, R.string.error_camera_open, Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_scan:

                if (checkCameraPermission(this, Manifest.permission.CAMERA)) {
                    startActivity(new Intent(this, org.juanjcsr.cloudotp.add.ScanActivity.class));
                } else {
                    requestCameraPermission(MainActivity.this, Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION);
                }

                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                return true;

            case R.id.action_add:
                startActivity(new Intent(this, AddActivity.class));
                return true;

            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                return true;

            case R.id.action_dropbox:
                startActivity(new Intent(this, DropboxManagerActivity.class));
                return true;
        }

        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Uri uri = intent.getData();
        if (uri != null)
            TokenPersistence.addWithToast(this, uri.toString());
    }
}
