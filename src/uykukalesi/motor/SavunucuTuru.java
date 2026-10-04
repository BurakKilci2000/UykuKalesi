package uykukalesi.motor;

import uykukalesi.varlik.BilyeMancinigi;
import uykukalesi.varlik.LastikSapan;
import uykukalesi.varlik.OyuncakSavunucu;
import uykukalesi.varlik.SuTabancasi;

/**
 * Kurulabilecek savunucu türleri. Her sabit kendi nesnesini üretir (fabrika metodu),
 * böylece arayüz somut sınıfları bilmeden savunucu kurdurabilir.
 */
public enum SavunucuTuru {

    LASTIK_SAPAN("Lastik Sapan", LastikSapan.MALIYET, LastikSapan.MENZIL,
            "Hasar 10 • 1 sn • tek hedef • zırhlıya %50 ceza") {
        @Override
        public OyuncakSavunucu uret(String kimlik, double x, double y) {
            return new LastikSapan(kimlik, x, y);
        }
    },
    BILYE_MANCINIGI("Bilye Mancınığı", BilyeMancinigi.MALIYET, BilyeMancinigi.MENZIL,
            "Hasar 20 • 3 sn • 50 px alan hasarı • uçanları vuramaz") {
        @Override
        public OyuncakSavunucu uret(String kimlik, double x, double y) {
            return new BilyeMancinigi(kimlik, x, y);
        }
    },
    SU_TABANCASI("Su Tabancası", SuTabancasi.MALIYET, SuTabancasi.MENZIL,
            "Hasar 15 • 2 sn • ıslatır: 3 sn boyunca %50 yavaşlatır") {
        @Override
        public OyuncakSavunucu uret(String kimlik, double x, double y) {
            return new SuTabancasi(kimlik, x, y);
        }
    };

    private final String gorunenAd;
    private final int maliyet;
    private final double menzil;
    private final String aciklama;

    SavunucuTuru(String gorunenAd, int maliyet, double menzil, String aciklama) {
        this.gorunenAd = gorunenAd;
        this.maliyet = maliyet;
        this.menzil = menzil;
        this.aciklama = aciklama;
    }

    public abstract OyuncakSavunucu uret(String kimlik, double x, double y);

    public String getGorunenAd() {
        return gorunenAd;
    }

    public int getMaliyet() {
        return maliyet;
    }

    public double getMenzil() {
        return menzil;
    }

    public String getAciklama() {
        return aciklama;
    }
}
