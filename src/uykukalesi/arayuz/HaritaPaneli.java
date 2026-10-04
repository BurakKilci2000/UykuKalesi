package uykukalesi.arayuz;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import uykukalesi.kayit.Bicim;
import uykukalesi.motor.Harita;
import uykukalesi.motor.OyuncakYuvasi;
import uykukalesi.motor.SavunucuTuru;
import uykukalesi.motor.SimulasyonMotoru;
import uykukalesi.varlik.Cizilebilir;
import uykukalesi.varlik.KabusYaratigi;
import uykukalesi.varlik.OyuncakSavunucu;

/**
 * Canlı görselleştirme (ön yüz). Her karede motorun durumunu okur ve çizer.
 * Harita 880x560 mantıksal düzlemde çizilir, panel boyutuna göre ölçeklenir;
 * fare koordinatları da aynı ölçekle harita düzlemine çevrilir.
 */
@SuppressWarnings("serial") // Swing bileşenleri serileştirilmiyor
final class HaritaPaneli extends JPanel {

    private static final int W = Harita.GENISLIK;
    private static final int H = Harita.YUKSEKLIK;

    private final SimulasyonMotoru motor;
    private final Supplier<SavunucuTuru> seciliTurSaglayici;
    private final Consumer<OyuncakYuvasi> yuvaTiklandi;
    private final Runnable secimIptal;

    private BufferedImage arkaPlan;
    private double olcek = 1;
    private double kaymaX;
    private double kaymaY;
    private double fareX = -1000;
    private double fareY = -1000;

    HaritaPaneli(SimulasyonMotoru motor, Supplier<SavunucuTuru> seciliTurSaglayici,
                 Consumer<OyuncakYuvasi> yuvaTiklandi, Runnable secimIptal) {
        this.motor = motor;
        this.seciliTurSaglayici = seciliTurSaglayici;
        this.yuvaTiklandi = yuvaTiklandi;
        this.secimIptal = secimIptal;
        setPreferredSize(new Dimension(W, H));
        setBackground(new Color(14, 14, 30));

        MouseAdapter fare = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                fareyiCevir(e);
                OyuncakYuvasi yuva = motor.getHarita().yuvaBul(fareX, fareY);
                setCursor(Cursor.getPredefinedCursor(yuva != null ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                fareX = -1000;
                fareY = -1000;
            }

            @Override
            public void mousePressed(MouseEvent e) {
                fareyiCevir(e);
                if (SwingUtilities.isRightMouseButton(e)) {
                    secimIptal.run();
                    return;
                }
                OyuncakYuvasi yuva = motor.getHarita().yuvaBul(fareX, fareY);
                if (yuva != null) {
                    yuvaTiklandi.accept(yuva);
                }
            }
        };
        addMouseListener(fare);
        addMouseMotionListener(fare);
    }

    private void fareyiCevir(MouseEvent e) {
        fareX = (e.getX() - kaymaX) / olcek;
        fareY = (e.getY() - kaymaY) / olcek;
    }

    // ==================================================================
    // Ana çizim
    // ==================================================================

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        Tema.yumusat(g);

        olcek = Math.min(getWidth() / (double) W, getHeight() / (double) H);
        kaymaX = (getWidth() - W * olcek) / 2;
        kaymaY = (getHeight() - H * olcek) / 2;
        g.translate(kaymaX, kaymaY);
        g.scale(olcek, olcek);
        g.clip(new Rectangle(0, 0, W, H));

        if (arkaPlan == null) {
            arkaPlan = arkaPlanOlustur();
        }
        g.drawImage(arkaPlan, 0, 0, null);

        SavunucuTuru seciliTur = seciliTurSaglayici.get();
        yuvalariCiz(g, seciliTur);

        for (OyuncakSavunucu s : motor.getSavunucular()) {
            s.ciz(g);
        }
        // Önce yerdekiler, sonra uçanlar (uçanlar üstte görünsün)
        List<KabusYaratigi> yaratiklar = motor.getSahadakiYaratiklar();
        for (KabusYaratigi y : yaratiklar) {
            if (!y.ucabilirMi()) {
                y.ciz(g);
            }
        }
        for (KabusYaratigi y : yaratiklar) {
            if (y.ucabilirMi()) {
                y.ciz(g);
            }
        }
        cizHepsini(g, motor.getFirlatmalar());
        cizHepsini(g, motor.getEfektler());

