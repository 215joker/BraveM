package com.bravem.app.data;

public class ErrorMapper {
    public static String map(Exception e) {
        if (e == null) return "Unknown error";
        String message = e.getMessage();
        if (message == null) return "An unexpected error occurred";
        return message;
    }
}
