package com.hrg.idolcafeclientapp.utils;

import com.hrg.idolcafeclientapp.data.models.Product;

import java.util.List;

public interface ProductLoadCallback {
    void onProductsLoaded(List<Product> products);
    void onProductsLoadFailed(String errorMessage);
}
