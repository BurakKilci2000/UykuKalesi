package uykukalesi.varlik;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;

/**
 * OKÇU KULESİ — Lastik Sapan.
 * Hasar 10, saniyede 1 atış, tek hedef (menzildeki yatağa en yakın yaratık).
 * Zırhlı yaratığa %50 ceza uygular; ardından Bölüm 4.3 zırh formülü işler.
 * Maliyet: 50 şeker.
 */
public class LastikSapan extends OyuncakSavunucu {

    public static final int MALIYET = 50;
    public static final int VURUS_GUCU = 10;
    public static final double MENZIL = 140;
    public static final double ATIS_ARALIGI = 1.0;
    public static final double ZIRHLI_CEZA_ORANI = 0.50;

    private static final Color KIRMIZI_BLOK = new Color(214, 72, 64);
    private static final Color AHSAP = new Color(150, 98, 52);

    public LastikSapan(String kimlik, double x, double y) {
        super(kimlik, x, y, VURUS_GUCU, MENZIL, ATIS_ARALIGI, MALIYET);
    }

    @Override
    public String getTurAdi() {
        return "LastikSapan";
    }

    @Override
    public String getGorunenAd() {
        return "Lastik Sapan";
    }

    /** Zırhlı yaratığa %50 daha az taban hasar (polimorfik ezme). */
    @Override
    protected double tabanHasarHesapla(KabusYaratigi hedef) {
        return hedef.zirhliMi() ? getVurusGucu() * (1 - ZIRHLI_CEZA_ORANI) : getVurusGucu();
    }

    @Override
    public void isabetEt(KabusYaratigi hedef, double vurusX, double vurusY, SavasAlani alan) {
        if (!hedef.sahadaMi()) {
            return; // Hedef taş varmadan dağıldı ya da yatağa ulaştı: taş boşa gitti.
        }
        hasarUygula(hedef, alan, "Sapan taşı isabet etti: ");
    }

    @Override
    public void mermiyiCiz(Graphics2D g, double x, double y) {
        g.setColor(new Color(95, 92, 100));
        g.fill(new Ellipse2D.Double(x - 3.5, y - 3.5, 7, 7));
        g.setColor(new Color(200, 200, 210));
        g.fill(new Ellipse2D.Double(x - 2, y - 2.5, 2.5, 2.5));
    }

    @Override
    protected void govdeyiCiz(Graphics2D g, double cx, double cy, double aci, double atesAnim) {
        oyuncakBlokCiz(g, cx, cy + 2, KIRMIZI_BLOK);
        AffineTransform eski = g.getTransform();
        g.translate(cx, cy - 2);
        g.rotate(aci + Math.PI / 2);
        // Y biçimli ahşap çatal
        g.setColor(AHSAP.darker());
        kalin(g, 5f);
        g.draw(new Line2D.Double(0, 6, 0, -3));
        g.draw(new Line2D.Double(0, -3, -7, -14));
        g.draw(new Line2D.Double(0, -3, 7, -14));
        g.setColor(AHSAP);
        kalin(g, 3f);
        g.draw(new Line2D.Double(0, 6, 0, -3));
        g.draw(new Line2D.Double(0, -3, -7, -14));
        g.draw(new Line2D.Double(0, -3, 7, -14));
        // Lastik: atışta ileri fırlar, dolarken gerilir
        double gerilme = atesAnim > 0 ? -6 : 4;
        g.setColor(new Color(240, 170, 60));
        kalin(g, 1.8f);
        g.draw(new QuadCurve2D.Double(-7, -14, 0, -14 + gerilme * 2, 7, -14));
        g.setTransform(eski);
    }

    private static void kalin(Graphics2D g, float k) {
        g.setStroke(new java.awt.BasicStroke(k, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
    }
}
