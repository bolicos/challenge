# Communication Preference API

API REST para gerenciamento de preferencias de canal de comunicacao de clientes, incluindo seus e-mails associados.

Este projeto foi desenvolvido como challenge backend Java, usando Spring Boot, DDD leve, persistencia relacional e publicacao de eventos apos alteracoes de dados.

## Stack

- Java 21
- Spring Boot 3.x
- Spring Webmvc
- Spring Data JPA
- Bean Validation
- Flyway
- PostgreSQL
- Kafka/Redpanda
- OpenAPI/Swagger
- Actuator
- Micrometer/Prometheus
- Logback com logs estruturados
- Gradle
- Docker Compose

## Arquitetura

O projeto segue uma organizacao em camadas com DDD leve e portas/adapters:

```text
application
  event
  model
  port
    in
    out
  service

domain
  model
  exception

infrastructure
  messaging
    dto
    kafka
  persistence
    adapter
    entity
    mapper
    repository
  web
    controller
    dto
    mapper
    exception
```

Decisoes principais:

- O dominio nao depende de Spring, JPA, Kafka ou DTOs.
- DTOs ficam na borda web/API.
- Mappers manuais ficam nas bordas, nao no dominio.
- A application layer depende de interfaces/ports.
- A infra implementa as portas de persistencia e mensageria.
- Eventos Kafka sao publicados somente apos commit da transacao.
- `X-Correlation-Id` e propagado nos logs HTTP e no header da mensagem Kafka.

## Endpoints

Base path:

```text
/api/*
```

| Metodo | Rota | Descricao |
|---|---|---|
| POST | `/api/preferencias` | Cria uma preferencia |
| GET | `/api/preferencias/{id}` | Busca por ID |
| PUT | `/api/preferencias/{id}` | Atualiza toda a preferencia |
| DELETE | `/api/preferencias/{id}` | Remove por ID |
| GET | `/api/preferencias` | Lista todas |

## Validacoes

- `preferenciaCanalComunicacao` e obrigatorio.
- `emails` pode ser vazio ou omitido.
- Quando enviado, cada e-mail deve ter:
  - `email` valido
  - `tipo`
  - `verificado`
- `dataCriacao`, `dataAtualizacao`, `criadoPor` e `alteradoPor` sao controlados pelo backend.

Valores aceitos:

```text
preferenciaCanalComunicacao: SMS, EMAIL, WHATSAPP, TELEFONE, RESIDENCIAL
tipo: PESSOAL, COMERCIAL
```

## Como Rodar

Subir infraestrutura:

```bash
docker compose up -d postgres kafka kafka-ui
```

Rodar aplicacao local:

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

Rodar testes:

```bash
./gradlew check
```

Ou apenas pelo Docker (Gerando a imagem e subindo o container do challenge):

```bash
docker compose up -d --build
```

Por padrao, a imagem da API e criada como:

```text
challenge-api:local
```

Para gerar uma tag versionada:

```bash
APP_VERSION=1.0.0 docker compose build api
APP_VERSION=1.0.0 docker compose up -d
```

Nesse caso, a imagem gerada sera:

```text
challenge-api:1.0.0
```

URLs uteis:

```text
API: http://localhost:8080
Swagger: http://localhost:8080/swagger-ui/index.html
Actuator health: http://localhost:8080/actuator/health
Kafka UI: http://localhost:8085
```

## cURL

(Opcional) : Defina um correlation id para rastrear logs e evento Kafka:

```bash
CORRELATION_ID="test-$(date +%s)"
```

### Criar Preferencia

```bash
curl -i -X POST "http://localhost:8080/api/preferencias" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: ${CORRELATION_ID}" \
  -d '{
    "preferenciaCanalComunicacao": "EMAIL",
    "emails": [
      {
        "email": "cliente@example.com",
        "tipo": "PESSOAL",
        "verificado": false
      }
    ]
  }'
```

Copie o `id` retornado para usar nos proximos comandos.

### Buscar Por ID

```bash
curl -i -X GET "http://localhost:8080/api/preferencias/{id}" \
  -H "X-Correlation-Id: ${CORRELATION_ID}"
```

### Listar Todas

```bash
curl -i -X GET "http://localhost:8080/api/preferencias" \
  -H "X-Correlation-Id: ${CORRELATION_ID}"
```

