package uykukalesi.arayuz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Oyuncak bloğu görünümlü düğme. Pasifken (setEnabled(false)) gri ve soluk çizilir;
 * böylece "para yetmiyorsa buton pasif" kuralı ekranda net görünür.
 */
@SuppressWarnings("serial") // Swing bileşenleri serileştirilmiyor
class OyunButonu extends JButton {

    private final Color anaRenk;
    private boolean secili;

    OyunButonu(String metin, Color anaRenk) {
        super(metin);
        this.anaRenk = anaRenk;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setOpaque(false);
        setFocusable(false); // Boşluk tuşu duraklatma kısayoluna kalsın
        setForeground(Color.WHITE);
        setFont(Tema.DUGME);
        setHorizontalAlignment(SwingConstants.CENTER);
        setIconTextGap(10);
        setMargin(new java.awt.Insets(2, 6, 2, 6)); // varsayılan iç boşluk metni kesmesin
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    void setSecili(boolean deger) {
        if (secili != deger) {
            secili = deger;
            repaint();
        }
    }

    @Override
    public void setEnabled(boolean deger) {
        if (deger != isEnabled()) {
            super.setEnabled(deger);
            setForeground(deger ? Color.WHITE : new Color(150, 150, 165));
            setCursor(Cursor.getPredefinedCursor(deger ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.yumusat(g);
        int w = getWidth();
        int h = getHeight();
        Color renk;
        if (!isEnabled()) {
            renk = new Color(62, 62, 80);
        } else if (getModel().isPressed()) {
            renk = anaRenk.darker();
        } else if (getModel().isRollover()) {
            renk = anaRenk.brighter();
        } else {
            renk = anaRenk;
        }
        // Alt gölge (oyuncak bloğu kalınlığı)
        g.setColor(isEnabled() ? renk.darker().darker() : new Color(40, 40, 52));
        g.fill(new RoundRectangle2D.Double(1, 4, w - 2, h - 5, 14, 14));
        g.setColor(renk);
        g.fill(new RoundRectangle2D.Double(1, 1, w - 2, h - 6, 14, 14));
        if (secili) {
            g.setColor(Tema.SEKER);
            g.setStroke(new BasicStroke(3f));
            g.draw(new RoundRectangle2D.Double(2.5, 2.5, w - 5, h - 8, 12, 12));
        }
        g.dispose();
        super.paintComponent(g0);
    }
}
