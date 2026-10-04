# Uyku Kalesi — Gece Yarısı Kabuslarına Karşı Oyuncak Savunması

Kocaeli Üniversitesi Bilgisayar Mühendisliği — Programlama Laboratuvarı-I, Proje II (Kule Savunma).

📄 **Proje raporu (IEEE):** [docs/UykuKalesi_Rapor.pdf](docs/UykuKalesi_Rapor.pdf)

Dil: **Java 11+**, arayüz: **Swing** (ek kütüphane yok).

## Konsept

Gece yarısı, bir çocuğun yatak odası. Dolabın kapısı aralanıyor ve kabuslar halının üzerinden
yatağa doğru yürüyor. Çocuğun oyuncakları onu korumak için nöbette. Kabuslar yatağa ulaşırsa
çocuğun **uykusu** azalıyor; uyku sıfıra inerse çocuk uyanır ve oyun kaybedilir.
Senaryo adı: **Gece Yarısı Koridoru**. Yol, dolaptan (başlangıç) yatağa (üs) kesintisiz bir halıdır.

| Dokümandaki kavram | Bu projedeki adı | Sınıf / değişken |
|---|---|---|
| Düşman (soyut) | Kabus yaratığı | `KabusYaratigi` |
| Standart Düşman | Toz Canavarı | `TozCanavari` |
| Zırhlı Düşman | Teneke Robot | `TenekeRobot` |
| Uçan Düşman | Gece Kelebeği | `GeceKelebegi` |
| Kule (soyut) | Oyuncak savunucu | `OyuncakSavunucu` |
| Okçu Kulesi | Lastik Sapan | `LastikSapan` |
| Topçu Kulesi | Bilye Mancınığı | `BilyeMancinigi` |
| Buz Kulesi | Su Tabancası (ıslatır → yavaşlatır, mavi görünür) | `SuTabancasi` |
| Can (düşman) | Korku enerjisi | `korkuEnerjisi` |
| Hız | Adım hızı | `temelAdimHizi`, `getAnlikAdimHizi()` |
| Zırh | Zırh kaplaması | `zirhKaplamasi` |
| Oyuncu canı | Uyku derinliği | `uykuDerinligi` |
| Para | Şeker | `seker`, `sekerMaliyeti`, `getSekerOdulu()` |
| Dalga | Gece Yarısı Akını | `AkinYoneticisi`, `akiniBaslat()` |
| Kule inşa alanı | Oyuncak yuvası | `OyuncakYuvasi` |
| Yavaşlatma | Islatma | `islat()`, `islakMi()` |
| Ölmek | Dağılmak | `Durum.DAGILDI` |
| Üsse ulaşmak | Yatağa ulaşmak | `Durum.YATAGA_ULASTI` |

## Derleme ve çalıştırma

