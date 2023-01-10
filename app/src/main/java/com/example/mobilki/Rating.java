package com.example.mobilki;

public class Rating {
    String name, image, rating, comment;

    public Rating(){

    }

    public Rating(String name, String image, String rating, String comment) {
        this.name = name;
        this.image = image;
        this.rating = rating;
        this.comment = comment;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

}
