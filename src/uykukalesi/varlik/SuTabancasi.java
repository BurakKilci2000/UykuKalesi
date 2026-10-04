package uykukalesi.varlik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

import uykukalesi.kayit.Bicim;

/**
 * BUZ KULESİ — Su Tabancası.
 * Hasar 15, 2 saniyede 1 atış, tek hedef (menzildeki yatağa en yakın yaratık).
 * Islattığı yaratığın hızını 3 saniye boyunca %50 düşürür; ıslak yaratık maviye döner.
 * Maliyet: 70 şeker.
 */
public class SuTabancasi extends OyuncakSavunucu {

    public static final int MALIYET = 70;
    public static final int VURUS_GUCU = 15;
    public static final double MENZIL = 120;
    public static final double ATIS_ARALIGI = 2.0;
    public static final double ISLATMA_SURESI = 3.0;

    private static final Color YESIL_BLOK = new Color(80, 170, 96);
    private static final Color TURUNCU_PLASTIK = new Color(245, 140, 40);
    private static final Color SU = new Color(90, 180, 255);

    public SuTabancasi(String kimlik, double x, double y) {
        super(kimlik, x, y, VURUS_GUCU, MENZIL, ATIS_ARALIGI, MALIYET);
    }

    @Override
    public String getTurAdi() {
        return "SuTabancasi";
    }

    @Override
    public String getGorunenAd() {
        return "Su Tabancası";
    }

    @Override
    protected double getFirlatmaHizi() {
        return 330;
    }

    @Override
    public void isabetEt(KabusYaratigi hedef, double vurusX, double vurusY, SavasAlani alan) {
        alan.efektEkle(GorselEfekt.sicrama(vurusX, vurusY, SU));
        if (!hedef.sahadaMi()) {
            return;
        }
        hasarUygula(hedef, alan, "Su jeti isabet etti: ");
        if (hedef.sahadaMi()) {
            hedef.islat(ISLATMA_SURESI);
            alan.gunlugeYaz("'" + hedef.getTamAd() + "' sırılsıklam oldu: Yavaşlatma %50 (3 sn), hız "
                    + Bicim.sayi(hedef.getTemelAdimHizi()) + " → " + Bicim.sayi(hedef.getAnlikAdimHizi())
                    + " px/sn.");
        }
    }

    @Override
    public void mermiyiCiz(Graphics2D g, double x, double y) {
        Path2D damla = new Path2D.Double();
        damla.moveTo(x, y - 6);
        damla.quadTo(x + 5, y + 1, x, y + 4);
        damla.quadTo(x - 5, y + 1, x, y - 6);
        g.setColor(SU);
        g.fill(damla);
        g.setColor(new Color(225, 245, 255));
        g.fill(new Ellipse2D.Double(x - 1.8, y - 1, 2, 2.5));
    }

    @Override
    protected void govdeyiCiz(Graphics2D g, double cx, double cy, double aci, double atesAnim) {
        oyuncakBlokCiz(g, cx, cy + 2, YESIL_BLOK);
        AffineTransform eski = g.getTransform();
        g.translate(cx, cy - 3);
        g.rotate(aci);
        // Tabanca gövdesi (namlu +x yönünde)
        g.setColor(TURUNCU_PLASTIK);
        g.fill(new RoundRectangle2D.Double(-9, -5, 20, 10, 6, 6));
        g.setColor(TURUNCU_PLASTIK.darker());
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new RoundRectangle2D.Double(-9, -5, 20, 10, 6, 6));
        // Namlu
        g.setColor(new Color(250, 210, 60));
        g.fill(new RoundRectangle2D.Double(10, -2.5, 8, 5, 3, 3));
        // Su deposu
        g.setColor(new Color(200, 235, 255, 230));
        g.fill(new Ellipse2D.Double(-8, -12, 12, 9));
        g.setColor(SU);
        g.fill(new Ellipse2D.Double(-7, -8.5, 10, 5));
        // Atış anında namlu ucunda su parlaması
        if (atesAnim > 0) {
            g.setColor(new Color(170, 220, 255, 200));
            g.fill(new Ellipse2D.Double(17, -4, 8, 8));
        }
        g.setTransform(eski);
    }
}
