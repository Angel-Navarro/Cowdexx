package com.example.cowdex.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cowdex.R;
import com.example.cowdex.ui.bdd.entities.Categoria;

import java.util.ArrayList;
import java.util.List;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.CategoriaViewHolder> {

    private List<Categoria> categorias = new ArrayList<>();
    private OnCategoriaClickListener listener;

    public interface OnCategoriaClickListener {
        void onEditClick(Categoria categoria);
        void onDeleteClick(Categoria categoria);
    }

    public void setOnCategoriaClickListener(OnCategoriaClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_categoria, parent, false);
        return new CategoriaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria currentCategoria = categorias.get(position);
        holder.textViewNombre.setText(currentCategoria.getNombre());

        holder.buttonEdit.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditClick(currentCategoria);
            }
        });

        holder.buttonDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(currentCategoria);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
        notifyDataSetChanged();
    }

    static class CategoriaViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewNombre;
        private ImageButton buttonEdit;
        private ImageButton buttonDelete;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNombre = itemView.findViewById(R.id.text_categoria_nombre);
            buttonEdit = itemView.findViewById(R.id.button_edit_categoria);
            buttonDelete = itemView.findViewById(R.id.button_delete_categoria);
        }
    }
}