        fareBilgisiniCiz(g, seciliTur);
        katmanlariCiz(g);
        g.dispose();
    }

    private static void cizHepsini(Graphics2D g, List<? extends Cizilebilir> liste) {
        for (Cizilebilir c : liste) {
            c.ciz(g);
        }
    }

    // ==================================================================
    // Oyuncak yuvaları (kule inşa alanları)
    // ==================================================================

    private void yuvalariCiz(Graphics2D g, SavunucuTuru seciliTur) {
        double k = OyuncakYuvasi.YARI_KENAR;
        for (OyuncakYuvasi yuva : motor.getHarita().getYuvalar()) {
            double x = yuva.getX();
            double y = yuva.getY();
            boolean ustunde = yuva.icerir(fareX, fareY);
            RoundRectangle2D kare = new RoundRectangle2D.Double(x - k, y - k, k * 2, k * 2, 12, 12);

            // Küçük yuvarlak halı
            g.setColor(new Color(64, 48, 92, 210));
            g.fill(kare);
            g.setColor(new Color(150, 120, 200, 140));
            g.setStroke(new BasicStroke(1.2f));
            g.draw(new RoundRectangle2D.Double(x - k + 4, y - k + 4, k * 2 - 8, k * 2 - 8, 8, 8));

            if (yuva.bosMu()) {
                Color kenar = new Color(255, 236, 190, 170);
                if (seciliTur != null) {
                    kenar = motor.satinAlinabilirMi(seciliTur)
                            ? new Color(120, 240, 150) : new Color(255, 110, 110);
                }
                g.setColor(kenar);
                g.setStroke(new BasicStroke(ustunde ? 2.6f : 1.6f, BasicStroke.CAP_ROUND,
                        BasicStroke.JOIN_ROUND, 1f, new float[]{5f, 4f}, 0f));
                g.draw(kare);
                g.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(new Line2D.Double(x - 6, y, x + 6, y));
                g.draw(new Line2D.Double(x, y - 6, x, y + 6));
                if (ustunde) {
                    g.setColor(new Color(255, 255, 255, 40));
                    g.fill(kare);
                }
            }
            // Yuva numarası (sol üst köşe)
            g.setFont(new Font("SansSerif", Font.BOLD, 9));
            g.setColor(new Color(255, 240, 210, 170));
            g.drawString("#" + yuva.getNumara(), (float) (x - k + 3), (float) (y - k + 10));
        }
    }

    // ==================================================================
    // Fare ile gösterilen bilgiler (menzil önizlemesi, ipucu kutuları)
    // ==================================================================

    private void fareBilgisiniCiz(Graphics2D g, SavunucuTuru seciliTur) {
        OyuncakYuvasi yuva = motor.getHarita().yuvaBul(fareX, fareY);
        if (yuva != null && yuva.bosMu() && seciliTur != null) {
            Color renk = motor.satinAlinabilirMi(seciliTur) ? new Color(120, 240, 150) : new Color(255, 110, 110);
            OyuncakSavunucu.menzilCiz(g, yuva.getX(), yuva.getY(), seciliTur.getMenzil(), renk);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
            seciliTur.uret("onizleme", 0, 0).onizlemeCiz(g2, yuva.getX(), yuva.getY());
            g2.dispose();
            bilgiKutusu(g, yuva.getX(), yuva.getY() - 34, seciliTur.getGorunenAd() + " — "
                    + seciliTur.getMaliyet() + " şeker", seciliTur.getAciklama());
            return;
        }
        if (yuva != null && !yuva.bosMu()) {
            OyuncakSavunucu s = yuva.getSavunucu();
            OyuncakSavunucu.menzilCiz(g, s.getX(), s.getY(), s.getGorusMenzili(), new Color(255, 230, 150));
            bilgiKutusu(g, s.getX(), s.getY() - 34, s.getGorunenAd() + " (" + s.getTamAd() + ")",
                    "Hasar " + s.getVurusGucu() + " • Menzil " + Bicim.sayi(s.getGorusMenzili())
                            + " px • Her " + Bicim.sayi(s.getAtisAraligi()) + " sn");
            return;
        }
        KabusYaratigi y = fareninAltindakiYaratik();
        if (y != null) {
            String ikinci = "Korku " + Bicim.sayi(y.getKorkuEnerjisi()) + "/" + Bicim.sayi(y.getMaksKorkuEnerjisi())
                    + " • Zırh " + y.getZirhKaplamasi() + " • Hız " + Bicim.sayi(y.getAnlikAdimHizi())
                    + (y.islakMi() ? " (ıslak)" : "");
            bilgiKutusu(g, y.getX(), y.getGorselY() - 40, y.getGorunenAd() + " (" + y.getTamAd() + ")", ikinci);
        }
    }

    private KabusYaratigi fareninAltindakiYaratik() {
        KabusYaratigi bulunan = null;
        for (KabusYaratigi y : motor.getSahadakiYaratiklar()) {
            if (Math.hypot(y.getX() - fareX, y.getGorselY() - fareY) < 16) {
                bulunan = y;
            }
        }
        return bulunan;
    }

    private void bilgiKutusu(Graphics2D g, double merkezX, double altY, String baslik, String satir) {
        Font f1 = new Font("SansSerif", Font.BOLD, 12);
        Font f2 = new Font("SansSerif", Font.PLAIN, 11);
        FontMetrics m1 = g.getFontMetrics(f1);
        FontMetrics m2 = g.getFontMetrics(f2);
        int w = Math.max(m1.stringWidth(baslik), m2.stringWidth(satir)) + 18;
        int h = 40;
        double x = Math.max(4, Math.min(W - w - 4, merkezX - w / 2.0));
        double y = Math.max(4, altY - h);
        g.setColor(new Color(20, 20, 44, 230));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        g.setColor(new Color(150, 140, 230));
        g.setStroke(new BasicStroke(1.2f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 10, 10));
        g.setFont(f1);
        g.setColor(Tema.AY_ISIGI);
        g.drawString(baslik, (float) x + 9, (float) y + 16);
        g.setFont(f2);
        g.setColor(Tema.SOLUK_YAZI);
        g.drawString(satir, (float) x + 9, (float) y + 32);
    }

    // ==================================================================
    // Üst katmanlar: duyuru, geri sayım, duraklatma, oyun sonu
    // ==================================================================

    private void katmanlariCiz(Graphics2D g) {
        String duyuru = motor.getDuyuru();
        if (duyuru != null && !motor.oyunBittiMi()) {
            float alfa = (float) Math.min(1, motor.getDuyuruOrani() * 3);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alfa));
            serit(g2, duyuru, 300, new Font("Serif", Font.BOLD | Font.ITALIC, 26), Tema.AY_ISIGI);
            g2.dispose();
        }
        if (motor.getDurum() == SimulasyonMotoru.Durum.AKIN_ARASI) {
            rozet(g, "Sonraki akın " + (int) Math.ceil(motor.getAraGeriSayim()) + " sn sonra", 14);
        } else if (motor.getDurum() == SimulasyonMotoru.Durum.HAZIRLIK) {
            rozet(g, "Hazırlık: savunucu kur, sonra üstteki düğmeyle akını başlat", 14);
        }

        if (motor.isDuraklatildi() && !motor.oyunBittiMi()) {
            g.setColor(new Color(10, 10, 25, 140));
            g.fillRect(0, 0, W, H);
            serit(g, "Duraklatıldı", 280, new Font("Serif", Font.BOLD | Font.ITALIC, 40), Tema.AY_ISIGI);
        }

        if (motor.oyunBittiMi()) {
            boolean kazandi = motor.getDurum() == SimulasyonMotoru.Durum.KAZANILDI;
            g.setColor(new Color(10, 10, 25, 165));
            g.fillRect(0, 0, W, H);
            serit(g, kazandi ? "KAZANDINIZ!" : "KAYBETTİNİZ", 250,
                    new Font("Serif", Font.BOLD, 56), kazandi ? new Color(150, 240, 160) : new Color(255, 120, 120));
            String alt = kazandi
                    ? "Çocuk sabaha kadar mışıl mışıl uyudu. Kalan uyku: " + motor.getUykuDerinligi()
                            + " • Şeker: " + motor.getSeker()
                    : "Kabuslar yatağa ulaştı, çocuk uyandı. Toplam şeker: " + motor.getSeker();
            ortaliYazi(g, alt, 318, new Font("SansSerif", Font.PLAIN, 16), Tema.AY_ISIGI);
        }
    }

    private void serit(Graphics2D g, String metin, int tabanY, Font font, Color renk) {
        FontMetrics fm = g.getFontMetrics(font);
        int w = fm.stringWidth(metin) + 60;
        int h = fm.getHeight() + 16;
        double x = (W - w) / 2.0;
        double y = tabanY - fm.getAscent() - 8;
        g.setColor(new Color(22, 22, 48, 225));
        g.fill(new RoundRectangle2D.Double(x, y, w, h, 22, 22));
        g.setColor(new Color(150, 140, 230, 200));
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(x, y, w, h, 22, 22));
        ortaliYazi(g, metin, tabanY, font, renk);
    }

    private void rozet(Graphics2D g, String metin, int ustY) {
        Font f = new Font("SansSerif", Font.BOLD, 13);
        FontMetrics fm = g.getFontMetrics(f);
        int w = fm.stringWidth(metin) + 24;
        double x = (W - w) / 2.0 + 60;
        g.setColor(new Color(22, 22, 48, 215));
        g.fill(new RoundRectangle2D.Double(x, ustY, w, 26, 26, 26));
        g.setColor(Tema.SEKER);
        g.setFont(f);
        g.drawString(metin, (float) x + 12, ustY + 18);
    }

    private void ortaliYazi(Graphics2D g, String metin, int tabanY, Font font, Color renk) {
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        float x = (W - fm.stringWidth(metin)) / 2f;
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(metin, x + 2, tabanY + 2);
        g.setColor(renk);
        g.drawString(metin, x, tabanY);
    }

    // ==================================================================
    // Sabit arka plan (bir kez çizilip önbelleğe alınır)
    // ==================================================================

    private BufferedImage arkaPlanOlustur() {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        Tema.yumusat(g);
        parkeCiz(g);
        // Gece karanlığı ve pencereden süzülen ay ışığı
        g.setColor(new Color(16, 14, 50, 95));
        g.fillRect(0, 0, W, H);
        g.setPaint(new RadialGradientPaint(new Point2D.Double(560, -60), 520,
                new float[]{0f, 0.55f, 1f},
                new Color[]{new Color(200, 210, 255, 90), new Color(150, 160, 255, 30), new Color(0, 0, 0, 0)}));
        g.fillRect(0, 0, W, H);
        pencereIsiginiCiz(g);
        yolCiz(g);
        dolapCiz(g);
        yatakCiz(g);
        dekorCiz(g);
        g.setFont(new Font("SansSerif", Font.BOLD, 11));
        g.setColor(new Color(255, 240, 220, 150));
        g.drawString("Senaryo: " + motor.getHarita().getSenaryoAdi(), 10, H - 10);
        g.dispose();
        return img;
    }

    private void parkeCiz(Graphics2D g) {
        Random r = new Random(7);
        int satirBoyu = 35;
        for (int satir = 0; satir * satirBoyu < H; satir++) {
            int y = satir * satirBoyu;
            int x = -((satir * 97) % 160);
            while (x < W) {
                int uzunluk = 140 + r.nextInt(110);
                int ton = r.nextInt(14);
                g.setColor(new Color(92 + ton, 62 + ton / 2, 44 + ton / 3));
                g.fillRect(x, y, uzunluk, satirBoyu);
                g.setColor(new Color(70, 46, 33, 120));
                for (int i = 0; i < 3; i++) {
                    int yy = y + 7 + i * 10 + r.nextInt(4);
                    g.drawLine(x + 6, yy, x + uzunluk - 6, yy + r.nextInt(3) - 1);
                }
                g.setColor(new Color(48, 31, 22));
                g.drawLine(x, y, x, y + satirBoyu);
                x += uzunluk;
            }
            g.setColor(new Color(48, 31, 22));
            g.drawLine(0, y, W, y);
        }
    }

    private void pencereIsiginiCiz(Graphics2D g) {
        // Pencere pervazından düşen dört bölmeli ay ışığı lekesi
        Path2D isik = new Path2D.Double();
        isik.moveTo(470, 0);
        isik.lineTo(650, 0);
        isik.lineTo(700, 120);
        isik.lineTo(500, 120);
        isik.closePath();
        g.setPaint(new GradientPaint(560, 0, new Color(210, 220, 255, 55), 600, 120, new Color(210, 220, 255, 0)));
        g.fill(isik);
    }

    private void yolCiz(Graphics2D g) {
        Path2D yol = new Path2D.Double();
        List<Point2D.Double> n = motor.getHarita().getYol().getNoktalar();
        yol.moveTo(n.get(0).x, n.get(0).y);
        for (int i = 1; i < n.size(); i++) {
            yol.lineTo(n.get(i).x, n.get(i).y);
        }
        float gen = (float) Harita.YOL_GENISLIGI;
        g.setColor(new Color(0, 0, 0, 70));
        g.setStroke(new BasicStroke(gen + 10, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.translate(0, 4);
        g.draw(yol);
        g.translate(0, -4);
        g.setColor(new Color(118, 52, 70));
        g.setStroke(new BasicStroke(gen + 6, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(yol);
        g.setColor(new Color(222, 196, 152));
        g.setStroke(new BasicStroke(gen - 4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(yol);
        g.setColor(new Color(196, 160, 116));
        g.setStroke(new BasicStroke(gen - 18, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g.draw(yol);
        g.setColor(new Color(150, 98, 84));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1f, new float[]{7f, 6f}, 0f));
        g.draw(yol);

        // Yön okları: yolun dolaptan yatağa kesintisiz gittiğini gösterir
        double toplam = motor.getHarita().getYol().getToplamUzunluk();
        g.setColor(new Color(130, 70, 70, 120));
        for (double m = 120; m < toplam - 60; m += 140) {
            Point2D.Double a = motor.getHarita().getYol().noktaBul(m - 4);
            Point2D.Double b = motor.getHarita().getYol().noktaBul(m + 4);
            double aci = Math.atan2(b.y - a.y, b.x - a.x);
            Path2D ok = new Path2D.Double();
            ok.moveTo(7, 0);
            ok.lineTo(-5, -6);
            ok.lineTo(-2, 0);
            ok.lineTo(-5, 6);
            ok.closePath();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(b.x, b.y);
            g2.rotate(aci);
            g2.fill(ok);
            g2.dispose();
        }
    }

    private void dolapCiz(Graphics2D g) {
        // Gardırop (yolun başlangıcı)
        Color ahsap = new Color(86, 56, 74);
        g.setColor(new Color(0, 0, 0, 80));
        g.fill(new RoundRectangle2D.Double(4, 34, 66, 146, 8, 8));
        g.setColor(ahsap);
        g.fill(new RoundRectangle2D.Double(0, 28, 64, 146, 8, 8));
        g.setColor(ahsap.darker());
        g.setStroke(new BasicStroke(2f));
        g.draw(new RoundRectangle2D.Double(0, 28, 64, 146, 8, 8));
        // Aralık kapı: içi karanlık, iki parlayan göz
        g.setColor(new Color(14, 8, 24));
        g.fill(new Rectangle2D.Double(14, 70, 36, 60));
        g.setColor(new Color(190, 120, 255));
        g.fill(new Ellipse2D.Double(22, 88, 6, 4));
        g.fill(new Ellipse2D.Double(34, 88, 6, 4));
        g.setColor(ahsap.brighter());
        Path2D kapi = new Path2D.Double();
        kapi.moveTo(50, 70);
        kapi.lineTo(62, 64);
        kapi.lineTo(62, 136);
        kapi.lineTo(50, 130);
        kapi.closePath();
        g.fill(kapi);
        g.setColor(new Color(220, 190, 120));
        g.fill(new Ellipse2D.Double(10, 98, 4, 4));
        etiket(g, "Dolap (başlangıç)", 32, 20);
    }

    private void yatakCiz(Graphics2D g) {
        double x = 794;
        double y = 352;
        // Gölge
        g.setColor(new Color(0, 0, 0, 90));
        g.fill(new RoundRectangle2D.Double(x + 4, y + 8, 84, 140, 12, 12));
        // Karyola
        g.setColor(new Color(120, 78, 50));
        g.fill(new RoundRectangle2D.Double(x, y, 84, 140, 12, 12));
        // Yatak başı
        g.setColor(new Color(98, 62, 40));
        g.fill(new RoundRectangle2D.Double(x + 70, y - 6, 16, 152, 8, 8));
        // Çarşaf ve yastık
        g.setColor(new Color(236, 232, 245));
        g.fill(new RoundRectangle2D.Double(x + 6, y + 8, 64, 124, 10, 10));
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Double(x + 44, y + 44, 22, 52, 12, 12));
        // Uyuyan çocuk
        g.setColor(new Color(244, 200, 160));
        g.fill(new Ellipse2D.Double(x + 44, y + 58, 20, 22));
        g.setColor(new Color(110, 70, 40));
        g.fill(new Ellipse2D.Double(x + 54, y + 56, 12, 26));
        g.setColor(new Color(70, 50, 40));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Line2D.Double(x + 48, y + 66, x + 51, y + 66));
        g.draw(new Line2D.Double(x + 48, y + 72, x + 51, y + 72));
        // Yıldızlı battaniye
        g.setColor(new Color(70, 92, 190));
        g.fill(new RoundRectangle2D.Double(x + 6, y + 30, 42, 102, 10, 10));
        g.setColor(new Color(255, 220, 120));
        Random r = new Random(3);
        for (int i = 0; i < 7; i++) {
            yildiz(g, x + 12 + r.nextInt(30), y + 40 + r.nextInt(84), 3);
        }
        // Zzz
        g.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 14));
        g.setColor(new Color(220, 215, 255));
        g.drawString("z", (float) x + 30, (float) y + 52);
        g.setFont(new Font("Serif", Font.BOLD | Font.ITALIC, 18));
        g.drawString("Z", (float) x + 18, (float) y + 40);
        etiket(g, "Yatak (üs)", x + 42, y - 14);
    }

    private void dekorCiz(Graphics2D g) {
        // Yerde dağınık birkaç oyuncak: odanın hikâyesini anlatır, yola değmez
        g.setColor(new Color(240, 200, 80, 170));
        yildiz(g, 845, 60, 6);
        yildiz(g, 820, 205, 4);
        g.setColor(new Color(214, 72, 64, 180));
        g.fill(new RoundRectangle2D.Double(30, 500, 18, 14, 4, 4));
        g.setColor(new Color(66, 120, 214, 180));
        g.fill(new RoundRectangle2D.Double(50, 506, 18, 14, 4, 4));
        g.setColor(new Color(80, 170, 96, 180));
        g.fill(new RoundRectangle2D.Double(40, 488, 18, 14, 4, 4));
    }

    private static void yildiz(Graphics2D g, double cx, double cy, double r) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < 10; i++) {
            double a = -Math.PI / 2 + i * Math.PI / 5;
            double rr = i % 2 == 0 ? r : r * 0.45;
            double px = cx + Math.cos(a) * rr;
            double py = cy + Math.sin(a) * rr;
            if (i == 0) {
                p.moveTo(px, py);
            } else {
                p.lineTo(px, py);
            }
        }
        p.closePath();
        g.fill(p);
    }

    private static void etiket(Graphics2D g, String metin, double merkezX, double tabanY) {
        Font f = new Font("SansSerif", Font.BOLD, 11);
        FontMetrics fm = g.getFontMetrics(f);
        int w = fm.stringWidth(metin) + 14;
        double x = Math.max(2, Math.min(W - w - 2, merkezX - w / 2.0));
        g.setColor(new Color(22, 22, 48, 220));
        g.fill(new RoundRectangle2D.Double(x, tabanY - 13, w, 18, 10, 10));
        g.setFont(f);
        g.setColor(Tema.AY_ISIGI);
        g.drawString(metin, (float) x + 7, (float) tabanY);
    }
}
