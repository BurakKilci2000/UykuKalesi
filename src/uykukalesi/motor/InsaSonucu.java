package uykukalesi.motor;

/** Savunucu kurma denemesinin sonucu. */
public enum InsaSonucu {
    BASARILI("Savunucu kuruldu."),
    YETERSIZ_SEKER("Yeterli şeker yok."),
    YUVA_DOLU("Bu yuvada zaten bir savunucu var."),
    OYUN_BITTI("Oyun bitti; yeni savunucu kurulamaz.");

    private final String mesaj;

    InsaSonucu(String mesaj) {
        this.mesaj = mesaj;
    }

    public String getMesaj() {
        return mesaj;
    }
}
