package com.example.cowdex;


import android.content.Context;
import android.net.Uri;
import android.os.Build;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FirestoreHelper {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseStorage storage;

    public FirestoreHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
    }

    /**
     * Actualiza la información del dispositivo en Firestore
     */
    public void updateDeviceInfo(Context context, OnCompleteListener listener) {
        String email = getCurrentUserEmail();
        if (email == null) {
            listener.onFailure(new Exception("Usuario no autenticado"));
            return;
        }

        Map<String, Object> deviceInfo = new HashMap<>();
        deviceInfo.put("model", Build.MODEL);
        deviceInfo.put("androidVersion", Build.VERSION.RELEASE);
        deviceInfo.put("manufacturer", Build.MANUFACTURER);

        Map<String, Object> updates = new HashMap<>();
        updates.put("deviceInfo", deviceInfo);
        updates.put("lastUpdated", System.currentTimeMillis());

        db.collection("users").document(email)
                .update(updates)
                .addOnSuccessListener(aVoid -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Sube el archivo de respaldo de Room a Firebase Storage
     */
    public void uploadBackupFile(File backupFile, OnUploadListener listener) {
        String email = getCurrentUserEmail();
        if (email == null) {
            listener.onFailure(new Exception("Usuario no autenticado"));
            return;
        }

        // Referencia al archivo en Storage
        StorageReference backupRef = storage.getReference()
                .child("backups")
                .child(email)
                .child("room_backup_" + System.currentTimeMillis() + ".db");

        Uri fileUri = Uri.fromFile(backupFile);

        backupRef.putFile(fileUri)
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    listener.onProgress((int) progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    // Obtener URL de descarga
                    backupRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Guardar URL en Firestore
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("backupFile", uri.toString());
                        updates.put("lastBackupDate", System.currentTimeMillis());

                        db.collection("users").document(email)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> listener.onSuccess(uri.toString()))
                                .addOnFailureListener(listener::onFailure);
                    });
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Descarga el archivo de respaldo desde Firebase Storage
     */
    public void downloadBackupFile(File destinationFile, OnDownloadListener listener) {
        String email = getCurrentUserEmail();
        if (email == null) {
            listener.onFailure(new Exception("Usuario no autenticado"));
            return;
        }

        // Obtener la URL del respaldo desde Firestore
        db.collection("users").document(email).get()
                .addOnSuccessListener(document -> {
                    if (document.exists() && document.contains("backupFile")) {
                        String backupUrl = document.getString("backupFile");
                        if (backupUrl != null && !backupUrl.isEmpty()) {
                            StorageReference backupRef = storage.getReferenceFromUrl(backupUrl);

                            backupRef.getFile(destinationFile)
                                    .addOnProgressListener(taskSnapshot -> {
                                        double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                        listener.onProgress((int) progress);
                                    })
                                    .addOnSuccessListener(taskSnapshot -> listener.onSuccess())
                                    .addOnFailureListener(listener::onFailure);
                        } else {
                            listener.onFailure(new Exception("No hay respaldo disponible"));
                        }
                    } else {
                        listener.onFailure(new Exception("No se encontró información de respaldo"));
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }

    /**
     * Obtiene el email del usuario actual
     */
    private String getCurrentUserEmail() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getEmail();
        }
        return null;
    }

    // Interfaces para callbacks
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnUploadListener {
        void onSuccess(String downloadUrl);
        void onProgress(int progress);
        void onFailure(Exception e);
    }

    public interface OnDownloadListener {
        void onSuccess();
        void onProgress(int progress);
        void onFailure(Exception e);
    }
}
