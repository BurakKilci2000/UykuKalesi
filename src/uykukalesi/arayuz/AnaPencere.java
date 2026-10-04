package uykukalesi.arayuz;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

/** Uygulamanın tek penceresi: ana menü ile oyun ekranı arasında geçiş yapar. */
@SuppressWarnings("serial") // Swing bileşenleri serileştirilmiyor
public final class AnaPencere extends JFrame {

    private static final String MENU = "menu";
    private static final String OYUN = "oyun";

    private final CardLayout kartlar = new CardLayout();
    private final JPanel kok = new JPanel(kartlar);
    private OyunEkrani oyunEkrani;

    public AnaPencere() {
        super("Uyku Kalesi — Gece Yarısı Kabuslarına Karşı Oyuncak Savunması");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cikisYap();
            }
        });
        kok.add(new AnaMenuPaneli(this::oyunuBaslat, this::cikisYap), MENU);
        setContentPane(kok);
        setMinimumSize(new Dimension(1100, 680));
        setSize(1320, 800);
        setLocationRelativeTo(null);
    }

    /** Yeni bir oyun başlatır (varsa eskisini kapatır). */
    void oyunuBaslat() {
        oyunEkraniniKapat();
        oyunEkrani = new OyunEkrani(this);
        kok.add(oyunEkrani, OYUN);
        kartlar.show(kok, OYUN);
        kok.revalidate();
        oyunEkrani.baslat();
    }

    void anaMenuyeDon() {
        oyunEkraniniKapat();
        kartlar.show(kok, MENU);
    }

    private void oyunEkraniniKapat() {
        if (oyunEkrani != null) {
            oyunEkrani.kapat();
            kok.remove(oyunEkrani);
            oyunEkrani = null;
        }
    }

    void cikisYap() {
        oyunEkraniniKapat();
        dispose();
        System.exit(0);
    }
}
