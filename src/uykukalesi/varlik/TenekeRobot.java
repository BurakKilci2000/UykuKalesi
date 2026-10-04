package uykukalesi.varlik;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.RoundRectangle2D;

import uykukalesi.motor.Yol;

/**
 * ZIRHLI DÜŞMAN — Teneke Robot.
 * Oyuncak kutusundan kaçmış paslı kurmalı robot: uçmaz,
 * standarda göre %50 fazla can (75) ve %50 az hız (25), zırh 50–100.
 * Dağıtılınca 20 şeker kazandırır, yatağa ulaşırsa uykuyu 10 azaltır.
 */
public class TenekeRobot extends KabusYaratigi {

    public static final int MIN_ZIRH = 50;
    public static final int MAKS_ZIRH = 100;
    public static final int SEKER_ODULU = 20;
    public static final int UYKU_HASARI = 10;

    private static final Color TENEKE_GUMUSU = new Color(168, 162, 150);

    public TenekeRobot(String kimlik, int zirhKaplamasi, Yol yol) {
        super(kimlik,
              STANDART_KORKU_ENERJISI * 1.5,
              STANDART_ADIM_HIZI * 0.5,
              zirhiDogrula(zirhKaplamasi),
              yol);
    }

    private static int zirhiDogrula(int zirh) {
        if (zirh < MIN_ZIRH || zirh > MAKS_ZIRH) {
            throw new IllegalArgumentException(
                    "Teneke Robot zırhı " + MIN_ZIRH + "-" + MAKS_ZIRH + " arasında olmalı: " + zirh);
        }
        return zirh;
    }

    @Override
    public String getTurAdi() {
        return "TenekeRobot";
    }

    @Override
    public String getGorunenAd() {
        return "Teneke Robot";
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
        return TENEKE_GUMUSU;
    }

    @Override
    protected void govdeyiCiz(Graphics2D g, double cx, double cy, Color renk, double saat) {
        double s = Math.sin(saat * 6) * 1.2;           // tıkır tıkır sallanma
        // Bacaklar
        g.setColor(renk.darker().darker());
        kalinCizgi(g, 3f);
        double adim = Math.sin(saat * 6) * 3;
        g.draw(new Line2D.Double(cx - 6, cy + 12, cx - 6 + adim, cy + 17));
        g.draw(new Line2D.Double(cx + 6, cy + 12, cx + 6 - adim, cy + 17));
        // Anten ve yanıp sönen ışık
        kalinCizgi(g, 2f);
        g.draw(new Line2D.Double(cx, cy - 13 + s, cx, cy - 19 + s));
        boolean yanik = ((int) (saat * 3)) % 2 == 0;
        g.setColor(yanik ? new Color(255, 70, 70) : new Color(130, 25, 25));
        g.fill(new Ellipse2D.Double(cx - 3, cy - 23 + s, 6, 6));
        // Teneke gövde
        RoundRectangle2D govde = new RoundRectangle2D.Double(cx - 13, cy - 13 + s, 26, 26, 8, 8);
        g.setColor(renk);
        g.fill(govde);
        g.setColor(renk.darker().darker());
        kalinCizgi(g, 2f);
        g.draw(govde);
        // Zırh plakası ve pas lekesi
        g.setColor(renk.darker());
        g.fill(new RoundRectangle2D.Double(cx - 9, cy + 2 + s, 18, 8, 3, 3));
        g.setColor(new Color(160, 85, 40, 170));
        g.fill(new Ellipse2D.Double(cx + 3, cy + 3 + s, 6, 4));
        // Perçinler
        g.setColor(renk.brighter());
        double[][] percin = {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}};
        for (double[] p : percin) {
            g.fill(new Ellipse2D.Double(cx + p[0] - 1.5, cy + p[1] - 1.5 + s, 3, 3));
        }
        // Kırmızı vizör gözler
        g.setColor(new Color(30, 28, 34));
        g.fill(new RoundRectangle2D.Double(cx - 9, cy - 8 + s, 18, 7, 4, 4));
        g.setColor(new Color(255, 70, 60));
        g.fill(new RoundRectangle2D.Double(cx - 7, cy - 6.5 + s, 5, 4, 2, 2));
        g.fill(new RoundRectangle2D.Double(cx + 2, cy - 6.5 + s, 5, 4, 2, 2));
        // Sırttaki kurma anahtarı
        g.setColor(new Color(200, 170, 80));
        kalinCizgi(g, 2.2f);
        double a = saat * 5;
        g.draw(new Line2D.Double(cx + 13, cy - 2 + s, cx + 17, cy - 2 + s));
        g.draw(new Line2D.Double(cx + 17, cy - 2 + s - 4 * Math.cos(a), cx + 17, cy - 2 + s + 4 * Math.cos(a)));
    }
}
