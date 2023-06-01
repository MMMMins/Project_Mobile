package com.inhatc.project_mobile;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView captchaImageView;

    private EditText edtUserID;
    private EditText edtUserPwd;
    private EditText edtUserCode;
    private Button btnLogin;
    private Button btnSignUpLoad;
    private ImageButton btnReset;
    private FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        captchaImageView = findViewById(R.id.captchaImageView);
        GeneratorCaptcha.resetCaptcha();
        captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());

        edtUserID = findViewById(R.id.edtUserID);
        edtUserPwd = findViewById(R.id.edtUserPwd);
        edtUserCode = findViewById(R.id.edtCode);

        btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);

        btnReset = findViewById(R.id.btnReset);
        btnReset.setOnClickListener(this);

        btnSignUpLoad = findViewById(R.id.btnSignUpLoad);
        btnSignUpLoad.setOnClickListener(this);


    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v){
        if(v == btnLogin){
            // 빈칸 검증
            if(edtIsEmpty(edtUserID)) return;
            if(edtIsEmpty(edtUserPwd)) return;
            if(edtIsEmpty(edtUserCode)) return;

            String userID   = edtUserID.getText().toString();
            String userPwd  = edtUserPwd.getText().toString();
            String userCode = edtUserCode.getText().toString();

            // Captcha 인증 확인
            if(!userCode.equals(GeneratorCaptcha.getCaptchaCode())){
                // 값 초기화
                edtUserCode.setText("");

                // 팝업 메시지
                Toast.makeText(this,"로그인 실패",Toast.LENGTH_SHORT).show();

                // Captcha 초기화
                GeneratorCaptcha.resetCaptcha();
                captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());

                return;
            }

            //로그인 성공로직 구현
            loginDB(userID, userPwd);

            //액티비티 전환
            Toast.makeText(this,edtUserID.getText().toString()+"님 반갑습니다.",Toast.LENGTH_SHORT).show();
        }

        if(v == btnSignUpLoad) {
            //액티비티 전환
            Intent signUpLoad = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(signUpLoad);
            //회원가입
        }

        if(v == btnReset){
            edtUserCode.setText("");
            edtUserCode.requestFocus();
            GeneratorCaptcha.resetCaptcha();
            captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());
        }
    }

    public void loginDB(String email, String password){
        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        String uid = user.getUid();

                        Intent mainAppLoad = new Intent(MainActivity.this, AppMainActivity.class);
                        mainAppLoad.putExtra("UID", uid);
                        startActivity(mainAppLoad);

                        Toast.makeText(getApplicationContext(), email+"님 환영홥니다.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        edtUserID.setText("");
                        edtUserPwd.setText("");
                        edtUserCode.setText("");
                        edtUserID.requestFocus();
                        Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                    }
                });
    }
    public boolean edtIsEmpty(EditText userValue){
        if(userValue.getText().toString().isEmpty()){
            userValue.requestFocus();
            userValue.setHint("비어있음!");
            return true;
        }
        return false;
    }
}
