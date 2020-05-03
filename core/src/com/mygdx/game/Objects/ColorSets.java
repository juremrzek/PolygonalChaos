package com.mygdx.game.Objects;

import com.badlogic.gdx.graphics.Color;

public class ColorSets {
    public static final Color [] whiteGray = new Color[6];
    public static final Color [] red = new Color[6];
    public static final Color [] yellowBlack = new Color[6];
    public static final Color [] orange = new Color[6];
    static {
        whiteGray[0] = new Color(0xFFFFFFFF); //middle hexagon outline
        whiteGray[1] = new Color(0x747474FF); //middle hexagon fill
        whiteGray[2] = new Color(0xadabacFF); //lighter background part
        whiteGray[3] = new Color(0x747474FF); //darker background part
        whiteGray[4] = new Color(0x919090FF); //third background color if the number of sides is odd (middle color)
        whiteGray[5] = new Color(0xFFFFFFFF); //color of obstacles

        yellowBlack[0] = new Color(0x9ea52aFF);
        yellowBlack[1] = new Color(0x010001FF);
        yellowBlack[2] = new Color(0x1a1805FF);
        yellowBlack[3] = new Color(0x010001FF);
        yellowBlack[4] = new Color(0x4d4711FF);
        yellowBlack[5] = new Color(0x9fa528FF);

        orange[0] = new Color(0xcda405FF);
        orange[1] = new Color(0x402e03FF);
        orange[2] = new Color(0x5f4304FF);
        orange[3] = new Color(0x3f2e03FF);
        orange[4] = new Color(0x919090FF);//todo - needs to be added
        orange[5] = new Color(0xcda405FF);

        red[0] = new Color(0xcb2118FF);
        red[1] = new Color(0x4a1402FF);
        red[2] = new Color(0x6f1d02FF);
        red[3] = new Color(0x4a1402FF);
        red[4] = new Color(0xcf211dFF);//todo - needs to be added
        red[5] = new Color(0xcf211dFF);

    }
}
