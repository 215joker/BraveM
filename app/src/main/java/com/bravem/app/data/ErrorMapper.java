package com.bravem.app.data;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class ErrorMapper {
    public static String map(Exception e) {
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Invalid password. Please try again.";
        } else if (e instanceof FirebaseAuthInvalidUserException) {
            return "No account found with this email.";
        } else if (e instanceof FirebaseAuthUserCollisionException) {
            return "An account already exists with this email.";
        } else if (e != null && e.getMessage() != null) {
            if (e.getMessage().contains("network")) return "Network error. Check your connection.";
            return e.getMessage();
        }
        return "An unknown error occurred.";
    }
}
