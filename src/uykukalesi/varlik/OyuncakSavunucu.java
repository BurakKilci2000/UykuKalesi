package uykukalesi.varlik;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

import uykukalesi.kayit.Bicim;

/**
 * Çocuğun uykusunu koruyan tüm oyuncak savunucuların soyut atası
 * (dokümandaki "Kule" sınıfı).
 *
 * Soyutlama   : isabet etkisi, mermi ve gövde görünümü alt sınıfa bırakılır.
 * Kapsülleme  : hasar, menzil, atış aralığı ve maliyet değiştirilemez (final, private).
 * Polimorfizm : hedefAlabilirMi(), hedefSec(), tabanHasarHesapla(), isabetEt()
 *               alt sınıflarda farklı davranır; motor yalnızca guncelle() çağırır.
 */
public abstract class OyuncakSavunucu extends OyunVarligi {

    private static final Font KUCUK_YAZI = new Font("SansSerif", Font.BOLD, 9);

    private final int vurusGucu;
    private final double gorusMenzili;
    private final double atisAraligi;
    private final int sekerMaliyeti;

    private double kalanDoldurma;
    private double nisanAcisi = -Math.PI / 2;
    private double atesAnimasyonu;
    private KabusYaratigi sonHedef;

    protected OyuncakSavunucu(String kimlik, double x, double y, int vurusGucu,
                              double gorusMenzili, double atisAraligi, int sekerMaliyeti) {
        super(kimlik, x, y);
        this.vurusGucu = vurusGucu;
        this.gorusMenzili = gorusMenzili;
        this.atisAraligi = atisAraligi;
        this.sekerMaliyeti = sekerMaliyeti;
    }

    // ------------------------------------------------------------------
    // Alt sınıfların doldurması gereken soyut davranışlar
    // ------------------------------------------------------------------

    /** Mermi hedefe vardığında ne olacağı (tek hedef hasarı, alan hasarı, ıslatma...). */
    public abstract void isabetEt(KabusYaratigi hedef, double vurusX, double vurusY, SavasAlani alan);

    /** Bu savunucunun fırlattığı merminin görünümü. */
    public abstract void mermiyiCiz(Graphics2D g, double x, double y);

    /** Savunucunun gövdesi; aci hedefe doğru nişan açısıdır (radyan). */
    protected abstract void govdeyiCiz(Graphics2D g, double cx, double cy, double aci, double atesAnim);

    // ------------------------------------------------------------------
    // Ortak davranış (alt sınıflar gerektiğinde ezer)
    // ------------------------------------------------------------------

    /** Varsayılan: her yaratık hedeflenebilir. Bilye Mancınığı uçanlar için false döndürür. */
    public boolean hedefAlabilirMi(KabusYaratigi yaratik) {
        return true;
    }

    /** Merminin hızı (piksel / saniye). */
    protected double getFirlatmaHizi() {
        return 380;
    }

    /**
     * Varsayılan hedef seçimi: menzildeki, hedeflenebilir yaratıklardan
     * yatağa (üsse) en yakın olanı seçer.
     */
    protected KabusYaratigi hedefSec(SavasAlani alan) {
        KabusYaratigi enYakin = null;
        for (KabusYaratigi y : alan.getSahadakiYaratiklar()) {
            if (!y.sahadaMi() || !menzildeMi(y) || !hedefAlabilirMi(y)) {
                continue;
            }
            if (enYakin == null || y.getYatagaKalanMesafe() < enYakin.getYatagaKalanMesafe()) {
                enYakin = y;
            }
        }
        return enYakin;
    }

    /** Taban hasar: varsayılan olarak savunucunun vuruş gücü. Lastik Sapan zırhlıya ceza uygular. */
    protected double tabanHasarHesapla(KabusYaratigi hedef) {
        return vurusGucu;
    }

    /** Doküman Bölüm 4.3: Net_Hasar = Kule_Hasarı * (1 - (Zırh / (Zırh + 100.0))) */
    public static double zirhFormuluUygula(double hasar, int zirh) {
        return hasar * (1 - (zirh / (zirh + 100.0)));
    }

    public final double netHasarHesapla(KabusYaratigi hedef) {
        return zirhFormuluUygula(tabanHasarHesapla(hedef), hedef.getZirhKaplamasi());
    }

    /**
     * Şablon metot — motor her karede çağırır:
     * doldurma süresini işletir, hedef seçer, mermi fırlatır.
     */
    public final void guncelle(double dt, SavasAlani alan) {
        if (atesAnimasyonu > 0) {
            atesAnimasyonu = Math.max(0, atesAnimasyonu - dt);
        }
        if (kalanDoldurma > 0) {
            kalanDoldurma -= dt;
            if (kalanDoldurma > 0) {
                return;
            }
        }
        KabusYaratigi hedef = hedefSec(alan);
        if (hedef == null) {
            kalanDoldurma = 0;
            sonHedef = null;
            return;
        }
        if (hedef != sonHedef) {
            alan.gunlugeYaz("Savunucu '" + getTamAd() + "' hedef aldı: '" + hedef.getTamAd()
                    + "' (öncelik: yatağa en yakın, kalan yol " + Bicim.sayi(Math.round(hedef.getYatagaKalanMesafe()))
                    + " px).");
            sonHedef = hedef;
        }
        nisanAcisi = Math.atan2(hedef.getGorselY() - getY(), hedef.getX() - getX());
        atesAnimasyonu = 0.2;
        kalanDoldurma = atisAraligi;
        alan.firlatmaEkle(new Firlatma(this, hedef, getFirlatmaHizi()));
    }

