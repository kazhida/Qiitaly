package com.abplus.qiitaly.app;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * ログイン用アクティビティ
 *
 * Created by kazhida on 2014/07/30.
 */
public class LoginActivity extends Activity {
    @InjectView(R.id.user_name_text)
    EditText userName;
    @InjectView(R.id.password_text)
    EditText password;
    @InjectView(R.id.login_button)
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);



    }
}
