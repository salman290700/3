
package com.example.dietjoggingapp.database.domains;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.processing.Generated;

@Generated("jsonschema2pojo")
public class FoodSuggest {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("tags")
    @Expose
    private List<String> tags;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("prepareTime")
    @Expose
    private Integer prepareTime;
    @SerializedName("cookTime")
    @Expose
    private Integer cookTime;
    @SerializedName("ingredients")
    @Expose
    private List<com.example.movieapp.Ingredient> ingredients;
    @SerializedName("steps")
    @Expose
    private List<String> steps;
    @SerializedName("servings")
    @Expose
    private Integer servings;
    @SerializedName("servingSizes")
    @Expose
    private List<com.example.movieapp.ServingSize__1> servingSizes;
    @SerializedName("nutrients")
    @Expose
    private com.example.movieapp.Nutrients nutrients;
    @SerializedName("image")
    @Expose
    private String image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrepareTime() {
        return prepareTime;
    }

    public void setPrepareTime(Integer prepareTime) {
        this.prepareTime = prepareTime;
    }

    public Integer getCookTime() {
        return cookTime;
    }

    public void setCookTime(Integer cookTime) {
        this.cookTime = cookTime;
    }

    public List<com.example.movieapp.Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<com.example.movieapp.Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public Integer getServings() {
        return servings;
    }

    public void setServings(Integer servings) {
        this.servings = servings;
    }

    public List<com.example.movieapp.ServingSize__1> getServingSizes() {
        return servingSizes;
    }

    public void setServingSizes(List<com.example.movieapp.ServingSize__1> servingSizes) {
        this.servingSizes = servingSizes;
    }

    public com.example.movieapp.Nutrients getNutrients() {
        return nutrients;
    }

    public void setNutrients(com.example.movieapp.Nutrients nutrients) {
        this.nutrients = nutrients;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
