package com.abplus.qiitaly.app;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import butterknife.ButterKnife;
import butterknife.InjectView;
import com.abplus.qiitaly.app.api.Backend;
import com.abplus.qiitaly.app.api.models.Auth;
import com.abplus.qiitaly.app.utils.Dialogs;
import org.jetbrains.annotations.NotNull;

/**
 * ログイン用アクティビティ
 *
 * Created by kazhida on 2014/07/30.
 */
public class LoginActivity extends Activity {
    @InjectView(R.id.user_name_text)
    EditText userNameText;
    @InjectView(R.id.password_text)
    EditText passwordText;
    @InjectView(R.id.login_button)
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        setTitle(R.string.login);
        setResult(Activity.RESULT_CANCELED);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NotNull View view) {
                login();
            }
        });
    }

    private String getEditText(EditText editText) {
        String s = editText.getText().toString();
        s = s.trim();
        if (s.length() == 0) {
            return null;
        } else {
            return s;
        }
    }

    private void errorInput(EditText editText, int resId) {
        editText.setHint(resId);
        editText.setText(null);
    }

    private void login() {
        String userName = getEditText(userNameText);
        String password = getEditText(passwordText);

        if (userName != null && password != null) {
            final ProgressDialog dialog = Dialogs.startLoading(LoginActivity.this);

            Backend.sharedInstance().auth(this, userName, password, new Backend.Callback<Auth>() {
                @Override
                public void onSuccess(Auth auth, String nextUrl, int rateLimitRemain) {
                    dialog.dismiss();
                    setResult(Activity.RESULT_OK);
                    finish();
                }

                @Override
                public void onException(Throwable throwable) {
                    throwable.printStackTrace();
                    dialog.dismiss();
                    Dialogs.errorMessage(LoginActivity.this, R.string.err_login, throwable.getLocalizedMessage());
                }

                @Override
                public void onError(String errorReason) {
                    dialog.dismiss();
                    Dialogs.errorMessage(LoginActivity.this, R.string.err_login, errorReason);
                }
            });
        } else {
            if (userName == null) errorInput(userNameText, R.string.err_empty_user_name);
            if (password == null) errorInput(passwordText, R.string.err_empty_password);
        }
    }
}
