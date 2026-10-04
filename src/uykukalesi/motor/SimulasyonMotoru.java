package uykukalesi.motor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import uykukalesi.kayit.Bicim;
import uykukalesi.kayit.SavunmaGunlugu;
import uykukalesi.varlik.Firlatma;
import uykukalesi.varlik.GorselEfekt;
import uykukalesi.varlik.KabusYaratigi;
import uykukalesi.varlik.OyuncakSavunucu;
import uykukalesi.varlik.SavasAlani;

/**
 * Simülasyon motoru (arka plan). Arayüzden tamamen bağımsızdır (Swing bilmez).
 *
 * Arayüz → Motor : savunucuInsaEt(), akiniBaslat(), setDuraklatildi(), guncelle(dt)
 * Motor → Arayüz : OyunOlayDinleyicisi (günlük satırı, oyun sonu)
 * Arayüz her karede durumu salt-okunur getter'larla okuyup çizer.
 */
public final class SimulasyonMotoru implements SavasAlani {

    public static final int BASLANGIC_UYKU = 100;     // oyuncu canı
    public static final int BASLANGIC_SEKER = 200;    // oyuncu parası
    public static final double AKIN_ARASI_BEKLEME = 10.0;
    private static final double DUYURU_SURESI = 2.6;

    /** Oyunun genel durumu. */
    public enum Durum {
        HAZIRLIK, AKIN_SURUYOR, AKIN_ARASI, KAZANILDI, KAYBEDILDI
    }

    private final Harita harita;
    private final AkinYoneticisi akinYoneticisi;
    private final SavunmaGunlugu gunluk;
    private final OyunOlayDinleyicisi dinleyici;

    private final List<OyuncakSavunucu> savunucular = new ArrayList<>();
    private final List<KabusYaratigi> sahadakiler = new ArrayList<>();
    private final List<Firlatma> firlatmalar = new ArrayList<>();
    private final List<GorselEfekt> efektler = new ArrayList<>();

    private int uykuDerinligi = BASLANGIC_UYKU;
    private int seker = BASLANGIC_SEKER;
    private Durum durum = Durum.HAZIRLIK;
    private double simulasyonSaati;
    private double araGeriSayim;
    private int savunucuSayaci;
    private boolean duraklatildi;
    private String duyuru;
    private double duyuruKalan;

    public SimulasyonMotoru(Harita harita, SavunmaGunlugu gunluk, OyunOlayDinleyicisi dinleyici, long tohum) {
        this.harita = harita;
        this.gunluk = gunluk;
        this.dinleyici = dinleyici;
        this.akinYoneticisi = new AkinYoneticisi(harita.getYol(), new Random(tohum));
    }

    /** İlk günlük kayıtlarını yazar. Arayüz hazır olduktan sonra çağrılır. */
    public void baslat() {
        gunlugeYaz("Simülasyon Başladı. Senaryo: '" + harita.getSenaryoAdi()
                + "'. Başlangıç Uyku Derinliği (Can): " + uykuDerinligi + ", Şeker (Para): " + seker + ".");
        for (int i = 0; i < akinYoneticisi.getToplamAkin(); i++) {
            StringBuilder sb = new StringBuilder();
            for (KabusYaratigi y : akinYoneticisi.getAkin(i)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(y.getTamAd());
                if (y.zirhliMi()) {
                    sb.append(" (Zırh ").append(y.getZirhKaplamasi()).append(")");
                }
            }
            gunlugeYaz("Dolapta hazırlanan Gece Yarısı Akını " + (i + 1) + ": "
                    + akinYoneticisi.akinOzeti(i) + " → " + sb + ".");
        }
        duyuruYap("Savunucularını yerleştir, sonra akını başlat");
    }

    // ------------------------------------------------------------------
    // Arayüzden gelen komutlar
    // ------------------------------------------------------------------

    public boolean satinAlinabilirMi(SavunucuTuru tur) {
        return !oyunBittiMi() && seker >= tur.getMaliyet();
    }

    public InsaSonucu savunucuInsaEt(SavunucuTuru tur, OyuncakYuvasi yuva) {
        String konum = "(" + (int) yuva.getX() + ", " + (int) yuva.getY() + ")";
        if (oyunBittiMi()) {
            return InsaSonucu.OYUN_BITTI;
        }
        if (!yuva.bosMu()) {
            gunlugeYaz("Kullanıcı, " + konum + " konumundaki Oyuncak Yuvası #" + yuva.getNumara()
                    + " için " + tur.getGorunenAd() + " istedi; yuva dolu, kurulmadı.");
            efektEkle(GorselEfekt.yazi(yuva.getX(), yuva.getY() - 26, "Yuva dolu", new Color(255, 120, 120)));
            return InsaSonucu.YUVA_DOLU;
        }
        if (seker < tur.getMaliyet()) {
            gunlugeYaz("Kullanıcı, " + konum + " konumuna " + tur.getGorunenAd() + " istedi; şeker yetersiz ("
                    + seker + "/" + tur.getMaliyet() + "), kurulmadı.");
            efektEkle(GorselEfekt.yazi(yuva.getX(), yuva.getY() - 26, "Şeker yetmiyor", new Color(255, 120, 120)));
            return InsaSonucu.YETERSIZ_SEKER;
        }
        String kimlik = String.format("ID%03d", ++savunucuSayaci);
        OyuncakSavunucu yeni = tur.uret(kimlik, yuva.getX(), yuva.getY());
        yuva.savunucuYerlestir(yeni);
        savunucular.add(yeni);
        seker -= tur.getMaliyet();
        gunlugeYaz("Kullanıcı, " + konum + " konumuna (Oyuncak Yuvası #" + yuva.getNumara() + ") '"
                + yeni.getTamAd() + "' kurdu. Maliyet: " + tur.getMaliyet() + ". Kalan Şeker: " + seker + ".");
        efektEkle(GorselEfekt.halka(yuva.getX(), yuva.getY(), 30, new Color(255, 230, 140)));
        efektEkle(GorselEfekt.yazi(yuva.getX(), yuva.getY() - 26, "-" + tur.getMaliyet() + " şeker",
                new Color(255, 214, 110)));
        return InsaSonucu.BASARILI;
    }

