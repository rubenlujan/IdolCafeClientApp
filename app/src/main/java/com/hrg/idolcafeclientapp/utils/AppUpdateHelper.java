package com.hrg.idolcafeclientapp.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast; // Opcional para mensajes simples

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider; // Asegúrate de usar AndroidX

import java.io.File; // Importa java.io.File

// Necesitas importar LatestVersionInfo (la clase que creaste arriba)

public class AppUpdateHelper {

    private static final String TAG = "AppUpdate"; // Para logs
    private static long downloadId = 0L; // Variable para guardar el ID de la descarga
    // !!! REEMPLAZA ESTO con la autoridad de tu FileProvider definida en AndroidManifest.xml !!!
    private static final String FILE_PROVIDER_AUTHORITY = "com.hrg.idolcafeclientapp.fileprovider"; // Ej: "com.misapps.gstock.fileprovider"


    // --- Método 1: Mostrar el Diálogo de Aviso ---
    public static void showUpdateDialog(final Activity activity, final LatestVersionInfo latestVersionInfo) {
        Log.d(TAG, "Mostrando diálogo de actualización.");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Actualización Disponible");

        String message = "Versión " + latestVersionInfo.getVersionName() + " disponible.";
        if (latestVersionInfo.getWhatsNew() != null && !latestVersionInfo.getWhatsNew().isEmpty()) {
            message += "\n\nNovedades:\n" + latestVersionInfo.getWhatsNew();
        }
        builder.setMessage(message);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            // El usuario hizo clic en Actualizar, iniciar el proceso de descarga e instalación
            startDownloadAndInstall(activity.getApplicationContext(), latestVersionInfo.getApkUrl()); // Usar applicationContext para el receiver
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            // El usuario canceló
            dialog.dismiss();
        });

        builder.setCancelable(true); // Permite cancelar tocando fuera
        builder.show();
    }


    // --- Método 2: Iniciar la Descarga y Configurar la Instalación ---
    public static void startDownloadAndInstall(final Context context, String apkUrl) {
        Log.d(TAG, "Iniciando descarga de " + apkUrl);

        // 1. Crear el Manager de Descargas
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Log.e(TAG, "DownloadManager no disponible");
            Toast.makeText(context, "Error: No se pudo iniciar la descarga.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Crear la Solicitud de Descarga
        Uri apkUri = Uri.parse(apkUrl);
        DownloadManager.Request request = new DownloadManager.Request(apkUri)
                .setTitle("Actualización de App Interna") // Título en la notificación
                .setDescription("Descargando el nuevo APK para instalar...") // Descripción
                // Guardar en el directorio de descargas específico de la app (no requiere permisos de almacenamiento)
                // El archivo se guardará en /Android/data/com.tu_paquete/files/Download/update_apk.apk
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "update_apk.apk")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED) // Mostrar notificación
                .setMimeType("application/vnd.android.package-archive"); // Tipo MIME para APK

        // 3. Encolar la solicitud de descarga
        downloadId = downloadManager.enqueue(request);
        Log.d(TAG, "Descarga encolada. ID: " + downloadId);

        // 4. Registrar un BroadcastReceiver para saber cuándo la descarga ha terminado
        // Lo registramos con applicationContext. Es VITAL desregistrarlo.
        // Una forma más robusta para receptores que inician Activities es registrarlos en la Activity/Fragment.
        // Para este ejemplo, lo registraremos aquí y confiaremos en el manejo de desregistro dentro de onReceive.
        // Sin embargo, en una app real, considera registrar/desregistrar en onResume/onPause o onCreate/onDestroy de tu Activity principal.
        BroadcastReceiver onComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    Log.d(TAG, "Descarga completa con ID: " + id);
                    // Descarga completa, ahora intentar instalar
                    installApk(context, id);

                    // **MUY IMPORTANTE**: Desregistrar este receiver
                    try {
                        // Usar el mismo contexto con el que fue registrado
                        context.unregisterReceiver(this);
                        Log.d(TAG, "Receiver de descarga desregistrado");
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error al desregistrar receiver (ya desregistrado o no registrado?)", e);
                    }
                }
            }
        };

        // Registrar el receiver para la acción de descarga completa
        // Usar el contexto de la aplicación para que pueda escuchar incluso si la Activity se cierra
        ContextCompat.registerReceiver(context, onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), ContextCompat.RECEIVER_EXPORTED);
    }


    // --- Método 3: Iniciar la Instalación del APK Descargado ---
    public static void installApk(final Context context, long downloadId) {
        Log.d(TAG, "Intentando instalar APK con Download ID: " + downloadId);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Log.e(TAG, "DownloadManager no disponible para instalación.");
            return;
        }

        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);

        if (cursor != null && cursor.moveToFirst()) {
            int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            int localUriIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI); // Para Android N+ (>= API 24)
            // int localFilenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME); // Menos recomendado

            if (statusIndex > -1 && cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL) {
                // La descarga fue exitosa
                String localUriString = (localUriIndex > -1) ? cursor.getString(localUriIndex) : null;
                // String localFilenameString = (localFilenameIndex > -1) ? cursor.getString(localFilenameIndex) : null; // Si necesitas el path absoluto

                cursor.close(); // Cerrar cursor tan pronto como sea posible

                if (localUriString == null) {
                    Log.e(TAG, "No se pudo obtener la URI local del archivo descargado.");
                    Toast.makeText(context, "Error al preparar instalación.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Uri uri = Uri.parse(localUriString);
                Log.d(TAG, "APK descargado URI: " + uri.toString());

                // --- Parte de la instalación ---

                // 1. Verificar el permiso para instalar apps desconocidas (Android >= O, API 26)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // packageManager.canRequestPackageInstalls() requiere targetSdkVersion >= 26
                    if (!context.getPackageManager().canRequestPackageInstalls()) {
                        Log.d(TAG, "Permiso REQUEST_INSTALL_PACKAGES no concedido. Solicitando...");
                        Toast.makeText(context, "Por favor, permite la instalación de apps desconocidas para actualizar.", Toast.LENGTH_LONG).show();

                        // Enviar al usuario a la pantalla de configuración de permisos
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
                        intent.setData(Uri.parse("package:" + context.getPackageName()));

                        // Si llamas a installApk desde un Receiver o servicio, necesitas esta flag
                        // Si llamas desde una Activity, a veces no es estrictamente necesaria pero es buena práctica.
                        if (!(context instanceof Activity)) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }

                        try {
                            context.startActivity(intent);
                            Log.d(TAG, "Enviado a configuración de permisos de instalación.");
                        } catch (Exception e) {
                            Log.e(TAG, "Error al iniciar intent de configuración de permisos", e);
                            Toast.makeText(context, "No se pudo abrir la configuración de permisos. Instala manualmente.", Toast.LENGTH_LONG).show();
                        }
                        // Después de que el usuario conceda el permiso, la Activity que inició esto podría reiniciarse o reanudarse.
                        // Deberías tener lógica allí para detectar que el permiso fue concedido y reintentar llamar a installApk.
                        return; // Salir por ahora, reintentar después
                    }
                    Log.d(TAG, "Permiso REQUEST_INSTALL_PACKAGES concedido.");
                }


                // 2. Preparar el Intent de instalación
                Intent installIntent = new Intent(Intent.ACTION_VIEW);

                // --- MUY IMPORTANTE: Usar FileProvider para URIs seguros (Android >= N, API 24) ---
                Uri apkUriForInstall;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Para Android N y posteriores, necesitas un content:// Uri a través de FileProvider
                    // El DownloadManager en >=N guarda el archivo en un área protegida, localUri es content://
                    // Pero si localUriIndex fue -1 y usaste localFilename, tendrías que crear un FileProvider Uri desde el File
                    try {
                        // Si DownloadManager ya dio un content:// URI (localUriIndex > -1): úsalo directamente
                        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
                            apkUriForInstall = uri;
                        } else {
                            // Si localUri era un file:// o localFilenameString se usó:
                            // Conviértelo a File y luego usa FileProvider
                            File apkFile = uriToFile(context, uri); // Implementa esta función helper
                            if (apkFile != null && apkFile.exists()) {
                                apkUriForInstall = FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, apkFile);
                            } else {
                                Log.e(TAG, "Archivo APK no encontrado después de descarga exitosa.");
                                Toast.makeText(context, "Error interno: Archivo no encontrado.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error al obtener URI con FileProvider. ¿Autoridad o paths incorrectos?", e);
                        Toast.makeText(context, "Error de configuración de instalación.", Toast.LENGTH_SHORT).show();
                        return; // Fallar si FileProvider falla
                    }
                    // Conceder permiso de lectura al instalador de paquetes
                    installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                } else {
                    // Para versiones antiguas (< N), se podía usar directamente el file:// Uri obtenido del DownloadManager
                    apkUriForInstall = uri; // Usa el file:// uri obtenido
                }

                if (apkUriForInstall == null) {
                    Log.e(TAG, "URI del APK para instalación es null.");
                    Toast.makeText(context, "Error al preparar instalación.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Establecer el tipo MIME y la URI para el Intent
                installIntent.setDataAndType(apkUriForInstall, "application/vnd.android.package-archive");

                // Es buena práctica añadir esta flag si inicias la Activity desde un contexto que no es Activity
                if (!(context instanceof Activity)) {
                    installIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }


                // 3. Iniciar la Activity del instalador de paquetes
                try {
                    Log.d(TAG, "Iniciando Intent de instalación: " + installIntent.toString());
                    context.startActivity(installIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Error al iniciar Intent de instalación.", e);
                    Toast.makeText(context, "No se pudo iniciar el instalador. Descarga completa.", Toast.LENGTH_SHORT).show();
                }


            } else {
                // La descarga no fue exitosa (podría estar en cola, pausada, fallida)
                int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
                String reason = (reasonIndex > -1) ? "Razón: " + cursor.getInt(reasonIndex) : "";
                Log.e(TAG, "Descarga no exitosa. Status: " + cursor.getInt(statusIndex) + " " + reason);
                Toast.makeText(context, "Error en la descarga de la actualización. Inténtalo de nuevo." + reason, Toast.LENGTH_LONG).show();

                // Opcional: Mostrar notificación de fallo, permitir reintentar, etc.
            }

        } else {
            Log.e(TAG, "Cursor de DownloadManager nulo o vacío.");
        }

        if (cursor != null) {
            cursor.close(); // Asegurarse de cerrar el cursor
        }
    }


    // --- Helper para convertir Uri a File (Necesario para FileProvider en algunos casos) ---
    private static File uriToFile(Context context, Uri uri) {
        // Este helper asume que la URI es un file:// URI o similar que se puede convertir a ruta de archivo
        // Para URIs content:// complejos o de otros proveedores, necesitarías una lógica más avanzada.
        // Para URIs de DownloadManager en >= N, getLocalUri() ya es content:// y GetUriForFile() es el camino.
        // Este helper es útil si trabajas con file:// de DownloadManager en < N o si conviertes rutas a Uri manualmente.
        if (uri == null) return null;

        String path = uri.getPath(); // Obtener la ruta del URI (ej: /storage/emulated/0/...)
        if (path == null) return null;

        return new File(path);
    }

    // --- Método para verificar si se necesita reintentar instalación después de permisos ---
    // Llama a esto en onResume() de tu Activity principal
    public static void checkAndRetryInstall(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Solo si estamos en O+ Y el permiso puede haber sido concedido
            if (activity.getPackageManager().canRequestPackageInstalls()) {
                // El permiso ahora está concedido
                // Aquí asumes que la descarga previa se completó y ahora solo falta instalar
                // Necesitas saber el downloadId del APK que se descargó previamente
                // Podrías guardar el downloadId en SharedPreferences al encolar la descarga
                long lastDownloadId = getLastDownloadId(activity); // Implementa esta función para leer de SharedPreferences

                if (lastDownloadId != 0L) {
                    // Llama a installApk de nuevo para reintentar la instalación
                    installApk(activity.getApplicationContext(), lastDownloadId);
                    // Opcional: Limpia el downloadId de SharedPreferences después de intentar
                    // clearLastDownloadId(activity);
                }
            }
        }
    }

    // --- Funciones helper para guardar/leer downloadId en SharedPreferences ---
    private static final String PREFS_NAME = "AppUpdatePrefs";
    private static final String KEY_LAST_DOWNLOAD_ID = "lastDownloadId";

    public static void saveLastDownloadId(Context context, long id) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_DOWNLOAD_ID, id)
                .apply();
    }

    public static long getLastDownloadId(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_DOWNLOAD_ID, 0L); // 0L si no se encuentra
    }

    public static void clearLastDownloadId(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .remove(KEY_LAST_DOWNLOAD_ID)
                .apply();
    }

}