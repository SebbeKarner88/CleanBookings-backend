# ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤

# Direktiv för att sätta upp programmets utvecklingsmiljö

# ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤

## Databas

För en enkel och korrekt databasuppkoppling så hämtas PostgreSQL i lämplig version och installeras på datorn.

### PostgreSQL finns att hämta här: https://www.postgresql.org/

####  * Konfigurera databasen enligt den standard som hänvisas i installationen. <br/>

####  * Välj ett lämpligt användarnamn och lösenord och spara dessa värden för framtida användning.

<br/>

####  * I programmets filstruktur, navigera till src/main/resources<br/>

####  * Skapa en fil i resources-mappen som heter env.properties och öppna denna fil.

#### Kopiera in dessa miljövariabler i env.properties filen:

DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USER=[Ditt Användarnamn]
DB_PASSWORD=[Ditt lösenord]
SMTP_USER=order.cleanbookings@gmail.com
SMTP_PASSWORD=vmno lpps vetz rtjh

KC_REALM= CB_REALM
KC_CLIENT_ID=
KC_CLIENT_NAME= CB_CLIENT
KC_CLIENT_SECRET=
KC_ADMIN_USERNAME=admin
KC_ADMIN_PASSWORD=admin
KC_ROLE_CUSTOMER_ID=
KC_ROLE_CLEANER_ID=
KC_ROLE_ADMIN_ID=
KC_PRINCIPAL_ATTRIBUTE=preferred_username

KLARNA_USERNAME=
KLARNA_PASSWORD=

### Uppkoppling till databas är nu konfigurerad.

<br/>

## Keycloak Authentiseringstjänst

### Nu ska vi konfigurera vår autentiseringstjänst.

<br/>

### Docker

#### Vi börjar med att installera Docker/Docker Desktop.

#### Hämta och installera Docker/Docker Desktop från denna url: https://www.docker.com/products/docker-desktop/

#### Om installationen inte lyckas, vänligen referera till denna url för felsökning (PC, Windows): https://docs.docker.com/desktop/vm-vdi/

<br/>

### Postman

#### Nu ska vi installera ett program som kommer hjälpa oss att hämta data från vår keycloak klient.

#### Hämta postman från följande URL: https://www.postman.com/

#### Installera programmet och säkerställ att det startar korrekt.

<br/>

### Starta Keycloak container från docker-image

#### I programmets terminal, kör detta kommando:

docker run -p 8080:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:22.0.5
start-dev

#### Detta kommando skapar en container innehållandes en keycloak klient på din dator.

#### Säkerställ att containern är igång, antingen i docker desktop gränssnittet eller i terminalen.

<br/>

### Konfigurera Keycloak klienten på klientsidan

#### Öppna ett nytt webbläsarfönster och kopiera in följande URL: http://localhost:8080/

#### Klicka på Administration Console och mata in "admin" som både username och password.

#### Nästan högst upp i vänstra hörnet på sidan kommer ni finna en "drop-down lista" där det just nu står master.

#### Klicka på pilen och gå ner till "Create Realm" och klicka på den.

#### Under Realm name så matar vi in "CB_REALM", detta blir namnet på vår Realm.

#### Se till så "Enabled" är ikryssad innan vi trycker på "Create".

<br/>

#### När vi tryckt på Create så kommer vi till startsidan för vår nyskapade Realm.

#### Nu navigerar vi till sidomenyn till vänster och trycker på Clients.

#### När vi kommit till Client-sidan så hittar vi en blå knapp i mitten av sidan som heter Create client, Klicka på denna.

#### Säkerställ att Client type är OpenID Connect.

#### Under Client ID så döper vi vår client till "CB_CLIENT", Name och Description är inte nödvändiga.

#### Säkerställ att "Always display in UI" är ikryssad.

#### Tryck på Next.

<br/>

#### Innan vi fortsätter så navigerar vi till "assets/images/" från programmets root-mapp.

#### I denna mapp finner vi ett antal bildfiler som vi nu ska ha som mall för konfigurering.

<br/>

#### På följande sida, säkerställ att konfigureringen ser ut enligt bildfilen "assets/images/capabilityConfig.png"

#### Tryck på Next.

<br/>

#### På följande sida, säkerställ att konfigureringen ser ut enligt bildfilen "assets/images/accessSettings.png"

#### Tryck på Save.

<br/>