    public boolean akinBaslatilabilirMi() {
        return (durum == Durum.HAZIRLIK || durum == Durum.AKIN_ARASI) && akinYoneticisi.sonrakiAkinVarMi();
    }

    public void akiniBaslat() {
        if (!akinBaslatilabilirMi()) {
            return;
        }
        akinYoneticisi.sonrakiAkiniBaslat();
        durum = Durum.AKIN_SURUYOR;
        int no = akinYoneticisi.getAktifAkinNumarasi();
        gunlugeYaz("Gece Yarısı Akını " + no + " Başladı! Dolap kapısı aralandı. (Dağılım: "
                + akinYoneticisi.akinOzeti(no - 1) + ")");
        duyuruYap("Gece yarısı akını " + no + " başladı");
    }

    public void setDuraklatildi(boolean deger) {
        if (oyunBittiMi() || duraklatildi == deger) {
            return;
        }
        duraklatildi = deger;
        gunlugeYaz(deger ? "Simülasyon duraklatıldı." : "Simülasyon devam ediyor.");
    }

    // ------------------------------------------------------------------
    // Ana döngü: arayüz her karede çağırır
    // ------------------------------------------------------------------

    public void guncelle(double dt) {
        if (duraklatildi) {
            return;
        }
        if (duyuruKalan > 0) {
            duyuruKalan -= dt;
        }
        for (GorselEfekt e : efektler) {
            e.guncelle(dt);
        }
        efektler.removeIf(GorselEfekt::bittiMi);
        if (oyunBittiMi()) {
            return;
        }
        simulasyonSaati += dt;

        // 1) Akın akışı
        if (durum == Durum.AKIN_ARASI) {
            araGeriSayim -= dt;
            if (araGeriSayim <= 0) {
                akiniBaslat();
            }
        }
        if (durum == Durum.AKIN_SURUYOR) {
            KabusYaratigi yeni = akinYoneticisi.guncelle(dt);
            if (yeni != null) {
                yeni.sahayaCik();
                sahadakiler.add(yeni);
                gunlugeYaz("Kabus '" + yeni.getTamAd() + "' dolaptan çıktı (Korku Enerjisi: "
                        + Bicim.sayi(yeni.getKorkuEnerjisi()) + "/" + Bicim.sayi(yeni.getMaksKorkuEnerjisi())
                        + ", Zırh: " + yeni.getZirhKaplamasi()
                        + ", Hız: " + Bicim.sayi(yeni.getTemelAdimHizi()) + " px/sn"
                        + (yeni.ucabilirMi() ? ", uçuyor" : ", yerde") + ").");
            }
        }

        // 2) Yaratıklar ilerler, 3) savunucular ateş eder, 4) mermiler uçar
        for (KabusYaratigi y : sahadakiler) {
            y.ilerle(dt);
        }
        for (OyuncakSavunucu s : savunucular) {
            s.guncelle(dt, this);
        }
        for (int i = 0; i < firlatmalar.size(); i++) {
            firlatmalar.get(i).guncelle(dt, this);
        }
        firlatmalar.removeIf(Firlatma::tamamlandiMi);

        // 5) Ölüm / yatağa ulaşma sonuçları, 6) kazanma-kaybetme denetimi
        sonuclariIsle();
        oyunSonunuDenetle();
    }

