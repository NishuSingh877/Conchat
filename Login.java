package com.example.conchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Login extends AppCompatActivity {
    EditText editText1,editText2;
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        editText1=findViewById(R.id.name);
        editText2=findViewById(R.id.mobile);
        button =findViewById(R.id.login);



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name=editText1.getText().toString();
                String mobile=editText2.getText().toString();
                Intent intent=new Intent(Login.this,MainActivity.class);
                intent.putExtra("name",name);
                intent.putExtra("mobile",mobile);
                startActivity(intent);
                finish();
            }
        });
    }
}