#### Nu kan vi gå vidare till att skapa de roller vi använder oss av.

#### På Client sidan så finns det en flik som heter Roles, klicka på denna.

#### Klicka på Create role.

#### Här ska vi nu skapa 3 olika roller, de roller som behöver upprättas är:

"client_admin"  <br/>
"client_cleaner"  <br/>
"client_customer"  <br/>

#### Skapa dessa 3 roller med varsina passande descriptions.

#### Nu är vi klara med rollskapandet, navigera nu till:

#### Credentials i Client vyn.

<br/>

#### I Credentials fliken så ska vi hämta en Miljövariabel till vår env.properties fil.

#### Säkerställ att Client Authenticator är satt till Client Id and Secret.

#### Under Client secret, tryck på knappen som liknar två pappersark för att kopiera secret.

#### gå nu till env.properties som vi upprättade tidigare, fyll i enligt nedan:

KC_CLIENT_SECRET=[din kopierade secret]

#### Nu är vi klara med konfigureringen av keycloak på klientens sida.

#### Nu ska vi fortsätta med att starta Postman och hämta lite data för konfigurering på applikations-sidan.

### Konfigurera Keycloak Klienten på applikationssidan

#### Starta Postman som installerades tidigare.

#### Nu ska vi skicka en rad anrop för att hämta data som vi sedan ska lägga till i vår env. fil.

#### Skapa ett nytt anrop och fyll i parametrar enligt "assets/images/adminTokenCall.png"

#### Tryck på Send.

#### I returen så kommer det nu finnas en position som heter "access_token", värdet av denna position ska vi kopiera för att kunna skicka följande anrop.
#### VIKTIGT! Denna access_token är bara giltig i 60 sekunder per default, så ha detta i åtanke när vi skickar de andra anropen.
<br/>

#### Nu ska vi skicka ett anrop för att hämta ID på vår client.
#### Skriv anropet enligt "assets/images/getClientKeycloak.png"
#### Under fliken Auth så väljer vi Bearer token, och därefter klistrar vi in vår access_token från tidigare anrop.
#### !!Observera att detta måste göras inom 60 sekunder från förra anropet!!
<br/>

#### I svaret på detta anrop så kommer vi få en lång rad objekt där varje objekt representerar en client i vår keycloak realm.
#### Svaret ser ut något åt dethär hållet:

"id": "0fc8c1c1-7ca8-40b9-8655-bc3a48e95540",
"clientId": "CB_CLIENT",
"name": "name",
"description": "description",
"rootUrl": "http://localhost:8081",
"adminUrl": "http://localhost:8081",
"baseUrl": "http://localhost:8081",
"surrogateAuthRequired": false,
"enabled": true,
"alwaysDisplayInConsole": true,
"clientAuthenticatorType": "client-secret",

#### Här letar vi nu upp det objekt i listan vars "name" parameter har CB_CLIENT som värde.
#### När vi hittat den så kopierar vi värdet på parametern "id" i samma objekt, i exemplet ovan skulle det då bli:
0fc8c1c1-7ca8-40b9-8655-bc3a48e95540

#### Detta värde ska vi nu klistra in i vår env. fil på:
KC_CLIENT_ID=[Id från anrop]

#### Vi har nu hämtat vårat Client id, nu saknas bara idn för våra roller. 
<br/>

#### För att hämta våra roll-id så ska vi göra ett liknande anrop via postman.
#### Säkerställ att vi har skickat ett access_token anrop och att den fortfarande är giltig.
#### Skriv anropet enligt "assets/images/getRolesKeycloak.png"
#### Fyll i auth likt tidigare anrop.
#### Observera att du måste kopiera in det ID vi hämtade i föregående anrop i URL på detta anrop, byt ut placeholder till klientens id.
#### Det vi får tillbaka på detta anrop bör se ut något likt detta:

"id": "c0c42f0d-76f0-46ca-9b8a-93a7e1c82407",
"name": "client_cleaner",
"description": "Cleaner",
"composite": false,
"clientRole": true,
"containerId": "0fc8c1c1-7ca8-40b9-8655-bc3a48e95540"

#### Här hämtar vi nu id från våra tre olika roller och sätter dessa värden i vår env. fil.
#### Här ovanför har vi till exempel client cleaner, då vill vi ta id:
c0c42f0d-76f0-46ca-9b8a-93a7e1c82407