    private void sonuclariIsle() {
        Iterator<KabusYaratigi> it = sahadakiler.iterator();
        while (it.hasNext()) {
            KabusYaratigi y = it.next();
            if (y.getDurum() == KabusYaratigi.Durum.DAGILDI) {
                seker += y.getSekerOdulu();
                gunlugeYaz("'" + y.getTamAd() + "' dağıldı (yenildi). Ödül +" + y.getSekerOdulu()
                        + " şeker. Toplam Şeker: " + seker + ".");
                efektEkle(GorselEfekt.dagilma(y.getX(), y.getGorselY(), new Color(200, 190, 220)));
                efektEkle(GorselEfekt.yazi(y.getX(), y.getGorselY() - 30, "+" + y.getSekerOdulu() + " şeker",
                        new Color(255, 214, 90)));
                it.remove();
            } else if (y.getDurum() == KabusYaratigi.Durum.YATAGA_ULASTI) {
                int hasar = y.getUykuHasari();
                uykuDerinligi = Math.max(0, uykuDerinligi - hasar);
                gunlugeYaz("'" + y.getTamAd() + "' yatağa ulaştı! Uyku Derinliği (Can): "
                        + uykuDerinligi + " (-" + hasar + ").");
                efektEkle(GorselEfekt.yazi(y.getX() + 30, y.getY() - 50, "-" + hasar + " uyku",
                        new Color(200, 160, 255)));
                efektEkle(GorselEfekt.halka(y.getX() + 30, y.getY(), 40, new Color(170, 120, 255)));
                it.remove();
            }
        }
    }

    private void oyunSonunuDenetle() {
        if (uykuDerinligi <= 0) {
            durum = Durum.KAYBEDILDI;
            firlatmalar.clear();
            gunlugeYaz("SON: Çocuk kabuslarla uyandı! OYUN KAYBEDİLDİ — KAYBETTİNİZ. (Kalan Uyku: 0, Toplam Şeker: "
                    + seker + ")");
            dinleyici.oyunBitti(false);
            return;
        }
        if (durum == Durum.AKIN_SURUYOR && akinYoneticisi.aktifAkinTamamenCiktiMi() && sahadakiler.isEmpty()) {
            int no = akinYoneticisi.getAktifAkinNumarasi();
            gunlugeYaz("Gece Yarısı Akını " + no + " püskürtüldü. Uyku: " + uykuDerinligi + ", Şeker: " + seker + ".");
            if (akinYoneticisi.sonrakiAkinVarMi()) {
                durum = Durum.AKIN_ARASI;
                araGeriSayim = AKIN_ARASI_BEKLEME;
                gunlugeYaz("Sonraki akına " + (int) AKIN_ARASI_BEKLEME + " sn var; savunmayı güçlendirme zamanı.");
                duyuruYap("Akın püskürtüldü. Sonraki akın birazdan");
            } else {
                durum = Durum.KAZANILDI;
                gunlugeYaz("SON: Tüm akınlar püskürtüldü, haritada kabus kalmadı. OYUN KAZANILDI — KAZANDINIZ! "
                        + "(Kalan Uyku: " + uykuDerinligi + ", Toplam Şeker: " + seker + ")");
                dinleyici.oyunBitti(true);
            }
        }
    }

    private void duyuruYap(String metin) {
        duyuru = metin;
        duyuruKalan = DUYURU_SURESI;
    }

    // ------------------------------------------------------------------
    // SavasAlani uygulaması (savunucuların ve mermilerin kullandığı sözleşme)
    // ------------------------------------------------------------------

    @Override
    public List<KabusYaratigi> getSahadakiYaratiklar() {
        return Collections.unmodifiableList(sahadakiler);
    }

    @Override
    public void firlatmaEkle(Firlatma firlatma) {
        firlatmalar.add(firlatma);
    }

    @Override
    public void efektEkle(GorselEfekt efekt) {
        efektler.add(efekt);
    }

    @Override
    public void gunlugeYaz(String olay) {
        String satir = gunluk.yaz(simulasyonSaati, olay);
        dinleyici.gunlukSatiriEklendi(satir);
    }

    // ------------------------------------------------------------------
    // Arayüzün okuduğu durum bilgileri
    // ------------------------------------------------------------------

    public Harita getHarita() {
        return harita;
    }

    public int getUykuDerinligi() {
        return uykuDerinligi;
    }

    public int getSeker() {
        return seker;
    }

    public Durum getDurum() {
        return durum;
    }

    public boolean oyunBittiMi() {
        return durum == Durum.KAZANILDI || durum == Durum.KAYBEDILDI;
    }

    public boolean isDuraklatildi() {
        return duraklatildi;
    }

    public double getSimulasyonSaati() {
        return simulasyonSaati;
    }

    public double getAraGeriSayim() {
        return Math.max(0, araGeriSayim);
    }

    public int getAktifAkinNumarasi() {
        return akinYoneticisi.getAktifAkinNumarasi();
    }

    public int getToplamAkin() {
        return akinYoneticisi.getToplamAkin();
    }

    public int getDolaptaBekleyenSayisi() {
        return akinYoneticisi.getDolaptaBekleyenSayisi();
    }

    /** Ekranda gösterilecek kısa duyuru; süresi dolduysa null. */
    public String getDuyuru() {
        return duyuruKalan > 0 ? duyuru : null;
    }

    public double getDuyuruOrani() {
        return Math.max(0, Math.min(1, duyuruKalan / DUYURU_SURESI));
    }

    public List<OyuncakSavunucu> getSavunucular() {
        return Collections.unmodifiableList(savunucular);
    }

    public List<Firlatma> getFirlatmalar() {
        return Collections.unmodifiableList(firlatmalar);
    }

    public List<GorselEfekt> getEfektler() {
        return Collections.unmodifiableList(efektler);
    }
}
