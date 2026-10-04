package uykukalesi.motor;

/**
 * Motor → Arayüz haberleşmesi (Gözlemci / Observer deseni).
 * Motor arayüzün hangi sınıf olduğunu bilmez; yalnızca bu arayüzü çağırır.
 */
public interface OyunOlayDinleyicisi {

    /** Günlüğe yeni bir satır eklendi (zaman damgalı). */
    void gunlukSatiriEklendi(String satir);

    /** Oyun bitti: kazanildi true ise KAZANDINIZ, false ise KAYBETTİNİZ. */
    void oyunBitti(boolean kazanildi);
}
