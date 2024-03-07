package com.example.dietjoggingapp.database.domains;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ListFoodSuggest {
    @SerializedName("")
    @Expose
    private List<FoodSuggest> data;

    public List<FoodSuggest> getData() {
        return data;
    }

    public void setData(List<FoodSuggest> data) {
        this.data = data;
    }
}
