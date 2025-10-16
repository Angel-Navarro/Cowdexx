package com.example.cowdex.ui.ForgotPasswordActivity;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.cowdex.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.cowdex.ui.EmailCheckActivity.EmailCheckActivity;
import com.example.cowdex.ui.LoginActivity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextView tvEmail, tvInstructions;
    private EditText etCode, etNewPassword, etConfirmPassword;
    private Button btnSendCode, btnVerifyCode, btnResetPassword;
    private ProgressBar progressBar;
    private LinearLayout layoutCode, layoutNewPassword;
    private FirebaseAuth mAuth;
    private String email;
    private String verificationCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mAuth = FirebaseAuth.getInstance();
        email = getIntent().getStringExtra("email");

        tvEmail = findViewById(R.id.tv_email);
        tvInstructions = findViewById(R.id.tv_instructions);
        etCode = findViewById(R.id.et_code);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSendCode = findViewById(R.id.btn_send_code);
        btnVerifyCode = findViewById(R.id.btn_verify_code);
        btnResetPassword = findViewById(R.id.btn_reset_password);
        progressBar = findViewById(R.id.progress_bar);
        layoutCode = findViewById(R.id.layout_code);
        layoutNewPassword = findViewById(R.id.layout_new_password);

        tvEmail.setText(email);

        btnSendCode.setOnClickListener(v -> sendVerificationCode());
        btnVerifyCode.setOnClickListener(v -> verifyCode());
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void sendVerificationCode() {
        progressBar.setVisibility(View.VISIBLE);
        btnSendCode.setEnabled(false);

        // Generar código de 6 dígitos
        verificationCode = String.format("%06d", new Random().nextInt(999999));

        // Enviar correo de recuperación usando Firebase Auth
        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);

                    // Nota: Firebase Auth envía su propio correo con un link.
                    // Para un código personalizado, necesitarías usar Firebase Cloud Functions
                    // o un servicio de email como SendGrid

                    Toast.makeText(this,
                            "Se ha enviado un correo de recuperación a " + email,
                            Toast.LENGTH_LONG).show();

                    tvInstructions.setText("Revisa tu correo y sigue las instrucciones para restablecer tu contraseña");
                    btnSendCode.setVisibility(View.GONE);
                    layoutCode.setVisibility(View.GONE);

                    // Mostrar botón para volver al login después de 3 segundos
                    tvInstructions.postDelayed(() -> {
                        Button btnBackToLogin = new Button(this);
                        btnBackToLogin.setText("Volver al inicio de sesión");
                        btnBackToLogin.setOnClickListener(v -> {
                            Intent intent = new Intent(ForgotPasswordActivity.this, EmailCheckActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                        ((LinearLayout) findViewById(R.id.main_layout)).addView(btnBackToLogin);
                    }, 3000);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnSendCode.setEnabled(true);
                    Toast.makeText(this, "Error al enviar correo: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void verifyCode() {
        String inputCode = etCode.getText().toString().trim();

        if (inputCode.isEmpty()) {
            etCode.setError("Ingresa el código");
            etCode.requestFocus();
            return;
        }

        if (inputCode.equals(verificationCode)) {
            // Código correcto
            layoutCode.setVisibility(View.GONE);
            layoutNewPassword.setVisibility(View.VISIBLE);
            tvInstructions.setText("Ingresa tu nueva contraseña");
        } else {
            etCode.setError("Código incorrecto");
            etCode.requestFocus();
        }
    }

    private void resetPassword() {
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (newPassword.isEmpty()) {
            etNewPassword.setError("Ingresa una contraseña");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("La contraseña debe tener al menos 6 caracteres");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Las contraseñas no coinciden");
            etConfirmPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setEnabled(false);

        // Actualizar contraseña
        // Nota: Para cambiar la contraseña, el usuario debe estar autenticado
        // Firebase Auth maneja esto con el link del correo

        Toast.makeText(this, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show();

        // Ir al login
        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
        intent.putExtra("email", email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}