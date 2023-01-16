package com.example.mobilki;

public class Diet {
    private String name;
    private String image;
    private String rating;

    public Diet() {

    }

    public Diet(String name, String image, String desc, String rating) {
        this.name = name;
        this.image = image;
        this.rating = rating;
    }

    public String getName() {
        return name;
    }

    public String getRating() {
        return rating;
    }

    public String getImage() {
        return image;
    }

}
