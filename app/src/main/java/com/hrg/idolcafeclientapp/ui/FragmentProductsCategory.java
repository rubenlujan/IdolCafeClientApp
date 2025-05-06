package com.hrg.idolcafeclientapp.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.adapters.ProductAdapter;
import com.hrg.idolcafeclientapp.data.models.Product;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedCartSingleton;
import com.hrg.idolcafeclientapp.utils.CallServices;
import com.hrg.idolcafeclientapp.utils.ProductLoadCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentProductsCategory#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentProductsCategory extends Fragment {
    private static final String ARG_CATEGORY_ID = "CategoryId";
    private static final String ARG_CART_LOCATION_X_ID = "DeltaX";
    private static final String ARG_CART_LOCATION_Y_ID = "DeltaY";
    private int categoryId;
    private int deltaX;
    private int deltaY;
    private ProductAdapter productAdapter;
    private List<Product> filteredProducts;
    RecyclerView recyclerView;
    ProgressBar progressBar;

    public static FragmentProductsCategory newInstance(int categoryId,  int deltaX, int deltaY) {
        FragmentProductsCategory fragment = new FragmentProductsCategory();
        Bundle args = new Bundle();

        args.putInt(ARG_CATEGORY_ID, categoryId);
        args.putInt(ARG_CART_LOCATION_X_ID, deltaX);
        args.putInt(ARG_CART_LOCATION_Y_ID, deltaY);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getInt(ARG_CATEGORY_ID, -1);
            deltaX = getArguments().getInt(ARG_CART_LOCATION_X_ID, 0);
            deltaY = getArguments().getInt(ARG_CART_LOCATION_Y_ID, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_products_category, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewProduct);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        filteredProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(filteredProducts, this::onProductSelected);
        recyclerView.setAdapter(productAdapter);

        TextView header = view.findViewById(R.id.textHeaderCategory);
        if(categoryId == 1)
            header.setText(getString(R.string.first_menu_item_text));
        else if(categoryId == 2)
            header.setText(getString(R.string.second_menu_item_text));
        else if(categoryId == 3)
            header.setText(getString(R.string.third_menu_item_text));
        else if(categoryId == 4)
            header.setText(getString(R.string.fourth_menu_item_text));
        else if(categoryId == 5)
            header.setText(getString(R.string.fifth_menu_item_text));
        else if(categoryId == 6)
            header.setText(getString(R.string.sixth_menu_item_text));

        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        SharedCartSingleton cart = SharedCartSingleton.getInstance();
        //Log.d("Cart", "Productos en carrito: " + cart.getCartItems().size());

        LoadProducts(categoryId);
        return view;
    }

    private void retryLoadProducts(final int categoryId, final int remainingAttempts) {
        if (remainingAttempts <= 0) {
            Toast.makeText(getContext(), "Error al cargar productos después de varios intentos.", Toast.LENGTH_SHORT).show();
            //Log.e("LoadProducts", "Error al cargar productos después de varios intentos.");
            progressBar.setVisibility(View.GONE);
            return;
        }

        CallServices.FillProductByCategoryList(categoryId, new ProductLoadCallback() {
            @Override
            public void onProductsLoaded(List<Product> products) {
                if (products != null) {
                    filteredProducts.clear();
                    filteredProducts.addAll(products);
                    productAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onProductsLoadFailed(String errorMessage) {
                Toast.makeText(getContext(), errorMessage + " Reintentando (" + (3 - remainingAttempts + 1) + " de 3)...", Toast.LENGTH_SHORT).show();
                Log.e("onProductsLoaded Error: ", errorMessage + " Reintentos restantes: " + (remainingAttempts - 1));
                // Esperar un breve periodo antes de reintentar (opcional pero recomendado)
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        retryLoadProducts(categoryId, remainingAttempts - 1);
                    }
                }, 2000); // Esperar 2 segundos antes de reintentar
                progressBar.setVisibility(View.VISIBLE); // Mostrar el progreso durante el reintento
            }
        });
    }
   private void LoadProducts(int categoryId) {
       retryLoadProducts(categoryId, 3);
    }
    private void onProductSelected(Product product) {
        FragmentProductShop fragment = FragmentProductShop.newInstance(product, deltaX, deltaY);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null) // Permite volver al fragmento anterior
                .commit();
    }
}