#### Och sätta detta id i env. filen till:
KC_ROLE_CLEANER_ID=c0c42f0d-76f0-46ca-9b8a-93a7e1c82407

#### Upprepa samma procedur för att hämta
KC_ROLE_ADMIN_ID=
KC_ROLE_CUSTOMER_ID=

#### Nu har vi konfigurerat vår Keycloak klient på båda sidor av tjänsten, nu kan vi gå vidare till Klarna konfig.
<br/><br/>

## Klarna Konfigurering

#### Nu ska vi sätta upp vår konfigurering för klarna checkout.
#### Skapa ett konto på https://playground.eu.portal.klarna.com/
#### Logga in och säkerställ att du har tillgång till Web-gränssnittet.
#### Klicka på Payment Settings nere till vänster i menyn.
#### På sidan som presenteras, tryck på generate Klarna API credentials.
#### Det du får tillbaka bör se ut ungefär såhär:

username: PK1XXXX7_ed17XXXXXXXX
password: 4osSiXXXXXXXXXUBi

#### De värden du fått klistrar du in i env. filen under:

KLARNA_USERNAME= [klarna api key username]
KLARNA_PASSWORD= [klarna api key password]

### Nu är Klarna konfigureringen klar.
<br/><br/>

# Data Initializer
#### Det sista vi behöver titta på är 
"src/main/java/com/example/cleanbookingsbackend/service/DataInitialization/InitDataService.java"

#### Denna klass initierar vår databas och Keycloak klient med test data.
### Såhär ser vi till att datan kommer in i systemet på ett korrekt sätt.

#### Per default så är klassen utkommenterad, första gången vi startar vår applikation vill vi ha med denna kalss.
#### Ta bort utkommenterings-kommandona (/* */) i klassen.
#### Starta programmet.
#### OBS! Glöm inte att ha dockercontainern igång med vår keycloak klient. Denna måste vara igång varje gång vi startar vårt program.
#### Kompilerar programmet som det ska så kan vi vid detta skede kommentera ut klassen igen, då har den fyllt sin funktion.

## Vi har nu konfigurerat allt vi behöver för att köra vårat program.


# ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤

# Dokumentation av programmets funktioner

# ¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤¤

# Controller

## AdminController:

Denna controller hanterar administrativa uppgifter.

### Visa och hantera jobb (getAllJobs):

Hämta alla jobb i databasen.

### Lista och uppdatera kundinformation (listAllCustomers, updateCustomer):

Hämta alla kunder ur databasen, ändra specifik kund i databasen.

### Hantera Anställda (getAllAdmins, getAllCleaners, updateEmployee):

Hantera information om anställda, inklusive administratörer och städare

### Ta bort (deleteCustomer, deleteCleaner, deleteAdmin):

Ta bort kunder, städare eller administratörer

<br/>

## CleanerController:

Denna controller fokuserar på städarnas roller och uppgifter.

### Visa jobb som tilldelats städare (getAllJobsCleaner).

### Uppdatera anställdas information (updateEmployee).

<br/>

## CustomerController:

Denna controller hanterar operationer relaterade till kunder.

### Registrering och inloggning av kunder (create, login).

### Uppdatering av token och utloggning för säkra sessioner (refresh, logout).

### Uppdatera kundinformation och lösenord (updateCustomerInfo, updateCustomerPassword).

### Ta emot meddelanden eller förfrågningar från kunder (contactUsForm).

<br/>

## GDPRController:

Denna controller hanterar förfrågningar relaterade till GDPR.

### Hämta kunddata (getCustomerData):

En kund kan begära sin egen personliga data. Om kunden finns, skickas datan tillbaka, annars returneras ett
felmeddelande.

### Hämta anställdas data (getEmployeeData):

Liknande som ovan, men för anställda. Anställda eller administratörer kan begära personliga data för en specifik
anställd.

<br/>

## JobController:

Denna controller hanterar olika aspekter av jobbhantering inom företaget.

### Skapa jobbförfrågan (createJobRequest):

En kund kan begära ett nytt städjobb. Om förfrågan är giltig, skapas jobbet och informationen returneras.

### Tilldela städare (assignCleanerRequest):

En administratör kan tilldela städare till specifika jobb.

### Rapportera utfört städjobb (executedCleaningRequest):

En städare rapporterar att ett jobb är utfört.

### Godkänn eller avslå städning (approvedCleaningRequest):

