package com.hrg.idolcafeclientapp.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Product implements Parcelable {
    private int CategoryId;
    private int Id;
    private  String Name;
    private String Description;
    private String Description_English;
    private double Price;
    private String Image;
    private String Status;
    private int PromoActive;

    public String getDescription_English() {
        return Description_English;
    }

    public void setDescription_English(String description_English) {
        Description_English = description_English;
    }

    public int getPromoActive() {
        return PromoActive;
    }
    public void setPromoActive(int promoActive) {
        PromoActive = promoActive;
    }

    public Product(int id, int categoryId, String name, String description, double price, String image, String status, int promo) {
        this.Id = id;
        this.CategoryId = categoryId;
        this.Name = name;
        this.Description = description;
        this.Price = price;
        this.Image = image;
        this.Status = status;
        this.PromoActive = promo;
    }

    protected Product(Parcel in) {
        this.Id = in.readInt();
        this.CategoryId = in.readInt();
        this.Name = in.readString();
        this.Description = in.readString();
        this.Price = in.readDouble();
        this.Image = in.readString();
        this.Status = in.readString();
        this.PromoActive = in.readInt();
    }

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(Id);
        parcel.writeInt(CategoryId);
        parcel.writeString(Name);
        parcel.writeString(Description);
        parcel.writeDouble(Price);
        parcel.writeString(Image);
        parcel.writeString(Status);
        parcel.writeInt(PromoActive);

    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public int getCategoryId() {
        return CategoryId;
    }
    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getImage() {
        return Image;
    }
    public void setImage(String image){
        Image = image;
    }
    public String getName() {
        return  Name;
    }
    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public double getPrice() {
        return Price;
    }
    public void setPrice(double price) {
        Price = price;
    }
}
