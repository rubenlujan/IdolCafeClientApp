package com.hrg.idolcafeclientapp.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.hrg.idolcafeclientapp.data.models.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductListSharedVM extends AndroidViewModel {
    private final MediatorLiveData<List<Product>> productList = new MediatorLiveData<>();

    public ProductListSharedVM(@NonNull Application application) {
        super(application);
        productList.setValue(new ArrayList<>());
    }

    public LiveData<List<Product>> getProductList() {
        return productList;
    }

    public void setProductList(List<Product> products) {
        productList.postValue(products);
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        return Transformations.map(productList, allProducts -> {
            List<Product> filtered = new ArrayList<>();
            if (allProducts != null) {
                for (Product p : allProducts) {
                    if (p.getCategoryId() == categoryId) {
                        filtered.add(p);
                    }
                }
            }
            return filtered;
        });
    }
}
