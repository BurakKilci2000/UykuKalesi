package uykukalesi;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import uykukalesi.arayuz.AnaPencere;

/**
 * Uyku Kalesi — Kocaeli Üniversitesi Programlama Laboratuvarı-I, Proje II.
 * Programın giriş noktası.
 */
public final class UykuKalesi {

    private UykuKalesi() {
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("sun.java2d.uiScale.enabled", "true");
        try {
            // Platformdan bağımsız görünüm: düğme renkleri her işletim sisteminde aynı çıkar.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {
            // Varsayılan görünümle devam edilir.
        }
        SwingUtilities.invokeLater(() -> new AnaPencere().setVisible(true));
    }
}
