package com.hrg.idolcafeclientapp.ui;

import static android.widget.Toast.LENGTH_LONG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;
import com.hrg.idolcafeclientapp.data.models.ItemComplementRequest;
import com.hrg.idolcafeclientapp.data.models.ItemComplementResponse;
import com.hrg.idolcafeclientapp.data.models.NewOrderRequest;
import com.hrg.idolcafeclientapp.data.models.NewOrderResponse;
import com.hrg.idolcafeclientapp.data.network.RetrofitClient;
import com.hrg.idolcafeclientapp.data.repositories.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        TextView textLogo = findViewById(R.id.textLogo);
        TextView textLogo2 = findViewById(R.id.textLogo2);

        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_animation);
        textLogo.startAnimation(scaleAnimation);
        textLogo2.startAnimation(scaleAnimation);

        new Handler().postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, LanguageSelectionActivity.class));
            finish();
        }, 3000);
    }


}