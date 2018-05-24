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

package org.juanjcsr.newcloudotp

import android.app.Activity
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import kotlinx.android.synthetic.main.about.*

class AboutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)
    }

    public override fun onStart() {
        super.onStart()

        val res = resources
        var tv: TextView

        try {
            val pm = packageManager
            val info = pm.getPackageInfo(packageName, 0)
            val version = res.getString(R.string.about_version, info.versionName, info.versionCode)
//            tv = findViewById(R.id.about_version) as TextView
            about_version.text = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        val apache2 = res.getString(R.string.link_apache2)
        val license = res.getString(R.string.about_license, apache2)
//        tv = findViewById(R.id.about_license) as TextView
        about_license.movementMethod = LinkMovementMethod.getInstance()
        about_license.text = Html.fromHtml(license)

        val lwebsite = res.getString(R.string.link_website)
        val swebsite = res.getString(R.string.about_website, lwebsite)
//        tv = findViewById(R.id.about_website) as TextView
        about_website.movementMethod = LinkMovementMethod.getInstance()
        about_website.text = Html.fromHtml(swebsite)

        val problem = res.getString(R.string.link_report_a_problem)
        val help = res.getString(R.string.link_ask_for_help)
        val feedback = res.getString(R.string.about_feedback, problem, help)
//        about_feedback = findViewById(R.id.about_feedback) as TextView
        about_feedback.movementMethod = LinkMovementMethod.getInstance()
        about_feedback.text = Html.fromHtml(feedback)
    }
}
