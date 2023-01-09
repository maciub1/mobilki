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

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
