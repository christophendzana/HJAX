
package HRIbbonTabs.view;

import java.awt.Color;


public abstract class HRibbonTabsTheme {

    public static final HRibbonTabsTheme PRIMARY = new PrimaryTheme();


    public static final HRibbonTabsTheme SECONDARY = new SecondaryTheme();


    public static final HRibbonTabsTheme SUCCESS = new SuccessTheme();


    public static final HRibbonTabsTheme DANGER = new DangerTheme();


    public static final HRibbonTabsTheme WARNING = new WarningTheme();


    public static final HRibbonTabsTheme INFO = new InfoTheme();


    public static final HRibbonTabsTheme DARK = new DarkTheme();


    public static final HRibbonTabsTheme OCEAN = new OceanTheme();


    public static final HRibbonTabsTheme PURPLE = new PurpleTheme();


    public static final HRibbonTabsTheme LIGHT = new LightTheme();


    public abstract Color getTabBarBackground();


    public abstract Color getTabBackground();


    public abstract Color getTabSelectedBackground();


    public abstract Color getTabHoverBackground();


    public abstract Color getTabTextColor();


    public abstract Color getTabSelectedTextColor();


    public abstract Color getTabIndicatorColor();


    public abstract Color getContentBackground();


    public abstract Color getContentBorderColor();


    public abstract Color getRibbonBackground();


    public abstract Color getRibbonBorderColor();


    public abstract Color getGroupGradientStart();


    public abstract Color getGroupGradientEnd();


    public abstract Color getGroupBorderColor();


    public abstract Color getGroupHoverBorderColor();


    public abstract Color getGroupHoverTint();


    public abstract Color getHeaderBackground();


    public abstract Color getHeaderTextColor();


    public abstract Color getCollapseButtonBackground();


    public abstract Color getCollapseButtonIconColor();


    protected static Color lighten(Color color, float factor) {

        int r = color.getRed() + (int) ((255 - color.getRed()) * factor);
        int g = color.getGreen() + (int) ((255 - color.getGreen()) * factor);
        int b = color.getBlue() + (int) ((255 - color.getBlue()) * factor);
        return new Color(
                Math.min(255, r),
                Math.min(255, g),
                Math.min(255, b)
        );
    }


    protected static Color darken(Color color, float factor) {

        int r = (int) (color.getRed() * (1f - factor));
        int g = (int) (color.getGreen() * (1f - factor));
        int b = (int) (color.getBlue() * (1f - factor));
        return new Color(
                Math.max(0, r),
                Math.max(0, g),
                Math.max(0, b)
        );
    }


    protected static Color withAlpha(Color color, int alpha) {
        return new Color(
                color.getRed(),
                color.getGreen(),
                color.getBlue(),
                Math.max(0, Math.min(255, alpha))
        );
    }


    private static final class PrimaryTheme extends HRibbonTabsTheme {


        private static final Color BASE = new Color(13, 110, 253);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.6f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.6f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.60f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }


