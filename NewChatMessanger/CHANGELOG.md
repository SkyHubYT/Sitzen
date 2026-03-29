# Till Messager V2

## Großes Update
- Paketname auf `till.messager` vereinheitlicht
- Projekt komplett auf stabile Kotlin-DSL-Konfiguration bereinigt
- neue App-Architektur mit separaten Schichten für Auth, Kontakte und Chats
- Firebase-Repositorys für echte E-Mail-Logins, Kontakte und Direct Chats
- Demo-Fallback integriert, damit die App auch ohne `google-services.json` startet
- Start/Login/Home/Contacts/Chats/Privacy/Chat Screens neu aufgebaut
- sichere Session-Speicherung mit `EncryptedSharedPreferences`
- `FLAG_SECURE`, Backup-Off und kein Cleartext-Traffic aktiv
- Privacy Center ergänzt

## Ehrlich wichtig
- Diese V2 ist eine starke Messenger-Basis mit echter Login-/Kontakt-/Chat-Architektur.
- Für echte Ende-zu-Ende-Sicherheit auf WhatsApp-/Signal-Niveau fehlt noch ein sauberer Schlüsselaustausch, Geräte-Verifikation und Multi-Device-Sync.
