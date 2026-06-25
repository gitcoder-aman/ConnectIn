package com.tech.connectIn.utils;

import static org.mindrot.jbcrypt.BCrypt.*;

public class BCryptUtil {
    public static String hash(String s){
        return hashpw(s,gensalt());
    }

    public static boolean match(String passwordText,String passwordHashed){
        return checkpw(passwordText,passwordHashed);
    }
}
