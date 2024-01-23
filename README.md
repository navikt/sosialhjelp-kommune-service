# sosialhjelp-kommune-service
Ktor GraphQL service som tilbyr informasjon om kommuner

## Henvendelser
Spørsmål knyttet til koden eller teamet kan stilles til teamdigisos@nav.no.

### For NAV-ansatte
Interne henvendelser kan sendes via Slack i kanalen #team_digisos.

## Teknologi
* Kotlin
* JDK 21
* Gradle
* Ktor
* GraphQL (KGraphQL)

### Krav
- JDK 21

### Manuell deploy til dev
Gjøres via GitHub Actions, se: https://github.com/navikt/sosialhjelp-kommune-service/actions/workflows/deploy_dev.yml

## Oppsett av nytt prosjekt
Dette prosjektet bygger og deployer vha Github Actions

### GitHub package registry
- Docker image pushes til github package registry, eks [https://github.com/navikt/sosialhjelp-kommune-service/packages/](https://github.com/navikt/sosialhjelp-kommune-service/packages/)

### GitHub Actions
- Docker image bygges og deployes til alle miljøer ved push til main => `.github/workflows/main.yml`
- Deploy til dev => `.github/workflows/deploy_dev.yml`
- Autodeploy til prod-fss fra master => `.github/workflows/deploy_prod.yml`

### GitHub deployment
- Deployments vises [her](https://github.com/navikt/sosialhjelp-kommune-service/deployments)

## Lokal kjøring
#### *uten* integrasjon til Fiks, dvs mot mock-alt
Kjør med miljøvariabel: `ENV=LOCAL`. Dette er også default. Da dropper vi auth.
#### *med* integrasjon til Fiks
Kjør med miljøvariabel: `ENV=PROD` eller `ENV=DEV`

Da må følgende env-variabler settes (hentes fra kubernetes secrets): \
`INTEGRASJONPASSORD_FIKS`, `INTEGRASJONSID_FIKS` og `TESTBRUKER_NATALIE`.

## Hvordan komme i gang
### [Felles dokumentasjon for våre backend apper](https://github.com/navikt/digisos/blob/main/oppsett-devmiljo.md#backend-gradle)
