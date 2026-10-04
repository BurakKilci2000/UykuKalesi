package uykukalesi.kayit;

import java.util.Locale;

/**
 * Günlükte ve arayüzde sayıların tek tip görünmesi için küçük yardımcı.
 * Tam sayılar "20", küsuratlılar en fazla iki basamakla "6.67" biçiminde yazılır.
 */
public final class Bicim {

    private Bicim() {
        // Yardımcı sınıf: nesnesi oluşturulmaz.
    }

    public static String sayi(double deger) {
        if (Math.abs(deger - Math.rint(deger)) < 1e-9) {
            return String.valueOf((long) Math.rint(deger));
        }
        String metin = String.format(Locale.ROOT, "%.2f", deger);
        if (metin.endsWith("0")) {
            metin = metin.substring(0, metin.length() - 1);
        }
        return metin;
    }
}
