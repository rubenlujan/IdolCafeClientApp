package com.hrg.idolcafeclientapp.data.repositories;

import com.hrg.idolcafeclientapp.data.models.APIMPagoDataResponse;
import com.hrg.idolcafeclientapp.data.models.CategoryRequest;
import com.hrg.idolcafeclientapp.data.models.ItemComplementRequest;
import com.hrg.idolcafeclientapp.data.models.ItemComplementResponse;
import com.hrg.idolcafeclientapp.data.models.NewOrderRequest;
import com.hrg.idolcafeclientapp.data.models.NewOrderResponse;
import com.hrg.idolcafeclientapp.data.models.PaymentInfoRequest;
import com.hrg.idolcafeclientapp.data.models.PaymentResponse;
import com.hrg.idolcafeclientapp.data.models.ProductResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("Products/GetItemComplements")
    Call<ItemComplementResponse> getItemComplements(@Body ItemComplementRequest request);
    @POST("Products/GetAllActiveProductsMenu")
    Call<ProductResponse> getProducts();
    @POST("Products/GetProductsMenuByCategory")
    Call<ProductResponse> getProductsByCategoryId(@Body CategoryRequest request);
    @POST("Orders/CreateNewOrder")
    Call<NewOrderResponse> createNetOrder(@Body NewOrderRequest request);
    @POST("Orders/GetAPIMPagoData")
    Call<APIMPagoDataResponse> getAPIMPData();
    @POST("Orders/SendPaymentRequest")
    Call<PaymentResponse> SendPaymentRequest(@Body PaymentInfoRequest request);

}
