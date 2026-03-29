# Till Messager Pro - Beta v6 Final

## Verbunden und reworked
- Firebase Google-Services-Plugin eingebaut
- `app/google-services.json` eingefügt
- Firebase Auth, Firestore und Analytics über BoM `34.11.0`
- Registrierung mit E-Mail-Verifizierung
- Admin-Erkennung für `tillscheidegget@gmail.com`
- Automatischer Lifetime-Pro-Status für den Admin
- Kontakt hinzufügen über Firestore `users/{uid}/contacts/{contactUid}`
- Reworked Login- und Home-Design
- Version erhöht auf `6.0-beta` / `versionCode 6`

## Projektstatus
Diese Beta v6 ist jetzt auf Firebase vorbereitet und lokal bereits verbunden. Nach dem Gradle-Sync kann die App mit echter Registrierung und Firestore-Daten arbeiten.

## Vor dem Testen
1. In Firebase Authentication E-Mail/Passwort aktivieren.
2. Firestore aktivieren.
3. Android Studio Gradle Sync ausführen.
4. App starten und Registrierung testen.

## GitHub-Hinweis
Für ein öffentliches Repository solltest du `app/google-services.json` später wieder aus dem Commit entfernen und nur lokal behalten.
