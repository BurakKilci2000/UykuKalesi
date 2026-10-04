package uykukalesi.motor;

import uykukalesi.varlik.OyuncakSavunucu;

/**
 * Haritadaki "Kule İnşa Alanı": üzerine tek bir oyuncak savunucu yerleştirilebilen yuva.
 */
public final class OyuncakYuvasi {

    public static final double YARI_KENAR = 22;

    private final int numara;
    private final double x;
    private final double y;
    private OyuncakSavunucu savunucu;

    public OyuncakYuvasi(int numara, double x, double y) {
        this.numara = numara;
        this.x = x;
        this.y = y;
    }

    public boolean bosMu() {
        return savunucu == null;
    }

    public boolean icerir(double px, double py) {
        return Math.abs(px - x) <= YARI_KENAR && Math.abs(py - y) <= YARI_KENAR;
    }

    /** Yalnızca motor (aynı paket) savunucu yerleştirebilir. */
    void savunucuYerlestir(OyuncakSavunucu yeni) {
        if (!bosMu()) {
            throw new IllegalStateException("Yuva #" + numara + " zaten dolu.");
        }
        this.savunucu = yeni;
    }

    public int getNumara() {
        return numara;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public OyuncakSavunucu getSavunucu() {
        return savunucu;
    }
}
