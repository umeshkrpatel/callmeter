package com.github.umeshkrpatel.LogMeter;

/**
 * Created by umpatel on 1/20/2016.
 */
public interface IDefs {
    long kDateTimeFactor = 86400000L;
    long kMilliSecondsPerSecond = 1000L;
    int kEightyth = 80;
    int kHundredth = 100;
    int kSecondsPerMinute = 60;
    int kSecondsPerHour = 60 * kSecondsPerMinute;
    int kSecondsPerDay = 24 * kSecondsPerHour;
    int kTenth = 10;
    long kBytesPerKiloByte = 1024L;
    long kBytesPerMegaByte = kBytesPerKiloByte * kBytesPerKiloByte;
    long kBytesPerGigaByte = kBytesPerMegaByte * kBytesPerKiloByte;
    long kBytesPerTeraByte = kBytesPerGigaByte * kBytesPerKiloByte;
}
