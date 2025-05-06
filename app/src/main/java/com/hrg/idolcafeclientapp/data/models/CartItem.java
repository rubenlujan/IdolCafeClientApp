package com.hrg.idolcafeclientapp.data.models;

import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private int Id;
    private int ItemId;
    private String Description;
    private Double Amount;
    private int Qty;
    private Double Price;
    private String Notes;
    private String NotesCompl;
    private String Name;
    private int Category;
    private int PromoActive;
    private List<ItemComplement> Complements;
    public CartItem(int ItemId,  int Qty, String Notes, String Name, String Description, Double Price, Double Amount) {
        this.ItemId = ItemId;
        this.Qty = Qty;
        this.Notes = Notes;
        this.Name = Name;
        this.Description = Description;
        this.Amount = Amount;
        this.Price = Price;
        Complements = new ArrayList<>();
    }

    public String getNotesCompl() {
        return NotesCompl;
    }

    public void setNotesCompl(String notesCompl) {
        NotesCompl = notesCompl;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getId() {
        return Id;
    }

    public void setComplements(List<ItemComplement> complements) {
        Complements = complements;
    }

    public List<ItemComplement> getComplements() {
        return Complements;
    }
    public void addComplement(ItemComplement item) {
        Complements.add(item);
    }
    public void clearCategoryComponent(String category) {
        for (ItemComplement item : Complements) {
            if (item.getCategory() == category) {
                Complements.remove(item);
                return;
            }
        }
    }

    public void setPromoActive(int promoActive) {
        PromoActive = promoActive;
    }

    public int getPromoActive() {
        return PromoActive;
    }

    public int getCategory() {
        return Category;
    }

    public void setCategory(int category) {
        Category = category;
    }

    public Double getPrice() {
        return Price;
    }

    public void setPrice(Double price) {
        Price = price;
    }

    public int getItemId() {
        return ItemId;
    }

    public void setItemId(int itemId) {
        ItemId = itemId;
    }

    public int getQty() {
        return Qty;
    }

    public void setQty(int qty) {
        Qty = qty;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        Notes = notes;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Double getAmount() {
        return Amount;
    }

    public void setAmount(Double amount) {
        Amount = amount;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }
}
