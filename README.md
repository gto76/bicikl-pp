Bicikl ++
=========

Namen aplikacije
----------------
Aplikacija je namenjena uporabnikom javnega sistema izposoje koles v Ljubljani. Omogoèa iskanje najhitrejših poti od trenutne do ciljne lokacije. Poleg prikaza trenutnega stanja, aplikacija tudi beleži pretekle informacije o številu koles na postajah.

Realizirani zasloni
-------------------
* Map – Zaèetni zaslon ki prikazuje googlov zemljevid Ljubljane. Na njemu so vidne lokacije postaj. Z daljšim klikom pa izberemo ciljno lokacijo, kar nam prikaže najhitrejšo pot (upoštevajo se samo postaje ki imajo na voljo kolesa).
* Stations – Zaslon prikaže spisek vseh postaj, urejen po oddaljenosti od trenutne lokacije. V ozadju je fotografija trenutne lokacije.
* Station – Zaslon prikaže podrobne podatke o postaji ter zgodovino preteklih stanj postaje. V ozadju je fotografija postaje.
* Paths – Zaslon prikaže spisek najkrajših poti od trenutne do izbrane ciljne lokacije. Poti so urejene po predvidenem èasu potovanja, ter obarvane glede na število prostih koles ter mest. V ozadju je fotografija izbrane ciljne lokacije. 
* Options – Zaslon omogoèa nastavitev hitrosti kolesarjenja ter zadovoljivega števila koles.

Realizacije zahtev za višjo oceno
---------------------------------
* Aplikacija je intuitivna za uporabo, 
* uporablja googlove zemljevide ter
* komunicira za zunanjimi storitvami: API od Biciklja, Google Duration API in Google Street View Image API. 
