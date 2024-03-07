
package com.example.dietjoggingapp.database.domains;

import javax.annotation.processing.Generated;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Generated("jsonschema2pojo")
public class ServingSize {

    @SerializedName("units")
    @Expose
    private String units;
    @SerializedName("desc")
    @Expose
    private String desc;
    @SerializedName("qty")
    @Expose
    private Float qty;
    @SerializedName("grams")
    @Expose
    private Float grams;
    @SerializedName("scale")
    @Expose
    private Float scale;

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Float getQty() {
        return qty;
    }

    public void setQty(Float qty) {
        this.qty = qty;
    }

    public Float getGrams() {
        return grams;
    }

    public void setGrams(Float grams) {
        this.grams = grams;
    }

    public Float getScale() {
        return scale;
    }

    public void setScale(Float scale) {
        this.scale = scale;
    }

}
