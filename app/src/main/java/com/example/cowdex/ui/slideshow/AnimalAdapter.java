package com.example.cowdex.ui.slideshow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cowdex.R;
import com.example.cowdex.ui.bdd.entities.Animal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AnimalAdapter extends RecyclerView.Adapter<AnimalAdapter.AnimalViewHolder> {

    private List<Animal> animales = new ArrayList<>();
    private OnAnimalClickListener listener;

    public interface OnAnimalClickListener {
        void onEditClick(Animal animal);
        void onDeleteClick(Animal animal);
    }

    public AnimalAdapter(OnAnimalClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnimalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_animal_card, parent, false);
        return new AnimalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnimalViewHolder holder, int position) {
        Animal animal = animales.get(position);
        holder.bind(animal);
    }

    @Override
    public int getItemCount() {
        return animales.size();
    }

    public void setAnimales(List<Animal> animales) {
        this.animales = animales;
        notifyDataSetChanged();
    }

    class AnimalViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageViewAnimal;
        private TextView textViewNombre;
        private TextView textViewId;
        private TextView textViewRaza;
        private TextView textViewSexo;
        private TextView textViewEdad;
        private ImageButton buttonEditar;
        private ImageButton buttonEliminar;

        public AnimalViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewAnimal = itemView.findViewById(R.id.imageViewAnimal);
            textViewNombre = itemView.findViewById(R.id.textViewNombreAnimal);
            textViewId = itemView.findViewById(R.id.textViewIdAnimal);
            textViewRaza = itemView.findViewById(R.id.textViewRaza);
            textViewSexo = itemView.findViewById(R.id.textViewSexo);
            textViewEdad = itemView.findViewById(R.id.textViewEdad);
            buttonEditar = itemView.findViewById(R.id.buttonEditarAnimal);
            buttonEliminar = itemView.findViewById(R.id.buttonEliminarAnimal);
        }

        public void bind(Animal animal) {
            textViewNombre.setText(animal.getNombre());
            textViewId.setText("ID: " + (animal.getIdAnimal() != null && !animal.getIdAnimal().isEmpty()
                    ? animal.getIdAnimal() : "N/A"));
            textViewRaza.setText(animal.getRaza() != null && !animal.getRaza().isEmpty()
                    ? animal.getRaza() : "Sin raza");
            textViewSexo.setText(animal.getSexo());

            // Calcular edad
            if (animal.getFechaNacimiento() > 0) {
                textViewEdad.setText(calcularEdad(animal.getFechaNacimiento()));
            } else {
                textViewEdad.setText("Edad desconocida");
            }

            // TODO: Cargar imagen si existe
            // Si tienes una imagen guardada, puedes usar Glide o Picasso aquí
            // Glide.with(itemView.getContext()).load(animal.getImagenUri()).into(imageViewAnimal);

            buttonEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(animal);
                }
            });

            buttonEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(animal);
                }
            });
        }

        private String calcularEdad(long fechaNacimiento) {
            Calendar fechaNac = Calendar.getInstance();
            fechaNac.setTimeInMillis(fechaNacimiento);

            Calendar hoy = Calendar.getInstance();

            int años = hoy.get(Calendar.YEAR) - fechaNac.get(Calendar.YEAR);
            int meses = hoy.get(Calendar.MONTH) - fechaNac.get(Calendar.MONTH);

            if (meses < 0) {
                años--;
                meses += 12;
            }

            if (años > 0) {
                return años + (años == 1 ? " año" : " años") +
                        (meses > 0 ? ", " + meses + (meses == 1 ? " mes" : " meses") : "");
            } else {
                return meses + (meses == 1 ? " mes" : " meses");
            }
        }
    }
}