package com.hrg.idolcafeclientapp.data.models;

public class CategoryRequest {
    private int CategoryId;

    // Constructor
    public CategoryRequest(int categoryId) {
        this.CategoryId = categoryId;
    }

    // Getter y Setter
    public int getCategoryId() {
        return CategoryId;
    }

    public void setCategoryId(int categoryId) {
        CategoryId = categoryId;
    }
}
