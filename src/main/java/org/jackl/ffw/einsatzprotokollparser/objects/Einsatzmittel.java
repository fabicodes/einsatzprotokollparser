package org.jackl.ffw.einsatzprotokollparser.objects;

import lombok.Data;

@SuppressWarnings("ALL")
@Data
public class Einsatzmittel {
    private final String einheit;
    private final String staerke;
    private final int zf;
    private final int gf;
    private final int unr;
    private final int agt;
    private final String alarm;
    private final String s3;
    private final String s4;
    private final String s1;
    private final String ende;
}
