# Communication Preference API

API REST para gerenciamento de preferencias de canal de comunicacao de clientes, incluindo seus e-mails associados.

Este projeto foi desenvolvido como challenge backend Java, usando Spring Boot, DDD leve, persistencia relacional e publicacao de eventos apos alteracoes de dados.

## Stack

- Java 21
- Spring Boot 3.x
- Spring Webmvc
- Spring Batch
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
  batch
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

shared
  util
```

Decisoes principais:

- O dominio nao depende de Spring, JPA, Kafka ou DTOs.
- DTOs ficam na borda web/API.
- Mappers manuais ficam nas bordas, nao no dominio.
- A application layer depende de interfaces/ports.
- A infra implementa as portas de persistencia e mensageria.
- A importacao batch tem controller e use case proprios para manter o CRUD mais simples.
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
| POST | `/api/preferencias/importacao` | Importa preferencias em lote |
| POST | `/api/preferencias/importacao/csv` | Importa preferencias por arquivo CSV usando Spring Batch |
| GET | `/api/preferencias/resumo` | Lista um resumo baseado em view SQL |

## Validacoes

- `preferenciaCanalComunicacao` e obrigatorio.
- `customerId` identifica o cliente dono da preferencia. Quando omitido, o backend gera um UUID para manter compatibilidade com o payload original do challenge.
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
    "customerId": "11111111-1111-1111-1111-111111111111",
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
    "customerId": "11111111-1111-1111-1111-111111111111",
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

### Importar Preferencias Em Lote

```bash
curl -i -X POST "http://localhost:8080/api/preferencias/importacao" \
  -H "Content-Type: application/json" \
  -H "X-Correlation-Id: ${CORRELATION_ID}" \
  -d '{
    "preferencias": [
      {
        "customerId": "22222222-2222-2222-2222-222222222222",
        "preferenciaCanalComunicacao": "EMAIL",
        "emails": [
          {
            "email": "cliente.batch.1@example.com",
            "tipo": "PESSOAL",
            "verificado": false
          }
        ]
      },
      {
        "customerId": "33333333-3333-3333-3333-333333333333",
        "preferenciaCanalComunicacao": "WHATSAPP",
        "emails": [
          {
            "email": "cliente.batch.2@example.com",
            "tipo": "COMERCIAL",
            "verificado": true
          }
        ]
      }
    ]
  }'
```

### Consultar Resumo

```bash
curl -i -X GET "http://localhost:8080/api/preferencias/resumo" \
  -H "X-Correlation-Id: ${CORRELATION_ID}"
```

O resumo e consultado a partir da view SQL `vw_communication_preference_summary`.

### Importar CSV Com Spring Batch

Formato esperado do CSV:

```csv
customerId,preferenciaCanalComunicacao,email,tipo,verificado
11111111-1111-1111-1111-111111111111,EMAIL,cliente.csv.1@example.com,PESSOAL,false
22222222-2222-2222-2222-222222222222,WHATSAPP,cliente.csv.2@example.com,COMERCIAL,true
```

Chamada:

```bash
curl -i -X POST "http://localhost:8080/api/preferencias/importacao/csv" \
  -H "X-Correlation-Id: ${CORRELATION_ID}" \
  -F "file=@samples/preferencias.csv;type=text/csv"
```

O endpoint dispara o job `preferenceCsvImportJob` e retorna o status da execucao. O arquivo `samples/preferencias.csv` fica no repositorio como exemplo pronto para teste manual.

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

Configuracoes aplicadas no Kafka producer:

- `acks=all`
- `retries=10`
- `linger.ms=10`
- `batch.size=32768`
- `compression.type=lz4`
- `enable.idempotence=true`
- `request.timeout.ms=30000`
- `delivery.timeout.ms=120000`
- `max.in.flight.requests.per.connection=5`

Configuracoes aplicadas no consumer demonstrativo:

- `ack-mode=manual`
- `enable-auto-commit=false`
- offset confirmado somente apos processamento/log do evento

Metricas customizadas expostas via Actuator/Prometheus:

```text
challenge.kafka.preference.events.publish.attempt
challenge.kafka.preference.events.publish.success
challenge.kafka.preference.events.publish.failure
challenge.batch.preference.csv.import.attempt
challenge.batch.preference.csv.import.success
challenge.batch.preference.csv.import.failure
challenge.batch.preference.csv.import.duration
```

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
- `customerId` foi adicionado ao payload como extensao do challenge, porque a preferencia pertence a um cliente. Ele permanece opcional para preservar compatibilidade com o exemplo original; quando ausente, a application layer gera um UUID.
- Docker image tag: o Compose usa `challenge-api:${APP_VERSION:-local}` para evitar imagens sem tag (`<none>`) e permitir versoes explicitas em build local ou CI.
- Flyway local: o profile `local` tambem usa Flyway e `ddl-auto=validate`. Isso evita divergencia entre ambiente local e runtime em container, mantendo o schema sob controle das migrations.
- PostgreSQL vs Oracle: Oracle Free foi considerado, mas a imagem Docker e mais pesada para um challenge. PostgreSQL foi escolhido por ser leve, simples de subir localmente e oferecer recursos SQL avancados. Para cenarios Oracle, as migrations poderiam ser adaptadas para sequences, packages, procedures e PL/SQL. No PostgreSQL, o equivalente procedural seria PL/pgSQL.
- View SQL: a migration `V3__create_communication_preference_summary_view.sql` cria a view `vw_communication_preference_summary`, usada pelo endpoint `/api/preferencias/resumo`. Isso demonstra uma abordagem de read model baseada em banco, comum em sistemas com consultas consolidadas.
- Batch import: o endpoint `/api/preferencias/importacao` permite processar uma lista de preferencias em lote. A implementacao usa controller e use case proprios para separar importacao do CRUD transacional principal.
- Spring Batch CSV: o endpoint `/api/preferencias/importacao/csv` demonstra um fluxo mais proximo de cenarios corporativos de importacao por arquivo. O job le o CSV em chunks, transforma linhas em objetos de dominio e reaproveita o use case de importacao.
- Metricas de batch: a importacao CSV registra metricas de tentativa, sucesso, falha e duracao com tag `job.name`, permitindo acompanhar execucoes no Prometheus.
- Flyway PostgreSQL module: o projeto inclui `flyway-database-postgresql`, necessario em versoes recentes do Flyway para reconhecer corretamente bancos PostgreSQL modernos.
- Outbox pattern: nao foi implementado nesta versao. Em um sistema produtivo, seria uma melhoria importante para garantir publicacao confiavel de eventos. A ideia e salvar o evento em uma tabela `outbox_events` na mesma transacao do dado principal; depois, um worker/batch publica eventos pendentes no Kafka e marca cada item como publicado. Isso evita perder eventos quando o banco confirma a alteracao, mas o Kafka esta temporariamente indisponivel.
