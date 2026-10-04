package uykukalesi.varlik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

/**
 * Kısa ömürlü görsel geri bildirimler: hasar sayısı, bilye patlama halkası,
 * su sıçraması, dağılma bulutu. Simülasyonu etkilemez, yalnızca görseldir.
 */
public final class GorselEfekt implements Cizilebilir {

    public enum Tur { YAZI, HALKA, SICRAMA, DAGILMA }

    private static final Font EFEKT_YAZISI = new Font("SansSerif", Font.BOLD, 12);

    private final Tur tur;
    private final double x;
    private final double y;
    private final double omur;
    private final String metin;
    private final Color renk;
    private final double yaricap;
    private double gecen;

    private GorselEfekt(Tur tur, double x, double y, double omur, String metin, Color renk, double yaricap) {
        this.tur = tur;
        this.x = x;
        this.y = y;
        this.omur = omur;
        this.metin = metin;
        this.renk = renk;
        this.yaricap = yaricap;
    }

    public static GorselEfekt yazi(double x, double y, String metin, Color renk) {
        return new GorselEfekt(Tur.YAZI, x, y, 0.9, metin, renk, 0);
    }

    public static GorselEfekt halka(double x, double y, double yaricap, Color renk) {
        return new GorselEfekt(Tur.HALKA, x, y, 0.45, null, renk, yaricap);
    }

    public static GorselEfekt sicrama(double x, double y, Color renk) {
        return new GorselEfekt(Tur.SICRAMA, x, y, 0.4, null, renk, 18);
    }

    public static GorselEfekt dagilma(double x, double y, Color renk) {
        return new GorselEfekt(Tur.DAGILMA, x, y, 0.55, null, renk, 22);
    }

    public void guncelle(double dt) {
        gecen += dt;
    }

    public boolean bittiMi() {
        return gecen >= omur;
    }

    private Color alfali(Color c, double oran) {
        int a = (int) Math.max(0, Math.min(255, c.getAlpha() * oran));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    @Override
    public void ciz(Graphics2D g) {
        double t = Math.min(1, gecen / omur);
        double kalan = 1 - t;
        switch (tur) {
            case YAZI: {
                g.setFont(EFEKT_YAZISI);
                FontMetrics fm = g.getFontMetrics();
                float tx = (float) (x - fm.stringWidth(metin) / 2.0);
                float ty = (float) (y - 24 * t);
                g.setColor(alfali(new Color(0, 0, 0, 200), kalan));
                g.drawString(metin, tx + 1, ty + 1);
                g.setColor(alfali(renk, kalan));
                g.drawString(metin, tx, ty);
                break;
            }
            case HALKA: {
                double r = yaricap * (0.35 + 0.65 * t);
                Ellipse2D daire = new Ellipse2D.Double(x - r, y - r, r * 2, r * 2);
                g.setColor(alfali(new Color(renk.getRed(), renk.getGreen(), renk.getBlue(), 70), kalan));
                g.fill(daire);
                g.setStroke(new BasicStroke(3f));
                g.setColor(alfali(renk, kalan));
                g.draw(daire);
                break;
            }
            case SICRAMA: {
                g.setColor(alfali(renk, kalan));
                for (int i = 0; i < 7; i++) {
                    double a = i * Math.PI * 2 / 7;
                    double r = yaricap * t;
                    double px = x + Math.cos(a) * r;
                    double py = y + Math.sin(a) * r - 6 * Math.sin(t * Math.PI);
                    g.fill(new Ellipse2D.Double(px - 2.5, py - 2.5, 5, 5));
                }
                break;
            }
            case DAGILMA: {
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI * 2 / 8 + 0.4;
                    double r = yaricap * t;
                    double boy = 10 * kalan + 3;
                    g.setColor(alfali(renk, kalan * 0.9));
                    g.fill(new Ellipse2D.Double(x + Math.cos(a) * r - boy / 2,
                            y + Math.sin(a) * r - boy / 2, boy, boy));
                }
                break;
            }
            default:
                break;
        }
    }
}
