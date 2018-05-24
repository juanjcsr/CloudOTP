package org.juanjcsr.newcloudotp.external

import android.app.DialogFragment
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_dropbox_password.*

import org.juanjcsr.newcloudotp.R

class DropboxPasswordFragment : DialogFragment(), TextView.OnEditorActionListener {


    private var editText: EditText? = null
    private var dismissButton: Button? = null
    private var passwordVerificationText: EditText? = null
    private var passwordVerificationLabel: TextView? = null
    private var hasRemoteFile = false


    interface DropboxFilePasswordListener {
        fun onFinishPasswordDialog(password: String)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dropbox_password, container)

        hasRemoteFile = arguments.getBoolean("hasRemoteFile")

        dismiss_password_button!!.setOnClickListener(dismissClickListener)
        //editText.requestFocus();
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        if (hasRemoteFile) {
            verify_password_label!!.visibility = View.GONE
            db_password_verification!!.visibility = View.GONE
        }
        return view
    }

    override fun onEditorAction(textView: TextView, i: Int, keyEvent: KeyEvent): Boolean {
        val activity = activity as DropboxFilePasswordListener
        activity.onFinishPasswordDialog(this.db_password.text.toString())
        dialog.dismiss()
        return true
    }

    private val dismissClickListener = View.OnClickListener {
        if (!hasRemoteFile && db_password!!.text.toString() != db_password_verification!!.text.toString()) {
            Toast.makeText(activity.applicationContext, R.string.passwords_does_not_match, Toast.LENGTH_SHORT).show()
        } else {
            val activity = activity as DropboxFilePasswordListener
            activity.onFinishPasswordDialog(db_password!!.text.toString())
            dialog.dismiss()
        }
    }

}
