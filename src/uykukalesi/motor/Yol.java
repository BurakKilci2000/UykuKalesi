package uykukalesi.motor;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dolaptan yatağa uzanan halı yolu: ardışık köşe noktalarından oluşan kesintisiz bir çizgi.
 * Yaratıklar "katedilen mesafe" ile ilerler; konumları noktaBul() ile hesaplanır.
 */
public final class Yol {

    private final List<Point2D.Double> noktalar;
    private final double[] birikimliUzunluk;
    private final double toplamUzunluk;

    public Yol(List<Point2D.Double> koseNoktalari) {
        if (koseNoktalari == null || koseNoktalari.size() < 2) {
            throw new IllegalArgumentException("Yol en az iki noktadan oluşmalı.");
        }
        List<Point2D.Double> kopya = new ArrayList<>();
        for (Point2D.Double p : koseNoktalari) {
            kopya.add(new Point2D.Double(p.x, p.y));
        }
        this.noktalar = Collections.unmodifiableList(kopya);
        this.birikimliUzunluk = new double[kopya.size()];
        double toplam = 0;
        for (int i = 1; i < kopya.size(); i++) {
            toplam += kopya.get(i - 1).distance(kopya.get(i));
            birikimliUzunluk[i] = toplam;
        }
        this.toplamUzunluk = toplam;
    }

    /** Yolun başından itibaren verilen mesafedeki noktayı döndürür. */
    public Point2D.Double noktaBul(double mesafe) {
        double m = Math.max(0, Math.min(toplamUzunluk, mesafe));
        for (int i = 1; i < noktalar.size(); i++) {
            if (m <= birikimliUzunluk[i]) {
                double parcaBasi = birikimliUzunluk[i - 1];
                double parcaBoyu = birikimliUzunluk[i] - parcaBasi;
                double t = parcaBoyu == 0 ? 0 : (m - parcaBasi) / parcaBoyu;
                Point2D.Double a = noktalar.get(i - 1);
                Point2D.Double b = noktalar.get(i);
                return new Point2D.Double(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t);
            }
        }
        Point2D.Double son = noktalar.get(noktalar.size() - 1);
        return new Point2D.Double(son.x, son.y);
    }

    /** Bir noktanın yol çizgisine en kısa uzaklığı (inşa alanlarının yolu kapatmadığını doğrulamak için). */
    public double noktayaUzaklik(double px, double py) {
        double enKisa = Double.MAX_VALUE;
        for (int i = 1; i < noktalar.size(); i++) {
            Point2D.Double a = noktalar.get(i - 1);
            Point2D.Double b = noktalar.get(i);
            enKisa = Math.min(enKisa, Line2D.ptSegDist(a.x, a.y, b.x, b.y, px, py));
        }
        return enKisa;
    }

    public double getToplamUzunluk() {
        return toplamUzunluk;
    }

    public Point2D.Double getBaslangic() {
        Point2D.Double p = noktalar.get(0);
        return new Point2D.Double(p.x, p.y);
    }

    public Point2D.Double getBitis() {
        Point2D.Double p = noktalar.get(noktalar.size() - 1);
        return new Point2D.Double(p.x, p.y);
    }

    /** Salt-okunur köşe listesi (çizim için). */
    public List<Point2D.Double> getNoktalar() {
        return noktalar;
    }
}
