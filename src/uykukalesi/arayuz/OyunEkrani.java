package uykukalesi.arayuz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.DefaultCaret;

import uykukalesi.kayit.SavunmaGunlugu;
import uykukalesi.motor.Harita;
import uykukalesi.motor.InsaSonucu;
import uykukalesi.motor.OyunOlayDinleyicisi;
import uykukalesi.motor.OyuncakYuvasi;
import uykukalesi.motor.SavunucuTuru;
import uykukalesi.motor.SimulasyonMotoru;

/**
 * Oyun ekranı: üstte bilgi çubuğu, ortada canlı harita, sağda savunma günlüğü,
 * altta savunucu kurma düğmeleri. Swing Timer ile yaklaşık 60 FPS oyun döngüsü çalıştırır.
 *
 * Bu sınıf OyunOlayDinleyicisi'ni uygular: motor, günlük satırlarını ve oyun sonunu
 * bu arayüz üzerinden bildirir.
 */
@SuppressWarnings("serial") // Swing bileşenleri serileştirilmiyor
final class OyunEkrani extends JPanel implements OyunOlayDinleyicisi {

    private static final int KARE_MS = 16;

    private final AnaPencere pencere;
    private final SavunmaGunlugu gunluk;
    private final SimulasyonMotoru motor;
    private final HaritaPaneli haritaPaneli;
    private final JTextArea gunlukAlani = new JTextArea();
    private final Timer oyunSaati;

    private final JLabel uykuEtiketi = bilgiEtiketi(Tema.UYKU);
    private final JProgressBar uykuCubugu = new JProgressBar(0, SimulasyonMotoru.BASLANGIC_UYKU);
    private final JLabel sekerEtiketi = bilgiEtiketi(Tema.SEKER);
    private final JLabel akinEtiketi = bilgiEtiketi(Tema.AY_ISIGI);
    private final JLabel ipucuEtiketi = new JLabel();
    private final OyunButonu akinButonu = new OyunButonu("1. akını başlat", new Color(196, 92, 160));
    private final OyunButonu duraklatButonu = new OyunButonu("Duraklat", Tema.MOR_DUGME);
    private final OyunButonu hizButonu = new OyunButonu("Hız ×1", Tema.MOR_DUGME);
    private final OyunButonu menuButonu = new OyunButonu("Ana menü", new Color(84, 84, 120));
    private final Map<SavunucuTuru, OyunButonu> savunucuButonlari = new EnumMap<>(SavunucuTuru.class);

    private SavunucuTuru seciliTur;
    private int hizCarpani = 1;
    private long sonZaman;
    private boolean sonucGosterildi;
    private long geciciIpucuBitis;

    OyunEkrani(AnaPencere pencere) {
        super(new BorderLayout());
        this.pencere = pencere;
        setBackground(Tema.GECE);

        gunluk = SavunmaGunlugu.ac();
        motor = new SimulasyonMotoru(Harita.geceYarisiKoridoru(), gunluk, this, System.nanoTime());
        haritaPaneli = new HaritaPaneli(motor, () -> seciliTur, this::yuvaTiklandi, this::secimiIptalEt);

        add(ustCubukOlustur(), BorderLayout.NORTH);
        add(haritaPaneli, BorderLayout.CENTER);
        add(gunlukPaneliOlustur(), BorderLayout.EAST);
        add(altCubukOlustur(), BorderLayout.SOUTH);
        klavyeKisayollari();

        if (!gunluk.dosyayaYaziyorMu()) {
            gunlukSatiriEklendi("UYARI: " + SavunmaGunlugu.DOSYA_ADI + " açılamadı (" + gunluk.getHataMesaji()
                    + "). Kayıtlar yalnızca bu panelde görünecek.");
        } else {
            gunlukSatiriEklendi("Günlük dosyası: " + gunluk.getDosyaYolu());
        }
        motor.baslat();
        paneliGuncelle();

        oyunSaati = new Timer(KARE_MS, e -> kare());
    }

    // ==================================================================
    // Oyun döngüsü
    // ==================================================================

    void baslat() {
        sonZaman = System.nanoTime();
        oyunSaati.start();
        haritaPaneli.requestFocusInWindow();
    }

    void kapat() {
        oyunSaati.stop();
        gunluk.close();
    }

