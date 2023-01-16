package com.example.mobilki;

public class Meal {
    private String name;
    private String image;
    private String calories;

    public Meal() {

    }

    public Meal(String name, String image, String calories, String carbohydrates, String proteins, String fat) {
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
