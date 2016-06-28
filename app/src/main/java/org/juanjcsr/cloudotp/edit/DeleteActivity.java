package org.juanjcsr.cloudotp.edit;

import org.juanjcsr.cloudotp.R;
import org.juanjcsr.cloudotp.Token;
import org.juanjcsr.cloudotp.TokenPersistence;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DeleteActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete);

        Token token = new TokenPersistence(this).get(getPosition());
        ((TextView) findViewById(R.id.issuer)).setText(token.getIssuer());
        ((TextView) findViewById(R.id.label)).setText(token.getLabel());
        Picasso.with(this)
                .load(token.getImage())
                .placeholder(R.drawable.logo)
                .into((ImageView) findViewById(R.id.image));

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new TokenPersistence(DeleteActivity.this).delete(getPosition());
                finish();
            }
        });
    }
}
