# Elo: feedback do cliente para a Hitss (protótipo)

Protótipo da ideia inscrita no Prêmio Nosso Sol (categoria "Ideia que Melhora a Experiência do Cliente").

- `backend/`: Quarkus 3 (Java 17+), API REST, dados em memória, login por perfil.
- `frontend/`: Vue 3 + Vite. O build já está em `backend/src/main/resources/META-INF/resources`.
- `docs/`: Ficha Técnica (docx e pdf) e prints das telas.

## Rodar (um comando)

```
cd backend
mvn quarkus:dev
```

Abra http://localhost:8080

## Rodar com o front em modo dev

```
cd backend && mvn quarkus:dev        # porta 8080
cd frontend && npm install && npm run dev   # porta 5173, proxy de /api para 8080
```

Para atualizar o front embutido no Quarkus: `cd frontend && npm run build`.

## Acessos de demonstração (dados fictícios)

| Perfil | E-mail | Senha |
|---|---|---|
| Gestor Hitss | gestor@hitss.demo | hitss123 |
| Gestor do cliente | carlos@cliente.demo | cliente123 |
| Gestor do cliente | patricia@cliente.demo | cliente123 |

Os dados reiniciam a cada start. Os feedbacks de demonstração são gerados em relação ao mês atual.

## Regras principais

- Gestor Hitss (`/api/hitss/**`): cadastra devs e gestores do cliente, cria e encerra alocações, lê todos os feedbacks e o painel.
- Gestor do cliente (`/api/client/**`): vê só os devs alocados a ele e envia 1 feedback por dev por mês (editável no mês corrente).
- Feedback: 5 critérios (1 a 5), recomendação (0 a 10, base do NPS), pontos fortes, melhorias e comentário.

## O que muda para produção

Login via SSO corporativo (OIDC) em vez de senha local, PostgreSQL com Hibernate Panache no lugar do armazenamento em memória, auditoria de acesso, política de retenção e revisão de LGPD com o DPO.

## Status de verificação

O Maven Central estava bloqueado no ambiente onde o protótipo foi escrito, então o `mvn` não rodou lá. O Java foi compilado contra stubs do jakarta, a lógica foi testada e os resources reais foram exercitados com o front por um mini-servidor de teste. O frontend foi buildado e testado no navegador. Se o `mvn quarkus:dev` reclamar de algo, é o primeiro lugar para olhar (versão do Quarkus em `pom.xml`: 3.15.1).