En kund kan godkänna eller avslå ett utfört städjobb.

### Om-utföra misslyckad städning (reissueFailedCleaningRequest):

Om ett städjobb misslyckas, kan en administratör beordra att det görs om.

### Avboka jobbförfrågan (cancelJobRequest):

En kund kan avboka ett bokat städjobb.

### Hämta städuppdrag efter status (getCleaningsByStatus, getCleaningsByStatusWithRole):

Kunden eller anställda kan se en lista över städuppdrag baserat på deras status.

### Visa bokade städuppdrag (getBookedCleanings):

Kunden kan se sina bokade städuppdrag.

### Visa bokningshistorik (getBookingHistory):

Kunden kan se sin historik av städuppdrag.

### Ta bort jobb (deleteJob):

En administratör kan ta bort ett städjobb.

### Hämta alla jobb för en kund (getAllJobsCustomer):

Visar alla jobb som är relaterade till en specifik kund.

<br/><br/>

# DTO

### AdminEmployeeUpdateRequest/AdminUserUpdateRequest:

Används för att uppdatera information om anställda eller kunder. De innehåller fält som namn, adress, telefonnummer,
etc.

### AssignCleanerRequest:

Används för att tilldela städare till ett specifikt jobb.

### AuthenticationRequest/Response:

Hanterar inloggningsförfrågningar och svar. En förfrågan innehåller användarens e-post och lösenord, medan svaret kan
innehålla information som användar-ID och tillgångstoken.

### ContactRequest:

Används för att hantera kontaktformulär-förfrågningar från kunder, inklusive namn, e-post, ämne och meddelande.

### CreateEmployeeRequest/Response:

För att skapa en ny anställd och få tillbaka ett svar med anställdas information.

### CreateJobRequest/Response:

Används för att begära ett nytt städjobb och få ett svar som bekräftar skapandet av jobbet.

### CustomerDataResponse:

Används för att svara med kundens data, vanligtvis efter en GDPR-förfrågan.

### CustomerRegistrationDTO:

Datastruktur för att registrera en ny kund med personliga och kontaktuppgifter.

### CustomerResponseDTO:

Används för att skicka kundinformation som svar på en förfrågan.

### EmployeeAuthenticationResponse:

Används för att svara på en anställds autentiseringsförfrågan, innehåller identitets- och rollinformation.

### EmployeeDataResponse/DTO:

Innehåller information om en anställd, vanligtvis som svar på en förfrågan.

### JobApproveRequest:

Används för att godkänna eller avvisa ett utfört städjobb.

### JobDto:

En datastruktur som beskriver ett städjobb, inklusive datum, typ och status.

### JobResponseDTO:

Används för att skicka information om ett jobb, inklusive detaljer om vilka anställda som är involverade.

### JobUserRequest:

Används för att länka en användare (kund eller anställd) till ett specifikt jobb.

### PasswordUpdateRequest:

Används för att uppdatera en användares lösenord.

### UserUpdateRequest:

Används för att uppdatera en kunds personliga och kontaktinformation.

<br/><br/>

# Exception

### CustomerNotFoundException:

Detta undantag uppstår när systemet inte kan hitta en kund som efterfrågas. Till exempel, om du försöker titta på
information om en kund som inte finns i systemet.

### EmployeeNotFoundException:

Detta inträffar när systemet inte hittar en efterfrågad anställd, till exempel vid försök att hämta information om en
anställd som inte finns registrerad.

### JobNotFoundException:

Detta undantag används när ett specifikt jobb eller uppdrag inte kan hittas i systemet.

### NotFoundException:

Ett mer generellt undantag som kan användas för olika typer av data som inte kan hittas.

### PaymentNotFoundException:

Detta undantag inträffar när betalningsinformation saknas, till exempel när systemet försöker hämta information om en
betalning som inte finns.

### SocSecNumberIsTakenException:

Detta undantag uppstår när någon försöker registrera sig med ett personnummer som redan används av en annan användare i
systemet.

### UnauthorizedCallException:

Detta undantag används när någon försöker utföra en åtgärd som de inte har behörighet till, till exempel att ändra
information som de inte har rätt att ändra.

### UsernameIsTakenException:

Liksom med personnummer, detta undantag inträffar när någon försöker registrera ett användarnamn som redan är taget av
någon annan.

### ValidationException:

