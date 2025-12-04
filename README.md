# TSIKT_GroupApp

Aplikácia TSIKT GroupApp je desktopový nástroj pre správu študentských (alebo tímových) skupín, úloh a termínov s podporou notifikácií v reálnom čase. Slúži ako jednoduchý intranet pre malý tím: umožňuje organizovať skupiny, pridávať úlohy, sledovať postup prác, zdieľať príspevky vo feede a plánovať udalosti v kalendári.

# Architektúra systému
Aplikácia je rozdelená na dve časti:

# JavaFX klient (Test_1):
- zobrazuje login/registráciu, hlavné menu, feed skupiny, úlohy, logy úloh, kalendár a profil používateľa,
- komunikuje so serverom cez REST API (java.net.http.HttpClient) a odoberá notifikácie cez WebSocket (NotificationWebSocketClient).
- 
# Spring Boot server (org.example.test_1_server):
- poskytuje REST API pre používateľov, skupiny, feed, úlohy, logy a kalendár (UserController, GroupController, FeedController, JobController, CalendarController),
- používa SQLite databázu (DBManager) a JDBC,
- uchováva heslá bezpečne zahashované pomocou BCryptPasswordEncoder (Spring Security Crypto),
- posiela notifikácie klientom cez WebSocket endpoint /ws-notify (konfigurácia SimpleWebSocketConfig, handler SimpleNotificationHandler).

# Z pohľadu vrstiev:

1) Prezentačná vrstva: JavaFX scény (LoginScreen, MainMenu, MojeSkupinyPane, MojePracePane, CalendarPane, ProfileScreen, komponent NotificationBell).
2) Klientská servisná vrstva: UserManager, ktorý zapuzdruje HTTP požiadavky a ručne parsuje JSON odpovede.
3) Serverová servisná vrstva: triedy UserService, GroupService, FeedService, JobService, CalendarService, ktoré pracujú s JDBC a mapujú výsledky na DTO.
4) Perzistentná vrstva: SQLite databáza users.db, vytváraná v DBManager.createTables().​

# Databázový model
Databáza SQLite sa vytvára pri štarte servera v triede DBManager. Hlavné tabuľky:
## users
- id (INTEGER PRIMARY KEY AUTOINCREMENT) – interný identifikátor používateľa,
- username (TEXT UNIQUE NOT NULL) – prihlasovacie meno,
- password (TEXT NOT NULL) – heslo zahashované algoritmom BCrypt,
- name (TEXT) – zobrazované meno,
- email (TEXT) – kontakt,
- age (INTEGER) – vek,
- photo (TEXT) – cesta k profilovej fotke na klientovi.

## groups
- id (INTEGER PRIMARY KEY AUTOINCREMENT),
- name (TEXT NOT NULL),
- owner (TEXT NOT NULL) – username vlastníka skupiny.

## group_members
- group_id (INTEGER),
- user_id (INTEGER),
- primárny kľúč (group_id, user_id) – každý používateľ môže byť v skupine len raz.
- 
## feed_messages – správy vo feede skupín (text + voliteľný PDF súbor).
## jobs – úlohy s názvom, popisom, stavom, priradením používateľovi a skupine.
## job_logs – záznamy práce na úlohách (popis odvedenej práce, commit správy, príloha).
## calendar_events – udalosti a termíny pre jednotlivé skupiny (dátum, názov, popis, farba, príznak notifikácie).

# Dokumentácia REST API a WebSocket endpointov
Server poskytuje jednoduché REST API
## Používatelia

- POST /api/users/register
  -Body: {"username": "...", "password": "...", "name": "...", "email": "...", "age": 0, "photoPath": "..."}
  -Funkcia: registrácia nového používateľa, heslo sa hashuje pomocou BCrypt na strane servera.


- POST /api/users/login
  -Body: {"username": "...", "password": "..."}
  -Funkcia: overenie prihlasovacích údajov (BCryptPasswordEncoder.matches).

- GET /api/users/find?value=...
  -Hľadá používateľa podľa username, e‑mailu alebo ID.

- POST /api/users/save
  -Ukladá zmeny profilu (meno, email, vek, foto).
