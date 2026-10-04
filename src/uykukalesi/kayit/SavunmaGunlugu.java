package uykukalesi.kayit;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * savunma_gunlugu.txt dosyasını yöneten sınıf.
 * Her satır iki zaman damgası taşır: gerçek saat ve simülasyon zamanı.
 * Her satırdan sonra flush yapılır; oyun aniden kapansa bile kayıt kaybolmaz.
 */
public final class SavunmaGunlugu implements AutoCloseable {

    public static final String DOSYA_ADI = "savunma_gunlugu.txt";

    private static final DateTimeFormatter SAAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter TARIH = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final PrintWriter yazici;      // dosya açılamazsa null olur
    private final Path dosyaYolu;
    private final String hataMesaji;

    private SavunmaGunlugu(PrintWriter yazici, Path dosyaYolu, String hataMesaji) {
        this.yazici = yazici;
        this.dosyaYolu = dosyaYolu;
        this.hataMesaji = hataMesaji;
    }

    /**
     * Çalışma klasöründe savunma_gunlugu.txt dosyasını (varsa üzerine yazarak) açar.
     * Dosya açılamazsa oyun yine çalışır; kayıtlar yalnızca ekrandaki günlükte görünür.
     */
    public static SavunmaGunlugu ac() {
        return ac(Paths.get(DOSYA_ADI));
    }

    public static SavunmaGunlugu ac(Path yol) {
        Path mutlak = yol.toAbsolutePath();
        try {
            PrintWriter w = new PrintWriter(Files.newBufferedWriter(mutlak, StandardCharsets.UTF_8));
            w.println("==== UYKU KALESİ — SAVUNMA GÜNLÜĞÜ ====");
            w.println("Kayıt tarihi: " + LocalDateTime.now().format(TARIH));
            w.println("Satır biçimi: [gerçek saat | simülasyon zamanı] olay");
            w.println();
            w.flush();
            return new SavunmaGunlugu(w, mutlak, null);
        } catch (IOException e) {
            return new SavunmaGunlugu(null, mutlak, e.getMessage());
        }
    }

    /**
     * Olayı zaman damgasıyla dosyaya yazar ve aynı satırı geri döndürür
     * (arayüz bu satırı ekrandaki günlük paneline ekler).
     */
    public synchronized String yaz(double simulasyonSaniyesi, String olay) {
        String satir = String.format(Locale.ROOT, "[%s | T+%06.2fs] %s",
                LocalTime.now().format(SAAT), simulasyonSaniyesi, olay);
        if (yazici != null) {
            yazici.println(satir);
            yazici.flush();
        }
        return satir;
    }

    public boolean dosyayaYaziyorMu() {
        return yazici != null;
    }

    public Path getDosyaYolu() {
        return dosyaYolu;
    }

    public String getHataMesaji() {
        return hataMesaji;
    }

    @Override
    public synchronized void close() {
        if (yazici != null) {
            yazici.flush();
            yazici.close();
        }
    }
}
