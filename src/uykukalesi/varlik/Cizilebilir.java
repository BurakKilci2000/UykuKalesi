package uykukalesi.varlik;

import java.awt.Graphics2D;

/**
 * Ekrana kendini çizebilen her şeyin sözleşmesi.
 * Harita paneli varlığın türünü bilmeden yalnızca ciz() çağırır (polimorfizm).
 */
public interface Cizilebilir {
    void ciz(Graphics2D g);
}