Detta undantag uppstår när data som matas in inte uppfyller vissa kriterier eller regler som systemet kräver, till
exempel om en e-postadress inte är formaterad korrekt.

<br/><br/>

# Entity/Model

### EmployeeEntity:

Denna entitet representerar en anställd i systemet. Den innehåller information som anställdas ID, förnamn, efternamn,
telefonnummer, roll (t.ex. städare, administratör) och e-postadress. Den har också en lista över jobb (städjobb) som den
anställde är associerad med.

### JobEntity:

Denna entitet representerar ett städjobb. Den innehåller information som jobbets ID, datumet det bokades, vilken kund
som bokade jobbet, vilka anställda som är tilldelade jobbet, jobbtypen (t.ex. grundstädning, fönsterputsning), en
meddelande om jobbet, jobbets status (t.ex. öppet, tilldelat, avslutat) och betalningsinformation relaterad till jobbet.

### PaymentEntity:

Denna entitet representerar betalningsinformation för ett jobb. Den innehåller information som betalnings-ID, datum för
utfärdande och förfallodatum, vilket jobb betalningen gäller, betalningens status (t.ex. fakturerad, betald, förfallen)
och prisbeloppet.

### PrivateCustomerEntity:

Denna entitet representerar en privatkund i systemet. Den innehåller kundens ID, förnamn, efternamn, personnummer,
kundtyp (privat eller företag), gatuadress, postnummer, stad, telefonnummer och e-postadress. Den har också en lista
över jobb som kunden har bokat.

<br/><br/>

# Repository

### CustomerRepository:

Detta repository hanterar operationer relaterade till "PrivateCustomerEntity" (privatkunder). Det inkluderar funktioner
som att kontrollera om en kund finns baserat på e-postadress, hämta en kund baserat på e-postadress eller ID, och så
vidare.

### EmployeeRepository:

Detta repository hanterar operationer relaterade till "EmployeeEntity" (anställda). Det erbjuder funktioner för att
hitta en anställd baserat på e-postadress, hämta alla anställda baserat på deras roll, kontrollera om en anställds
e-postadress redan finns, och liknande.

### JobRepository:

Detta repository hanterar operationer relaterade till "JobEntity" (jobb eller uppdrag). Det inkluderar
funktioner som att hitta jobb baserat på bokningsdatum och jobbtyp, hämta jobb för en specifik kund som inte är
avslutade, hitta jobb för en kund baserat på olika statusar (öppna, avslutade, etc.), och hämta alla jobb för en
specifik anställd.

### PaymentRepository:

Detta repository hanterar operationer relaterade till "PaymentEntity" (betalningar). Det innehåller
funktioner för att hitta betalningar baserat på förfallodatum och betalningsstatus.

<br/><br/>

# Service

### CustomerService:

Den del av applikationen som hanterar olika operationer relaterade till kunder.

#### Skapa kunder:

Metoden create används för att registrera en ny kund. Den validerar kundens inmatade data, såsom
e-postadress och personnummer, och sparar den nya kunden i systemet om all information är giltig.

#### Kundinloggning:

Metoden login hanterar inloggningsprocessen för kunder. Den kontrollerar om kunden finns och
autentiserar dem med hjälp av deras e-postadress och lösenord.

#### Tokenhantering:

Metoderna refresh och logout används för att hantera säkerhetstokens. refresh skapar nya
säkerhetstokens baserat på en befintlig token, medan logout avaktiverar en befintlig token.

#### Lista alla kunder:

Metoden listAllCustomers används för att hämta en lista över alla kunder, men bara om den användare
som begär informationen är en administratör.

#### Uppdatera kundinformation:

Metoderna updateCustomerInfo och updateCustomerAdmin tillåter uppdatering av
kundinformation. Den förstnämnda används av kunder för att uppdatera sin egen information, medan den sistnämnda används
av administratörer för att uppdatera information om andra kunder.

#### Ändra lösenord för kund:

Metoden updateCustomerPassword hanterar byte av lösenord för en kund.

#### Ta bort kunder:

Metoden deleteCustomer används för att ta bort en kunds information från systemet, men bara om
användaren som utför borttagningen är en administratör.

#### Kontaktformulär:

Metoden contactUsForm används för att hantera kontaktformulärsförfrågningar från kunder.

<br/>

### EmployeeService

En tjänst i en webbapplikation som hanterar olika operationer relaterade till anställda.

#### Anställdas inloggning:

