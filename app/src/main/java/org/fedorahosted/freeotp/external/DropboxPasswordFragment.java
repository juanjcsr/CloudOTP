package org.fedorahosted.freeotp.external;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.fedorahosted.freeotp.R;

public class DropboxPasswordFragment extends DialogFragment implements TextView.OnEditorActionListener {


    private EditText editText;
    private Button dismissButton;

    public interface DropboxFilePasswordListener {
        void onFinishPasswordDialog(String password);
    }

    public DropboxPasswordFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view  = inflater.inflate(R.layout.fragment_dropbox_password, container);
        editText = (EditText) view.findViewById(R.id.db_password);
        dismissButton = (Button) view.findViewById(R.id.dismiss_password_button);

        dismissButton.setOnClickListener(dismissClickListener);
        //editText.requestFocus();

        //getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
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
            DropboxFilePasswordListener activity = (DropboxFilePasswordListener) getActivity();
            activity.onFinishPasswordDialog(editText.getText().toString());
            getDialog().dismiss();
        }
    };

}
