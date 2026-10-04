package uykukalesi.varlik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import uykukalesi.motor.Yol;

/**
 * Dolaptan çıkıp yatağa ulaşmaya çalışan tüm kabus yaratıklarının soyut atası
 * (dokümandaki "Düşman" sınıfı).
 *
 * Soyutlama   : ödül, uyku hasarı, uçabilme ve görünüm alt sınıflara bırakılır.
 * Kapsülleme  : can (korkuEnerjisi), hız, zırh private; yalnızca hasarAl() / islat()
 *               gibi kontrollü metotlarla değişir.
 * Şablon metot: ciz() ortak kısmı (gölge, ıslaklık rengi, sağlık barı) çizer,
 *               gövdeyi ise soyut govdeyiCiz() ile alt sınıfa bırakır.
 */
public abstract class KabusYaratigi extends OyunVarligi {

    /** Standart düşmanın can (korku enerjisi) değeri. */
    public static final double STANDART_KORKU_ENERJISI = 50.0;
    /** Standart düşmanın hızı (piksel / saniye). */
    public static final double STANDART_ADIM_HIZI = 50.0;
    /** Su Tabancası ıslattığında hız %50 düşer. */
    public static final double ISLAKLIK_YAVASLATMA_ORANI = 0.50;

    private static final Color ISLAK_MAVI = new Color(35, 120, 255);
    private static final double UCUS_YUKSEKLIGI = 16.0;
    private static final double DARBE_PARLAMA_SURESI = 0.15;
    private static final Font KUCUK_YAZI = new Font("SansSerif", Font.BOLD, 9);

    /** Yaratığın yaşam döngüsü. */
    public enum Durum {
        DOLAPTA_BEKLIYOR, SAHADA, DAGILDI, YATAGA_ULASTI
    }

    private final double maksKorkuEnerjisi;
    private double korkuEnerjisi;
    private final double temelAdimHizi;
    private final int zirhKaplamasi;
    private final Yol yol;

    private Durum durum = Durum.DOLAPTA_BEKLIYOR;
    private double katedilenYol;
    private double islaklikSuresi;
    private double darbeParlamasi;
    private double animasyonSaati;

    protected KabusYaratigi(String kimlik, double maksKorkuEnerjisi, double temelAdimHizi,
                            int zirhKaplamasi, Yol yol) {
        super(kimlik, yol.getBaslangic().x, yol.getBaslangic().y);
        if (maksKorkuEnerjisi <= 0 || temelAdimHizi <= 0 || zirhKaplamasi < 0) {
            throw new IllegalArgumentException("Geçersiz yaratık değerleri.");
        }
        this.maksKorkuEnerjisi = maksKorkuEnerjisi;
        this.korkuEnerjisi = maksKorkuEnerjisi;
        this.temelAdimHizi = temelAdimHizi;
        this.zirhKaplamasi = zirhKaplamasi;
        this.yol = yol;
    }

    // ------------------------------------------------------------------
    // Alt sınıfların doldurması gereken soyut davranışlar
    // ------------------------------------------------------------------

    /** Yaratık dağıtıldığında (öldüğünde) oyuncuya verilen şeker (para). */
    public abstract int getSekerOdulu();

    /** Yatağa (üsse) ulaştığında oyuncunun uyku derinliğinden (candan) düşen miktar. */
    public abstract int getUykuHasari();

    public abstract boolean ucabilirMi();

    protected abstract Color getAnaRenk();

    /** Yaratığın gövdesini (cx, cy) merkezine verilen renkle çizer. */
    protected abstract void govdeyiCiz(Graphics2D g, double cx, double cy, Color renk, double saat);

    // ------------------------------------------------------------------
    // Simülasyon davranışı
    // ------------------------------------------------------------------

    /** Yaratık dolaptan çıkıp yolun başına yerleşir. */
    public void sahayaCik() {
        if (durum == Durum.DOLAPTA_BEKLIYOR) {
            durum = Durum.SAHADA;
            katedilenYol = 0;
            konumuGuncelle();
        }
    }