Metoden login hanterar inloggningsprocessen för anställda. Den verifierar att en anställd finns
med den angivna e-postadressen och autentiserar sedan med ett lösenord.

#### Tokenhantering:

Liksom med kunder, hanterar refresh och logout säkerhetstokens för anställda. refresh uppdaterar
säkerhetstokens och logout inaktiverar dem.

#### Skapa en anställd:

Metoden createEmployeeRequest används för att registrera en ny anställd. Den validerar inmatade
data och sparar den nya anställda i systemet.

#### Hämta anställdas information:

Metoden getEmployeeById hämtar information om en specifik anställd baserat på deras ID.

#### Hantera anställdas tillgänglighet:

Metoden getAllAvailableEmployees listar alla tillgängliga anställda för ett visst
jobb, baserat på deras tilldelade uppgifter och tillgänglighet.

#### Lista anställda:

Metoderna getAllAdmins, getAllCleaners och getAllCleanersInfo tillhandahåller listor över anställda
baserat på deras roller, som administratörer eller städare.

#### Uppdatera anställdas information:

Metoden updateEmployee låter administratörer uppdatera information om anställda.

#### Ta bort en anställd:

Metoden deleteCleaner används för att ta bort en städare från systemet, men endast om användaren
som utför borttagningen är en administratör.

#### Ändra lösenord för en anställd:

Metoden updateEmployeePassword används för att ändra en anställds lösenord.

### JobService

En tjänst i en webbapplikation som hanterar olika operationer relaterade till jobb eller uppdrag.

#### Skapa jobbförfrågan:

Metoden createJobRequest används för att skapa en ny jobbförfrågan från en kund. Den validerar
förfrågan och sparar informationen om jobbet i systemet. Den kan också integreras med en betalningstjänst (som Klarna)
för att hantera betalningar.

#### Avboka jobb:

Metoden cancelJobRequest låter en kund eller administratör avboka ett jobb. Den kontrollerar att
användaren är behörig att avboka jobbet och uppdaterar sedan systemet för att återspegla avbokningen.

#### Tilldela städare:

Metoden assignCleanerRequest används av administratörer för att tilldela specifika städare till ett
jobb. Den kontrollerar att tilldelningen är giltig och uppdaterar sedan jobbinformationen i systemet.

#### Rapportera utfört städjobb:

Metoden executedCleaningRequest används av städare för att rapportera att ett jobb har
utförts. Detta uppdaterar jobbets status i systemet.

#### Godkänna eller avvisa städning:

Metoden approveDeclineCleaningRequest används av kunder för att godkänna eller avvisa
ett utfört städjobb. Detta uppdaterar jobbets status och kan också utlösa fakturering.

#### Om-utföra misslyckat städjobb:

Metoden reissueFailedCleaningRequest används för att hantera jobb som inte har godkänts
och behöver göras om.

#### Hämta alla jobb:

Metoder som getAllJobs, getAllJobsCleaner och getAllJobsCustomer används för att hämta listor över
jobb, antingen för alla jobb i systemet, specifika jobb för städare, eller jobb bokade av en specifik kund.

#### Radera jobb:

Metoden deleteJob används för att ta bort ett jobb från systemet, vanligtvis av en administratör.

#### Hantera bokningar och bokningshistorik:

Metoder som getBookedCleaningsForCustomer och getBookingHistoryForCustomer
används för att hantera och hämta information om bokningar och bokningshistorik för kunder.

### PaymentService

Denna del hanterar olika aspekter av betalningsprocessen, som fakturering, betalning och
uppföljning av obetalda fakturor.

#### Skapa faktura för jobb:

Metoden createInvoiceOnJob används för att skapa en faktura när ett jobb har genomförts. Den
skapar en betalningspost (faktura) för jobbet, sparar den i systemet och skickar sedan en faktura via e-post.

#### Hämta alla fakturor:

Metoden getAllInvoices används av administratörer för att hämta en lista över alla fakturor.
Detta innebär att endast en administratör kan hämta denna information.

#### Markera faktura som betald:

Metoden markInvoiceAsPaid används av administratörer för att markera en faktura som
betald. Detta uppdaterar fakturans status i systemet och markerar även det associerade jobbet som avslutat.

#### Beräkna priser:

Funktionen solvePrice används för att beräkna priset för ett jobb baserat på jobbtypen. Priset
varierar beroende på typen av städtjänst som erbjuds.

