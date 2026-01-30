package com.hrg.idolcafeclientapp.ui;


import static android.widget.Toast.LENGTH_LONG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.hrg.idolcafeclientapp.R;
import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.NewOrderDetail;
import com.hrg.idolcafeclientapp.data.models.NewOrderRequest;
import com.hrg.idolcafeclientapp.data.models.NewOrderResponse;
import com.hrg.idolcafeclientapp.data.models.PaymentInfoRequest;
import com.hrg.idolcafeclientapp.data.models.PaymentResponse;
import com.hrg.idolcafeclientapp.data.network.RetrofitClient;
import com.hrg.idolcafeclientapp.data.repositories.ApiService;
import com.hrg.idolcafeclientapp.data.viewmodel.SharedCartSingleton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentCardPayment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentCardPayment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SharedCartSingleton cart;

    // TODO: Rename and change types of parameters
    private String mClientName;
    private String mOrderType;
    private String PaymentId;
    List<CartItem> products;
    private final BroadcastReceiver paymentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("FCM", "Respuesta pinpad");
            if ("PAYMENT_RECEIVE".equals(intent.getAction())) {
                String status = intent.getStringExtra("body");
                PaymentId = intent.getStringExtra("paymentid");
                //Log.d("FCM", "Trajo el status");
                assert status != null;
                Log.d("FCM", status);
                if(status.equals("approved")) {
                    SendNewOrder();
                    showConfirmationDialog(status);
                }
                else
                    showErrorDialog(status);
            }
        }
    };

    public FragmentCardPayment() {
        // Required empty public constructor
    }

    public static FragmentCardPayment newInstance(String param1, String param2) {
        FragmentCardPayment fragment = new FragmentCardPayment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_payment, container, false);
        ImageView imageView = view.findViewById(R.id.imgPinpad);
        Glide.with(this)
                .asGif()
                .load(R.drawable.cardpayment_mov)
                .fitCenter()
                .into(imageView);

        cart = SharedCartSingleton.getInstance();

        products = cart.getCartItems();

        view.findViewById(R.id.btnCardPaymentBack).setOnClickListener(v -> requireActivity().onBackPressed());

        sendPaymetRequest();
        return view;
    }
    private void sendPaymetRequest() {
        double totalAmount = products.stream()
                .mapToDouble(CartItem::getAmount)
                .sum();
        totalAmount = totalAmount  * 100;
        int amount = (int)totalAmount;

        PaymentInfoRequest request = new PaymentInfoRequest();
        request.setAmount(amount);

        ApiService apiService = RetrofitClient.getApiService();
        Call<PaymentResponse> call = apiService.SendPaymentRequest(request);

        call.enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentResponse> call, @NonNull Response<PaymentResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String result = response.body().getMessage();
                    if(result.contains("Error")) {
                        showErrorDialog(result);
                    }
                } else {
                    showErrorDialog(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentResponse> call, Throwable t) {
                showErrorDialog(t.getMessage());
            }
        });

    }
    private void showConfirmationDialog(String status) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Cobro realizado.")
                .setMessage("Gracias por tu compra " +  mClientName.toUpperCase() + ". Te avisaremos cuando esté lista tu orden.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> cerrarFragment());

        AlertDialog dialog = builder.create();
        dialog.show();
        cart.clearCart();

        // Cerrar automáticamente después de 5 segundos
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
                cerrarFragment();
            }
        }, 10000);
    }

    private void showErrorDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        if(message.equals("CANCELED")) {
            message = "Se ha salido de la pantalla de cobro en la pinpad, vuelve a intentar.";
        }
        else if (message.equals("rejected")) {
            message ="El cobro ha sido rechazado por su banco, intenta con otro medio de pago.";
        }
        else {
            message = "Hay un problema con el cobro, por favor solicita apoyo a nuestro personal.(" + message + ")";
        }

        builder.setTitle("Error Pinpad")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> backToPaymentSelection());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void backToPaymentSelection() {
        requireActivity().getSupportFragmentManager().popBackStack();
    }
    private void cerrarFragment() {
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    private void SendNewOrder() {
        String name = mClientName;
        name = name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
        String orderType = mOrderType;
        NewOrderRequest request = new NewOrderRequest();
        request.setName(name);
        request.setType(orderType);
        request.setPaymentStatus("1");
        request.setPaymentId(PaymentId);
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
                        //showConfirmationDialog("");
                    }
                    else {
                        //Toast.makeText(requireContext(),"Ocurrió un error:" + result, LENGTH_LONG).show();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mClientName = getArguments().getString(ARG_PARAM1);
            mOrderType = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Desregistra el BroadcastReceiver
        //getActivity().unregisterReceiver(paymentReceiver);
    }
    @Override
    public void onResume() {
        super.onResume();
        // Registra el BroadcastReceiver
   /*     IntentFilter filter = new IntentFilter("PAYMENT_RECEIVE");
        ContextCompat.registerReceiver(getActivity(), paymentReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);*/
    }
    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("PAYMENT_RECEIVE");
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(paymentReceiver, filter);
        Log.d("FCM", "Fragment en onStart");
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(paymentReceiver);
        super.onStop();
    }
}