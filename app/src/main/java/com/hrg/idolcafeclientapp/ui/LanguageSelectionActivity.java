package com.hrg.idolcafeclientapp.ui;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;

import com.hrg.idolcafeclientapp.data.messages.CompanyRequest;
import com.hrg.idolcafeclientapp.data.messages.SystemConfigResponse;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;
import com.hrg.idolcafeclientapp.data.models.ItemComplementRequest;
import com.hrg.idolcafeclientapp.data.models.ItemComplementResponse;
import com.hrg.idolcafeclientapp.data.network.RetrofitClient;
import com.hrg.idolcafeclientapp.data.repositories.ApiService;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedItemComplementSingleton;
import com.hrg.idolcafeclientapp.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import org.json.JSONObject;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;


public class LanguageSelectionActivity extends AppCompatActivity {

    private ImageView backgroundImage;
    private int[] images = {
            R.drawable.portadacafe,
    };
    private int currentIndex = 0;
    private Handler handler = new Handler();
    private Runnable imageSwitcher;
    private SharedItemComplementSingleton complementsViewModel;
    private static final String VERSION_CHECK_URL = "https://hrg-it.com/idolcafe/version.json";
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final int PERMISSION_REQUEST_CODE = 100;
    private ProgressBar progressBar;
    private TextView progressText;
    private AlertDialog progressDialog;
    private long downloadID;
    private static final String TAG = "AppUpdate";
    private final BroadcastReceiver onDownloadComplete = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "BroadcastReceiver recibido.");
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            Log.d(TAG, "BroadcastReceiver recibido. ID descargado: " + id + ", ID esperado: " + downloadID);
            if (id == downloadID) {
                Toast.makeText(context, "Descarga completada.", Toast.LENGTH_SHORT).show();
                installApk();
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_language_selection);

        deleteDownloadedApk();

        checkForUpdates();

        try {
            String versionName = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;

            TextView tvVersion = findViewById(R.id.tv_version);
            tvVersion.setText("Versión: " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Referencia al fondo
        backgroundImage = findViewById(R.id.background_image);

        // Referencias a los botones
        LinearLayout btnSpanish = findViewById(R.id.btn_spanish);
        LinearLayout btnEnglish = findViewById(R.id.btn_english);

        complementsViewModel = SharedItemComplementSingleton.getInstance();

        // Listeners
        btnSpanish.setOnClickListener(v -> openMainActivity("es")
        );
        btnEnglish.setOnClickListener(v -> openMainActivity("en"));

        getItemComplements();
        getSystemConfig();

        // Inicia el bucle de imágenes
        startImageLoop();
        //List<ItemComplement> newlist = complementsViewModel.getComplementList();

    }
    private void deleteDownloadedApk() {
        // Obtener el directorio donde se guardó el APK.
        File downloadDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        if (downloadDir != null) {
            File apkFile = new File(downloadDir, "cafeclientapp.apk");

            if (apkFile.exists()) {
                Log.d(TAG, "Encontrado archivo APK descargado para borrar: " + apkFile.getAbsolutePath());
                boolean deleted = apkFile.delete();

                if (deleted) {
                    Log.d(TAG, "Archivo APK descargado borrado exitosamente.");
                } else {
                    Log.w(TAG, "Fallo al borrar el archivo APK descargado.");
                    // Posibles razones de fallo: el archivo sigue en uso (raro después de instalar),
                    // algún problema de permisos (raro para getExternalFilesDir),
                    // o problema del sistema de archivos.
                }
            } else {
                Log.d(TAG, "Archivo APK descargado no encontrado para borrar (quizás ya se borró o nunca existió aquí).");
                // Esto es normal si es la primera ejecución o si ya se borró antes.
            }
        } else {
            Log.e(TAG, "No se pudo obtener el directorio externo de archivos para borrar.");
            // Esto puede ocurrir si el almacenamiento externo no está montado o disponible
        }
    }
    private int getCurrentVersionCode() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                return (int) getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .getLongVersionCode();
            } else {
                return getPackageManager()
                        .getPackageInfo(getPackageName(), 0)
                        .versionCode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
    private void checkForUpdates() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(VERSION_CHECK_URL);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.connect();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder json = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        json.append(line);
                    }

                    reader.close();
                    connection.disconnect();

                    JSONObject jsonObject = new JSONObject(json.toString());

                    int serverVersionCode = jsonObject.getInt("versionCode");
                    String apkUrl = jsonObject.getString("apkUrl");
                    int currentVersionCode = getCurrentVersionCode();

                    if (serverVersionCode > currentVersionCode) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showUpdateDialog(LanguageSelectionActivity.this,serverVersionCode,  apkUrl);
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("UpdateCheck", "Error checking for update: " + e.getMessage());
                }
            }
        });
    }
    private void showUpdateDialog(Context context, int version, final String apkUrl) {
        new AlertDialog.Builder(context)
                .setTitle("Actualización disponible")
                .setMessage("Hay una nueva versión de la aplicación disponible (Ver. " + version + "). ¿Desea actualizar ahora?")
                .setPositiveButton("Actualizar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        downloadApk(context, apkUrl);
                    }
                })
                .setNegativeButton("Más tarde", null)
                .setCancelable(false)
                .show();
    }
    private void downloadApk(Context context, String apkUrl) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_progress, null);
        builder.setView(dialogView);

        progressBar = dialogView.findViewById(R.id.progressBar);
        progressText = dialogView.findViewById(R.id.progressText);

        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressDialog = builder.create();
        progressDialog.show();

        Uri uri = Uri.parse(apkUrl);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle("Actualización");
        request.setDescription("Descargando nueva versión...");
        request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "cafeclientapp.apk");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setAllowedOverMetered(true);
        request.setAllowedOverRoaming(true);

        DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (manager != null) {
            downloadID = manager.enqueue(request);
            Log.d(TAG, "Descarga encolada. ID: " + downloadID);

            new Thread(() -> {
                boolean downloading = true;

                while (downloading) {
                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(downloadID);
                    Cursor cursor = manager.query(q);
                    if (cursor != null && cursor.moveToFirst()) {
                        int bytesDownloaded = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        int bytesTotal = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                        if (bytesTotal > 0) {
                            final int progress = (int) ((bytesDownloaded * 100L) / bytesTotal);
                            runOnUiThread(() -> {
                                progressBar.setProgress(progress);
                                progressText.setText(progress + "%");
                            });
                        }
                        int status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Log.d(TAG, "Hilo monitoreo: Descarga COMPLETADA EXITOSAMENTE para ID: " + downloadID);
                            downloading = false; // Salir del bucle
                            // ** NO LLAMES installApk() AQUÍ. Déjaselo al BroadcastReceiver. **
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            int reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON));
                            Log.e(TAG, "Hilo monitoreo: Descarga FALLIDA para ID: " + downloadID + ", Razón: " + reason);
                            downloading = false; // Salir del bucle
                            // Opcional: Mostrar Toast de fallo en la UI
                            runOnUiThread(() -> {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss();
                                }
                                Toast.makeText(context, "Descarga fallida. Código de razón: " + reason, Toast.LENGTH_LONG).show();
                            });
                        }
                        cursor.close();
                    }
                    try {
                        Thread.sleep(500); // Esperar 500 milisegundos
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); // Restablecer la flag de interrupción
                    }
                }

                runOnUiThread(() -> {
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            }).start();
        }
    }

    private void installApk() {
        File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "cafeclientapp.apk");
        Log.d(TAG, "installApk: " + file);
        if (file.exists()) {
            Log.d(TAG, "installApk: " + file);
            Uri apkUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".provider",
                    file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Archivo APK no encontrado", Toast.LENGTH_SHORT).show();
        }
    }
    private void setLocale(String languageCode) {
        var locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);

        this.getBaseContext().getResources().updateConfiguration(config, this.getBaseContext().getResources().getDisplayMetrics());

        // Recreate the activity to apply the new locale
        this.recreate();
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        prefs.edit().putString("lang", languageCode).apply();
    }
    private void openMainActivity(String languageCode) {
        Intent intent = new Intent(LanguageSelectionActivity.this,  MainActivity.class);
        intent.putExtra("LANGUAGE", languageCode);
        setLocale(languageCode);
        startActivity(intent);
        finish();
    }
    private void startImageLoop() {
        imageSwitcher = new Runnable() {
            @Override
            public void run() {
                backgroundImage.setImageResource(images[currentIndex]);
                currentIndex = (currentIndex + 1) % images.length;
                handler.postDelayed(this, 3000); // cambia cada 3 segundos
            }
        };
        handler.post(imageSwitcher);
    }
    private void getItemComplements() {
        ApiService apiService = RetrofitClient.getApiService();
        ItemComplementRequest request = new ItemComplementRequest();
        request.setCompanyId(1);
        Call<ItemComplementResponse> call = apiService.getItemComplements(request);
        call.enqueue(new Callback<ItemComplementResponse>() {
            @Override
            public void onResponse(@NonNull Call<ItemComplementResponse> call, @NonNull Response<ItemComplementResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ItemComplement> result= response.body().getComplements();
                    if(!result.isEmpty()) {
                        Log.d("Ok", String.valueOf(result.size()));
                        complementsViewModel.setComplements(result);
                    }
                    else {
                        Log.d("Error","Error");
                    }
                } else {
                    System.out.println("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ItemComplementResponse> call, Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }
        });
    }
    private void getSystemConfig() {
        ApiService apiService;
        apiService = RetrofitClient.getApiService();
        CompanyRequest request = new CompanyRequest();
        request.setCompanyId(1);
        apiService.getSystemConfig(request).enqueue(new Callback<SystemConfigResponse>() {
            @Override
            public void onResponse(@NonNull Call<SystemConfigResponse> call, @NonNull Response<SystemConfigResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    var config = response.body().getConfig();
                    Context context = getApplicationContext();
                    SharedPreferences sharedPref = context.getSharedPreferences(
                            "AppSettings",
                            Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String key = "allow_alcohol";
                    editor.putInt(key, config.getAllowSaleAlcohol());
                    key = "allow_terminal";
                    editor.putInt(key, config.getAllowPaymentWithTerminal());
                    editor.apply();
                }
                else {
                    Toast.makeText(getBaseContext(), "Error al cargar configuración", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SystemConfigResponse> call, @NonNull Throwable t) {
                Toast.makeText(getBaseContext(), "Error al cargar configuración: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, ya puedes descargar
                checkForUpdates();
            } else {
                Toast.makeText(this, "Se necesitan permisos para descargar actualizaciones", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        // Registrar el BroadcastReceiver para recibir la notificación de descarga completa.
        // Usamos RECEIVER_EXPORTED para API >= 33 porque el broadcast proviene del sistema
        // (fuera del proceso de la app) y RECEIVER_NOT_EXPORTED parece bloquearlo en este caso.
        // El riesgo de seguridad es bajo dado que verificamos el downloadID y la instalación requiere permiso.
        registerReceiver(onDownloadComplete, filter, Context.RECEIVER_EXPORTED);
    }
    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(onDownloadComplete);
            //Log.d(TAG, "BroadcastReceiver para descarga desregistrado en onPause"); // Usa TAG
        } catch (IllegalArgumentException e) {
            //Log.w(TAG, "BroadcastReceiver no estaba registrado al intentar desregistrar en onPause", e); // Usa TAG
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(imageSwitcher);
        executorService.shutdown();
    }
}
