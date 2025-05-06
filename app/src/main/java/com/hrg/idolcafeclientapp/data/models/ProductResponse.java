package com.hrg.idolcafeclientapp.data.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductResponse {
    @SerializedName("Products")
    private List<Product> Products;
    public List<Product> getProducts() {
        return Products;
    }
}
