package com.hrg.idolcafeclientapp.ui;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.NewOrderDetail;
import com.hrg.idolcafeclientapp.data.models.NewOrderRequest;
import com.hrg.idolcafeclientapp.data.models.NewOrderResponse;
import com.hrg.idolcafeclientapp.data.network.RetrofitClient;
import com.hrg.idolcafeclientapp.data.repositories.ApiService;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedCartSingleton;
import com.hrg.idolcafeclientapp.utils.LocaleHelper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentPaymentTypeSelection#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentPaymentTypeSelection extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mClientName;
    private String mOrderType;
    private ImageButton btnPago1, btnPago2;
    private SharedCartSingleton cart;
    List<CartItem> products;
    private String confirmed;
    private String confirmed_message;
    private Context context;

    public FragmentPaymentTypeSelection() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PaymentTypeSelection.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentPaymentTypeSelection newInstance(String param1, String param2) {
        FragmentPaymentTypeSelection fragment = new FragmentPaymentTypeSelection();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mClientName = getArguments().getString(ARG_PARAM1);
            mOrderType = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_payment_type_selection, container, false);

        btnPago1 = view.findViewById(R.id.btnPago1);
        btnPago2 = view.findViewById(R.id.btnPago2);

        Drawable selectedBg = ContextCompat.getDrawable(requireContext(), R.drawable.btn_background_selected);
        Drawable normalBg = ContextCompat.getDrawable(requireContext(), R.drawable.btn_ripple);

        cart = SharedCartSingleton.getInstance();
        products = cart.getCartItems();

        btnPago1.setOnClickListener(v -> {
            btnPago1.setBackground(selectedBg);
            btnPago2.setBackground(normalBg);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error Pinpad")
                    .setMessage("No hay terminal instalada para el cobro con tarjeta.")
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> { btnPago1.setBackground(normalBg); });
            AlertDialog dialog = builder.create();
            dialog.show();
            //openPaymentSelection();
        });

        btnPago2.setOnClickListener(v -> {
            btnPago2.setBackground(selectedBg);
            btnPago1.setBackground(normalBg);
            SendNewOrder();
        });

        // Botón "Otro Pago" para volver atrás
        view.findViewById(R.id.btnOtroPago).setOnClickListener(v -> requireActivity().onBackPressed());

        context = LocaleHelper.wrap(requireContext());
        confirmed = context.getString(R.string.text_pedido_confirmado);
        return view;
    }
    private void openPaymentSelection() {
        FragmentCardPayment fragment = FragmentCardPayment.newInstance(mClientName,mOrderType);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
    }
    private void showConfirmationDialog(String clientName) {
        cart.clearCart();
        confirmed_message = String.format(
                context.getString(R.string.text_confirmar_cliente),
                clientName);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(confirmed)
                .setMessage(confirmed_message)
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
    private void SendNewOrder() {
        String name = mClientName;
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        String orderType = mOrderType;
        NewOrderRequest request = new NewOrderRequest();
        request.setName(name);
        request.setType(orderType);
        request.setPaymentStatus("2");
        request.setPaymentId("");
        for (CartItem item : products) {
            NewOrderDetail itemDetail = new NewOrderDetail();
            itemDetail.setPrice(item.getPrice());
            itemDetail.setId(item.getItemId());
            itemDetail.setQuantity(item.getQty());
            String notes = item.getNotes().isEmpty() ? item.getNotesCompl() : item.getNotes() + '-' + item.getNotesCompl();
            itemDetail.setNotes(notes);
            request.AddItem(itemDetail);
        }
        ApiService apiService = RetrofitClient.getApiService();
        Call<NewOrderResponse> call = apiService.createNetOrder(request);

        String clientName = name;
        call.enqueue(new Callback<NewOrderResponse>() {
            @Override
            public void onResponse(@NonNull Call<NewOrderResponse> call, @NonNull Response<NewOrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getResult();
                    if(result.equals("OK")) {
                        showConfirmationDialog(clientName);
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
    private void cerrarFragment() {
        Intent intent = new Intent(getActivity(), LanguageSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

}