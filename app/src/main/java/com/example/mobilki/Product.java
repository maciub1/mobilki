package com.example.mobilki;

public class Product {
    private String name;
    private String image;
    private String calories;

    public Product() {

    }

    public Product(String name, String image, String calories, String carbohydrates, String proteins, String fat) {
        this.name = name;
        this.image = image;
        this.calories = calories;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getCalories() {
        return calories;
    }

}
