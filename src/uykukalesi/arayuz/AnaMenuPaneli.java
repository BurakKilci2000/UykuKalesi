package uykukalesi.arayuz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import uykukalesi.motor.Harita;
import uykukalesi.motor.SavunucuTuru;
import uykukalesi.motor.Yol;
import uykukalesi.varlik.GeceKelebegi;
import uykukalesi.varlik.OyunVarligi;
import uykukalesi.varlik.TenekeRobot;
import uykukalesi.varlik.TozCanavari;

/**
 * Ana menü: "Oyunu Başlat" ve "Çıkış" düğmeleri.
 * Arka planda gece gökyüzü; altta kabuslar ve oyuncak savunucular tanıtılır.
 */
@SuppressWarnings("serial") // Swing bileşenleri serileştirilmiyor
final class AnaMenuPaneli extends JPanel {

    private final List<OyunVarligi> kabuslar;
    private final List<OyunVarligi> savunucular;
    private final String[] kabusBilgileri = {
        "Korku 50, hız 50, ödül 10",
        "Korku 75, hız 25, zırh 50–100, ödül 20",
        "Uçar. Korku 50, hız 75, ödül 15"
    };
    private final String[] savunucuBilgileri = {
        "50 şeker. Hasar 10, her 1 sn",
        "75 şeker. Hasar 20 alan, her 3 sn",
        "70 şeker. Hasar 15, ıslatır, her 2 sn"
    };

    AnaMenuPaneli(Runnable baslat, Runnable cikis) {
        super(new GridBagLayout());
        setBackground(Tema.GECE);
        setPreferredSize(new Dimension(1100, 700));

        Yol yol = Harita.geceYarisiKoridoru().getYol();
        kabuslar = Arrays.asList(new TozCanavari("", yol), new TenekeRobot("", 75, yol), new GeceKelebegi("", yol));
        savunucular = Arrays.asList(
                SavunucuTuru.LASTIK_SAPAN.uret("", 0, 0),
                SavunucuTuru.BILYE_MANCINIGI.uret("", 0, 0),
                SavunucuTuru.SU_TABANCASI.uret("", 0, 0));

        JPanel dugmeler = new JPanel(new GridLayout(2, 1, 0, 14));
        dugmeler.setOpaque(false);
        OyunButonu basla = new OyunButonu("Oyunu Başlat", new Color(196, 92, 160));
        OyunButonu cik = new OyunButonu("Çıkış", new Color(84, 84, 120));
        basla.setFont(Tema.DUGME.deriveFont(18f));
        cik.setFont(Tema.DUGME.deriveFont(18f));
        basla.setPreferredSize(new Dimension(280, 56));
        cik.setPreferredSize(new Dimension(280, 56));
        basla.addActionListener(e -> baslat.run());
        cik.addActionListener(e -> cikis.run());
        dugmeler.add(basla);
        dugmeler.add(cik);
        add(dugmeler);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Tema.yumusat(g);
        int w = getWidth();
        int h = getHeight();
        int orta = h / 2;

        g.setPaint(new GradientPaint(0, 0, new Color(14, 15, 38), 0, h, new Color(48, 34, 86)));
        g.fillRect(0, 0, w, h);
        Random r = new Random(11);
        for (int i = 0; i < 140; i++) {
            int a = 60 + r.nextInt(170);
            g.setColor(new Color(255, 255, 255, a));
            double s = 1 + r.nextDouble() * 1.8;
            g.fill(new Ellipse2D.Double(r.nextInt(Math.max(1, w)), r.nextInt(Math.max(1, h)), s, s));
        }
        ayCiz(g, w - 170, 120, 62);

        // Başlık
        g.setFont(Tema.MASAL_BASLIK);
        FontMetrics fm = g.getFontMetrics();
        String baslik = "Uyku Kalesi";
        int bx = (w - fm.stringWidth(baslik)) / 2;
        int by = orta - 150;
        g.setColor(new Color(120, 90, 255, 90));
        g.drawString(baslik, bx + 3, by + 4);
        g.setColor(Tema.AY_ISIGI);
        g.drawString(baslik, bx, by);
        g.setFont(Tema.ALT_BASLIK);
        fm = g.getFontMetrics();
        String alt = "Gece yarısı kabuslarına karşı oyuncak savunması";
        g.setColor(Tema.SOLUK_YAZI);
        g.drawString(alt, (w - fm.stringWidth(alt)) / 2, by + 38);

        // Tanıtım: kabuslar ve savunucular
        int ust = orta + 110;
        int sutunW = Math.min(420, (w - 80) / 2);
        int solX = w / 2 - sutunW - 20;
        int sagX = w / 2 + 20;
        tanitimSutunu(g, "Dolaptan çıkan kabuslar", kabuslar, kabusBilgileri, solX, ust, sutunW);
        tanitimSutunu(g, "Uykuyu koruyan oyuncaklar", savunucular, savunucuBilgileri, sagX, ust, sutunW);
        g.dispose();
    }

    private void tanitimSutunu(Graphics2D g, String baslik, List<OyunVarligi> varliklar, String[] bilgiler,
                               int x, int y, int genislik) {
        g.setColor(new Color(24, 22, 54, 200));
        g.fill(new RoundRectangle2D.Double(x, y, genislik, 190, 18, 18));
        g.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 18));
        g.setColor(Tema.AY_ISIGI);
        g.drawString(baslik, x + 16, y + 28);
        for (int i = 0; i < varliklar.size(); i++) {
            int satirY = y + 44 + i * 48;
            OyunVarligi v = varliklar.get(i);
            v.onizlemeCiz(g, x + 36, satirY + 22);
            g.setFont(new Font("SansSerif", Font.BOLD, 14));
            g.setColor(Tema.AY_ISIGI);
            g.drawString(v.getGorunenAd(), x + 70, satirY + 18);
            g.setFont(Tema.KUCUK);
            g.setColor(Tema.SOLUK_YAZI);
            g.drawString(bilgiler[i], x + 70, satirY + 35);
        }
    }

    private static void ayCiz(Graphics2D g, double cx, double cy, double r) {
        g.setColor(new Color(240, 236, 210, 40));
        g.fill(new Ellipse2D.Double(cx - r * 1.6, cy - r * 1.6, r * 3.2, r * 3.2));
        Path2D hilal = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        hilal.append(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2), false);
        hilal.append(new Ellipse2D.Double(cx - r + r * 0.55, cy - r - r * 0.2, r * 2, r * 2), false);
        java.awt.geom.Area a = new java.awt.geom.Area(new Ellipse2D.Double(cx - r, cy - r, r * 2, r * 2));
        a.subtract(new java.awt.geom.Area(new Ellipse2D.Double(cx - r + r * 0.55, cy - r - r * 0.2, r * 2, r * 2)));
        g.setColor(new Color(248, 240, 200));
        g.fill(a);
    }
}
