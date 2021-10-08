package com.ict3104.t10_idk_2020.model;

public class Feature {
    private String age;
    private String commuteMethod;
    private String gender;
    private String name;
    private String rating;
    private String location;
    private String feedback;

    public Feature(){}

    public Feature(String age, String commuteMethod, String gender, String name, String rating, String location, String feedback) {
        this.age = age;
        this.commuteMethod = commuteMethod;
        this.gender = gender;
        this.name = name;
        this.rating = rating;
        this.location = location;
        this.feedback = feedback;
    }


    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getCommuteMethod() {
        return commuteMethod;
    }

    public void setCommuteMethod(String commuteMethod) {
        this.commuteMethod = commuteMethod;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

}
