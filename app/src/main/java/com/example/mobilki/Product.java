package com.example.mobilki;

public class Product {
    private String name, image, calories, carbohydrates, proteins, fat;

    public Product(){

    }

    public Product(String name, String image, String calories, String carbohydrates, String proteins, String fat) {
        this.name = name;
        this.image = image;
        this.calories = calories;
        this.carbohydrates = carbohydrates;
        this.proteins = proteins;
        this.fat = fat;
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
