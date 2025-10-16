package com.hrg.idolcafeclientapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.firebase.messaging.FirebaseMessaging;
import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedItemComplementSingleton;
import com.hrg.idolcafeclientapp.utils.BaseActivity;

import java.util.List;

public class MainActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        FirebaseMessaging.getInstance().subscribeToTopic("global")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d("FCM", "Suscrito al Topic: global");
                    } else {
                        Log.w("FCM", "Error al suscribirse al Topic", task.getException());
                    }
                });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                //showExitDialog();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        findViewById(R.id.card1).setOnClickListener(v -> openProductsView(1));
        findViewById(R.id.card2).setOnClickListener(v -> openProductsView(2));
        findViewById(R.id.card3).setOnClickListener(v -> openProductsView(3));
        findViewById(R.id.card4).setOnClickListener(v -> openProductsView(4));
        findViewById(R.id.card5).setOnClickListener(v -> openProductsView(5));
        findViewById(R.id.card6).setOnClickListener(v -> openProductsView(6));
        findViewById(R.id.card7).setOnClickListener(v -> openProductsView(10));
        findViewById(R.id.card8).setOnClickListener(v -> openProductsView(11));

    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remueve la bandera para permitir que la pantalla se apague normalmente
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("¿Salir de la aplicación?")
                .setMessage("¿Estás seguro que deseas salir?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }
    private void openProductsView(int category) {
        Intent intent = new Intent(this, ProductSelection.class);
        intent.putExtra("categoryId", category);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }
}