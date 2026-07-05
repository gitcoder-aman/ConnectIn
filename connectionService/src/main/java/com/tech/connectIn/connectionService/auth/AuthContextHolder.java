package com.tech.connectIn.connectionService.auth;

public class AuthContextHolder {

    //It creates one separate copy of the variable for every thread.
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static Long getCurrentUserId(){
        return currentUserId.get();
    }
    static void setCurrentUserId(Long userId){
        currentUserId.set(userId);
    }

    static void clear(){
        currentUserId.remove();
    }
}

