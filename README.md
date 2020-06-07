Dieses Projekt dient zum Parsen von Einsatzprotokollen
gewisser Leitstellen, welche diese mittlerweile anstelle der 
vorherigen Alarmdrucke versenden.

Bisher werden die Einsatzdaten und auch die Einsatzmittel
korrekt eingelesen. Die Meldungen sind jedoch noch kaputt
und fürs Erste auch nicht weiter von Relevanz.

Zum Testen ist auch eine Main-Methode vorhanden, 
d.h. das Ganze ist auch ausführbar. Als Parameter wird eine PDF
Datei erwartet, diese wird dann geparst und im Arbeitsverzeichnis
des Programmes als JSON ausgegeben.

Beispielaufruf:
```
A:\einsatzprotokollparser\>java -jar einsatzprotokollparser-1.0-SNAPSHOT-all.jar ..\Einsatzauftrag.pdf
Einsatzauftrag.pdf has been successfully parsed, the output is saved as A:\einsatzprotokollparser\Einsatzauftrag.json
```
Die JSON Datei entspricht den Feldern der Objekte
in `org.jackl.ffw.einsatzprotokollparser.objects`. Diese Felder werden
erweitert, sobald ich entsprechende neue Felder in den PDF finde.
Unerkannte Felder in den PDF werden natürlich auch nicht verworfen,
sondern in entsprechende Felder gespeichert.

Downloadbare JARs findest du in den [Releases](https://github.com/fabicodes/einsatzprotokollparser/releases), es wird mindestens Java 1.8 benötigt.
