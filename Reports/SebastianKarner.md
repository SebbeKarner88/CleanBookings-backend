# Rapport CleanBookings Webbutveckling
### Elev: Sebastian Kärner

Detta projekt startade med att företaget Städafint AB kontaktade Codecrafters och ville skapa ett administrativt system för dem själva och deras kunder.
De hade sökt brett ute på marknaden men kunde inte hitta något system som passade just deras behov, därför valde de att ta kontakt med Codecrafters för att undersöka alternativet att skapa ett gränssnitt som är skräddarsytt efter deras önskemål.

Projektet startade med att teamet satte sig ner och började lägga upp en första grov planering. Vi hade fått en kravspecifikation från Städafint med de funktioner de behövde som ett minimum i programmet, så nästa steg vart att välja tekniker för projektet.
Valet landade på en Webbapplikation, här såg vi fördelar då kunder lätt kan surfa till en hemsida och boka städning. Vår frontend skulle skapas i React och backend skulle bli ett spring boot projekt med en Relationsdatabas koppling.
Här vill vi att man ska kunna koppla en städning både till en anställd och en kund, men också till en betalning, därför landade valet på PostgreSQL.

Projektet gick nu vidare till nästa planeringsfas där vi började med att tolka kravspecifikationen lite grundligare och översatte den till user stories och tasks i JIRA. Vi skapade ett grovt flödesschema för att ha en relativ kurs att gå efter samt att vi kom överens om hur vi ville lägga upp arbetet i teamet.

Projektets första Sprintar flöt på relativt bra, det dök upp några enstaka problem som kunde lösas ganska så omgående. Vi märkte mot slutet av sprintarna att vi hade underskattat tidsåtgången på de tasks vi hade lagt in, så det blev en del extra uppflyttat från backlogen.

Vid mitten på sprint 3 så hade vi en MVP klar med alla grundfunktioner som efterfrågades i kravspecifikationen. Nu började några i teamet att jobba på UX/Design aspekten av projektet där vi ville ha en enkel, avskalad design med ett lugnt, inbjudande färgschema. Medan en del av teamet jobbade med detta så tog en annan del av teamet tag i säkerhetsaspekten av programmet.
Vi bestämde att vi skulle ge Keycloak ett försök. Eftersom vi tidigare använt egenskrivna implementationer av webtokens så ville vi denna gång testa att använda oss av en extern tjänst för autentisering och auktorisering av våra användare. Denna implementation skulle visa sig ta en stund då dokumentationen var mycket bristfällig från leverantörens sida, det slutade med att vi fick sitta och testa oss fram till en fungerande implementation.
Resultatet av detta är en 100% integrerad Keycloak-implementation där vi autentiserar och auktoriserar programmets användare med JWT och roller.

När vi nu befann oss i slutet på sprint 4 så hade vi en färdigdesignad webapplikation med ett gränssnitt för kunder, (en hemsida med inloggningsfunktion och bokningsfunktion), samt ett gränssnitt för Städafints anställda där de kan administrera kunder, bokningar och städare. Allt detta omfamnas dessutom av en säkerhetsimplementation där användarnas data lagras krypterat och säkert.

Teamets sista mål innan överlämning var nu att integrera en betalfunktion så kunder lätt kan betala sina städningar med kort, faktura eller delbetalning. Här valde teamet att använda sig av Klarna och deras ”checkout” API. Klarna har ett mycket lättanvänt gränssnitt för att administrera beställningar och köp kunder har gjort på hemsidan, dessutom är Klarnas betalfunktioner så pass välanvända ute på marknaden att kunder kommer känna sig 100% trygga med att använda dem för fakturering.

Vi på Codecrafters är mycket nöjda med webbapplikationen vi har skapat åt Städafint AB. All den funktionalitet som eftersöktes enligt kravspecifikation finns i programmet, men även de delar som ofta förbises vid planering såsom säkerhet för användare, faktureringsfunktioner med mera.

Överlämning av projektet kommer att ske närmaste dagarna och vi är övertygade om att Städafint kommer att bli mycket nöjda med sitt nya system.