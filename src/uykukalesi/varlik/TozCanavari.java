package uykukalesi.varlik;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

import uykukalesi.motor.Yol;

/**
 * STANDART DÜŞMAN — Toz Canavarı.
 * Yatağın altından sürünen toz yumağı: uçmaz, zırhsız, can 50, hız 50.
 * Dağıtılınca 10 şeker kazandırır, yatağa ulaşırsa uykuyu 5 azaltır.
 */
public class TozCanavari extends KabusYaratigi {

    public static final int SEKER_ODULU = 10;
    public static final int UYKU_HASARI = 5;

    private static final Color TOZ_GRISI = new Color(128, 122, 118);

    public TozCanavari(String kimlik, Yol yol) {
        super(kimlik, STANDART_KORKU_ENERJISI, STANDART_ADIM_HIZI, 0, yol);
    }

    @Override
    public String getTurAdi() {
        return "TozCanavari";
    }

    @Override
    public String getGorunenAd() {
        return "Toz Canavarı";
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
        return false;
    }

    @Override
    protected Color getAnaRenk() {
        return TOZ_GRISI;
    }

    @Override
    protected void govdeyiCiz(Graphics2D g, double cx, double cy, Color renk, double saat) {
        double r = 12 * (1 + 0.06 * Math.sin(saat * 8));
        // Kabarık tüyler
        g.setColor(renk.darker());
        for (int i = 0; i < 11; i++) {
            double a = i * Math.PI * 2 / 11 + saat * 0.8;
            double px = cx + Math.cos(a) * r;
            double py = cy + Math.sin(a) * r;
            g.fill(new Ellipse2D.Double(px - 4.5, py - 4.5, 9, 9));
        }
        g.setColor(renk);
        g.fill(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        // Gözler ve çatık kaşlar
        gozCiz(g, cx - 4.5, cy - 1.5, 3.4, Color.BLACK);
        gozCiz(g, cx + 4.5, cy - 1.5, 3.4, Color.BLACK);
        g.setColor(new Color(40, 35, 35));
        kalinCizgi(g, 1.8f);
        g.draw(new Line2D.Double(cx - 8.5, cy - 8, cx - 2, cy - 5));
        g.draw(new Line2D.Double(cx + 8.5, cy - 8, cx + 2, cy - 5));
        // Sırıtan ağız
        g.draw(new Line2D.Double(cx - 4, cy + 5, cx + 4, cy + 5));
    }
}
