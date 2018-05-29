package com.example.tamir.sharenotes;

public class LibraryInformation {

    //Class is used to push the values to Firebase

    public String Name;
    public double lat;
    public double lng;

    public LibraryInformation() {

    }

    public LibraryInformation(String Name, double lat, double lng){
        this.Name = Name;
        this.lat = lat;
        this.lng = lng;
    }
}