    private void kare() {
        long simdi = System.nanoTime();
        double dt = Math.min(0.05, (simdi - sonZaman) / 1e9);  // takılmalarda büyük sıçramayı önle
        sonZaman = simdi;
        for (int i = 0; i < hizCarpani; i++) {
            motor.guncelle(dt);
        }
        paneliGuncelle();
        haritaPaneli.repaint();
    }

    // ==================================================================
    // Kullanıcı etkileşimi
    // ==================================================================

    private void savunucuSec(SavunucuTuru tur) {
        if (!motor.satinAlinabilirMi(tur)) {
            return;
        }
        seciliTur = (seciliTur == tur) ? null : tur;
    }

    private void secimiIptalEt() {
        seciliTur = null;
    }

    private void yuvaTiklandi(OyuncakYuvasi yuva) {
        if (seciliTur == null) {
            if (yuva.bosMu()) {
                geciciIpucu("Önce aşağıdan bir savunucu seç, sonra bu yuvaya tıkla.");
            }
            return;
        }
        InsaSonucu sonuc = motor.savunucuInsaEt(seciliTur, yuva);
        if (sonuc == InsaSonucu.BASARILI) {
            seciliTur = null;
        } else {
            geciciIpucu(sonuc.getMesaj());
        }
    }

    private void geciciIpucu(String metin) {
        ipucuEtiketi.setText(metin);
        geciciIpucuBitis = System.currentTimeMillis() + 2500;
    }

    private void duraklatDegistir() {
        motor.setDuraklatildi(!motor.isDuraklatildi());
    }

    private void hizDegistir() {
        hizCarpani = hizCarpani == 1 ? 2 : 1;
    }

    private void klavyeKisayollari() {
        kisayol("ESCAPE", "iptal", this::secimiIptalEt);
        kisayol("SPACE", "duraklat", this::duraklatDegistir);
        kisayol("1", "sapan", () -> savunucuSec(SavunucuTuru.LASTIK_SAPAN));
        kisayol("2", "bilye", () -> savunucuSec(SavunucuTuru.BILYE_MANCINIGI));
        kisayol("3", "su", () -> savunucuSec(SavunucuTuru.SU_TABANCASI));
    }

