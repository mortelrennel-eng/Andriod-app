package com.example.finalproject;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtils {
    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseDatabase getDatabase() {
        return FirebaseDatabase.getInstance("https://finalproject-b08f4-default-rtdb.firebaseio.com/");
    }
}
