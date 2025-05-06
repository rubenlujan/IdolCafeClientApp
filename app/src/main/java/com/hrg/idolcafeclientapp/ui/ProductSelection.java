package com.hrg.idolcafeclientapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.adapters.ProductAdapter;
import com.hrg.idolcafeclientapp.data.models.Product;
import com.hrg.idolcafeclientapp.data.viewmodel.ProductListSharedVM;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedCartSingleton;
import com.hrg.idolcafeclientapp.utils.BaseActivity;
import com.hrg.idolcafeclientapp.utils.MyApplication;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSelection extends BaseActivity implements FragmentProductShop.OnButtonClickListener, FragmentShowOrder.OnButtonRemoveCartClickListener {
    private List<Product> filteredProducts = new ArrayList<>();
    private ImageButton buttonCarrito;
    private TextView textViewContador;
    private int contador = 0;
    private SharedCartSingleton cart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_product_selection);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        int categoryId = getIntent().getIntExtra("categoryId", 1);

        findViewById(R.id.imageButtonCarrito).setOnClickListener(v -> loadFragment(new FragmentShowOrder(), true));

        textViewContador = findViewById(R.id.textViewContador);

        findViewById(R.id.imageButtonMenu).setOnClickListener(v -> openMenuView());

        int counter = GetItemsCount();
        textViewContador.setText(String.valueOf(counter));

    //    buttonCarrito.setOnClickListener(v -> loadFragment(new FragmentShowOrder(), true));
        int[] cartLocation = new int[2];

        if (savedInstanceState == null) {
            loadFragment(FragmentProductsCategory.newInstance(categoryId, cartLocation[0], cartLocation[1]), false);
        }
    }
    private void openMenuView() {
        int[] cartLocation = new int[2];
        //textViewContador =  findViewById(R.id.imageButtonCarrito);
        textViewContador.getLocationOnScreen(cartLocation);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        int[] cartLocation = new int[2];
        textViewContador.getLocationOnScreen(cartLocation);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }
    private void RefreshCartView() {

    }
    public int GetItemsCount() {
        cart = SharedCartSingleton.getInstance();
        return cart.getCartItems().size();
    }
    @Override
    public void onButtonClicked() {
        contador++;
        int counter = GetItemsCount();
        textViewContador.setText(String.valueOf(counter));
    }
    @Override
    public void onButtonRemoveClicked() {
        contador--;
        int counter = GetItemsCount();
        textViewContador.setText(String.valueOf(counter));
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Remueve la bandera para permitir que la pantalla se apague normalmente
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}