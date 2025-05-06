package com.hrg.idolcafeclientapp.ui;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.CycleInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.adapters.OrderAdapter;
import com.hrg.idolcafeclientapp.data.models.APIMPagoDataResponse;
import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.NewOrderDetail;
import com.hrg.idolcafeclientapp.data.models.NewOrderRequest;
import com.hrg.idolcafeclientapp.data.models.NewOrderResponse;
import com.hrg.idolcafeclientapp.data.models.PaymentInfoRequest;
import com.hrg.idolcafeclientapp.data.models.PaymentResponse;
import com.hrg.idolcafeclientapp.data.models.Product;
import com.hrg.idolcafeclientapp.data.network.RetrofitClient;
import com.hrg.idolcafeclientapp.data.repositories.ApiService;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedCartSingleton;
import com.hrg.idolcafeclientapp.utils.CallServices;
import com.hrg.idolcafeclientapp.utils.LocaleHelper;
import com.hrg.idolcafeclientapp.utils.ProductLoadCallback;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FragmentShowOrder extends Fragment {

    private SharedCartSingleton cart;
    private EditText clientName;
    List<CartItem> products;
    private Button btnAmount;
    private Button btnShopping;
    private RadioButton rbOnSite;
    private RadioButton rbPick;
    private Button btnClearCart;
    private RadioGroup rgroupOrder;
    private OrderAdapter adapter;
    private Context context;
    private String message;
    private OnButtonRemoveCartClickListener mListener;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_show_order, container, false);

        // Configura el botón de regreso
        ImageButton btnBack = view.findViewById(R.id.btnOrderBack);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        rbOnSite = view.findViewById(R.id.checkBoxConsumirAqui);
        rbPick = view.findViewById(R.id.checkBoxParaLlevar);

        btnShopping = view.findViewById(R.id.btnSeguirComprando);
        btnShopping.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        btnClearCart = view.findViewById(R.id.btnLimpiarCarrito);
        btnClearCart.setOnClickListener(v -> {
            cart.clearCart();
            cerrarFragment();
        });

        // Configura el RecyclerView
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewProductos);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        cart = SharedCartSingleton.getInstance();

        products = cart.getCartItems();

        double totalAmount = products.stream()
                .mapToDouble(CartItem::getAmount)
                .sum();


        btnAmount = view.findViewById(R.id.btnConfirmarOrden);

        CalculateTotal();

        adapter = new OrderAdapter(products, this::onItemSelected);
        recyclerView.setAdapter(adapter);

        //btnAmount.setOnClickListener(v -> SendNewOrder(totalAmount));

        btnAmount.setOnClickListener(v -> openPaymentSelection(totalAmount));

        clientName = view.findViewById(R.id.editTextNombrePedido);

        clientName.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Oculta el teclado
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(clientName.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (clientName.isFocused()) {
                        Rect outRect = new Rect();
                        clientName.getGlobalVisibleRect(outRect);
                        if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                            clientName.clearFocus();
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(clientName.getWindowToken(), 0);
                        }
                    }
                }
                return false;
            }
        });

        context = LocaleHelper.wrap(requireContext());

        return view;
    }
    private void CalculateTotal() {
        products = cart.getCartItems();

        double totalAmount = products.stream()
                .mapToDouble(CartItem::getAmount)
                .sum();

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        String formattedText = String.format(
                getString(R.string.button_amount_order_text),
                currencyFormat.format(totalAmount)
        );
        btnAmount.setText(formattedText);
    }
    private void SendNewOrder(double amount) {
      if(!ValidateData(amount))
          return;

        String name = String.valueOf(clientName.getText());
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        String orderType = rbPick.isChecked() ? "Para LLevar" : "Consumo en sitio";
        NewOrderRequest request = new NewOrderRequest();
        request.setName(name);
        request.setType(orderType);
        for (CartItem item : products) {
            NewOrderDetail itemDetail = new NewOrderDetail();
            itemDetail.setId(item.getItemId());
            itemDetail.setQuantity(item.getQty());
            itemDetail.setNotes(item.getNotes());
            request.AddItem(itemDetail);
        }
        //Log.d("NewOrder","Articulos:" + request.getItems().size());
        ApiService apiService = RetrofitClient.getApiService();
        Call<NewOrderResponse> call = apiService.createNetOrder(request);

        call.enqueue(new Callback<NewOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewOrderResponse> call, @NonNull Response<NewOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getResult();
                    if(result.equals("OK")) {
                        showConfirmationDialog();
                    }
                    else {
                        Toast.makeText(requireContext(),"Ocurrió un error:" + result, LENGTH_LONG).show();
                    }

                } else {
                    System.out.println("Error: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<NewOrderResponse> call, Throwable t) {
                System.out.println("Error: " + t.getMessage());
            }
        });
    }
    private boolean ValidateData(double amount) {
        if (amount == 0) {
            Toast.makeText(requireContext(),"Debes agregar articulos a tu orden.", LENGTH_LONG).show();
            shakeAnimation(btnAmount);
            return false;
        }
        String name = String.valueOf(clientName.getText());
        if (name.isEmpty()) {
            clientName.setBackgroundResource(R.drawable.edittext_error);
            message = context.getString(R.string.text_requerir_cliente);
            clientName.setError(message);
            Toast.makeText(requireContext(),message, LENGTH_SHORT).show();
            shakeAnimation(clientName);
            return false;
        }
        else {
            clientName.setBackgroundResource(R.drawable.edittext_normal);
        }
        if(!rbOnSite.isChecked() && !rbPick.isChecked()) {
            rbOnSite.setBackgroundResource(R.drawable.edittext_error);
            rbPick.setBackgroundResource(R.drawable.edittext_error);
            message = context.getString(R.string.text_requerir_tipo_orden);
            Toast.makeText(requireContext(),message, LENGTH_SHORT).show();
            shakeAnimation(rbOnSite);
            shakeAnimation(rbPick);
            return  false;
        }
        else {
            rbOnSite.setBackgroundResource(R.drawable.edittext_normal);
            rbPick.setBackgroundResource(R.drawable.edittext_normal);
        }
        return true;
    }
    private void shakeAnimation(View view) {
        Animation shake = new TranslateAnimation(0, 10, 0, 0);
        shake.setDuration(100);
        shake.setInterpolator(new CycleInterpolator(5));
        view.startAnimation(shake);
    }
    private void showConfirmationDialog() {
        cart.clearCart();
        String name = String.valueOf(clientName.getText());;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Pedido Confirmado")
                .setMessage("Gracias por tu compra " + name.toUpperCase() + "! Te avisaremos cuando esté lista.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> cerrarFragment());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Cerrar automáticamente después de 5 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                cerrarFragment();
            }
        }, 10000);
    }
    private void cerrarFragment() {
        Intent intent = new Intent(getActivity(), LanguageSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }
    private void onItemSelected(CartItem product) {
        products.remove(product);
        SharedCartSingleton.getInstance().removeItem(product);
        adapter.notifyDataSetChanged();
        CalculateTotal();
        if(mListener !=null) {
            mListener.onButtonRemoveClicked();
        }
    }

    private void openPaymentSelection(Double amount) {
        if(!ValidateData(amount))
            return;
        if(!ValidateItems())
            return;

        String nameClient = String.valueOf(clientName.getText());
        String orderType = rbPick.isChecked() ? "Para LLevar" : "Consumo en sitio";
        FragmentPaymentTypeSelection fragment = FragmentPaymentTypeSelection.newInstance(nameClient,orderType);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null) // Permite volver al fragmento anterior
                .commit();
    }
    public boolean ValidateItems() {
        List<Integer> categoriesFood = List.of(3, 5);
        long itemsFood = products.stream()
                .filter(product -> categoriesFood.contains(product.getCategory()))
                .count();

        int categoryId = 4;
        long alcoholItems = products.stream()
                .filter(product -> product.getCategory() == 4)
                .count();

        if(itemsFood == 0 && alcoholItems > 0 ) {
            String name = String.valueOf(clientName.getText());;
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Venta no permitida")
                    .setMessage("La venta de bebidas con alcohol solo se permite acompañado de alimentos")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> cerrarFragment());

            AlertDialog dialog = builder.create();
            dialog.show();
            return false;
        }
        else
            return true;

    }
    public interface OnButtonRemoveCartClickListener {
        void onButtonRemoveClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnButtonRemoveCartClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " debe implementar OnButtonClickListener");
        }
    }
}