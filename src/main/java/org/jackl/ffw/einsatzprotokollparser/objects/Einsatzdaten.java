package org.jackl.ffw.einsatzprotokollparser.objects;

import lombok.Data;

@Data
public class Einsatzdaten {
    private String einsatznummer = "";
    private String datum = "";
    private String meldender = "";
    private String einsatzstichwort = "";
    private String ort = "";
    private String ortsteil = "";
    private String strasse = "";
    private String objekt = "";
    private String gefahrenmeldeanlage = "";
    private String unerkannt = "";
}