    /** Yaratığı yol boyunca dt saniye ilerletir. */
    public void ilerle(double dt) {
        if (durum != Durum.SAHADA) {
            return;
        }
        animasyonSaati += dt;
        if (islaklikSuresi > 0) {
            islaklikSuresi = Math.max(0, islaklikSuresi - dt);
        }
        if (darbeParlamasi > 0) {
            darbeParlamasi = Math.max(0, darbeParlamasi - dt);
        }
        katedilenYol += getAnlikAdimHizi() * dt;
        if (katedilenYol >= yol.getToplamUzunluk()) {
            katedilenYol = yol.getToplamUzunluk();
            durum = Durum.YATAGA_ULASTI;
        }
        konumuGuncelle();
    }

    private void konumuGuncelle() {
        Point2D.Double p = yol.noktaBul(katedilenYol);
        konumuAyarla(p.x, p.y);
    }

    /**
     * Net hasarı uygular. Korku enerjisi 0'a ya da altına inerse yaratık dağılır.
     * @return gerçekte düşen enerji miktarı
     */
    public double hasarAl(double netHasar) {
        if (durum != Durum.SAHADA || netHasar <= 0) {
            return 0;
        }
        double onceki = korkuEnerjisi;
        korkuEnerjisi -= netHasar;
        darbeParlamasi = DARBE_PARLAMA_SURESI;
        if (korkuEnerjisi <= 0) {
            korkuEnerjisi = 0;
            durum = Durum.DAGILDI;
        }
        return onceki - korkuEnerjisi;
    }

    /** Su Tabancası etkisi: süre boyunca hız %50 düşer. Tekrar ıslatma süreyi yeniler. */
    public void islat(double sure) {
        if (durum == Durum.SAHADA) {
            islaklikSuresi = Math.max(islaklikSuresi, sure);
        }
    }

    // ------------------------------------------------------------------
    // Okuma metotları (getter)
    // ------------------------------------------------------------------

    public double getAnlikAdimHizi() {
        return islakMi() ? temelAdimHizi * (1 - ISLAKLIK_YAVASLATMA_ORANI) : temelAdimHizi;
    }

    public double getTemelAdimHizi() {
        return temelAdimHizi;
    }

    public double getKorkuEnerjisi() {
        return korkuEnerjisi;
    }

    public double getMaksKorkuEnerjisi() {
        return maksKorkuEnerjisi;
    }

    public int getZirhKaplamasi() {
        return zirhKaplamasi;
    }

    public boolean zirhliMi() {
        return zirhKaplamasi > 0;
    }

    public boolean islakMi() {
        return islaklikSuresi > 0;
    }

    public double getIslaklikSuresi() {
        return islaklikSuresi;
    }

    public Durum getDurum() {
        return durum;
    }

    public boolean sahadaMi() {
        return durum == Durum.SAHADA;
    }

    public double getKatedilenYol() {
        return katedilenYol;
    }

    /** Hedef önceliği için: yatağa (üsse) kalan yol uzunluğu. Küçük olan yatağa daha yakındır. */
    public double getYatagaKalanMesafe() {
        return yol.getToplamUzunluk() - katedilenYol;
    }

    /** Uçan yaratıklar yolun biraz üstünde çizilir; mermiler bu noktaya nişan alır. */
    public double getGorselY() {
        if (!ucabilirMi()) {
            return getY();
        }
        return getY() - UCUS_YUKSEKLIGI - Math.sin(animasyonSaati * 4) * 2;
    }

    // ------------------------------------------------------------------
    // Çizim (şablon metot)
    // ------------------------------------------------------------------

