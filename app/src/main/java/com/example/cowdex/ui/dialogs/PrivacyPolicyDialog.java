package com.example.cowdex.ui.dialogs;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cowdex.R;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.annotation.NonNull;

public class PrivacyPolicyDialog extends Dialog {

    private CheckBox checkboxAccept;
    private Button btnAccept;
    private OnPrivacyAcceptedListener listener;

    public interface OnPrivacyAcceptedListener {
        void onPrivacyAccepted();
    }

    public PrivacyPolicyDialog(@NonNull Context context, OnPrivacyAcceptedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.fragment_privacy_policy_dialog);
        setCancelable(false); // No se puede cerrar sin aceptar

        checkboxAccept = findViewById(R.id.checkbox_accept_privacy);
        btnAccept = findViewById(R.id.btn_accept_privacy);

        btnAccept.setOnClickListener(v -> {
            if (checkboxAccept.isChecked()) {
                savePrivacyAcceptance();
                if (listener != null) {
                    listener.onPrivacyAccepted();
                }
                dismiss();
            } else {
                Toast.makeText(getContext(),
                        "Debes aceptar el aviso de privacidad para continuar",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePrivacyAcceptance() {
        SharedPreferences prefs = getContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("privacy_accepted", true).apply();
    }

    public static boolean isPrivacyAccepted(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        return prefs.getBoolean("privacy_accepted", false);
    }
}