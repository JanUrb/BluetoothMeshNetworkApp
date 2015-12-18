# MobileCompAlarmanlage
Mit unserer Alarmanlage ist jeder Alarm anlage!

## API
* Ich persönlich würde die Bilderkennung als observable gestalten. Wenn eine Bewegung erkennt wird -> notifyObservers(). So müssen wir uns keine Gedanken über Schnittstellen usw machen, da Java dies in der Standard Library anbietet. 
* Eine andere Idee wäre, dass das System alle paar Sekunden die Bewegungserkennung ausführt und das Ergebnis auswertet lässt.

## SDK-Level
* Die Kameras laufen auf Android 2.2 (sdk: 8)
* Wir testen mit sdk 15 Geräten. 
* Die Kameras müssen mindestens sdk 8 haben
