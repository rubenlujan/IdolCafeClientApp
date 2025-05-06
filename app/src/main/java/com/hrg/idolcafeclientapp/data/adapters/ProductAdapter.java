package com.hrg.idolcafeclientapp.data.adapters;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.Product;
import com.hrg.idolcafeclientapp.utils.LocaleHelper;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private Context context;
    private final List<Product> productList;
    private OnProductClickListener listener;
    public ProductAdapter(List<Product> productList, OnProductClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new ProductViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);
        String name = product.getName();
        context = LocaleHelper.wrap(holder.itemView.getContext());

        if (Objects.equals(product.getStatus(), "Inactivo")) {
            name += " " + context.getString(R.string.text_no_disponible);
            holder.itemView.setEnabled(false);
            holder.itemView.setAlpha(0.5f);
            holder.itemView.setClickable(false);
        } else {
            holder.itemView.setEnabled(true);
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setClickable(true);
        }
        holder.itemName.setText(name);


        SharedPreferences prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
        String lang = prefs.getString("lang", "es");
        if(lang.equals("es"))
            holder.itemDescription.setText(product.getDescription());
        else {
            holder.itemDescription.setText(product.getDescription_English());
            product.setDescription(product.getDescription_English());
        }


        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        Context context;
        context = LocaleHelper.wrap(holder.itemView.getContext());
        String priceFormatted = currencyFormat.format(product.getPrice());
        holder.itemPrice.setText(String.format(
                context.getString(R.string.text_price_text),
                priceFormatted));

        //holder.itemPrice.setText(currencyFormat.format(product.getPrice()));

        byte[] decodedBytes = Base64.decode(product.getImage(), Base64.DEFAULT);
        Glide.with(holder.itemView.getContext())
                .load(decodedBytes)
                .into(holder.itemImageButton);

        holder.itemView.setOnClickListener(v -> listener.onProductSelected(product));

    }
    @Override
    public int getItemCount() {
        return productList.size();
    }
     static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageButton;
        TextView itemName;
        TextView itemDescription;
        TextView itemPrice;
        ImageButton buttonAdd;

        public ProductViewHolder(@NonNull View itemView, ProductAdapter adapter) {
            super(itemView);
            itemImageButton = itemView.findViewById(R.id.itemImageButton);
            itemName = itemView.findViewById(R.id.itemName);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            buttonAdd =  itemView.findViewById(R.id.itemImageButton);

            buttonAdd.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    // Acceder a listener y productList a través del adaptador
                    adapter.listener.onProductSelected(adapter.productList.get(position));
                }
            });
        }
    }

    // Interfaz para manejar el clic en un producto
    public interface OnProductClickListener {
        void onProductSelected(Product product);
    }
}
