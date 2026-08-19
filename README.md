# API de Clientes com Padrões de Projeto

Evolução do laboratório da DIO sobre padrões de projeto com Java e Spring Boot. A API cadastra clientes e resolve seus endereços pelo CEP usando o ViaCEP, com preenchimento manual como alternativa.

## O que o projeto faz

- Cadastra, consulta, atualiza e remove clientes.
- Reutiliza endereços já persistidos pelo CEP.
- Consulta a API pública do ViaCEP quando o CEP ainda não está salvo.
- Permite informar endereço manual quando o ViaCEP não encontra o CEP ou está indisponível.
- Valida os dados obrigatórios no cadastro.
- Retorna erros HTTP claros para dados inválidos e endereço manual incompleto.

## Tecnologias

- Java 11+
- Spring Boot 2.5.4
- Spring Web e Bean Validation
- Spring Data JPA e H2
- OpenFeign para integração com ViaCEP
- Springdoc OpenAPI / Swagger
- JUnit 5, Mockito e MockMvc

## Como executar

Pré-requisito: JDK 11 ou superior configurado.

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação inicia em `http://localhost:8080`.

Para executar os testes:

```powershell
.\mvnw.cmd test
```

## Documentação interativa

Com a aplicação em execução, acesse o Swagger em:

`http://localhost:8080/swagger-ui.html`

## Endpoints

| Método | Rota | Descrição | Resposta principal |
| --- | --- | --- | --- |
| `GET` | `/clientes` | Lista todos os clientes | `200 OK` |
| `GET` | `/clientes/{id}` | Busca um cliente pelo id | `200 OK` |
| `POST` | `/clientes` | Cadastra um cliente e resolve seu endereço | `201 Created` |
| `PUT` | `/clientes/{id}` | Atualiza um cliente | `200 OK` |
| `DELETE` | `/clientes/{id}` | Remove um cliente | `200 OK` |

### Cadastro com consulta automática de CEP

```http
POST /clientes
Content-Type: application/json
```

```json
{
  "nome": "Miguel",
  "endereco": {
    "cep": "01001-000"
  }
}
```

Quando o CEP é encontrado, a API usa o endereço retornado pelo ViaCEP e o salva no H2.

### Cadastro com endereço manual

Se o CEP não estiver disponível no ViaCEP, envie os campos manuais obrigatórios:

```json
{
  "nome": "Miguel",
  "endereco": {
    "cep": "00000-000",
    "logradouro": "Rua Manual",
    "complemento": "Casa 10",
    "bairro": "Centro",
    "localidade": "Recife",
    "uf": "PE"
  }
}
```

Os campos `logradouro`, `bairro`, `localidade` e `uf` são exigidos apenas nesse fallback. Caso estejam ausentes, a API retorna `422 Unprocessable Entity`.

### Erros do cadastro

| Situação | Status | Exemplo de retorno |
| --- | --- | --- |
| Nome, endereço ou CEP ausente | `400 Bad Request` | `{ "status": 400, "erros": { "nome": "não deve estar em branco" } }` |
| CEP não encontrado e endereço manual incompleto | `422 Unprocessable Entity` | `{ "status": 422, "mensagem": "CEP não encontrado..." }` |

## Fluxo de endereço

```text
CEP recebido
  → endereço já existe no H2? usa-o
  → não existe? consulta ViaCEP
  → ViaCEP encontrou? persiste e usa-o
  → ViaCEP não encontrou? valida endereço manual
  → manual completo? persiste e usa-o
  → manual incompleto? responde 422
```

## Padrões aplicados

| Padrão | Onde está | Problema que resolve |
| --- | --- | --- |
| Singleton | Beans Spring como `ClienteServiceImpl`, repositories e controller | O container Spring controla uma instância compartilhada de cada componente. |
| Strategy / interface de serviço | `ClienteService` e `ClienteServiceImpl` | O controller depende de uma abstração; outra implementação pode ser trocada sem alterar quem a consome. |
| Repository | `ClienteRepository` e `EnderecoRepository` | Separa a regra de negócio do acesso ao banco de dados. |
| Facade | `ClienteServiceImpl` no fluxo de cadastro | Esconde a coordenação entre banco H2, ViaCEP, fallback manual e persistência do cliente. |

> A interface de serviço abre espaço para Strategy. Neste projeto existe uma implementação (`ClienteServiceImpl`); estratégias alternativas podem ser adicionadas quando houver regras distintas de cadastro.

## Testes

O projeto possui testes para:

- criação de cliente com endereço retornado pelo ViaCEP;
- fallback para endereço manual;
- bloqueio de cadastro sem endereço manual completo;
- retorno HTTP `201` no cadastro válido;
- retorno `400` para DTO inválido;
- retorno `422` para endereço manual incompleto.

## Limitações atuais

- O banco H2 é em memória: os dados são perdidos quando a aplicação é encerrada.
- A atualização (`PUT`) permanece no formato do laboratório-base e recebe a entidade `Cliente` diretamente.
- As buscas por id inexistente ainda não possuem resposta `404` personalizada.

## Créditos

Projeto baseado no laboratório [Explorando Padrões de Projetos na Prática com Java](https://github.com/digitalinnovationone/lab-padroes-projeto-spring), da [Digital Innovation One (DIO)](https://www.dio.me/). A evolução deste repositório adiciona um fluxo de cadastro por CEP com validação, fallback manual, tratamento de erros e testes.
