# Documentação da API — PrintHub Regional

Base: `http://localhost:8080`. Todas as rotas exigem **HTTP Basic**. O corpo é sempre JSON e os valores em dinheiro vão em reais, com duas casas decimais (`48.00`).

## Usuários de teste e perfis

| Usuário | Senha | Perfil | O que pode fazer |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | Categorias, usuários, pontos, lotes, produtos, pedidos e confirmação de retirada. Não aceita nem rejeita pedidos (só o PRODUTOR) |
| `produtor` | `produtor123` | PRODUTOR | Consultar, anunciar produtos, aceitar e rejeitar pedidos |
| `consumidor` | `consumidor123` | CONSUMIDOR | Consultar, fazer, alterar e cancelar pedidos |

Qualquer usuário logado faz `GET`, exceto em `/usuarios`, que é só do ADMIN por conter e-mail e telefone.

## Formato de erro

Todo erro volta no mesmo formato:

```json
{
  "momento": "2026-11-23T14:30:00",
  "status": 409,
  "erro": "Conflict",
  "mensagem": "Estoque insuficiente para Miniatura de dragão: pedido 3, disponível 2",
  "caminho": "/pedidos"
}
```

Nos erros de validação vem também `campos`, por exemplo `[{ "campo": "preco", "mensagem": "O preço deve ser maior que zero" }]`.

| Código | Quando acontece |
|---|---|
| 400 Bad Request | Campo inválido (Bean Validation), JSON mal formado, tipo errado na URL (`/produtos/abc`), status inexistente |
| 401 Unauthorized | Sem usuário e senha, ou credenciais erradas |
| 403 Forbidden | Logado, mas o perfil não tem permissão para a operação |
| 404 Not Found | Registro ou rota inexistente |
| 405 Method Not Allowed | Método HTTP que a rota não aceita |
| 409 Conflict | Regra de negócio impediu a operação |

---

## Categorias — `/categorias`

| Método | Rota | Perfil | Sucesso |
|---|---|---|---|
| GET | `/categorias` | logado | 200 lista |
| GET | `/categorias/{id}` | logado | 200 |
| POST | `/categorias` | ADMIN | 201 + `Location` |
| PUT | `/categorias/{id}` | ADMIN | 200 |
| DELETE | `/categorias/{id}` | ADMIN | 204 |

Corpo: `{ "nome": "Cosplay", "descricao": "Acessórios para fantasias" }`. O `nome` é obrigatório (até 60 caracteres).

Regras (409): nome repetido; excluir categoria que tem produtos.

## Usuários — `/usuarios` (só ADMIN)

| Método | Rota | Sucesso |
|---|---|---|
| GET | `/usuarios` | 200 |
| GET | `/usuarios/{id}` | 200 |
| GET | `/usuarios/tipo/{tipo}` | 200, consulta personalizada (`CONSUMIDOR` ou `PRODUTOR`) |
| POST | `/usuarios` | 201 |
| PUT | `/usuarios/{id}` | 200 |
| DELETE | `/usuarios/{id}` | 204 |

Corpo:

```json
{ "nome": "Ana Costa", "email": "ana@printhub.local", "telefone": "14999990010", "tipo": "CONSUMIDOR", "ativo": true }
```

Validações: `nome` e `email` obrigatórios, `email` válido, `telefone` com 10 ou 11 dígitos, `tipo` obrigatório.
Regras (409): e-mail repetido; produtor com produtos não pode virar consumidor; quem tem produtos ou pedidos não pode ser excluído (o caminho é desativar).

## Pontos de retirada — `/pontos-retirada`

| Método | Rota | Perfil | Sucesso |
|---|---|---|---|
| GET | `/pontos-retirada` | logado | 200 |
| GET | `/pontos-retirada/{id}` | logado | 200 |
| POST | `/pontos-retirada` | ADMIN | 201 |
| PUT | `/pontos-retirada/{id}` | ADMIN | 200 |
| DELETE | `/pontos-retirada/{id}` | ADMIN | 204 |

Corpo:

```json
{ "nome": "Ponto Universidade", "endereco": "Rua Exemplo, 100", "bairro": "Centro", "cidade": "Ourinhos", "custoEntrega": 18.00, "ativo": true }
```

