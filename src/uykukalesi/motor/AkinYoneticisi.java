package uykukalesi.motor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import uykukalesi.varlik.GeceKelebegi;
import uykukalesi.varlik.KabusYaratigi;
import uykukalesi.varlik.TenekeRobot;
import uykukalesi.varlik.TozCanavari;

/**
 * Gece yarısı akınlarını (dalgaları) yönetir.
 * TÜM yaratık nesneleri oyun başında, bu sınıfın yapıcısında oluşturulur;
 * akın başlayınca belirli aralıklarla dolaptan tek tek salınırlar.
 *
 * Akın 1: 2 Toz Canavarı + 1 Teneke Robot + 1 Gece Kelebeği = 4 (en fazla 5)
 * Akın 2: 3 Toz Canavarı + 5 Teneke Robot + 2 Gece Kelebeği = 10 (5–10 arası)
 *
 * Toplam olası uyku kaybı 25 + 75 = 100: hiç savunucu kurulmazsa oyun kaybedilir.
 */
public final class AkinYoneticisi {

    public static final double CIKIS_ARALIGI = 1.8; // iki yaratığın dolaptan çıkışı arası (sn)

    private final List<List<KabusYaratigi>> akinlar;
    private int aktifAkinIndeksi = -1;    // -1: henüz akın başlamadı
    private int siradakiIndeks;
    private double cikisSayaci;

    public AkinYoneticisi(Yol yol, Random rastgele) {
        List<KabusYaratigi> akin1 = Arrays.asList(
                new TozCanavari("ID101", yol),
                new GeceKelebegi("ID102", yol),
                new TenekeRobot("ID103", rastgeleZirh(rastgele), yol),
                new TozCanavari("ID104", yol));

        List<KabusYaratigi> akin2 = Arrays.asList(
                new TozCanavari("ID201", yol),
                new TenekeRobot("ID202", rastgeleZirh(rastgele), yol),
                new GeceKelebegi("ID203", yol),
                new TenekeRobot("ID204", rastgeleZirh(rastgele), yol),
                new TozCanavari("ID205", yol),
                new TenekeRobot("ID206", rastgeleZirh(rastgele), yol),
                new GeceKelebegi("ID207", yol),
                new TenekeRobot("ID208", rastgeleZirh(rastgele), yol),
                new TozCanavari("ID209", yol),
                new TenekeRobot("ID210", rastgeleZirh(rastgele), yol));

        List<List<KabusYaratigi>> liste = new ArrayList<>();
        liste.add(Collections.unmodifiableList(akin1));
        liste.add(Collections.unmodifiableList(akin2));
        this.akinlar = Collections.unmodifiableList(liste);
        kurallariDogrula();
    }

    private static int rastgeleZirh(Random r) {
        return TenekeRobot.MIN_ZIRH + r.nextInt(TenekeRobot.MAKS_ZIRH - TenekeRobot.MIN_ZIRH + 1);
    }

    /** Doküman Bölüm 3.1 ve 4.1'deki sayısal kuralları oyun başında denetler. */
    private void kurallariDogrula() {
        if (akinlar.size() != 2) {
            throw new IllegalStateException("Oyun tam 2 akından oluşmalı.");
        }
        List<KabusYaratigi> a1 = akinlar.get(0);
        if (say(a1, TozCanavari.class) != 2 || say(a1, TenekeRobot.class) != 1
                || say(a1, GeceKelebegi.class) != 1 || a1.size() > 5) {
            throw new IllegalStateException("1. akın: 2 standart, 1 zırhlı, 1 uçan (en fazla 5) olmalı.");
        }
        int n2 = akinlar.get(1).size();
        if (n2 < 5 || n2 > 10) {
            throw new IllegalStateException("2. akında 5–10 yaratık olmalı.");
        }
        for (List<KabusYaratigi> akin : akinlar) {
            if (say(akin, TozCanavari.class) < 1 || say(akin, TenekeRobot.class) < 1
                    || say(akin, GeceKelebegi.class) < 1) {
                throw new IllegalStateException("Her akında her türden en az 1 yaratık olmalı.");
            }
        }
    }

    private static int say(List<KabusYaratigi> akin, Class<? extends KabusYaratigi> tur) {
        int n = 0;
        for (KabusYaratigi y : akin) {
            if (tur.isInstance(y)) {
                n++;
            }
        }
        return n;
    }

    public boolean sonrakiAkinVarMi() {
        return aktifAkinIndeksi + 1 < akinlar.size();
    }

    public void sonrakiAkiniBaslat() {
        if (!sonrakiAkinVarMi()) {
            throw new IllegalStateException("Başlatılacak akın kalmadı.");
        }
        aktifAkinIndeksi++;
        siradakiIndeks = 0;
        cikisSayaci = 0; // ilk yaratık hemen çıkar
    }

    /** Sırası gelen yaratığı döndürür; çıkış zamanı gelmediyse null. */
    public KabusYaratigi guncelle(double dt) {
        if (aktifAkinIndeksi < 0 || aktifAkinTamamenCiktiMi()) {
            return null;
        }
        cikisSayaci -= dt;
        if (cikisSayaci <= 0) {
            cikisSayaci = CIKIS_ARALIGI;
            return akinlar.get(aktifAkinIndeksi).get(siradakiIndeks++);
        }
        return null;
    }

    public boolean aktifAkinTamamenCiktiMi() {
        return aktifAkinIndeksi >= 0 && siradakiIndeks >= akinlar.get(aktifAkinIndeksi).size();
    }

    /** 1'den başlayan akın numarası; henüz başlamadıysa 0. */
    public int getAktifAkinNumarasi() {
        return aktifAkinIndeksi + 1;
    }

    public int getToplamAkin() {
        return akinlar.size();
    }

    public int getDolaptaBekleyenSayisi() {
        if (aktifAkinIndeksi < 0) {
            return 0;
        }
        return akinlar.get(aktifAkinIndeksi).size() - siradakiIndeks;
    }

    public List<KabusYaratigi> getAkin(int indeks) {
        return akinlar.get(indeks);
    }

    /** Ör. "TozCanavari x2, GeceKelebegi x1, TenekeRobot x1; Toplam: 4" */
    public String akinOzeti(int indeks) {
        Map<String, Integer> sayac = new LinkedHashMap<>();
        for (KabusYaratigi y : akinlar.get(indeks)) {
            sayac.merge(y.getTurAdi(), 1, Integer::sum);
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Integer> e : sayac.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(e.getKey()).append(" x").append(e.getValue());
        }
        sb.append("; Toplam: ").append(akinlar.get(indeks).size());
        return sb.toString();
    }
}
