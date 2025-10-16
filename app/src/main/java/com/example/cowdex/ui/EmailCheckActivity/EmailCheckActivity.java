package com.example.cowdex.ui.EmailCheckActivity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cowdex.R;


import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cowdex.ui.LoginActivity.LoginActivity;
import com.example.cowdex.ui.RegisterActivity.RegisterActivity;
import com.example.cowdex.ui.dialogs.PrivacyPolicyDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class EmailCheckActivity extends AppCompatActivity {


    private EditText etEmail;
    private Button btnContinue;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar si el aviso de privacidad ya fue aceptado
        if (!PrivacyPolicyDialog.isPrivacyAccepted(this)) {
            showPrivacyDialog();
        }

        setContentView(R.layout.activity_email_check);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.et_email);
        btnContinue = findViewById(R.id.btn_continue);
        progressBar = findViewById(R.id.progress_bar);

        btnContinue.setOnClickListener(v -> checkEmail());
    }

    private void showPrivacyDialog() {
        PrivacyPolicyDialog dialog = new PrivacyPolicyDialog(this, () -> {
            // El usuario aceptó el aviso de privacidad
            Toast.makeText(this, "Aviso de privacidad aceptado", Toast.LENGTH_SHORT).show();
        });
        dialog.show();
    }

    private void checkEmail() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Ingresa tu correo electrónico");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Ingresa un correo válido");
            etEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnContinue.setEnabled(false);

        // Verificar si el usuario existe en Firestore
        db.collection("users").document(email).get()
                .addOnSuccessListener(document -> {
                    progressBar.setVisibility(View.GONE);
                    btnContinue.setEnabled(true);

                    if (document.exists()) {
                        // Usuario existe - ir a login
                        Intent intent = new Intent(EmailCheckActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    } else {
                        // Usuario nuevo - ir a registro
                        Intent intent = new Intent(EmailCheckActivity.this, RegisterActivity.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnContinue.setEnabled(true);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}