`custoEntrega` é o valor para levar um lote inteiro até o ponto. Ele é dividido entre os pedidos do lote.
Regras (409): desativar ponto com lote em formação; excluir ponto com pedidos ou lotes.

## Produtos — `/produtos`

| Método | Rota | Perfil | Sucesso |
|---|---|---|---|
| GET | `/produtos` | logado | 200 |
| GET | `/produtos/{id}` | logado | 200 |
| GET | `/produtos/categoria/{categoriaId}` | logado | 200, consulta personalizada |
| GET | `/produtos/busca?nome=vaso` | logado | 200, consulta personalizada |
| GET | `/produtos/estoque-baixo?limite=5` | logado | 200, consulta personalizada com JPQL |
| POST | `/produtos` | PRODUTOR, ADMIN | 201 |
| PUT | `/produtos/{id}` | PRODUTOR, ADMIN | 200 |
| DELETE | `/produtos/{id}` | PRODUTOR, ADMIN | 204 |

Corpo:

```json
{
  "nome": "Chaveiro personalizado", "descricao": "Com nome gravado", "material": "PLA", "cor": "Azul",
  "preco": 8.50, "estoque": 20, "diasProducao": 1, "categoriaId": 2, "produtorId": 1, "ativo": true
}
```

Validações: `preco` maior que zero, `estoque` maior ou igual a 0, `diasProducao` entre 1 e 60.
Regras (409): só usuário do tipo PRODUTOR, e ativo, anuncia produto; produto que já aparece em pedidos não pode ser excluído.

## Pedidos — `/pedidos`

| Método | Rota | Perfil | Sucesso |
|---|---|---|---|
| GET | `/pedidos` | logado | 200 |
| GET | `/pedidos/{id}` | logado | 200 |
| GET | `/pedidos/status/{status}` | logado | 200, consulta personalizada |
| GET | `/pedidos/consumidor/{id}?status=ACEITO` | logado | 200, consulta personalizada com JPQL (`status` opcional) |
| POST | `/pedidos` | CONSUMIDOR, ADMIN | 201 |
| PUT | `/pedidos/{id}` | CONSUMIDOR, ADMIN | 200, refaz ponto e itens |
| DELETE | `/pedidos/{id}` | CONSUMIDOR, ADMIN | 204 |
| POST | `/pedidos/{id}/aceitar` | **só PRODUTOR** | 200 |
| POST | `/pedidos/{id}/rejeitar` | **só PRODUTOR** | 200, corpo `{ "motivo": "..." }` |
| POST | `/pedidos/{id}/cancelar` | CONSUMIDOR, ADMIN | 200 |
| POST | `/pedidos/{id}/retirar` | **só ADMIN** | 200 |

Corpo do POST e do PUT:

```json
{ "consumidorId": 3, "pontoRetiradaId": 1, "itens": [ { "produtoId": 1, "quantidade": 2 } ] }
```

Status do pedido: `AGUARDANDO_ACEITACAO → ACEITO → DISPONIVEL_RETIRADA → RETIRADO`. Os desvios possíveis são `REJEITADO` e `CANCELADO`.

Regras (409):
- estoque insuficiente, conferido ao criar e de novo ao aceitar;
- o produtor não pode comprar o próprio produto;
- todos os itens de um pedido precisam ser do mesmo produtor;
- produto inativo, ponto inativo ou consumidor inativo;
- alterar, aceitar ou rejeitar só enquanto `AGUARDANDO_ACEITACAO`;
- cancelar só enquanto `AGUARDANDO_ACEITACAO`, ou `ACEITO` com o lote ainda `EM_FORMACAO` (o estoque volta);
- retirar só quando `DISPONIVEL_RETIRADA`;
- excluir só se estiver `AGUARDANDO_ACEITACAO`, `REJEITADO` ou `CANCELADO`.

## Itens do pedido — `/pedidos/{pedidoId}/itens`

