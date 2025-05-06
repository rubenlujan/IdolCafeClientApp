package com.hrg.idolcafeclientapp.data.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;

import java.util.ArrayList;
import java.util.List;

public class SharedItemComplementSingleton  {
    private static SharedItemComplementSingleton instance;
    private List<ItemComplement> complements = new ArrayList<>();
    private SharedItemComplementSingleton() {}

    public static SharedItemComplementSingleton getInstance() {
        if (instance == null) {
            instance = new SharedItemComplementSingleton();
        }
        return instance;
    }
    public List<ItemComplement> getComplements() {
        return complements;
    }
    public List<ItemComplement> getComplementsById(int itemId) {
        List<ItemComplement> items = new ArrayList<>();
        for (ItemComplement item : complements) {
            if (item.getItemId() == itemId) {
                items.add(item);
            }
        }
        return  items;
    }
    public void setComplements(List<ItemComplement> list) {
        complements = list;
    }
    public void clearComponents() {
        complements.clear();
    }
}