Proje klasöründe (README'nin olduğu yerde):

```bash
# Derle (Türkçe karakterler için -encoding UTF-8 şart)
javac -encoding UTF-8 -d out $(find src -name "*.java")

# Çalıştır
java -cp out uykukalesi.UykuKalesi
```

Windows (PowerShell):

```powershell
javac -encoding UTF-8 -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out uykukalesi.UykuKalesi
```

**IntelliJ IDEA:** File → Open → `UykuKalesi` klasörü. `src` klasörüne sağ tık → *Mark Directory as → Sources Root*.
Settings → Editor → File Encodings → hepsini **UTF-8** yap. `uykukalesi.UykuKalesi` sınıfındaki `main` ile çalıştır.
**NetBeans / Eclipse:** Yeni Java projesi oluşturup `src/uykukalesi` klasörünü kaynak klasörüne kopyalayın; proje kodlamasını UTF-8 yapın.

`savunma_gunlugu.txt` dosyası programın **çalıştırıldığı klasöre** (çalışma dizini) yazılır. Tam yolu
oyun ekranındaki günlük panelinin ilk satırında görünür. Her yeni oyun dosyayı baştan yazar.

## Nasıl oynanır

1. Ana menüde **Oyunu Başlat**.
2. Alttan bir savunucu düğmesine tıkla (ya da 1/2/3 tuşu). Şeker yetmiyorsa düğme gridir ve tıklanmaz.
3. Haritada kesikli çerçeveli boş bir **oyuncak yuvasına** tıkla. Seçim varken yuvalar yeşil/kırmızı
   görünür ve menzil önizlemesi çıkar. Sağ tık ya da Esc seçimi iptal eder.
4. Üstteki **1. akını başlat** düğmesiyle ilk akını başlat. Akın bitince 10 sn sonra ikinci akın
   kendiliğinden gelir (istersen hemen başlatabilirsin).
5. **Boşluk** duraklatır, **Hız ×1/×2** düğmesi simülasyonu hızlandırır. Fare ile bir kabusun ya da
   savunucunun üzerine gelince bilgi kutusu çıkar.

## İsterlerin koddaki karşılığı

| İster (PDF) | Nerede |
|---|---|
| Düşman özellikleri (can/hız/zırh/ödül/üs hasarı) | `TozCanavari`, `TenekeRobot`, `GeceKelebegi` sabitleri |
| Zırh 50–100 arası tam sayı | `AkinYoneticisi.rastgeleZirh()` |
| Akın 1: 2 standart + 1 zırhlı + 1 uçan (≤5); Akın 2: 5–10, her türden ≥1 | `AkinYoneticisi` kurucusu + `kurallariDogrula()` (kural bozulursa program hata verir) |
| Tüm düşmanlar oyun başında oluşturulmuş | `AkinYoneticisi` kurucusu; günlükte "Dolapta hazırlanan…" satırları |
| Başlangıç can 100, para 200 | `SimulasyonMotoru.BASLANGIC_UYKU`, `BASLANGIC_SEKER` |
| Yol takibi, son noktada silinme, −10 / −5 | `KabusYaratigi.ilerle()`, `SimulasyonMotoru.guncelle()` |
| Hasar formülü | `OyuncakSavunucu.zirhFormuluUygula()` |
| Okçu: 10 hasar, 1 sn, zırhlıya %50 az, tek hedef, üsse en yakın | `LastikSapan.tabanHasarHesapla()`, `OyuncakSavunucu.hedefSec()` |
| Topçu: 20 hasar, 3 sn, uçanı hedeflemez, 50 px alan hasarı (uçanlar hariç) | `BilyeMancinigi.hedefAlabilirMi()`, `isabetEt()` |
| Buz: 15 hasar, 2 sn, 3 sn %50 yavaşlatma | `SuTabancasi.isabetEt()`, `KabusYaratigi.islat()` |
| Ölümde ödül, haritadan kaldırma | `SimulasyonMotoru.guncelle()` (DAGILDI durumu) |
| KAZANDINIZ / KAYBETTİNİZ | `SimulasyonMotoru` → `oyunBitti()` bildirimi, haritada büyük yazı + diyalog |
| Ana menü (Oyunu Başlat / Çıkış) | `AnaMenuPaneli` |
| Can, para, dalga her zaman görünür | `OyunEkrani` üst çubuk |
| "Lastik Sapan [50 şeker]" düğmeleri, para yetmezse pasif | `OyunEkrani.paneliGuncelle()` |
| Sağlık barları, ıslak = mavi, isabet geri bildirimi | `KabusYaratigi.ciz()` (bar, mavi ton, beyaz parlama), uçan hasar yazıları |
| Zaman damgalı günlük dosyası | `SavunmaGunlugu` |

## Hasar hesabı (önemli — sunumda sorulabilir)

Tüm savunucular PDF Bölüm 4.3'teki formülü kullanır:

```
Net_Hasar = Taban * (1 - (Zırh / (Zırh + 100.0)))
```

Lastik Sapan (Okçu) zırhlı bir hedefe vururken **önce** taban hasarına %50 ceza uygular, **sonra**
formülü uygular. Bu, PDF'deki örnek günlük satırıyla aynı sıradır
("Taban → Zırhlı cezası %50 → …; Zırh formülü ile Net Hasar …"). Örnek: zırh 94 olan Teneke Robot'a
Lastik Sapan: 10 → 5 → 5 × (1 − 94/194) = 2.58.

> PDF'de bu kural iki şekilde okunabiliyor (yalnızca %50 ceza mı, yoksa ceza + formül mü?).
> Kesin cevap için edestek forumunda araştırma görevlilerine sormanız iyi olur. Değiştirmek
> gerekirse tek yer `LastikSapan.tabanHasarHesapla()` metodudur.

Düşmanın canı **0 veya altına** indiğinde dağılır (PDF "0'ın altına düştüğünde" diyor; tam 0'da
yaşayan bir düşman anlamsız olacağı için ≤ 0 seçildi).

## NYP ilkeleri

**Kalıtım:** `OyunVarligi` → `KabusYaratigi` → `TozCanavari`/`TenekeRobot`/`GeceKelebegi`;
`OyunVarligi` → `OyuncakSavunucu` → `LastikSapan`/`BilyeMancinigi`/`SuTabancasi`. Ortak alanlar
(kimlik, konum, can, hız, menzil, atış aralığı…) üst sınıfta bir kez yazılır.

**Soyutlama:** `OyunVarligi`, `KabusYaratigi`, `OyuncakSavunucu` soyut sınıflardır; doğrudan
nesnesi oluşturulamaz. `getSekerOdulu()`, `getUykuHasari()`, `ucabilirMi()`, `isabetEt()` gibi
metotlar soyuttur ve her alt sınıf kendi davranışını yazmak zorundadır. `Cizilebilir`, `SavasAlani`,
`OyunOlayDinleyicisi` arayüzleri de soyutlama örneğidir.

**Polimorfizm:** Motor bir `List<OyuncakSavunucu>` üzerinde dolaşıp `guncelle()` çağırır; hangi
savunucu olduğunu bilmez. Bilye Mancınığı `hedefAlabilirMi()` metodunu ezerek uçanları eler,
Lastik Sapan `tabanHasarHesapla()` metodunu ezerek zırhlı cezasını uygular, her savunucunun
`isabetEt()` metodu farklıdır (tek hedef / alan hasarı / ıslatma). Harita paneli her varlık için yalnızca
`ciz()` çağırır. `SavunucuTuru` enum'ındaki `uret()` metodu da her sabitte farklı sınıf üretir (fabrika).

**Kapsülleme:** Tüm alanlar `private`. Can yalnızca `hasarAl()` ile azalır (negatife düşmez,
dağılınca durum değişir), hız yalnızca `islat()` ile düşer, şeker yalnızca motorun içinden değişir.
Arayüz motorun alanlarına dokunamaz; yalnızca `getUykuDerinligi()` gibi okuyucuları ve
`savunucuInsaEt()` gibi komutları kullanır.

**Şablon metot:** `OyuncakSavunucu.guncelle()` `final`dır ve sırayı sabitler (doldurma süresi →
hedef seç → fırlat); alt sınıflar yalnızca adımları özelleştirir. `KabusYaratigi.ciz()` de aynı şekilde
sağlık barı / ıslaklık / parlama kısmını ortak çizer, gövdeyi `govdeyiCiz()` ile alt sınıfa bırakır.

## Arayüz ↔ simülasyon motoru haberleşmesi (rapor "Yöntem" bölümü için)

Motor paketi (`uykukalesi.motor`) **hiç Swing sınıfı kullanmaz**; konsolda/testte arayüz olmadan
çalışabilir. Haberleşme üç yoldan olur:

1. **Arayüz → Motor (komut):** Düğme ve fare olayları motorun metotlarını çağırır:
   `savunucuInsaEt(tur, yuva)` (sonucu `InsaSonucu` enum'ı olarak döner), `akiniBaslat()`,
   `setDuraklatildi()`.
2. **Oyun döngüsü (çekme):** `OyunEkrani` içindeki `javax.swing.Timer` her 16 ms'de bir
   (≈60 FPS) geçen süreyi ölçer, `motor.guncelle(dt)` çağırır (dt en fazla 0.05 sn, hız ×2 ise iki kez),
   sonra üst çubuğu günceller ve `HaritaPaneli.repaint()` ile haritayı yeniden çizdirir. Harita paneli
   motordan listeleri okuyup her varlığın `ciz()` metodunu çağırır. Hepsi Swing olay iş parçacığında
   çalıştığı için ek senkronizasyon gerekmez.
3. **Motor → Arayüz (bildirim, Observer):** Motor yalnızca `OyunOlayDinleyicisi` arayüzünü bilir.
   Her günlük satırında `gunlukSatiriEklendi()`, oyun bitince `oyunBitti(kazanildi)` çağrılır.
   `OyunEkrani` bu arayüzü uygular, satırı sağdaki günlük paneline ekler ve sonuç diyaloğunu açar.

Varlıklar da motoru doğrudan tanımaz; `SavasAlani` arayüzü üzerinden konuşur
(`getSahadakiYaratiklar()`, `firlatmaEkle()`, `gunlugeYaz()`).

## Paketler

```
src/uykukalesi/
├── UykuKalesi.java          main
├── varlik/                  oyun varlıkları (düşmanlar, savunucular, mermi, efekt, arayüzler)
├── motor/                   simülasyon motoru, harita, yol, yuvalar, akın yöneticisi
├── kayit/                   savunma_gunlugu.txt yazımı, sayı biçimi
└── arayuz/                  Swing ekranları (ana menü, oyun ekranı, harita paneli, düğmeler)
docs/
├── sinif_diyagrami.puml / UykuKalesi_Sinif_Diyagrami.png   tam sınıf diyagramı
├── kalitim_diyagrami.puml / UykuKalesi_Kalitim.png         rapor için sade kalıtım diyagramı
├── ekran_goruntuleri/       örnek ekran görüntüleri
└── ornek_gunlukler/         hızlandırılmış testten alınmış örnek günlükler
```

`.puml` dosyalarını https://www.plantuml.com/plantuml adresine yapıştırarak ya da IntelliJ'nin
*PlantUML Integration* eklentisiyle yeniden çizebilirsiniz.

`ornek_gunlukler` içindeki dosyalar arayüzsüz, hızlandırılmış bir testten alındığı için gerçek saat
sütunu sıkışıktır (simülasyon zamanı `T+…` doğrudur). Rapora koyacağınız günlüğü oyunu kendiniz
oynayarak üretin.

## Akınlar

- **Akın 1 (4 kabus):** Toz Canavarı ID101, Gece Kelebeği ID102, Teneke Robot ID103, Toz Canavarı ID104
- **Akın 2 (10 kabus):** 3 Toz Canavarı, 5 Teneke Robot, 2 Gece Kelebeği (ID201–ID210)

Kabuslar dolaptan 1.8 sn arayla çıkar. Hiç savunucu kurulmazsa toplam uyku kaybı tam 100 olur;
yani savunmasız oyun **KAYBETTİNİZ** ile biter, iyi savunma ile **KAZANDINIZ** görülür.
Sunumda iki sonucu da göstermek için bunu kullanabilirsiniz.
