package com.mygdx.game.Objects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import java.io.Serializable;

public class ColorSets implements Serializable {
    public static final Color [] GRAY = new Color[5];
    public static final Color [] YELLOW_BLACK = new Color[5];
    public static final Color [] RED = new Color[5];
    public static final Color [] BROWN = new Color[5];
    public static final Color [] GREEN = new Color[5];
    public static final Color [] PURPLE = new Color[5];
    public static final Color [] CYAN = new Color[5];
    public static final Color [] PINK = new Color[5];
    public static final Color [] YELLOW = new Color[5];
    public static final Color [] BLACK = new Color[5];
    public static Color[][] colorSets;
    static {
        GRAY[0] = new Color(0xFFFFFFFF); //middle hexagon outline
        GRAY[1] = new Color(0x747474FF); //middle hexagon fill
        GRAY[2] = new Color(0xadabacFF); //lighter background part
        GRAY[3] = new Color(0x747474FF); //darker background part
        GRAY[4] = new Color(0x919090FF); //third background color if the number of sides is odd (middle color)

        YELLOW_BLACK[0] = new Color(0x9ea52aFF);
        YELLOW_BLACK[1] = new Color(0x010001FF);
        YELLOW_BLACK[2] = new Color(0x1a1805FF);
        YELLOW_BLACK[3] = new Color(0x010001FF);
        YELLOW_BLACK[4] = new Color(0x4d4711FF);

        BROWN[0] = new Color(0xcda405FF);
        BROWN[1] = new Color(0x402e03FF);
        BROWN[2] = new Color(0x5f4304FF);
        BROWN[3] = new Color(0x3f2e03FF);
        BROWN[4] = new Color(0x856513FF);

        RED[0] = new Color(0xcb2118FF);
        RED[1] = new Color(0x4a1402FF);
        RED[2] = new Color(0x6f1d02FF);
        RED[3] = new Color(0x4a1402FF);
        RED[4] = new Color(0x942e0dFF);

        GREEN[0] = new Color(0x51d625FF);
        GREEN[1] = new Color(0x054600FF);
        GREEN[2] = new Color(0x0e5400FF);
        GREEN[3] = new Color(0x044700FF);
        GREEN[4] = new Color(0x066300FF);

        PURPLE[0] = new Color(0x7226ffFF);
        PURPLE[1] = new Color(0x180755FF);
        PURPLE[2] = new Color(0x240866FF);
        PURPLE[3] = new Color(0x16064fFF);
        PURPLE[4] = new Color(0x2e1c70FF);

        CYAN[0] = new Color(0x00c5ffFF);
        CYAN[1] = new Color(0x004653FF);
        CYAN[2] = new Color(0x005165FF);
        CYAN[3] = new Color(0x004653FF);
        CYAN[4] = new Color(0x00687bFF);

        PINK[0] = new Color(0xff32ddFF);
        PINK[1] = new Color(0x570d4fFF);
        PINK[2] = new Color(0x6b0e5bFF);
        PINK[3] = new Color(0x4d0b47FF);
        PINK[4] = new Color(0x7f1676FF);

        YELLOW[0] = new Color(0xf2eb06FF);
        YELLOW[1] = new Color(0x645b00FF);
        YELLOW[2] = new Color(0x645b00FF);
        YELLOW[3] = new Color(0x524600FF);
        YELLOW[4] = new Color(0x837315FF);

        BLACK[0] = new Color(Color.WHITE);
        BLACK[1] = new Color(Color.BLACK);
        BLACK[2] = new Color(Color.BLACK);
        BLACK[3] = new Color(Color.BLACK);
        BLACK[4] = new Color(Color.BLACK);

        int numberOfColorSets = 8;
        colorSets = new Color[numberOfColorSets][];
        colorSets[0] = ColorSets.BROWN;
        colorSets[1] = ColorSets.YELLOW_BLACK;
        colorSets[2] = ColorSets.GREEN;
        colorSets[3] = ColorSets.PURPLE;
        colorSets[4] = ColorSets.CYAN;
        colorSets[5] = ColorSets.GRAY;
        colorSets[6] = ColorSets.YELLOW;
        colorSets[7] = ColorSets.PINK;
    }
    public static String[] toString(Color[] colors){
        String[] s = new String[colors.length];
        for(int i=0; i<colors.length; i++){
            s[i] = colors[i].toString();
        }
        return s;
    }
    public static Color getColorFromHex (String hex) {
        int red = Integer.valueOf(hex.substring(0, 2), 16);
        int green = Integer.valueOf(hex.substring(2, 4), 16);
        int blue = Integer.valueOf(hex.substring(4, 6), 16);
        int alpha = hex.length() != 8 ? 255 : Integer.valueOf(hex.substring(6, 8), 16);
        return new Color(red/255f, green/255f, blue/255f, alpha/255f);
    }
    public static String getName(Color[] colorSet){
        if(colorSet == GRAY)
            return "gray";
        if(colorSet == YELLOW_BLACK)
            return "dark yellow";
        if(colorSet == RED)
            return "red";
        if(colorSet == BROWN)
            return "brown";
        if(colorSet == GREEN)
            return "green";
        if(colorSet == PURPLE)
            return "purple";
        if(colorSet == CYAN)
            return "cyan";
        if(colorSet == PINK)
            return "pink";
        if(colorSet == YELLOW)
            return "yellow";
        if(colorSet == BLACK)
            return "black";
        return "";
    }
}