#### Automatisk uppdatering av betalningsstatus:

Metoden updatePaymentStatusForDuePayments, som är schemalagd att köras
automatiskt (t.ex. dagligen vid midnatt), uppdaterar statusen för betalningar som har passerat förfallodagen till "
OVERDUE" (förfallen).

#### Konvertera till Data Transfer Object (DTO):

Metoden toDTO konverterar en betalningsentitet till ett format som kan
överföras och användas av andra delar av applikationen eller skickas till frontend.

#### Radera faktura:

Metoden deleteInvoice används av administratörer för att ta bort en faktura ur systemet.

<br/>

## InputValidation

Detta är en tjänst i webbapplikationen som ansvarar för att validera data som matas in av användare. Denna
tjänst säkerställer att informationen som skickas till systemet är korrekt och uppfyller förväntade kriterier.

### Validera Identifikatorer:

Metoderna validateCustomerId, validateEmployeeId, validateJobId, och validatePaymentId
används för att kontrollera att en specifik kund, anställd, jobb eller betalning finns i systemet. Om det inte finns
någon post som motsvarar det angivna ID:t, kastas ett undantag.

### Validera Inmatningsdata:

Metoden validateInputDataField används för att säkerställa att inmatningsdata för olika
fält (som kund-ID eller jobb-ID) är korrekta och inte saknas.

### Validera Jobbtyp:

Metoden validateJobType används för att se till att en begärd jobbtyp är en av de definierade
typerna (som grundstädning eller fönsterputsning).

### Validera E-postadress och Lösenord:

Metoderna isValidEmailAddress och isValidPassword används för att kontrollera att
en e-postadress och ett lösenord uppfyller vissa kriterier, som längd och innehåll.

### Kontrollera Administratörsroll:

Metoden isAdmin kontrollerar om en anställd med ett visst ID har rollen som
administratör.

<br/>

## MailSenderService

En tjänst i webbapplikationen som hanterar skickande av e-postmeddelanden i olika scenarier. Denna
tjänst används för att automatiskt skicka e-postmeddelanden till kunder och anställda beroende på olika händelser inom
systemet.

### Bekräftelse på Bokat Jobb:

Metoden sendEmailConfirmationBookedJob skickar ett e-postmeddelande till en kund när de har
bokat ett städjobb, för att bekräfta bokningen.

### Bekräftelse på Avbokat Jobb:

Metoden sendEmailConfirmationCanceledJob skickar ett e-postmeddelande till en kund för
att bekräfta att ett städjobb har avbokats.

### Bekräftelse till Städare om Nytt Jobb:

Metoden sendEmailConfirmationOnAssignedJob skickar ett e-postmeddelande till en
städare när de har tilldelats ett nytt städjobb.

### Bekräftelse på Utfört Jobb:

Metoden sendEmailConfirmationExecutedJob skickar ett e-postmeddelande till en kund när en
städare har rapporterat att ett jobb är slutfört.

### Bekräftelse på Godkänt Jobb:

Metoden sendEmailConfirmationApprovedJob skickar ett e-postmeddelande till en kund när de
har godkänt ett utfört städjobb.

### Bekräftelse på Misslyckat Jobb:

Metoden sendEmailConfirmationFailedJob skickar ett e-postmeddelande till en kund när
ett städjobb inte har godkänts.

### Bekräftelse på Omplanerat Jobb:

Metoden sendEmailConfirmationReissuedJob skickar ett e-postmeddelande till en städare
när ett jobb behöver göras om.

### Skicka Faktura:

Metoden sendInvoice skickar en faktura till en kund för ett utfört städjobb.

### Bekräftelse på Betalad Faktura:

Metoden sendEmailConfirmationOnPaidInvoice skickar ett e-postmeddelande till en kund
för att bekräfta att en faktura har betalats.

### Bekräftelse på Mottaget Meddelande:

Metoden sendEmailConfirmationMessageReceived skickar ett e-postmeddelande till en
person som har kontaktat företaget via ett kontaktformulär.

<br/><br/>

# Keycloak

KeycloakAPI är en tjänst i webbapplikationen som används för att hantera användarautentisering och auktorisering med
hjälp av Keycloak, en öppen källkodsidentitets- och åtkomsthanteringslösning. Denna tjänst kommunicerar med
Keycloak-servern för att utföra olika uppgifter relaterade till användarkonton.

