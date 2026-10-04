package uykukalesi.motor;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Senaryo haritası: halı yolu, oyuncak yuvaları (inşa alanları) ve boyutlar.
 * Tüm koordinatlar 880 x 560 piksellik mantıksal harita düzlemindedir;
 * arayüz bu düzlemi pencere boyutuna göre ölçekler.
 */
public final class Harita {

    public static final int GENISLIK = 880;
    public static final int YUKSEKLIK = 560;
    public static final double YOL_GENISLIGI = 44;

    private final String senaryoAdi;
    private final Yol yol;
    private final List<OyuncakYuvasi> yuvalar;

    private Harita(String senaryoAdi, Yol yol, List<OyuncakYuvasi> yuvalar) {
        this.senaryoAdi = senaryoAdi;
        this.yol = yol;
        this.yuvalar = Collections.unmodifiableList(new ArrayList<>(yuvalar));
        for (OyuncakYuvasi yuva : yuvalar) {
            double mesafe = yol.noktayaUzaklik(yuva.getX(), yuva.getY());
            if (mesafe < YOL_GENISLIGI / 2 + OyuncakYuvasi.YARI_KENAR + 6) {
                throw new IllegalStateException("Yuva #" + yuva.getNumara() + " yolun üstüne taşıyor.");
            }
        }
    }

    /** Oyunun tek senaryosu: "Gece Yarısı Koridoru". */
    public static Harita geceYarisiKoridoru() {
        Yol yol = new Yol(Arrays.asList(
                new Point2D.Double(30, 100),    // Dolap kapısı (başlangıç)
                new Point2D.Double(250, 100),
                new Point2D.Double(250, 250),
                new Point2D.Double(90, 250),
                new Point2D.Double(90, 460),
                new Point2D.Double(410, 460),
                new Point2D.Double(410, 170),
                new Point2D.Double(610, 170),
                new Point2D.Double(610, 420),
                new Point2D.Double(792, 420)    // Yatak (üs)
        ));
        List<OyuncakYuvasi> yuvalar = Arrays.asList(
                new OyuncakYuvasi(1, 170, 175),
                new OyuncakYuvasi(2, 330, 190),
                new OyuncakYuvasi(3, 170, 355),
                new OyuncakYuvasi(4, 330, 375),
                new OyuncakYuvasi(5, 510, 90),
                new OyuncakYuvasi(6, 510, 300),
                new OyuncakYuvasi(7, 510, 512),
                new OyuncakYuvasi(8, 705, 330),
                new OyuncakYuvasi(9, 705, 512),
                new OyuncakYuvasi(10, 720, 140)
        );
        return new Harita("Gece Yarısı Koridoru", yol, yuvalar);
    }

    /** Verilen noktadaki yuvayı döndürür, yoksa null. */
    public OyuncakYuvasi yuvaBul(double x, double y) {
        for (OyuncakYuvasi yuva : yuvalar) {
            if (yuva.icerir(x, y)) {
                return yuva;
            }
        }
        return null;
    }

    public String getSenaryoAdi() {
        return senaryoAdi;
    }

    public Yol getYol() {
        return yol;
    }

    public List<OyuncakYuvasi> getYuvalar() {
        return yuvalar;
    }
}
