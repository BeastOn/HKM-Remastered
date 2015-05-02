package lb.themike10452.hellscorekernelmanagerl.CustomClasses;

import java.io.Serializable;

/**
 * Created by Mike on 4/2/2015.
 */
public class MakoColorProfile implements Serializable {
    public boolean isUserProfile;
    private Integer R, G, B;
    private String gammaR, gammaG, gammaB, alias;

    public MakoColorProfile(String profileData) {
        String[] data = profileData.split(",");
        init(data[0], data[1], data[2], data[3], data[4]);
        isUserProfile = false;
    }

    public MakoColorProfile(String alias, String gR, String gG, String gB, String RGB) {
        init(alias, gR, gG, gB, RGB);
    }

    private void init(String alias, String gR, String gG, String gB, String RGB) {
        String[] rgb = RGB.trim().split(" ");
        R = Integer.parseInt(rgb[0].trim());
        G = Integer.parseInt(rgb[1].trim());
        B = Integer.parseInt(rgb[2].trim());
        gammaR = gR.trim();
        gammaG = gG.trim();
        gammaB = gB.trim();
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public String getRGB() {
        return R + " " + G + " " + B;
    }

    public int getR() {
        return R;
    }

    public int getG() {
        return G;
    }

    public int getB() {
        return B;
    }

    public String getGammaR() {
        return gammaR;
    }

    public String getGammaG() {
        return gammaG;
    }

    public String getGammaB() {
        return gammaB;
    }

    public String toString() {
        return getRGB()
                .concat(" ")
                .concat(getGammaR())
                .concat(" ")
                .concat(getGammaG())
                .concat(" ")
                .concat(getGammaB());
    }

    public int compareTo(MakoColorProfile other) {
        return other.toString().compareTo(toString());
    }
}
