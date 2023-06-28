package com.example.crime_report_management_system_miniproject.Java_Classes;

public class PostCrimeHelper {

    public String uid,title, description, image, condition, latitude, longitude, type;
    public String year;
    public String month;
    public PostCrimeHelper(){

    }

    public PostCrimeHelper(String title, String description, String image, String condition, String latitude, String longitude, String type, String month, String year) {
        this.title = title;
        this.description = description;
        this.image = image;
        this.condition = condition;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.uid = uid;
        this.month=month;
        this.year=year;
    }
    public String getMonth() {
        return month;
    }
    public String getYear() {
        return year;
    }


    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }



    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String image) {
        this.longitude = longitude;
    }



    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
