package org.beatrice.diploma_new_pharmacy.auth.util;

public class PhoneNormalizer {
    public static String normalize(String phone) {
        String digits = phone.replaceAll("[^0-9+]", "");

        if (digits.startsWith("8") && digits.length() == 11) {
            return "+7" + digits.substring(1);
        }

        if (digits.startsWith("+7") && digits.length() == 12) {
            return digits;
        }

        throw new IllegalArgumentException("Invalid phone number format");

    }
}
