package com.hrg.idolcafeclientapp.data.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.models.ComplementCategoryHeader;
import com.hrg.idolcafeclientapp.data.models.ComplementListItem;
import com.hrg.idolcafeclientapp.data.models.ComplementOptionItem;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemComplementAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Tipos de vista
    private static final int VIEW_TYPE_CATEGORY_HEADER = 0;
    private static final int VIEW_TYPE_COMPLEMENT_OPTION = 1;

    private List<ComplementListItem> complementList; // Lista plana de ítems (encabezados y opciones)
    private Map<String, ItemComplement> selectedComplementsMap; // Mapeo de Categoria -> ItemComplement seleccionado
    private OnComplementSelectedListener listener; // Callback para notificar selecciones


    // --- Interface de Callback ---
    public interface OnComplementSelectedListener {
        void onComplementSelected(String category, ItemComplement selectedItem);
        // Añade un método si necesitas saber cuando la selección de una categoría cambia a ninguna (ej: desmarcar)
        // void onSelectionCleared(String category);
    }


    // --- Constructor ---
    public ItemComplementAdapter(List<ItemComplement> complements, Map<String, ItemComplement> initialSelections,
                                 OnComplementSelectedListener listener) {
        this.listener = listener;
        this.selectedComplementsMap = new HashMap<>(initialSelections);
        this.complementList = prepareAdapterList(complements); // Prepara la lista plana para el adapter
        // Opcional: Pre-seleccionar opciones si ya tienes algunas seleccionadas inicialmente
        // initializeSelections(complements);
    }

    // --- Método para preparar la lista plana (Agrupar por categoría e insertar encabezados) ---
    private List<ComplementListItem> prepareAdapterList(List<ItemComplement> complements) {
        List<ComplementListItem> list = new ArrayList<>();
        if (complements == null || complements.isEmpty()) {
            return list; // Retorna lista vacía si no hay datos
        }

        // Agrupar los ItemComplement por Categoría
        Map<String, List<ItemComplement>> groupedComplements = new HashMap<>();
        for (ItemComplement item : complements) {
            groupedComplements
                    .computeIfAbsent(item.getCategory(), k -> new ArrayList<>())
                    .add(item);
        }

        // Ordenar categorías alfabéticamente (opcional)
        List<String> categories = new ArrayList<>(groupedComplements.keySet());
        java.util.Collections.sort(categories);

        // Construir la lista plana para el adapter (encabezado, opciones, encabezado, opciones...)
        for (String category : categories) {
            // Añadir encabezado de categoría
            list.add(new ComplementCategoryHeader(category));

            // Añadir opciones de esa categoría (ordenar por descripción opcional)
            List<ItemComplement> categoryItems = groupedComplements.get(category);
            // java.util.Collections.sort(categoryItems, Comparator.comparing(ItemComplement::getDescription)); // Ordenar opciones
            if (categoryItems != null) {
                for (ItemComplement item : categoryItems) {
                    list.add(new ComplementOptionItem(item));
                }
            }
        }
        return list;
    }

    // --- Implementar métodos del Adapter ---

    @Override
    public int getItemViewType(int position) {
        // Devuelve el tipo de vista basado en el tipo de objeto en la lista
        ComplementListItem item = complementList.get(position);
        if (item instanceof ComplementCategoryHeader) {
            return VIEW_TYPE_CATEGORY_HEADER;
        } else if (item instanceof ComplementOptionItem) {
            return VIEW_TYPE_COMPLEMENT_OPTION;
        }
        return -1; // Tipo desconocido (no debería ocurrir)
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Infla el layout correcto y crea el ViewHolder apropiado
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_CATEGORY_HEADER) {
            View view = inflater.inflate(R.layout.item_complement_category_header, parent, false);
            return new CategoryHeaderViewHolder(view);
        } else if (viewType == VIEW_TYPE_COMPLEMENT_OPTION) {
            View view = inflater.inflate(R.layout.item_complement_option, parent, false);
            return new ComplementOptionViewHolder(view);
        }
        throw new IllegalArgumentException("Invalid view type");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // Enlaza los datos a las vistas del ViewHolder
        int viewType = getItemViewType(position);
        ComplementListItem item = complementList.get(position);

        if (viewType == VIEW_TYPE_CATEGORY_HEADER) {
            CategoryHeaderViewHolder headerHolder = (CategoryHeaderViewHolder) holder;
            ComplementCategoryHeader headerItem = (ComplementCategoryHeader) item;
            headerHolder.textCategoryHeader.setText(headerItem.getCategory());

        } else if (viewType == VIEW_TYPE_COMPLEMENT_OPTION) {
            ComplementOptionViewHolder optionHolder = (ComplementOptionViewHolder) holder;
            ComplementOptionItem optionItem = (ComplementOptionItem) item;
            final ItemComplement complement = optionItem.getItemComplement(); // El ItemComplement real

            optionHolder.radioComplementOption.setText(complement.getDescription());
            // Opcional: Formatear y mostrar precio si agregaste el TextView en el layout
            // optionHolder.textPrice.setText("+" + String.format("%.2f", complement.getPrice()));

            // --- Lógica de Selección de RadioButton ---
            String category = complement.getCategory();
            ItemComplement selectedItemForCategory = selectedComplementsMap.get(category);

            // Marcar el RadioButton si este ítem es el seleccionado para su categoría
            optionHolder.radioComplementOption.setChecked(
                    selectedItemForCategory != null &&
                            selectedItemForCategory.getItemComplementId() == complement.getItemComplementId()
            );

            // --- Listener de Click en la Fila ---
            // El listener está en el LinearLayout padre en item_complement_option.xml
            optionHolder.itemView.setOnClickListener(v -> {
                Log.d("Complememtos", "Opción clicada: " + complement.getDescription() + " (ID: " + complement.getItemComplementId() + ")");

                ItemComplement previouslySelectedItem = selectedComplementsMap.get(category);

                // Si el ítem clicado ya estaba seleccionado, desmarcar (comportamiento de radio group)
                if (previouslySelectedItem != null && previouslySelectedItem.getItemComplementId() == complement.getItemComplementId()) {
                    //selectedComplementsMap.remove(category); // Opcional: remover si desmarcar es permitido
                    // Log.d(TAG, "Opción ya seleccionada, desmarcando (si la lógica lo permite)");
                    // Si no quieres que se pueda desmarcar una vez seleccionado, simplemente no hagas nada aquí
                    // Para un RadioGroup, un clic en el seleccionado no hace nada. Replicamos ese comportamiento.
                    return;
                }

                // Si es una nueva selección, actualizar el mapa
                selectedComplementsMap.put(category, complement);
                Log.d("Complememtos", "Seleccionado para '" + category + "': " + complement.getDescription());


                // --- Notificar al adapter que actualice las vistas de esta categoría ---
                // Para desmarcar el RadioButton previamente seleccionado en esta categoría
                // y marcar el nuevo.
                // Recorrer la lista plana y encontrar todos los items de la misma categoría
                // y notificar que cambien para que se re-enlacen
                for (int i = 0; i < complementList.size(); i++) {
                    ComplementListItem listItem = complementList.get(i);
                    if (listItem instanceof ComplementOptionItem) {
                        ComplementOptionItem option = (ComplementOptionItem) listItem;
                        if (option.getItemComplement().getCategory().equals(category)) {
                            // Notificar que este ítem ha cambiado -> onBindViewHolder se llamará de nuevo
                            notifyItemChanged(i);
                        }
                    }
                }

                // --- Llamar al Callback para notificar a la Activity/Fragment ---
                if (listener != null) {
                    listener.onComplementSelected(category, complement);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return complementList.size();
    }

    // --- Método para obtener las selecciones actuales (útil al confirmar pedido) ---
    public Map<String, ItemComplement> getSelectedComplements() {
        return selectedComplementsMap;
    }

    // --- Métodos para ViewHolders ---

    // ViewHolder para el encabezado de categoría
    static class CategoryHeaderViewHolder extends RecyclerView.ViewHolder {
        TextView textCategoryHeader;
        CategoryHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textCategoryHeader = itemView.findViewById(R.id.textCategoryHeader);
        }
    }

    // ViewHolder para la opción de complemento con RadioButton
    static class ComplementOptionViewHolder extends RecyclerView.ViewHolder {
        RadioButton radioComplementOption;
        // TextView textPrice; // Si agregaste el TextView de precio

        ComplementOptionViewHolder(@NonNull View itemView) {
            super(itemView);
            radioComplementOption = itemView.findViewById(R.id.radioComplementOption);
        }
    }
}