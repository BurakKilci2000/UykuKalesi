package uykukalesi.varlik;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;

import uykukalesi.motor.Yol;

/**
 * UÇAN DÜŞMAN — Gece Kelebeği.
 * Lambayı arayan dev güve: uçar, zırhsız, standartla aynı can (50) ve %50 fazla hız (75).
 * Dağıtılınca 15 şeker kazandırır, yatağa ulaşırsa uykuyu 5 azaltır.
 * Bilye Mancınığı onu hedefleyemez (bilyeler yerde yuvarlanır).
 */
public class GeceKelebegi extends KabusYaratigi {

    public static final int SEKER_ODULU = 15;
    public static final int UYKU_HASARI = 5;

    private static final Color KELEBEK_MORU = new Color(160, 112, 178);

    public GeceKelebegi(String kimlik, Yol yol) {
        super(kimlik, STANDART_KORKU_ENERJISI, STANDART_ADIM_HIZI * 1.5, 0, yol);
    }

    @Override
    public String getTurAdi() {
        return "GeceKelebegi";
    }

    @Override
    public String getGorunenAd() {
        return "Gece Kelebeği";
    }

    @Override
    public int getSekerOdulu() {
        return SEKER_ODULU;
    }

    @Override
    public int getUykuHasari() {
        return UYKU_HASARI;
    }

    @Override
    public boolean ucabilirMi() {
        return true;
    }

    @Override
    protected Color getAnaRenk() {
        return KELEBEK_MORU;
    }

    @Override
    protected void govdeyiCiz(Graphics2D g, double cx, double cy, Color renk, double saat) {
        double k = 0.5 + 0.5 * Math.abs(Math.sin(saat * 14));   // kanat çırpma
        Color koyu = renk.darker();
        // Üst kanatlar
        g.setColor(renk);
        g.fill(new Ellipse2D.Double(cx - 3 - 15 * k, cy - 11, 15 * k, 13));
        g.fill(new Ellipse2D.Double(cx + 3, cy - 11, 15 * k, 13));
        // Alt kanatlar
        g.setColor(koyu);
        g.fill(new Ellipse2D.Double(cx - 2 - 10 * k, cy, 10 * k, 9));
        g.fill(new Ellipse2D.Double(cx + 2, cy, 10 * k, 9));
        // Kanat üzerindeki göz desenleri
        g.setColor(new Color(255, 230, 150, 200));
        g.fill(new Ellipse2D.Double(cx - 3 - 9 * k, cy - 7, 5 * k, 5));
        g.fill(new Ellipse2D.Double(cx + 3 + 4 * k, cy - 7, 5 * k, 5));
        // Gövde
        g.setColor(new Color(55, 40, 60));
        g.fill(new Ellipse2D.Double(cx - 3, cy - 9, 6, 18));
        // Antenler
        kalinCizgi(g, 1.3f);
        g.draw(new QuadCurve2D.Double(cx - 1, cy - 9, cx - 4, cy - 16, cx - 8, cy - 15));
        g.draw(new QuadCurve2D.Double(cx + 1, cy - 9, cx + 4, cy - 16, cx + 8, cy - 15));
        // Parlayan gözler
        g.setColor(new Color(255, 240, 120));
        g.fill(new Ellipse2D.Double(cx - 2.6, cy - 8, 2.2, 2.2));
        g.fill(new Ellipse2D.Double(cx + 0.4, cy - 8, 2.2, 2.2));
    }
}
