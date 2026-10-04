package uykukalesi.arayuz;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import uykukalesi.varlik.OyunVarligi;

/**
 * Herhangi bir oyun varlığını küçük bir düğme ikonu olarak çizer.
 * Varlığın kendi onizlemeCiz() metodunu kullanır; tür ne olursa olsun çalışır (polimorfizm).
 */
final class VarlikIkonu implements Icon {

    private final OyunVarligi ornek;
    private final int boyut;

    VarlikIkonu(OyunVarligi ornek, int boyut) {
        this.ornek = ornek;
        this.boyut = boyut;
    }

    @Override
    public void paintIcon(Component c, Graphics g0, int x, int y) {
        Graphics2D g = (Graphics2D) g0.create();
        Tema.yumusat(g);
        g.translate(x, y);
        double olcek = boyut / 44.0;
        g.scale(olcek, olcek);
        ornek.onizlemeCiz(g, 22, 24);
        g.dispose();
    }

    @Override
    public int getIconWidth() {
        return boyut;
    }

    @Override
    public int getIconHeight() {
        return boyut;
    }
}
