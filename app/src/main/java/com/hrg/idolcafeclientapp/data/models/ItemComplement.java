package com.hrg.idolcafeclientapp.data.models;

public class ItemComplement {
    private int Id;
    private int ItemId;
    private String Category;
    private int ItemComplementId;
    private String Description;
    private String DescriptionEnglish;
    private double Price;

    public String getDescriptionEnglish() {
        return DescriptionEnglish;
    }

    public void setDescriptionEnglish(String descriptionEnglish) {
        DescriptionEnglish = descriptionEnglish;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getCategory() {
        return Category;
    }

    public int getItemId() {
        return ItemId;
    }

    public void setItemId(int itemId) {
        ItemId = itemId;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDescription() {
        return Description;
    }

    public int getItemComplementId() {
        return ItemComplementId;
    }

    public void setItemComplementId(int itemComplementId) {
        ItemComplementId = itemComplementId;
    }
}
