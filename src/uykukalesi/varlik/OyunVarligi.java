package uykukalesi.varlik;

/**
 * Haritada konumu olan her oyun varlığının (kabus yaratıkları ve oyuncak savunucular)
 * ortak soyut atası.
 *
 * Kapsülleme: kimlik ve konum private tutulur; konumu yalnızca alt sınıflar
 * korumalı konumuAyarla() metoduyla değiştirebilir.
 */
public abstract class OyunVarligi implements Cizilebilir {

    private final String kimlik;
    private double x;
    private double y;

    protected OyunVarligi(String kimlik, double x, double y) {
        if (kimlik == null) {
            throw new IllegalArgumentException("Kimlik boş olamaz.");
        }
        this.kimlik = kimlik;
        this.x = x;
        this.y = y;
    }

    /** Günlükte ve kimlikte kullanılan sınıf adı, ör. "LastikSapan". */
    public abstract String getTurAdi();

    /** Arayüzde gösterilen Türkçe ad, ör. "Lastik Sapan". */
    public abstract String getGorunenAd();

    /** Varlığı yalnızca gövdesiyle, verilen noktaya çizer (menü ve buton ikonları için). */
    public abstract void onizlemeCiz(java.awt.Graphics2D g, double cx, double cy);

    public final String getKimlik() {
        return kimlik;
    }

    /** Ör. "TenekeRobot-ID103" */
    public final String getTamAd() {
        return getTurAdi() + "-" + kimlik;
    }

    public final double getX() {
        return x;
    }

    public final double getY() {
        return y;
    }

    protected final void konumuAyarla(double yeniX, double yeniY) {
        this.x = yeniX;
        this.y = yeniY;
    }

    public final double uzaklik(double px, double py) {
        return Math.hypot(x - px, y - py);
    }

    public final double uzaklik(OyunVarligi diger) {
        return uzaklik(diger.x, diger.y);
    }
}
