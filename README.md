Bicikl ++
=========

Namen aplikacije
----------------
Aplikacija je namenjena uporabnikom javnega sistema izposoje koles v Ljubljani. Omogo�a iskanje najhitrej�ih poti od trenutne do ciljne lokacije. Poleg prikaza trenutnega stanja, aplikacija tudi bele�i pretekle informacije o �tevilu koles na postajah.

Realizirani zasloni
-------------------
* Map � Za�etni zaslon ki prikazuje googlov zemljevid Ljubljane. Na njemu so vidne lokacije postaj. Z dalj�im klikom pa izberemo ciljno lokacijo, kar nam prika�e najhitrej�o pot (upo�tevajo se samo postaje ki imajo na voljo kolesa).
* Stations � Zaslon prika�e spisek vseh postaj, urejen po oddaljenosti od trenutne lokacije. V ozadju je fotografija trenutne lokacije.
* Station � Zaslon prika�e podrobne podatke o postaji ter zgodovino preteklih stanj postaje. V ozadju je fotografija postaje.
* Paths � Zaslon prika�e spisek najkraj�ih poti od trenutne do izbrane ciljne lokacije. Poti so urejene po predvidenem �asu potovanja, ter obarvane glede na �tevilo prostih koles ter mest. V ozadju je fotografija izbrane ciljne lokacije. 
* Options � Zaslon omogo�a nastavitev hitrosti kolesarjenja ter zadovoljivega �tevila koles.

Realizacije zahtev za vi�jo oceno
---------------------------------
* Aplikacija je intuitivna za uporabo, 
* uporablja googlove zemljevide ter
* komunicira za zunanjimi storitvami: API od Biciklja, Google Duration API in Google Street View Image API. 
