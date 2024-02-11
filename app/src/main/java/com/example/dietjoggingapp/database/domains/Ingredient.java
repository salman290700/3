
package com.example.movieapp;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class Ingredient {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("servingSize")
    @Expose
    private ServingSize servingSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ServingSize getServingSize() {
        return servingSize;
    }

    public void setServingSize(ServingSize servingSize) {
        this.servingSize = servingSize;
    }

}