    @Override
    public final void ciz(Graphics2D g) {
        if (durum != Durum.SAHADA) {
            return;
        }
        double cx = getX();
        double cy = getGorselY();

        if (ucabilirMi()) {
            g.setColor(new Color(0, 0, 0, 70));
            g.fill(new Ellipse2D.Double(cx - 11, getY() - 4, 22, 8));
        }

        Color renk = islakMi() ? karistir(getAnaRenk(), ISLAK_MAVI, 0.78) : getAnaRenk();
        govdeyiCiz(g, cx, cy, renk, animasyonSaati);

        if (darbeParlamasi > 0) {
            int alfa = (int) (170 * darbeParlamasi / DARBE_PARLAMA_SURESI);
            g.setColor(new Color(255, 255, 255, Math.max(0, Math.min(255, alfa))));
            g.fill(new Ellipse2D.Double(cx - 14, cy - 14, 28, 28));
        }
        if (islakMi()) {
            damlalariCiz(g, cx, cy);
        }
        saglikBariniCiz(g, cx, cy - 25);
    }

    @Override
    public final void onizlemeCiz(Graphics2D g, double cx, double cy) {
        govdeyiCiz(g, cx, cy, getAnaRenk(), 0.3);
    }

    private void saglikBariniCiz(Graphics2D g, double cx, double ust) {
        double genislik = 34;
        double oran = korkuEnerjisi / maksKorkuEnerjisi;
        double sol = cx - genislik / 2;

        g.setColor(new Color(20, 16, 30, 220));
        g.fill(new RoundRectangle2D.Double(sol - 1, ust - 1, genislik + 2, 7, 4, 4));
        Color dolgu = oran > 0.6 ? new Color(110, 220, 120)
                : oran > 0.3 ? new Color(250, 200, 70) : new Color(240, 80, 80);
        g.setColor(dolgu);
        g.fill(new Rectangle2D.Double(sol, ust, genislik * oran, 5));

        g.setFont(KUCUK_YAZI);
        FontMetrics fm = g.getFontMetrics();
        String etiket = getKimlik();
        float ex = (float) (cx - fm.stringWidth(etiket) / 2.0);
        g.setColor(new Color(0, 0, 0, 150));
        g.drawString(etiket, ex + 1, (float) ust - 2);
        g.setColor(new Color(255, 255, 255, 220));
        g.drawString(etiket, ex, (float) ust - 3);

        if (zirhliMi()) {
            String z = "Z" + zirhKaplamasi;
            float zx = (float) (sol - fm.stringWidth(z) - 3);
            g.setColor(new Color(40, 40, 50, 210));
            g.fill(new RoundRectangle2D.Double(zx - 2, ust - 3, fm.stringWidth(z) + 4, 11, 4, 4));
            g.setColor(new Color(210, 215, 230));
            g.drawString(z, zx, (float) ust + 6);
        }
    }

    private void damlalariCiz(Graphics2D g, double cx, double cy) {
        g.setColor(new Color(140, 200, 255, 220));
        for (int i = 0; i < 3; i++) {
            double faz = (animasyonSaati * 1.6 + i / 3.0) % 1.0;
            double dx = cx - 10 + i * 10;
            double dy = cy + 4 + faz * 14;
            g.fill(new Ellipse2D.Double(dx - 1.5, dy - 2, 3, 4));
        }
    }

    // ------------------------------------------------------------------
    // Alt sınıfların ortak kullandığı küçük çizim yardımcıları
    // ------------------------------------------------------------------

    protected static void gozCiz(Graphics2D g, double x, double y, double r, Color bebek) {
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
        g.setColor(bebek);
        g.fill(new Ellipse2D.Double(x - r / 2, y - r / 2 + 0.5, r, r));
    }

    protected static void kalinCizgi(Graphics2D g, float kalinlik) {
        g.setStroke(new BasicStroke(kalinlik, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    }

    public static Color karistir(Color a, Color b, double oran) {
        double t = Math.max(0, Math.min(1, oran));
        return new Color(
                (int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t),
                a.getAlpha());
    }

    @Override
    public String toString() {
        return getTamAd();
    }
}
