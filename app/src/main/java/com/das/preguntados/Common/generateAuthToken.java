package com.das.preguntados.Common;

import java.security.SecureRandom;

public class generateAuthToken {
    //Clase para generar un token de autenticacion
    /**Extraido de Stack Overflow
       Pregunta: https://stackoverflow.com/questions/41107/how-to-generate-a-random-alpha-numeric-string/41156#41156
     */
    public static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final int SECURE_TOKEN_LENGTH = 128;
    private static final SecureRandom random = new SecureRandom();
    private static final char[] symbols = CHARACTERS.toCharArray();
    private static final char[] buf = new char[SECURE_TOKEN_LENGTH];

    public static String generateToken() {
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }
}
