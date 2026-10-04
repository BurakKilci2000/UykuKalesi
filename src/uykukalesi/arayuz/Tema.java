package uykukalesi.arayuz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Arayüzün renk ve yazı tipi paleti: gece yarısı çocuk odası. */
final class Tema {

    private Tema() {
    }

    static final Color GECE = new Color(24, 25, 52);
    static final Color GECE_ACIK = new Color(36, 38, 74);
    static final Color CIZGI = new Color(78, 80, 140);
    static final Color AY_ISIGI = new Color(232, 228, 255);
    static final Color SOLUK_YAZI = new Color(170, 168, 210);
    static final Color SEKER = new Color(255, 200, 74);
    static final Color UYKU = new Color(164, 144, 255);
    static final Color OYUNCAK_KIRMIZI = new Color(214, 72, 64);
    static final Color OYUNCAK_MAVI = new Color(66, 120, 214);
    static final Color OYUNCAK_YESIL = new Color(70, 160, 90);
    static final Color MOR_DUGME = new Color(108, 86, 196);

    static final Font MASAL_BASLIK = new Font("Serif", Font.BOLD | Font.ITALIC, 64);
    static final Font ALT_BASLIK = new Font("Serif", Font.ITALIC, 20);
    static final Font DUGME = new Font("SansSerif", Font.BOLD, 15);
    static final Font BILGI = new Font("SansSerif", Font.BOLD, 15);
    static final Font NORMAL = new Font("SansSerif", Font.PLAIN, 13);
    static final Font KUCUK = new Font("SansSerif", Font.PLAIN, 12);
    static final Font GUNLUK = new Font("Monospaced", Font.PLAIN, 12);

    static void yumusat(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }
}
