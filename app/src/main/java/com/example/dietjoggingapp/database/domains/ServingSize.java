
package com.example.movieapp;

import javax.annotation.Generated;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class ServingSize {

    @SerializedName("units")
    @Expose
    private String units;
    @SerializedName("qty")
    @Expose
    private Double qty;
    @SerializedName("grams")
    @Expose
    private Double grams;
    @SerializedName("scale")
    @Expose
    private Double scale;

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Double getQty() {
        return qty;
    }

    public void setQty(Double qty) {
        this.qty = qty;
    }

    public Double getGrams() {
        return grams;
    }

    public void setGrams(Double grams) {
        this.grams = grams;
    }

    public Double getScale() {
        return scale;
    }

    public void setScale(Double scale) {
        this.scale = scale;
    }

}
