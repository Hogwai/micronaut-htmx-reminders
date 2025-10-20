# Micronaut Reminder Application

Reminder application with real-time notifications using Micronaut, HTMX, WebSocket and a bit of JavaScript.

## Technical stack

- **Backend**: Micronaut 4 (Java 21)
- **Frontend**: Htmx + minimal JavaScript
- **Database**: H2
- **WebSocket**: Real-time notifications
- **Cron**: cron-utils for recurring alerts

## Prerequisites
- Java 21
- Gradle

## Running the app
1. Launch the app

```bash
./gradlew run
```

2. Open [http://localhost:8080/alerts](http://localhost:8080/alerts)
3. You must grant the notification permission to the app: [Firefox](https://support.mozilla.org/en-US/kb/push-notifications-firefox), [Chrome](https://support.google.com/chrome/answer/3220216?hl=en-en)