    private static final class SecondaryTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(108, 117, 125);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.5f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.5f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.4f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.3f);
        }
    }


    private static final class SuccessTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(25, 135, 84);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }


    private static final class DangerTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(220, 53, 69);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.82f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.68f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.58f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.72f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }


    private static final class WarningTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(255, 193, 7);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.5f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.40f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.40f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.72f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(darken(BASE, 0.2f), 50);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.45f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.55f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.60f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.45f);
        }
    }


    private static final class InfoTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(13, 202, 240);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.78f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.90f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.45f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return darken(BASE, 0.15f);
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.78f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.45f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.82f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.45f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.75f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.58f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(darken(BASE, 0.1f), 45);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return darken(BASE, 0.15f);
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 35);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.48f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.50f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.62f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.40f);
        }
    }


    private static final class DarkTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(52, 58, 64);

        @Override
        public Color getTabBarBackground() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabBackground() {
            return new Color(52, 58, 64);
        }

        @Override
        public Color getTabSelectedBackground() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getTabHoverBackground() {
            return new Color(62, 68, 75);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(200, 200, 200);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return Color.WHITE;
        }

        @Override
        public Color getTabIndicatorColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getContentBackground() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getContentBorderColor() {
            return new Color(90, 98, 106);
        }

        @Override
        public Color getRibbonBackground() {
            return new Color(68, 74, 80);
        }

        @Override
        public Color getRibbonBorderColor() {
            return new Color(90, 98, 106);
        }

        @Override
        public Color getGroupGradientStart() {
            return new Color(80, 87, 94);
        }

        @Override
        public Color getGroupGradientEnd() {
            return new Color(65, 72, 78);
        }

        @Override
        public Color getGroupBorderColor() {
            return new Color(100, 108, 115);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getGroupHoverTint() {
            return new Color(255, 255, 255, 20);
        }

        @Override
        public Color getHeaderBackground() {
            return new Color(45, 50, 55);
        }

        @Override
        public Color getHeaderTextColor() {
            return new Color(190, 195, 200);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return new Color(80, 87, 94);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return new Color(190, 195, 200);
        }
    }


    private static final class OceanTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(0, 150, 136);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.3f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.2f);
        }
    }


    private static final class PurpleTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(156, 39, 176);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getTabHoverBackground() {
            return lighten(BASE, 0.92f);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getTabIndicatorColor() {
            return BASE;
        }

        @Override
        public Color getContentBackground() {
            return lighten(BASE, 0.85f);
        }

        @Override
        public Color getContentBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getRibbonBackground() {
            return lighten(BASE, 0.88f);
        }

        @Override
        public Color getRibbonBorderColor() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getGroupGradientStart() {
            return lighten(BASE, 0.80f);
        }

        @Override
        public Color getGroupGradientEnd() {
            return lighten(BASE, 0.65f);
        }

        @Override
        public Color getGroupBorderColor() {
            return withAlpha(BASE, 40);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return BASE;
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 30);
        }

        @Override
        public Color getHeaderBackground() {
            return lighten(BASE, 0.55f);
        }

        @Override
        public Color getHeaderTextColor() {
            return darken(BASE, 0.2f);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return lighten(BASE, 0.70f);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return darken(BASE, 0.15f);
        }
    }


    private static final class LightTheme extends HRibbonTabsTheme {

        private static final Color BASE = new Color(173, 181, 189);

        @Override
        public Color getTabBarBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getTabBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getTabSelectedBackground() {
            return Color.WHITE;
        }

        @Override
        public Color getTabHoverBackground() {
            return new Color(241, 243, 245);
        }

        @Override
        public Color getTabTextColor() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getTabSelectedTextColor() {
            return new Color(33, 37, 41);
        }

        @Override
        public Color getTabIndicatorColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getContentBackground() {
            return Color.WHITE;
        }

        @Override
        public Color getContentBorderColor() {
            return new Color(222, 226, 230);
        }

        @Override
        public Color getRibbonBackground() {
            return new Color(248, 249, 250);
        }

        @Override
        public Color getRibbonBorderColor() {
            return new Color(222, 226, 230);
        }

        @Override
        public Color getGroupGradientStart() {
            return new Color(255, 255, 255);
        }

        @Override
        public Color getGroupGradientEnd() {
            return new Color(241, 243, 245);
        }

        @Override
        public Color getGroupBorderColor() {
            return new Color(222, 226, 230);
        }

        @Override
        public Color getGroupHoverBorderColor() {
            return new Color(173, 181, 189);
        }

        @Override
        public Color getGroupHoverTint() {
            return withAlpha(BASE, 20);
        }

        @Override
        public Color getHeaderBackground() {
            return new Color(233, 236, 239);
        }

        @Override
        public Color getHeaderTextColor() {
            return new Color(73, 80, 87);
        }

        @Override
        public Color getCollapseButtonBackground() {
            return new Color(241, 243, 245);
        }

        @Override
        public Color getCollapseButtonIconColor() {
            return new Color(108, 117, 125);
        }
    }

}
