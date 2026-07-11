package com.tech.connectIn.connectionService.auth;

public class AuthContextHolder {

    //It creates one separate copy of the variable for every thread.
    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static Long getCurrentUserId(){
        System.out.println("GET = " + currentUserId.get());
        return currentUserId.get();
    }
    static void setCurrentUserId(Long userId){
        System.out.println("SET = " + userId);
        currentUserId.set(userId);
    }

    static void clear(){
        currentUserId.remove();
    }
}