### Hantera Användarroller och Åtkomst:

KeycloakAPI tillhandahåller funktioner för att bestämma och tilldela roller till
användare (som kunder, städare, eller administratörer) och hantera deras åtkomst till olika delar av systemet.

### Användarregistrering och -uppdatering:

Metoderna addCustomerKeycloak och addEmployeeKeycloak används för att
registrera nya användare i Keycloak, medan updateCustomerKeycloak och updateEmployeeKeycloak används för att uppdatera
befintliga användares information.

### Autentisering och Tokenhantering:

Metoderna loginKeycloak och refreshToken hanterar användarautentisering och förnya
säkerhetstokens. logoutKeycloak används för att logga ut användare och avaktivera deras tokens.

### Lösenordshantering:

Metoden changePasswordKeycloak används för att ändra en användares lösenord.

### Användarborttagning:

Metoden deleteUserKeycloak används för att ta bort användare från Keycloak.

### Hämta Användarinformation:

KeycloakAPI kan också hämta information om användare från Keycloak genom metoder som
getKeycloakUserEntities och getKeycloakRoleEntities.

### Följande klasser är delar av den kodstruktur som används för att interagera med Keycloak, de används för att representera olika typer av data som är nödvändiga för autentisering och användarhantering.

### Credentials: Representerar autentiseringsuppgifter för en användare. Den innehåller information som typ av

autentiseringsuppgift (till exempel ett lösenord), själva värdet på uppgiften och om det är temporärt eller inte.

### NewUserEntity:

Används för att representera en ny användare i systemet. Den innehåller användarinformation som
e-postadress, förnamn, efternamn och användarnamn, samt användarens autentiseringsuppgifter.

### KeycloakRoleAssignmentEntity:

Används för att tilldela roller till användare i Keycloak. Den innehåller information om
rollens ID och namn.

### KeycloakRoleEntity:

Representerar en roll i Keycloak. Den innehåller information om rollen, som ID, namn, beskrivning,
och om rollen är sammansatt av andra roller eller specifik för en klient.

### KeycloakTokenEntity:

Används för att hantera säkerhetstokens i Keycloak. Den innehåller information om access-token
och refresh-token, samt andra relaterade data som token-typ och giltighetstid.

### KeycloakAccess:

Innehåller information om vilka åtkomsträttigheter en användare har i Keycloak, till exempel om
användaren kan hantera gruppmedlemskap eller roller.

### KeycloakUserEntity:

Representerar en användare i Keycloak. Den innehåller detaljerad information om användaren, som
ID, skapandetid, användarnamn, om kontot är aktivt, e-postverifiering och användarens namn.

<br/><br/>

# Klarna

KlarnaAPI är en tjänst i webbapplikationen som används för att integrera med Klarnas betalningssystem. Klarna är en
populär betalningslösning som tillåter kunder att betala för produkter och tjänster på olika sätt, inklusive faktura och
delbetalning.

### Kommunikation med Klarna:

KlarnaAPI använder en RestTemplate för att skicka förfrågningar till Klarnas API.

### Skapa Beställning hos Klarna:

Metoden createOrder används för att skapa en ny beställning hos Klarna. Den tar en
jobbtyp (till exempel "BASIC" för grundstädning) och skapar en förfrågan som skickas till Klarna för att initiera
betalningsprocessen.

### Hantera olika typer av städjobb:

Inuti createOrder finns det olika fall för olika typer av städjobb, som varje
genererar en anpassad beställningsförfrågan. Till exempel kan det skilja sig åt i pris mellan en grundstädning och en
fönsterputsning.

### Sammansätta en Orderförfrågan:

För varje typ av städjobb skapas en KlarnaCreateOrderRequest som innehåller detaljer om
produkten eller tjänsten som köps, inklusive priset, skattesatsen och totalbeloppet.

### Sända Beställningsförfrågan till Klarna:

Denna förfrågan skickas sedan till Klarna, och som svar får man en KlarnaCreateOrderResponse som innehåller HTML-kod för
Klarnas checkout-snippet. Detta snippet kan sedan användas på webbplatsen för att visa Klarnas betalningsgränssnitt.

### Hantera Svar från Klarna:

Svaret från Klarna, representerat av KlarnaCreateOrderResponse, innehåller vanligtvis en
HTML-snutt som kan integreras i webbplatsen för att låta användaren genomföra betalningen.