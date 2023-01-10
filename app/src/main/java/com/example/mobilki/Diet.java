package com.example.mobilki;

public class Diet {
    private String name, image, desc, rating;

    public Diet(){

    }

    public Diet(String name, String image, String desc, String rating)
    {
        this.name = name;
        this.image = image;
        this.desc = desc;
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
