package uykukalesi.varlik;

import java.awt.Graphics2D;

/**
 * Bir savunucunun fırlattığı mermi (sapan taşı, bilye, su jeti).
 * Hedefi takip eder; hedefe vardığında kaynağın isabetEt() metodunu çağırır.
 * Hangi etkinin oluşacağına kaynak savunucu karar verir (polimorfizm).
 */
public final class Firlatma implements Cizilebilir {

    private final OyuncakSavunucu kaynak;
    private final KabusYaratigi hedef;
    private final double hiz;

    private double x;
    private double y;
    private double hedefX;
    private double hedefY;
    private boolean tamamlandi;

    public Firlatma(OyuncakSavunucu kaynak, KabusYaratigi hedef, double hiz) {
        this.kaynak = kaynak;
        this.hedef = hedef;
        this.hiz = hiz;
        this.x = kaynak.getX();
        this.y = kaynak.getY() - 8;
        this.hedefX = hedef.getX();
        this.hedefY = hedef.getGorselY();
    }

    public void guncelle(double dt, SavasAlani alan) {
        if (tamamlandi) {
            return;
        }
        // Hedef hâlâ sahadaysa onu takip et; değilse son bilinen noktaya git.
        if (hedef.sahadaMi()) {
            hedefX = hedef.getX();
            hedefY = hedef.getGorselY();
        }
        double dx = hedefX - x;
        double dy = hedefY - y;
        double mesafe = Math.hypot(dx, dy);
        double adim = hiz * dt;
        if (mesafe <= adim) {
            x = hedefX;
            y = hedefY;
            tamamlandi = true;
            kaynak.isabetEt(hedef, x, y, alan);
        } else {
            x += dx / mesafe * adim;
            y += dy / mesafe * adim;
        }
    }

    public boolean tamamlandiMi() {
        return tamamlandi;
    }

    @Override
    public void ciz(Graphics2D g) {
        if (!tamamlandi) {
            kaynak.mermiyiCiz(g, x, y);
        }
    }
}
