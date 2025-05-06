package com.hrg.idolcafeclientapp.data.models;

import java.util.ArrayList;
import java.util.List;

public class NewOrderRequest {
    private String Name;
    private String type;
    private String PaymentStatus;
    private String PaymentId;
    private List<NewOrderDetail> Items;
    public NewOrderRequest() {
        Items = new ArrayList<>();
    }

    public String getPaymentId() {
        return PaymentId;
    }

    public void setPaymentId(String paymentId) {
        PaymentId = paymentId;
    }

    public void setPaymentStatus(String paymentStatus) {
        PaymentStatus = paymentStatus;
    }

    public String getPaymentStatus() {
        return PaymentStatus;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<NewOrderDetail> getItems() {
        return Items;
    }

    public void setItems(List<NewOrderDetail> items) {
        Items = items;
    }
    public void AddItem(NewOrderDetail item) {
        Items.add(item);
    }
}
