package com.hrg.idolcafeclientapp.data.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<CartItem> productos;
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private OrderAdapter.OnItemClickListener listener;

    public OrderAdapter(List<CartItem> productos, OnItemClickListener listener) {
        this.productos = productos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
            return new OrderViewHolder(view, this);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            // No necesitas hacer nada aquí, los títulos son estáticos
        } else {
            OrderViewHolder orderViewHolder = (OrderViewHolder) holder;
            CartItem producto = productos.get(position - 1); // Resta 1 por el encabezado
            orderViewHolder.itemView.setOnClickListener(v -> listener.onItemSelected(producto));
            orderViewHolder.bind(producto);

        }
    }

    @Override
    public int getItemCount() {
        return productos.size() + 1; // +1 para el encabezado
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    // ViewHolders para cada tipo de vista
    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textProducto;
        private final TextView textCantidad;
        private final TextView textPrecio;
        //private final TextView textImporte;
        private final TextView textNotas;
        private  final LinearLayout layoutNotes;
        private ImageButton btnDelete;

        public OrderViewHolder(@NonNull View itemView, OrderAdapter adapter) {
            super(itemView);
            textProducto = itemView.findViewById(R.id.textProducto);
            textCantidad = itemView.findViewById(R.id.textCantidad);
            textPrecio = itemView.findViewById(R.id.textPrecio);
            textNotas = itemView.findViewById(R.id.textNotas);
            layoutNotes = itemView.findViewById(R.id.linearNotasSummary);;
            btnDelete = itemView.findViewById(R.id.btnDeleteItem);
            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Acceder a listener y productList a través del adaptador
                    adapter.listener.onItemSelected(adapter.productos.get(position - 1));
                }
            });
        }

        public void bind(CartItem producto) {
            textProducto.setText(producto.getName());
            int qty = producto.getQty();
            if(producto.getPromoActive() == 1) {
                qty = qty * 2;
            }

            textCantidad.setText(String.valueOf(qty));
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
            textPrecio.setText(currencyFormat.format(producto.getPrice()));
            StringBuilder notes = new StringBuilder(producto.getNotes());
            List<ItemComplement> complements = producto.getComplements();
            for (ItemComplement item : complements) {
                notes.append((notes.length() == 0) ? item.getDescription() : "-" + item.getDescription());
            }

            if (notes.toString().trim().isEmpty()) {
                layoutNotes.setVisibility(View.GONE);
            } else {
                layoutNotes.setVisibility(View.VISIBLE);

                Context context = itemView.getContext();
                String formattedText = String.format(
                        context.getString(R.string.textview_notes_text),
                        notes.toString()
                );
                textNotas.setText(formattedText);
            }
        }
    }
    public interface OnItemClickListener {
        void onItemSelected(CartItem product);
    }
}