    /**
     * Hasarı hesaplar, uygular, adım adım günlüğe yazar ve ekranda hasar sayısı gösterir.
     * @return uygulanan net hasar
     */
    protected final double hasarUygula(KabusYaratigi hedef, SavasAlani alan, String onEk) {
        double taban = tabanHasarHesapla(hedef);
        double net = zirhFormuluUygula(taban, hedef.getZirhKaplamasi());
        hedef.hasarAl(net);

        StringBuilder sb = new StringBuilder(onEk);
        sb.append("'").append(hedef.getTamAd()).append("' ← ").append(getTamAd())
          .append(": Taban ").append(Bicim.sayi(vurusGucu));
        if (taban != vurusGucu) {
            sb.append(" → Zırhlı cezası %50 → ").append(Bicim.sayi(taban));
        }
        if (hedef.zirhliMi()) {
            sb.append("; Zırh formülü (Zırh ").append(hedef.getZirhKaplamasi())
              .append(") ile Net Hasar ").append(Bicim.sayi(net));
        } else {
            sb.append("; zırh yok, Net Hasar ").append(Bicim.sayi(net));
        }
        sb.append(". Kalan Korku Enerjisi: ").append(Bicim.sayi(hedef.getKorkuEnerjisi()))
          .append("/").append(Bicim.sayi(hedef.getMaksKorkuEnerjisi())).append(".");
        alan.gunlugeYaz(sb.toString());

        alan.efektEkle(GorselEfekt.yazi(hedef.getX(), hedef.getGorselY() - 14,
                "-" + Bicim.sayi(net), new Color(255, 105, 105)));
        return net;
    }

    public final boolean menzildeMi(KabusYaratigi yaratik) {
        return uzaklik(yaratik) <= gorusMenzili;
    }

    // ------------------------------------------------------------------
    // Çizim
    // ------------------------------------------------------------------

    @Override
    public final void ciz(Graphics2D g) {
        double cx = getX();
        double cy = getY();
        g.setColor(new Color(0, 0, 0, 70));
        g.fill(new Ellipse2D.Double(cx - 19, cy + 9, 38, 11));
        govdeyiCiz(g, cx, cy, nisanAcisi, atesAnimasyonu);

        // Doldurma çubuğu: atış hızının (1 / 2 / 3 sn) görünür olması için
        double oran = atisAraligi <= 0 ? 1 : 1 - Math.max(0, kalanDoldurma) / atisAraligi;
        g.setColor(new Color(20, 16, 30, 200));
        g.fill(new RoundRectangle2D.Double(cx - 16, cy + 21, 32, 4, 3, 3));
        g.setColor(oran >= 1 ? new Color(255, 214, 90) : new Color(170, 170, 200));
        g.fill(new Rectangle2D.Double(cx - 16, cy + 21, 32 * oran, 4));

        g.setFont(KUCUK_YAZI);
        FontMetrics fm = g.getFontMetrics();
        String etiket = getKimlik();
        float ex = (float) (cx - fm.stringWidth(etiket) / 2.0);
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(etiket, ex + 1, (float) cy + 35);
        g.setColor(new Color(255, 248, 225));
        g.drawString(etiket, ex, (float) cy + 34);
    }

    @Override
    public final void onizlemeCiz(Graphics2D g, double cx, double cy) {
        govdeyiCiz(g, cx, cy, -Math.PI / 4, 0);
    }

    /** Menzil dairesi (fare üzerine gelince ve yerleştirme önizlemesinde). */
    public static void menzilCiz(Graphics2D g, double cx, double cy, double menzil, Color renk) {
        Ellipse2D daire = new Ellipse2D.Double(cx - menzil, cy - menzil, menzil * 2, menzil * 2);
        g.setColor(new Color(renk.getRed(), renk.getGreen(), renk.getBlue(), 38));
        g.fill(daire);
        g.setColor(new Color(renk.getRed(), renk.getGreen(), renk.getBlue(), 150));
        g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                1f, new float[]{6f, 5f}, 0f));
        g.draw(daire);
    }

    /** Oyuncak blok taban (savunucuların ortak kaidesi). */
    protected static void oyuncakBlokCiz(Graphics2D g, double cx, double cy, Color renk) {
        RoundRectangle2D blok = new RoundRectangle2D.Double(cx - 16, cy - 2, 32, 18, 6, 6);
        g.setColor(renk);
        g.fill(blok);
        g.setColor(renk.darker());
        g.setStroke(new BasicStroke(1.6f));
        g.draw(blok);
        // Lego benzeri çıkıntılar
        g.setColor(renk.brighter());
        g.fill(new Ellipse2D.Double(cx - 12, cy - 5, 8, 5));
        g.fill(new Ellipse2D.Double(cx + 4, cy - 5, 8, 5));
    }

    // ------------------------------------------------------------------
    // Okuma metotları
    // ------------------------------------------------------------------

    public int getVurusGucu() {
        return vurusGucu;
    }

    public double getGorusMenzili() {
        return gorusMenzili;
    }

    public double getAtisAraligi() {
        return atisAraligi;
    }

    public int getSekerMaliyeti() {
        return sekerMaliyeti;
    }

    public double getKalanDoldurma() {
        return Math.max(0, kalanDoldurma);
    }

    @Override
    public String toString() {
        return getTamAd();
    }
}