| Método | Rota | Perfil | Sucesso |
|---|---|---|---|
| GET | `/pedidos/{pedidoId}/itens` | logado | 200 |
| GET | `/pedidos/{pedidoId}/itens/{itemId}` | logado | 200 |
| POST | `/pedidos/{pedidoId}/itens` | CONSUMIDOR, ADMIN | 201, corpo `{ "produtoId": 2, "quantidade": 1 }` |
| PUT | `/pedidos/{pedidoId}/itens/{itemId}` | CONSUMIDOR, ADMIN | 200, corpo `{ "quantidade": 3 }` |
| DELETE | `/pedidos/{pedidoId}/itens/{itemId}` | CONSUMIDOR, ADMIN | 200 |

As alterações devolvem o pedido inteiro, com o valor recalculado. Só valem enquanto o pedido aguarda aceitação.
Regras (409): produto repetido no pedido; produto de outro produtor; remover o último item.

## Lotes — `/lotes`

| Método | Rota | Perfil | Sucesso |
|---|---|---|---|
| GET | `/lotes` | logado | 200 |
| GET | `/lotes/{id}` | logado | 200 |
| GET | `/lotes/ponto/{pontoId}?status=EM_FORMACAO` | logado | 200, consulta personalizada (`status` opcional) |
| POST | `/lotes` | ADMIN | 201, abre um lote: `{ "pontoRetiradaId": 2, "prazo": "2026-12-01T18:00:00" }` (prazo opcional) |
| PUT | `/lotes/{id}` | ADMIN | 200, novo prazo: `{ "prazo": "2026-12-05T18:00:00" }` |
| DELETE | `/lotes/{id}` | ADMIN | 204, só lote vazio |
| POST | `/lotes/{id}/fechar` | ADMIN | 200, fecha antes de completar 3 pedidos |
| POST | `/lotes/{id}/receber` | ADMIN | 200, o lote chegou ao ponto |

Status do lote: `EM_FORMACAO → FECHADO → ENTREGUE_NO_PONTO → CONCLUIDO`.

---

## Funcionalidade específica: lote coletivo de entrega

O problema que o PrintHub resolve é o frete: entregar uma peça pequena sai caro para um consumidor só. O lote junta os pedidos que vão para o mesmo ponto de retirada e divide o custo da entrega.

1. O consumidor faz o pedido (`POST /pedidos`). Ele fica `AGUARDANDO_ACEITACAO`, sem taxa de entrega.
2. O produtor aceita (`POST /pedidos/{id}/aceitar`). O estoque baixa e o pedido entra no lote `EM_FORMACAO` daquele ponto. Se não houver lote aberto, um novo é criado, copiando o custo de entrega do ponto, com prazo de 7 dias.
3. No **3º pedido** o lote fecha sozinho. O administrador também pode fechar antes, em `POST /lotes/{id}/fechar`, por exemplo quando o prazo vence.
4. Ao fechar, o custo é dividido em centavos e a sobra fica com os primeiros pedidos. Exemplo no Ponto Centro, que custa R$ 20,00: 2000 ÷ 3 = 666, com sobra de 2, então as taxas ficam **6,67 + 6,67 + 6,66**.
5. O lote chega ao ponto (`POST /lotes/{id}/receber`) e os pedidos ficam `DISPONIVEL_RETIRADA`.
6. O atendente confirma a retirada (`POST /pedidos/{id}/retirar`). Quando todos são retirados, o lote fica `CONCLUIDO`.

A regra está em `LoteServico` e o cálculo em `RateioEntrega`.

## Dados de exemplo (`import.sql`)

Carregados a cada inicialização. Como `ddl-auto=create`, o banco sempre começa no mesmo estado.

- **Categorias:** 1 Decoração, 2 Utilidades, 3 Brinquedos e colecionáveis, 4 Peças técnicas.
- **Usuários:** 1 Carlos Mendes e 2 Beatriz Rocha (PRODUTOR); 3 João Pereira, 4 Maria Souza e 5 Pedro Lima (CONSUMIDOR).
- **Produtos:** de 1 a 3 são do Carlos; de 4 a 6 são da Beatriz. O 4 (Miniatura de dragão) tem só 2 unidades em estoque.
- **Pontos:** 1 Ponto Centro (R$ 20,00), 2 Ponto Vila Margarida (R$ 15,00), 3 Ponto Jardim Paulista (R$ 12,00).
