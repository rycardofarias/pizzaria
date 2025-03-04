# Pizzaria API 🍕
Bem-vindo à **API Pizzaria**, que fornece endpoints para gerenciar pizzas, incluindo criação, listagem e seus atributos, como ingredientes, tamanhos e preços. Esta API segue princípios RESTful e está protegida por autenticação Bearer.

---

## **Instalação**

1. Clone o repositório:

   ```bash
   git clone https://github.com/sua-conta/pizzaria-api.git
   ```
   
2. Navegue até a pasta do projeto:

   ```bash
   cd pizzaria-api
   ```

3. Configure o arquivo `application.properties` ou `application.yml` para incluir variáveis, como conexão com o banco de dados e configurações adicionais.

4. Compile e execute o projeto:

   ```bash
   ./mvnw spring-boot:run
   ```

5. **Usando Docker:**

   ```bash
   docker-compose up -d
   ```

---

## **Autenticação**

Para acessar os endpoints protegidos, você precisará de um token JWT. Para obter o token:

`POST /api/auth/login`

**Exemplo de Requisição:**

```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Exemplo de Resposta:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

Use o token retornado no cabeçalho `Authorization`:

```
Authorization: Bearer <seu-token>
```

---

## **Endpoints**

### **Criação de Pizzas**

`POST /api/pizzas`

Este endpoint permite criar uma nova pizza no sistema.

#### Exemplo de Requisição

```http
POST /api/pizzas HTTP/1.1
Content-Type: application/json
```

**Corpo da Requisição:**

```json
{
  "name": "Pepperoni",
  "description": "Deliciosa pizza de pepperoni com queijo",
  "size": "LARGE",
  "ingredientIds": [1, 2, 3]
}
```

- **`name`** (obrigatório): Nome da pizza.
- **`description`**: Descrição opcional da pizza.
- **`size`** (obrigatório): O tamanho da pizza. Valores possíveis:
  - `SMALL`
  - `MEDIUM`
  - `LARGE`
- **`ingredientIds`** (obrigatório): Lista contendo o ID dos ingredientes que compõem a pizza. Deve possuir pelo menos 1 item.

#### Exemplo de Resposta

**Status 200 (Pizza criada com sucesso):**

```json
{
  "id": 1,
  "name": "Pepperoni",
  "description": "Deliciosa pizza de pepperoni com queijo",
  "size": "LARGE",
  "price": 35.00,
  "active": true,
  "ingredients": [
    {
      "id": 1,
      "name": "Tomate"
    },
    {
      "id": 2,
      "name": "Queijo"
    },
    {
      "id": 3,
      "name": "Pepperoni"
    }
  ]
}
```

---

### **Listagem de Pizzas**

`GET /api/pizzas`

Este endpoint retorna uma lista de todas as pizzas armazenadas no sistema.

#### Exemplo de Requisição

```http
GET /api/pizzas HTTP/1.1
Authorization: Bearer <seu-token-aqui>
```

#### Exemplo de Resposta

**Status 200 (Lista de Pizzas):**

```json
[
  {
    "id": 1,
    "name": "Pepperoni",
    "description": "Deliciosa pizza de pepperoni com queijo",
    "size": "LARGE",
    "price": 35.00,
    "active": true,
    "ingredients": [
      {
        "id": 1,
        "name": "Tomate"
      },
      {
        "id": 2,
        "name": "Queijo"
      },
      {
        "id": 3,
        "name": "Pepperoni"
      }
    ]
  },
  {
    "id": 2,
    "name": "Margherita",
    "description": "Pizza tradicional com molho de tomate, mozzarella e manjericão",
    "size": "MEDIUM",
    "price": 28.00,
    "active": true,
    "ingredients": [
      {
        "id": 4,
        "name": "Molho de tomate"
      },
      {
        "id": 5,
        "name": "Mozzarella"
      },
      {
        "id": 6,
        "name": "Manjericão"
      }
    ]
  }
]
```

---

### **Ingredient Management**

`GET /api/ingredients`

Retorna a lista de todos os ingredientes disponíveis.

**Exemplo de Resposta:**

```json
[
  {
    "id": 1,
    "name": "Tomate"
  },
  {
    "id": 2,
    "name": "Queijo"
  }
]
```

---

### **Order Management**

`POST /api/orders`

Cria um novo pedido de pizza.

**Exemplo de Requisição:**

```json
{
  "pizzaIds": [1, 2],
  "customerName": "João Silva",
  "deliveryAddress": "Rua das Flores, 123"
}
```

**Exemplo de Resposta:**

```json
{
  "id": 1,
  "status": "PREPARING",
  "totalPrice": 63.00,
  "estimatedDeliveryTime": "30 minutes"
}
```

---

## **Requisitos**

- Java 21
- Maven 3.8+
- Banco de Dados (MySQL, PostgreSQL ou outro configurado)
- Token JWT para autenticação (Bearer Token)

---

## **Enumeração: PizzaSize**

A propriedade `size` da requisição corresponde ao tipo de pizza e aceita os seguintes valores:

- `SMALL`: Pizza pequena.
- `MEDIUM`: Pizza média.
- `LARGE`: Pizza grande.

---

## **Rate Limiting**

A API possui um limite de 100 requisições por minuto por IP. Caso o limite seja excedido, a API retornará o status `429 Too Many Requests`.

---

## **Erros Comuns**

1. **400 Bad Request:** Quando algum campo obrigatório não é informado ou contém valores inválidos.
2. **401 Unauthorized:** Quando o token de autenticação não é fornecido ou é inválido.
3. **404 Not Found:** Quando o recurso solicitado não existe.
4. **429 Too Many Requests:** Quando o limite de requisições por minuto é excedido.
5. **500 Internal Server Error:** Quando ocorre um erro inesperado na execução do sistema.

---

## **Desenvolvido por Ricardo Farias**