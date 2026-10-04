package uykukalesi.varlik;

import java.util.List;

/**
 * Savunucuların ve fırlatmaların simülasyon motoruyla konuştuğu soyut arayüz.
 * Varlıklar motorun kendisini değil yalnızca bu sözleşmeyi bilir (gevşek bağlılık).
 * SimulasyonMotoru bu arayüzü uygular.
 */
public interface SavasAlani {

    /** Şu an haritada yürüyen/uçan kabusların salt-okunur listesi. */
    List<KabusYaratigi> getSahadakiYaratiklar();

    void firlatmaEkle(Firlatma firlatma);

    void efektEkle(GorselEfekt efekt);

    /** Olayı zaman damgasıyla savunma_gunlugu.txt dosyasına ve ekrandaki günlüğe yazar. */
    void gunlugeYaz(String olay);
}
