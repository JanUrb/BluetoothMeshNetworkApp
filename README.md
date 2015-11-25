# MobileCompAlarmanlage
Mit unserer Alarmanlage ist jeder Alarm anlage!

## API
* Ich persönich würde die Bilderkennung als observable gestalten. Wenn eine Bewegung erkennt wird -> notifyObservers(). So müssen wir uns keine Gedanken über Schnittstellen usw machen, da Java dies in der Standard Library anbietet. 
* Eine andere Idee wäre, dass das System alle paar Sekunden die Bewegungserkennung ausführt und das Ergebnis auswertet lässt.
