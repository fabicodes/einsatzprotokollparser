package org.jackl.ffw.einsatzprotokollparser.objects;

import lombok.Data;

@Data public class Meldungen {
    private String meldung;
    private String zeitpunkt;
    private String einsatzmittel;
    private String benutzer;
}
