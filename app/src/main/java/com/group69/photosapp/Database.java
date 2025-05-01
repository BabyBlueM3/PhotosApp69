package com.group69.photosapp;

import android.content.Context;

import java.io.*;
import java.util.ArrayList;

public class Database {

    // Save users to a file in internal storage
    public static void saveUsers(Context context, ArrayList<User> users) {
        try {
            // Open a private file in internal storage
            FileOutputStream fileOut = context.openFileOutput("users.dat", Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(users);
            out.close();
            fileOut.close();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    // Load users from the file in internal storage
    @SuppressWarnings("unchecked")
    public static ArrayList<User> loadUsers(Context context) {
        try {
            FileInputStream fileIn = context.openFileInput("users.dat");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            ArrayList<User> users = (ArrayList<User>) in.readObject();
            in.close();
            fileIn.close();
            return users;
        } catch (IOException | ClassNotFoundException e) {
            // If no file found or error occurs, return an empty list
            return new ArrayList<>();
        }
    }
}
