package org.jackl.ffw.einsatzprotokollparser.objects;

import lombok.Data;

import java.util.List;

@Data public class Einsatzprotokoll {
    private Einsatzdaten einsatzdaten;
    private List<Einsatzmittel> einsatzmittel;
    private List<Meldungen> meldungen;
    private String unrecognized;
}