    private void kisayol(String tus, String ad, Runnable is) {
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tus), ad);
        getActionMap().put(ad, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                is.run();
            }
        });
    }

    // ==================================================================
    // OyunOlayDinleyicisi (motor → arayüz)
    // ==================================================================

    @Override
    public void gunlukSatiriEklendi(String satir) {
        gunlukAlani.append(satir + "\n");
    }

    @Override
    public void oyunBitti(boolean kazanildi) {
        if (sonucGosterildi) {
            return;
        }
        sonucGosterildi = true;
        seciliTur = null;
        Timer gecikme = new Timer(1400, e -> sonucDiyalogu(kazanildi));
        gecikme.setRepeats(false);
        gecikme.start();
    }

    private void sonucDiyalogu(boolean kazanildi) {
        String mesaj = (kazanildi ? "KAZANDINIZ! Tüm gece yarısı akınları püskürtüldü."
                : "KAYBETTİNİZ. Kabuslar çocuğu uyandırdı.")
                + "\n\nKalan uyku: " + motor.getUykuDerinligi() + "   Şeker: " + motor.getSeker()
                + "\nTüm olaylar " + SavunmaGunlugu.DOSYA_ADI + " dosyasına kaydedildi.";
        Object[] secenekler = {"Tekrar oyna", "Ana menü"};
        int secim = JOptionPane.showOptionDialog(this, mesaj, kazanildi ? "Kazandınız" : "Kaybettiniz",
                JOptionPane.DEFAULT_OPTION, kazanildi ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE,
                null, secenekler, secenekler[0]);
        if (secim == 0) {
            pencere.oyunuBaslat();
        } else if (secim == 1) {
            pencere.anaMenuyeDon();
        }
    }

    // ==================================================================
    // Bilgi çubuğu ve düğme durumlarının her karede güncellenmesi
    // ==================================================================

    private void paneliGuncelle() {
        metinAyarla(uykuEtiketi, "Uyku: " + motor.getUykuDerinligi() + "/" + SimulasyonMotoru.BASLANGIC_UYKU);
        if (uykuCubugu.getValue() != motor.getUykuDerinligi()) {
            uykuCubugu.setValue(motor.getUykuDerinligi());
        }
        metinAyarla(sekerEtiketi, "Şeker: " + motor.getSeker());
        metinAyarla(akinEtiketi, akinMetni());

        // Akın düğmesi
        akinButonu.setEnabled(motor.akinBaslatilabilirMi());
        if (motor.getDurum() == SimulasyonMotoru.Durum.HAZIRLIK) {
            metinAyarla(akinButonu, "1. akını başlat");
        } else if (motor.getDurum() == SimulasyonMotoru.Durum.AKIN_ARASI) {
            metinAyarla(akinButonu, (motor.getAktifAkinNumarasi() + 1) + ". akını şimdi başlat");
        } else {
            metinAyarla(akinButonu, motor.oyunBittiMi() ? "Oyun bitti" : "Akın sürüyor");
        }
        duraklatButonu.setEnabled(!motor.oyunBittiMi());
        metinAyarla(duraklatButonu, motor.isDuraklatildi() ? "Devam et" : "Duraklat");
        metinAyarla(hizButonu, "Hız ×" + hizCarpani);

        // Para yetmiyorsa savunucu düğmesi pasif
        if (seciliTur != null && !motor.satinAlinabilirMi(seciliTur)) {
            seciliTur = null;
        }
        for (Map.Entry<SavunucuTuru, OyunButonu> e : savunucuButonlari.entrySet()) {
            e.getValue().setEnabled(motor.satinAlinabilirMi(e.getKey()));
            e.getValue().setSecili(e.getKey() == seciliTur);
        }

        if (System.currentTimeMillis() < geciciIpucuBitis) {
            return;
        }
        if (motor.oyunBittiMi()) {
            metinAyarla(ipucuEtiketi, "Oyun bitti. Tüm olaylar " + SavunmaGunlugu.DOSYA_ADI + " dosyasına yazıldı.");
        } else if (seciliTur != null) {
            metinAyarla(ipucuEtiketi, seciliTur.getGorunenAd() + " seçili: haritada boş bir oyuncak yuvasına tıkla. "
                    + "Vazgeçmek için sağ tık ya da Esc.");
        } else {
            metinAyarla(ipucuEtiketi, "Bir savunucu seç (1, 2, 3), sonra haritadaki boş bir yuvaya tıkla. "
                    + "Boşluk: duraklat.");
        }
    }

    private String akinMetni() {
        int no = motor.getAktifAkinNumarasi();
        int toplam = motor.getToplamAkin();
        switch (motor.getDurum()) {
            case HAZIRLIK:
                return "Akın: 0/" + toplam + " (hazırlık)";
            case AKIN_SURUYOR:
                return "Akın: " + no + "/" + toplam + "   Sahada " + motor.getSahadakiYaratiklar().size()
                        + ", dolapta " + motor.getDolaptaBekleyenSayisi();
            case AKIN_ARASI:
                return "Akın: " + no + "/" + toplam + " püskürtüldü   Sonraki " + (int) Math.ceil(motor.getAraGeriSayim())
                        + " sn";
            case KAZANILDI:
                return "Akın: " + no + "/" + toplam + "   KAZANDINIZ";
            default:
                return "Akın: " + no + "/" + toplam + "   KAYBETTİNİZ";
        }
    }

    private static void metinAyarla(JLabel etiket, String metin) {
        if (!metin.equals(etiket.getText())) {
            etiket.setText(metin);
        }
    }

    private static void metinAyarla(OyunButonu buton, String metin) {
        if (!metin.equals(buton.getText())) {
            buton.setText(metin);
        }
    }

    // ==================================================================
    // Bileşenlerin kurulumu
    // ==================================================================

    private JPanel ustCubukOlustur() {
        JPanel ust = new JPanel(new BorderLayout());
        ust.setBackground(Tema.GECE_ACIK);
        ust.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Tema.CIZGI),
                BorderFactory.createEmptyBorder(8, 14, 8, 10)));

        JPanel sol = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        sol.setOpaque(false);
        uykuCubugu.setValue(SimulasyonMotoru.BASLANGIC_UYKU);
        uykuCubugu.setPreferredSize(new Dimension(130, 14));
        uykuCubugu.setForeground(Tema.UYKU);
        uykuCubugu.setBackground(new Color(50, 46, 90));
        uykuCubugu.setBorderPainted(false);
        uykuCubugu.setToolTipText("Uyku derinliği = oyuncunun canı");
        sol.add(uykuEtiketi);
        sol.add(uykuCubugu);
        sol.add(sekerEtiketi);
        sol.add(akinEtiketi);
        ust.add(sol, BorderLayout.WEST);

        JPanel sag = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        sag.setOpaque(false);
        boyutla(akinButonu, 200, 38);
        boyutla(duraklatButonu, 110, 38);
        boyutla(hizButonu, 90, 38);
        boyutla(menuButonu, 120, 38);
        akinButonu.addActionListener(e -> motor.akiniBaslat());
        duraklatButonu.addActionListener(e -> duraklatDegistir());
        hizButonu.addActionListener(e -> hizDegistir());
        menuButonu.addActionListener(e -> pencere.anaMenuyeDon());
        sag.add(akinButonu);
        sag.add(duraklatButonu);
        sag.add(hizButonu);
        sag.add(menuButonu);
        ust.add(sag, BorderLayout.EAST);
        return ust;
    }

    private JPanel gunlukPaneliOlustur() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Tema.GECE_ACIK);
        panel.setPreferredSize(new Dimension(380, 100));
        panel.setBorder(BorderFactory.createMatteBorder(0, 2, 0, 0, Tema.CIZGI));

        JLabel baslik = new JLabel("Savunma günlüğü");
        baslik.setFont(Tema.ALT_BASLIK.deriveFont(java.awt.Font.BOLD | java.awt.Font.ITALIC, 19f));
        baslik.setForeground(Tema.AY_ISIGI);
        baslik.setBorder(BorderFactory.createEmptyBorder(10, 12, 8, 12));
        panel.add(baslik, BorderLayout.NORTH);

        gunlukAlani.setEditable(false);
        gunlukAlani.setLineWrap(true);
        gunlukAlani.setWrapStyleWord(true);
        gunlukAlani.setFont(Tema.GUNLUK);
        gunlukAlani.setBackground(new Color(18, 18, 38));
        gunlukAlani.setForeground(new Color(214, 212, 240));
        gunlukAlani.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 8));
        ((DefaultCaret) gunlukAlani.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane kaydirma = new JScrollPane(gunlukAlani);
        kaydirma.setBorder(BorderFactory.createEmptyBorder());
        kaydirma.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(kaydirma, BorderLayout.CENTER);
        return panel;
    }

    private JPanel altCubukOlustur() {
        JPanel alt = new JPanel(new BorderLayout(0, 6));
        alt.setBackground(Tema.GECE_ACIK);
        alt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Tema.CIZGI),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)));

        JPanel dugmeler = new JPanel(new GridLayout(1, 3, 12, 0));
        dugmeler.setOpaque(false);
        Color[] renkler = {Tema.OYUNCAK_KIRMIZI, Tema.OYUNCAK_MAVI, Tema.OYUNCAK_YESIL};
        int i = 0;
        for (SavunucuTuru tur : SavunucuTuru.values()) {
            OyunButonu b = new OyunButonu(tur.getGorunenAd() + "  [" + tur.getMaliyet() + " şeker]", renkler[i++]);
            b.setIcon(new VarlikIkonu(tur.uret("ikon", 0, 0), 36));
            b.setToolTipText(tur.getAciklama());
            b.setPreferredSize(new Dimension(260, 50));
            b.addActionListener(e -> savunucuSec(tur));
            savunucuButonlari.put(tur, b);
            dugmeler.add(b);
        }
        alt.add(dugmeler, BorderLayout.CENTER);

        ipucuEtiketi.setFont(Tema.KUCUK);
        ipucuEtiketi.setForeground(Tema.SOLUK_YAZI);
        alt.add(ipucuEtiketi, BorderLayout.SOUTH);
        return alt;
    }

    private static JLabel bilgiEtiketi(Color renk) {
        JLabel l = new JLabel();
        l.setFont(Tema.BILGI);
        l.setForeground(renk);
        return l;
    }

    private static void boyutla(JComponent c, int w, int h) {
        c.setPreferredSize(new Dimension(w, h));
    }
}
