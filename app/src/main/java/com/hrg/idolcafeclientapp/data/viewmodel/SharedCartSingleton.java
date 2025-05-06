package com.hrg.idolcafeclientapp.data.viewmodel;

import com.hrg.idolcafeclientapp.data.models.CartItem;
import com.hrg.idolcafeclientapp.data.models.ItemComplement;

import java.util.ArrayList;
import java.util.List;

public class SharedCartSingleton {
    private static SharedCartSingleton instance;
    private List<CartItem> cartItems = new ArrayList<>();

    private SharedCartSingleton() {}

    public static SharedCartSingleton getInstance() {
        if (instance == null) {
            instance = new SharedCartSingleton();
        }
        return instance;
    }

    public List<CartItem> getCartItems() {
        return cartItems;
    }
    public CartItem getItemById(int itemId) {
        for (CartItem item : cartItems) {
            if (item.getItemId() == itemId) {
                return item; // Devuelve el primer item encontrado con el itemId especificado
            }
        }
        return null; // Retorna null si no se encuentra ningún elemento
    }

    public void addItem(CartItem item) {
        cartItems.add(item);
    }
    public void removeItem(CartItem item) {
        cartItems.remove(item);
    }

    public void clearCart() {
        cartItems.clear();
    }
}
