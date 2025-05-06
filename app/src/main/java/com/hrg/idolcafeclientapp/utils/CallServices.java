package com.hrg.idolcafeclientapp.utils;

import androidx.annotation.NonNull;

import com.hrg.idolcafeclientapp.data.models.CategoryRequest;
import com.hrg.idolcafeclientapp.data.models.Product;
import com.hrg.idolcafeclientapp.data.models.ProductResponse;
import com.hrg.idolcafeclientapp.data.network.RetrofitClient;
import com.hrg.idolcafeclientapp.data.repositories.ApiService;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallServices {
    public static void FillProductList(ProductLoadCallback callback) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<ProductResponse> call = apiService.getProducts();

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getProducts();
                    if (!products.isEmpty()) {
                        Product firstProduct = products.get(0);
                        callback.onProductsLoaded(products);
                    }
                } else {
                    callback.onProductsLoadFailed("Error al obtener los productos");
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                callback.onProductsLoadFailed("Error de conexión: " + t.getMessage());
            }
        });
    }
    public static void FillProductByCategoryList(int categoryId, ProductLoadCallback callback) {
        ApiService apiService = RetrofitClient.getApiService();
        CategoryRequest request = new CategoryRequest(categoryId);
        Call<ProductResponse> call = apiService.getProductsByCategoryId(request);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> products = response.body().getProducts();
                    if (!products.isEmpty()) callback.onProductsLoaded(products);
                } else callback.onProductsLoadFailed("Error al obtener los productos");
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                callback.onProductsLoadFailed("Error de conexión: " + t.getMessage());
            }
        });
    }
}
