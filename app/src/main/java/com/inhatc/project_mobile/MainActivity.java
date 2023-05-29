package com.inhatc.project_mobile;


import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView captchaImageView;

    private EditText edtUserID;
    private EditText edtUserPwd;
    private EditText edtUserCode;
    private Button btnLogin;
    private Button btnSignUp;
    private ImageButton btnReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        btnSignUp = findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);


    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v){
        if(v == btnLogin){
            String userID   = edtUserID.getText().toString();
            String userPwd  = edtUserPwd.getText().toString();
            String userCode = edtUserCode.getText().toString();

            // 빈칸 검증
            if(edtIsEmpty(edtUserID)) return;
            if(edtIsEmpty(edtUserPwd)) return;
            if(edtIsEmpty(edtUserCode)) return;

            // Captcha 인증 확인
            if(!userCode.equals(GeneratorCaptcha.getCaptchaCode())){
                // 입력한 Captcha 값과 설정된 값이 틀리다면
                /*

                로그인 실패 로직 구현

                */
                // 값 초기화
                edtUserID.setText("");
                edtUserPwd.setText("");
                edtUserCode.setText("");

                // 팝업 메시지
                Toast.makeText(this,"로그인 실패",Toast.LENGTH_SHORT).show();

                // Captcha 초기화
                GeneratorCaptcha.resetCaptcha();
                captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());

                return;
            }
            //로그인 성공로직 구현

            //액티비티 전환
            Toast.makeText(this,edtUserID.getText().toString()+"님 반갑습니다.",Toast.LENGTH_SHORT).show();
        }

        if(v == btnSignUp) {
            //액티비티 전환
        }

        if(v == btnReset){
            edtUserCode.setText("");
            edtUserCode.requestFocus();
            GeneratorCaptcha.resetCaptcha();
            captchaImageView.setImageBitmap(GeneratorCaptcha.getCaptchaImage());
        }
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
