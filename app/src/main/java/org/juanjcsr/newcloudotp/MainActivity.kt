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

package org.juanjcsr.newcloudotp

import org.juanjcsr.newcloudotp.add.AddActivity
import org.juanjcsr.newcloudotp.add.ScanActivity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.DataSetObserver
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnMenuItemClickListener
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.GridView
import android.widget.Toast
import kotlinx.android.synthetic.main.main.*

class MainActivity : Activity(), OnMenuItemClickListener {
    private var mTokenAdapter: TokenAdapter? = null
    private var mDataSetObserver: DataSetObserver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        setContentView(R.layout.main)

        mTokenAdapter = TokenAdapter(this)
        grid.adapter = mTokenAdapter

        // Don't permit screenshots since these might contain OTP codes.
        window.setFlags(LayoutParams.FLAG_SECURE, LayoutParams.FLAG_SECURE)

        mDataSetObserver = object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                if (mTokenAdapter!!.count == 0)
                    tv_empty.visibility = View.VISIBLE
                else {
                    tv_empty.visibility = View.INVISIBLE
                }
            }
        }
        mTokenAdapter!!.registerDataSetObserver(mDataSetObserver)
    }

    override fun onResume() {
        super.onResume()
        mTokenAdapter!!.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        mTokenAdapter!!.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        mTokenAdapter!!.unregisterDataSetObserver(mDataSetObserver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menu.findItem(R.id.action_scan).isVisible = ScanActivity.haveCamera()
        menu.findItem(R.id.action_scan).setOnMenuItemClickListener(this)
        menu.findItem(R.id.action_add).setOnMenuItemClickListener(this)
        menu.findItem(R.id.action_about).setOnMenuItemClickListener(this)
        menu.findItem(R.id.action_dropbox).setOnMenuItemClickListener(this)
        return true
    }

    fun requestCameraPermission(thisActivity: Activity, permission: String, code: Int) {
        if (ContextCompat.checkSelfPermission(thisActivity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, permission)) {
            } else {
                ActivityCompat.requestPermissions(thisActivity, arrayOf(permission), code)
            }
        }
    }

    fun checkCameraPermission(context: Context, permission: String): Boolean {
//        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
//            return true
//        } else {
//            return false
//        }
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(permsRequestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (permsRequestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    startActivity(Intent(this, org.juanjcsr.newcloudotp.add.ScanActivity::class.java))
                } else {
                    Toast.makeText(this, R.string.error_camera_open, Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_scan -> {

                if (checkCameraPermission(this, Manifest.permission.CAMERA)) {
                    startActivity(Intent(this, org.juanjcsr.newcloudotp.add.ScanActivity::class.java))
                } else {
                    requestCameraPermission(this@MainActivity, Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION)
                }

                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                return true
            }

            R.id.action_add -> {
                startActivity(Intent(this, AddActivity::class.java))
                return true
            }

            R.id.action_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                return true
            }

            R.id.action_dropbox -> {
                startActivity(Intent(this, DropboxManagerActivity::class.java))
                return true
            }
        }

        return false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val uri = intent.data
        if (uri != null)
            TokenPersistence.addWithToast(this, uri.toString())
    }

    companion object {
        private val REQUEST_CAMERA_PERMISSION = 1
    }
}