### Atualizar Preferencia

```bash
curl -i -X PUT "http://localhost:8080/api/preferencias/{id}" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: ${CORRELATION_ID}" \
  -d '{
    "preferenciaCanalComunicacao": "WHATSAPP",
    "emails": [
      {
        "email": "cliente.novo@example.com",
        "tipo": "COMERCIAL",
        "verificado": true
      }
    ]
  }'
```

### Deletar Preferencia

```bash
curl -i -X DELETE "http://localhost:8080/api/preferencias/{id}" \
  -H "X-Correlation-Id: ${CORRELATION_ID}"
```

## Eventos Kafka

Apos alteracoes de dados, a API publica eventos no topico:

```text
communication-preference-events
```

Tipos de evento:

```text
PREFERENCE_CREATED
PREFERENCE_UPDATED
PREFERENCE_DELETED
```

Headers enviados:

```text
eventId
eventType
X-Correlation-Id
```

O payload inclui:

- `eventId`
- `eventType`
- `occurredAt`
- dados da preferencia
- dados de auditoria

As datas sao serializadas como string ISO-8601.

Em ambiente local, existe um consumer demonstrativo para o mesmo topico, habilitado por:

```properties
challenge.kafka.consumers.preference-event-logger.enabled=true
```

Esse consumer apenas registra o evento no log da aplicacao para facilitar validacao manual do fluxo.

## Observabilidade

Cada requisicao recebe ou gera um `X-Correlation-Id`.

Esse valor e:

- retornado no header HTTP da resposta
- colocado no MDC dos logs
- propagado para o header Kafka
- registrado nos logs de publicacao de evento

Isso permite rastrear o fluxo completo:

```text
HTTP request -> service -> database -> Kafka event
```

## Decisoes E Tradeoffs

- DDD leve: o projeto separa dominio, application e infrastructure sem adicionar complexidade desnecessaria para um CRUD. A ideia foi proteger o dominio de frameworks e deixar as bordas responsaveis por HTTP, banco e Kafka.
- Ports and adapters: a application layer depende de interfaces (`PreferenceUseCase`, `PreferencePersistencePort`, `PreferenceEventPublisher`). Isso permite trocar persistencia, mensageria ou transporte HTTP sem mudar regra de negocio.
- Web como infraestrutura: controllers, DTOs e mappers HTTP ficam em `infrastructure.web`, porque REST e apenas um adapter de entrada para o caso de uso.
- Mappers manuais: foram mantidos para simplicidade e controle explicito. MapStruct pode ser introduzido depois, principalmente para DTOs e views. A reconciliacao da colecao JPA de e-mails deve continuar como regra customizada para preservar IDs e auditoria.
- Eventos Kafka apos commit: os eventos sao registrados para publicacao apos a transacao confirmar. Isso evita publicar evento de uma alteracao que sofreu rollback.
- Sem transactional outbox: o projeto loga falhas de envio Kafka, mas nao persiste eventos pendentes. Em um sistema produtivo com maior criticidade, o proximo passo seria implementar o padrao Outbox.
- Consumer Kafka demonstrativo: existe um consumer local para logar eventos do proprio topico e facilitar validacao manual. Ele fica desabilitado por padrao e habilitado no profile `local`.
- Observabilidade: `X-Correlation-Id` e gerado ou reaproveitado em cada request, propagado para Kafka e registrado nos logs. Isso ajuda a rastrear o fluxo completo em ferramentas como OpenSearch, Loki, Datadog ou similares.
- Serializacao: datas sao serializadas como string ISO-8601, evitando arrays de data/hora no JSON. A configuracao fica centralizada em `JacksonConfiguration`.
- CORS: `WebConfiguration` centraliza a configuracao de CORS para `/api/**`, expondo `X-Correlation-Id` e `Location` para clientes HTTP.
- `customerId` ainda nao esta no payload do challenge. Por enquanto, a application layer gera um UUID quando ausente. Em uma aplicacao real, esse valor deveria vir do payload, path param ou usuario autenticado.
- Docker image tag: o Compose usa `challenge-api:${APP_VERSION:-local}` para evitar imagens sem tag (`<none>`) e permitir versoes explicitas em build local ou CI.
