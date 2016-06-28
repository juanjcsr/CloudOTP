package org.juanjcsr.cloudotp.external;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.juanjcsr.cloudotp.R;

public class DropboxPasswordFragment extends DialogFragment implements TextView.OnEditorActionListener {


    private EditText editText;
    private Button dismissButton;
    private EditText passwordVerificationText;
    private TextView passwordVerificationLabel;
    private boolean hasRemoteFile = false;


    public interface DropboxFilePasswordListener {
        void onFinishPasswordDialog(String password);
    }

    public DropboxPasswordFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_dropbox_password, container);

        hasRemoteFile = getArguments().getBoolean("hasRemoteFile");

        editText = (EditText) view.findViewById(R.id.db_password);
        dismissButton = (Button) view.findViewById(R.id.dismiss_password_button);
        passwordVerificationText = (EditText) view.findViewById(R.id.db_password_verification);
        passwordVerificationLabel = (TextView) view.findViewById(R.id.verify_password_label);
        dismissButton.setOnClickListener(dismissClickListener);
        //editText.requestFocus();
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        if ( hasRemoteFile ) {
            passwordVerificationLabel.setVisibility(View.GONE);
            passwordVerificationText.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
        DropboxFilePasswordListener activity = (DropboxFilePasswordListener) getActivity();
        activity.onFinishPasswordDialog(editText.getText().toString());
        getDialog().dismiss();
        return true;
    }

    private View.OnClickListener dismissClickListener = new View.OnClickListener(){

        @Override
        public void onClick(View view) {
            if (!hasRemoteFile && !editText.getText().toString().equals((passwordVerificationText.getText().toString()))){
                Toast.makeText(getActivity().getApplicationContext(), R.string.passwords_does_not_match, Toast.LENGTH_SHORT).show();
            } else {
                DropboxFilePasswordListener activity = (DropboxFilePasswordListener) getActivity();
                activity.onFinishPasswordDialog(editText.getText().toString());
                getDialog().dismiss();
            }

        }
    };

}
