package com.hrg.idolcafeclientapp.ui;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.adapters.ItemComplementAdapter;
import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;
import com.hrg.idolcafeclientapp.data.models.Product;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedCartSingleton;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedItemComplementSingleton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;


public class FragmentProductShop extends Fragment implements ItemComplementAdapter.OnComplementSelectedListener {

    private static final String ARG_PRODUCT = "product";
    private static final String ARG_CART_LOCATION_X_ID = "DeltaX";
    private static final String ARG_CART_LOCATION_Y_ID = "DeltaY";
    private int deltaX;
    private int deltaY;
    private Product product;
    private int counter = 1;
    private TextView tvCounter;
    private  TextView tvNotes;
    private double amount;
    private Button btnAmount;
    private Button btnDeleteItem;
    private ImageView imageProduct;
    private SharedCartSingleton cart;
    private CartItem cartItem;
    private OnButtonClickListener mListener;
    private SharedItemComplementSingleton complementsViewModel;
    private RecyclerView recyclerViewComplements;
    private ItemComplementAdapter complementAdapter;
    private List<ItemComplement> itemComplement;
    private double priceComplement;
    private ItemComplement previousSelectedItem; // Guardará el ítem previamente seleccionado
    private String currentCategory;
    public interface OnButtonClickListener {
        void onButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnButtonClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " debe implementar OnButtonClickListener");
        }
    }

    public FragmentProductShop() {
        // Constructor vacío requerido
    }
    public static FragmentProductShop newInstance(Product product, int deltaX, int deltaY) {
        FragmentProductShop fragment = new FragmentProductShop();
        Bundle args = new Bundle();
        args.putParcelable(ARG_PRODUCT, product);  // Pasar el objeto completo
        args.putInt(ARG_CART_LOCATION_X_ID, deltaX);
        args.putInt(ARG_CART_LOCATION_Y_ID, deltaY);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = getArguments().getParcelable(ARG_PRODUCT);
            deltaX = getArguments().getInt(ARG_CART_LOCATION_X_ID, 0);
            deltaY = getArguments().getInt(ARG_CART_LOCATION_Y_ID, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_shop, container, false);

        view.findViewById(R.id.buttonAmount).setOnClickListener(v -> {
            var totalComplements = complementAdapter.getSelectedComplements();
            Log.d("RLO", String.valueOf(totalComplements.size()));
            if(complementAdapter.getItemCount() > 0 && complementAdapter.getSelectedComplements().isEmpty())  {
                showWarningMessage();
                return;
            } else {
                SetItemCart();
            }

            if(mListener !=null) {
                mListener.onButtonClicked();
            }
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Ejecutar después de 2 segundos (2000 milisegundos)
                    getParentFragmentManager().popBackStack();
                }
            }, 1000);
        });

        tvCounter = view.findViewById(R.id.tvCounter);
        btnAmount = view.findViewById(R.id.buttonAmount);
        tvNotes = view.findViewById(R.id.editTextNotes);
        imageProduct = view.findViewById(R.id.imageProduct);
        btnDeleteItem = view.findViewById(R.id.buttonDeleteItemOrder);
        btnDeleteItem.setOnClickListener(v -> {
            RemoveItemCart();
            if(mListener !=null) {
                mListener.onButtonClicked();
            }
            getParentFragmentManager().popBackStack();
        });

        cart = SharedCartSingleton.getInstance();
        Log.d("Cart", "Productos en carrito: " + cart.getCartItems().size());

        cartItem = cart.getItemById(product.getId());

        SetProductData(view);

        tvCounter.setText(String.valueOf(counter));

        ImageButton btnDecrease = view.findViewById(R.id.btnDecrease);
        ImageButton btnIncrease = view.findViewById(R.id.btnIncrease);

        btnDecrease.setOnClickListener(v -> {
            if (counter > 0) {
                counter--;
                tvCounter.setText(String.valueOf(counter));
                SetAmount();
            }
        });

        btnIncrease.setOnClickListener(v -> {
            counter++;
            tvCounter.setText(String.valueOf(counter));
            SetAmount();
        });

        ImageButton btnBack = view.findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        complementsViewModel = SharedItemComplementSingleton.getInstance();
        List<ItemComplement> items = complementsViewModel.getComplementsById(product.getId());

        List<ItemComplement> complements = new ArrayList<>();
        if(cartItem != null) {
            complements = cartItem.getComplements();
        }
        Map<String, ItemComplement> initialSelectionsMap = new HashMap<>();

        recyclerViewComplements = view.findViewById(R.id.recyclerViewComplements);
        recyclerViewComplements.setLayoutManager(new LinearLayoutManager(requireContext()));
        complementAdapter = new ItemComplementAdapter(items, initialSelectionsMap, this);

        recyclerViewComplements.setAdapter(complementAdapter);

        if(items.isEmpty()) {
            recyclerViewComplements.setVisibility(GONE);
        }
        priceComplement = 0;
        itemComplement = new ArrayList<>();

        btnDeleteItem.setVisibility(GONE);
        return view;
    }
    private void SetAmount() {
        amount = (product.getPrice() + priceComplement) *  counter;
        //product.setPrice((product.getPrice() + priceComplement));
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedText = String.format(
                getString(R.string.button_amount_text),
                currencyFormat.format(amount)
        );
        btnAmount.setText(formattedText);
    }

    private void SetAmountWithComplement(double price) {
        amount = (product.getPrice() + price) *  counter;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedText = String.format(
                getString(R.string.button_amount_text),
                currencyFormat.format(amount)
        );
        btnAmount.setText(formattedText);
    }
    private void SetProductData(View view) {
        ImageView photo = view.findViewById(R.id.imageProduct);
        String base64Image = product.getImage();
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        photo.setImageBitmap(bitmap);

        TextView productName =view.findViewById(R.id.textViewNameProduct);
        productName.setText(product.getName());

        TextView productDescrip =view.findViewById(R.id.textViewDescripProduct);
        productDescrip.setText(product.getDescription());

        /*
        if(cartItem != null) {
            FillLastData(view);
            btnDeleteItem.setVisibility(VISIBLE);

        }
        else{
            counter = 1;
            btnDeleteItem.setVisibility(GONE);
        }*/
        counter = 1;
        SetAmount();
    }
    private void FillLastData(View view) {
        tvNotes.setText(cartItem.getNotes());
        counter = cartItem.getQty();
    }
    private  void SetItemCart() {
        animateImageToCart();
        int partId = cart.getCartItems().size() + 1;

        Double amount = counter * (product.getPrice() + priceComplement);
        cartItem = new CartItem(product.getId(), counter,tvNotes.getText().toString(), product.getName(), product.getDescription(), product.getPrice(), amount);
        cartItem.setId(partId);
        cartItem.setCategory(product.getCategoryId());
        cartItem.setPromoActive(product.getPromoActive());
        cartItem.setComplements(itemComplement);
        cartItem.setPrice(product.getPrice() + priceComplement);
        cartItem.setNotesCompl(String.valueOf(getNotesComplements()));
        cart.addItem(cartItem);

        /*if(cartItem != null) {
            //cart.removeItem(cartItem);
            cartItem.setId(partId);
            cartItem.setQty(counter);
            Double amount = counter * (product.getPrice() + priceComplement);
            cartItem.setAmount(amount);
            cartItem.setNotes(tvNotes.getText().toString());
            cartItem.setCategory(product.getCategoryId());
            cartItem.setPromoActive(product.getPromoActive());
            cartItem.setComplements(itemComplement);
            cart.addItem(cartItem);
            Log.d("RLO", String.valueOf(cart.getCartItems().size()));
        }
        else {
            Double amount = counter * (product.getPrice() + priceComplement);
            cartItem = new CartItem(product.getId(), counter,tvNotes.getText().toString(), product.getName(), product.getDescription(), product.getPrice(), amount);
            cartItem.setId(partId);
            cartItem.setCategory(product.getCategoryId());
            cartItem.setPromoActive(product.getPromoActive());
            cartItem.setComplements(itemComplement);
            cart.addItem(cartItem);
        }*/
    }
    private StringBuilder getNotesComplements() {
        StringBuilder notes = new StringBuilder();
        for(ItemComplement item : itemComplement) {
            notes.append(item.getDescription());
        }
        return notes;
    }
    private void RemoveItemCart() {
        cart.removeItem(cartItem);
    }
    private void animateImageToCart() {
        int[] cartLocation = new int[2];
        btnAmount.getLocationOnScreen(cartLocation);

        // Obtener las coordenadas iniciales de la imagen
        int[] imageLocation = new int[2];
        imageProduct.getLocationOnScreen(imageLocation);

        // Calcular el desplazamiento necesario
        /*float _deltaX = deltaX - imageLocation[0];
        float _deltaY = deltaY - imageLocation[1];*/

        float _deltaX = cartLocation[0] - imageLocation[0];
        float _deltaY = cartLocation[1] - imageLocation[1];
        Log.d("RLO", "x Local: " + cartLocation[0]);
        Log.d("RLO", "x Param: " + deltaX);

        // Animación de escala y traslación
        imageProduct.animate()
                .scaleX(0.1f) // Reducir la escala en X
                .scaleY(0.1f) // Reducir la escala en Y
                .translationX(_deltaX) // Mover en X
                .translationY(_deltaY) // Mover en Y
                .setDuration(1000) // Duración de la animación en milisegundos
                .setInterpolator(new AccelerateInterpolator()) // Interpolador para suavizar la animación
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Restaurar la imagen a su estado original
                        imageProduct.setScaleX(1f);
                        imageProduct.setScaleY(1f);
                        imageProduct.setTranslationX(0f);
                        imageProduct.setTranslationY(0f);
                    }
                })
                .start();
    }
    public void clearComplementByCategory(String cateogry) {
        for (ItemComplement item : itemComplement) {
            if (Objects.equals(item.getCategory(), cateogry)) {
                itemComplement.remove(item);
                return;
            }
        }
    }
    private void showWarningMessage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Selección incompleta")
                .setMessage("Por favor selecciona una de las opciones para tu articulo.")
                .setCancelable(false)
                .setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    @Override
    public void onComplementSelected(String category, ItemComplement selectedItem) {
        //priceComplement = 0;
        // Verifica si ya había una selección previa en la misma categoría
        if (previousSelectedItem != null && previousSelectedItem.getCategory().equals(category)) {
                   // Opcional: Restaura el precio del ítem anterior
            priceComplement -= previousSelectedItem.getPrice();
        }

        // Actualiza la selección anterior con la actual
        previousSelectedItem = selectedItem;
        currentCategory = category;

        // Lógica existente
        priceComplement += selectedItem.getPrice();
        clearComplementByCategory(category);
        itemComplement.add(selectedItem);
        SetAmount();

        /*priceComplement = selectedItem.getPrice();
        clearComplementByCategory(selectedItem.getCategory());
        itemComplement.add(selectedItem);
        SetAmount();*/
    }
}