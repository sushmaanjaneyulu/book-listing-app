package com.example.android.booklistingapp;

public class Book {

    private String mTitle;
    private StringBuilder mAuthor;
    private StringBuilder mCategory;
    private String mFirstCategory;
    private String mDescription;
    private String mUrl;

    public Book(String title, StringBuilder author, StringBuilder category, String firstCategory, String description, String url){
        mTitle = title;
        mAuthor = author;
        mCategory = category;
        mFirstCategory = firstCategory;
        mDescription = description;
        mUrl = url;
    }

    public String getTitle(){
        return mTitle;
    }

    public StringBuilder getAuthor(){
        return mAuthor;
    }

    public StringBuilder getCategory(){
        return mCategory;
    }

    public String getFirstCategory() {
        return mFirstCategory;
    }

    public String getmDescription(){
        return mDescription;
    }

    public String getUrl(){
        return mUrl;
    }
}
