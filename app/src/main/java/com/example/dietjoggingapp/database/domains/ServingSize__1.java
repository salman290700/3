
package com.example.dietjoggingapp.database.domains;

import javax.annotation.processing.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class ServingSize__1 {

    @SerializedName("scale")
    @Expose
    private Integer scale;
    @SerializedName("qty")
    @Expose
    private Integer qty;
    @SerializedName("grams")
    @Expose
    private Integer grams;
    @SerializedName("units")
    @Expose
    private String units;
    @SerializedName("originalWeight")
    @Expose
    private Integer originalWeight;
    @SerializedName("originalWeightUnits")
    @Expose
    private String originalWeightUnits;

    public Integer getScale() {
        return scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Integer getGrams() {
        return grams;
    }

    public void setGrams(Integer grams) {
        this.grams = grams;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public Integer getOriginalWeight() {
        return originalWeight;
    }

    public void setOriginalWeight(Integer originalWeight) {
        this.originalWeight = originalWeight;
    }

    public String getOriginalWeightUnits() {
        return originalWeightUnits;
    }

    public void setOriginalWeightUnits(String originalWeightUnits) {
        this.originalWeightUnits = originalWeightUnits;
    }

}
