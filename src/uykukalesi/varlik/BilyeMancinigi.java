package uykukalesi.varlik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * TOPÇU KULESİ — Bilye Mancınığı.
 * Hasar 20, 3 saniyede 1 atış. Uçanları hedefleyemez (bilye yerde yuvarlanır).
 * Menzildeki yatağa en yakın yer yaratığına atar; bilye çarptığı noktayı merkez
 * alarak 50 piksel yarıçaptaki TÜM yer yaratıklarına hasar verir.
 * Maliyet: 75 şeker.
 */
public class BilyeMancinigi extends OyuncakSavunucu {

    public static final int MALIYET = 75;
    public static final int VURUS_GUCU = 20;
    public static final double MENZIL = 130;
    public static final double ATIS_ARALIGI = 3.0;
    public static final double PATLAMA_YARICAPI = 50;

    private static final Color MAVI_BLOK = new Color(66, 120, 214);
    private static final Color AHSAP = new Color(170, 118, 66);

    /** Aynı uçan yaratık için "hedefleyemez" kaydını bir kez yazmak için. */
    private final Set<String> atlananUcanlar = new HashSet<>();

    public BilyeMancinigi(String kimlik, double x, double y) {
        super(kimlik, x, y, VURUS_GUCU, MENZIL, ATIS_ARALIGI, MALIYET);
    }

    @Override
    public String getTurAdi() {
        return "BilyeMancinigi";
    }

    @Override
    public String getGorunenAd() {
        return "Bilye Mancınığı";
    }

    @Override
    protected double getFirlatmaHizi() {
        return 240;
    }

    /** Uçan yaratıklar hedeflenemez. */
    @Override
    public boolean hedefAlabilirMi(KabusYaratigi yaratik) {
        return !yaratik.ucabilirMi();
    }

    @Override
    protected KabusYaratigi hedefSec(SavasAlani alan) {
        for (KabusYaratigi y : alan.getSahadakiYaratiklar()) {
            if (y.sahadaMi() && y.ucabilirMi() && menzildeMi(y) && atlananUcanlar.add(y.getKimlik())) {
                alan.gunlugeYaz("Savunucu '" + getTamAd() + "' uçan '" + y.getTamAd()
                        + "' yaratığını hedefleyemez; atladı (bilyeler yerde yuvarlanır).");
            }
        }
        return super.hedefSec(alan);
    }

    @Override
    public void isabetEt(KabusYaratigi hedef, double vurusX, double vurusY, SavasAlani alan) {
        double mx = hedef.sahadaMi() ? hedef.getX() : vurusX;
        double my = hedef.sahadaMi() ? hedef.getY() : vurusY;

        alan.gunlugeYaz("Savunucu '" + getTamAd() + "' bilyesi yere çarptı (uçanları hedefleyemez). "
                + "Alan vuruşu: merkez '" + hedef.getTamAd() + "', yarıçap "
                + (int) PATLAMA_YARICAPI + " px.");
        alan.efektEkle(GorselEfekt.halka(mx, my, PATLAMA_YARICAPI, new Color(255, 176, 70)));

        List<KabusYaratigi> etkilenenler = new ArrayList<>();
        for (KabusYaratigi y : alan.getSahadakiYaratiklar()) {
            if (y.sahadaMi() && !y.ucabilirMi() && y.uzaklik(mx, my) <= PATLAMA_YARICAPI) {
                etkilenenler.add(y);
            }
        }
        if (etkilenenler.isEmpty()) {
            alan.gunlugeYaz("— Bilye boşa yuvarlandı; patlama alanında yer yaratığı yok.");
            return;
        }
        for (KabusYaratigi y : etkilenenler) {
            hasarUygula(y, alan, y == hedef ? "— Merkez: " : "— Yakındaki: ");
        }
    }

    @Override
    public void mermiyiCiz(Graphics2D g, double x, double y) {
        float r = 6.5f;
        RadialGradientPaint cam = new RadialGradientPaint(
                new Point2D.Double(x - 2, y - 2), r * 1.4f, new float[]{0f, 0.5f, 1f},
                new Color[]{new Color(230, 255, 250), new Color(60, 190, 170), new Color(20, 90, 110)});
        g.setPaint(cam);
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        g.setColor(new Color(255, 120, 60));
        g.setStroke(new BasicStroke(1.4f));
        g.draw(new Line2D.Double(x - 3, y + 2, x + 3, y - 1));
    }

    @Override
    protected void govdeyiCiz(Graphics2D g, double cx, double cy, double aci, double atesAnim) {
        oyuncakBlokCiz(g, cx, cy + 2, MAVI_BLOK);
        // Tekerlekler
        g.setColor(new Color(60, 45, 35));
        g.fill(new Ellipse2D.Double(cx - 15, cy + 8, 9, 9));
        g.fill(new Ellipse2D.Double(cx + 6, cy + 8, 9, 9));
        g.setColor(new Color(200, 180, 120));
        g.fill(new Ellipse2D.Double(cx - 12, cy + 11, 3, 3));
        g.fill(new Ellipse2D.Double(cx + 9, cy + 11, 3, 3));
        // Kol: hedefe doğru döner, atışta öne savrulur
        AffineTransform eski = g.getTransform();
        g.translate(cx, cy);
        g.rotate(aci + Math.PI / 2);
        double savrulma = atesAnim > 0 ? -0.6 : 0.25;
        g.rotate(savrulma);
        g.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(AHSAP.darker());
        g.draw(new Line2D.Double(0, 4, 0, -16));
        g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(AHSAP);
        g.draw(new Line2D.Double(0, 4, 0, -16));
        // Kaşık ve içindeki bilye
        g.setColor(AHSAP.darker());
        g.fill(new Ellipse2D.Double(-6, -22, 12, 8));
        if (atesAnim <= 0) {
            g.setColor(new Color(70, 200, 180));
            g.fill(new Ellipse2D.Double(-4, -23, 8, 8));
        }
        g.setTransform(eski);
        // Mil
        g.setColor(new Color(90, 90, 100));
        g.fill(new Ellipse2D.Double(cx - 3, cy - 3, 6, 6));
    }
}
