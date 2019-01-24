package org.labkey.serverapi.reader;

import java.text.DecimalFormat;

public class Formats
{
    public static DecimalFormat f0 = new DecimalFormat("0");
    public static DecimalFormat f1 = new DecimalFormat("0.0");
    public static DecimalFormat f2 = new DecimalFormat("0.00");
    public static DecimalFormat fv2 = new DecimalFormat("#.##");
    public static DecimalFormat f3 = new DecimalFormat("0.000");
    public static DecimalFormat fv3 = new DecimalFormat("#.###");
    public static DecimalFormat f4 = new DecimalFormat("0.0000");
    public static DecimalFormat fv4 = new DecimalFormat("#.####");
    public static DecimalFormat signf4 = new DecimalFormat("+0.0000;-0.0000");
    public static DecimalFormat percent = new DecimalFormat("0%");
    public static DecimalFormat percent1 = new DecimalFormat("0.0%");
    public static DecimalFormat percent2 = new DecimalFormat("0.00%");
    public static DecimalFormat commaf0 = new DecimalFormat("#,##0");
    public static DecimalFormat commaf3 = new DecimalFormat("#,##0.###");
    public static DecimalFormat chargeFilter = new DecimalFormat("0.#");
}
