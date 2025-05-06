package com.hrg.idolcafeclientapp.data.models;

public class NewOrderDetail {
    private int Id;
    private  int Quantity;
    private String Notes;
    private double Price;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getNotes() {
        return Notes;
    }

    public int getQuantity() {
        return Quantity;
    }

    public void setQuantity(int quantity) {
        Quantity = quantity;
    }

    public void setPrice(double price) {
        Price = price;
    }
}
