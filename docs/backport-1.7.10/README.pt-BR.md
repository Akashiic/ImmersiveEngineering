# Immersive Engineering 1.7.10 — dossiê do backport 1.21.1 (Forbidden Series)

> **Ponto mais recente trabalhado: `auto-workbench-test-1-r3` (R3)** — tag em `5d467a61`, 17/09/2026. É o build que o autor usa hoje.
> Estado: build, artefato e smoke de cliente **PASS**; **gameplay (A6) ainda não executado**.
> **Cuidado:** o R3 vive numa linha separada de Alloy Kiln, Mixer, hardening de rede e multiplayer da Cloche — e essas linhas carregam o Auto Workbench R2, **rejeitado**. Nenhum jar existente junta tudo.

Este diretório reúne o contexto, a cronologia, os fluxogramas e as evidências do que foi feito neste fork até 25/10/2026, para que qualquer pessoa retome do ponto mais recente.

## Por onde começar

| Se você quer… | Leia |
|---|---|
| Entender o projeto em 10 minutos | [CONTEXTO.md](CONTEXTO.md) |
| Ver **quando** cada coisa aconteceu, com commits e tags | [CRONOLOGIA.md](CRONOLOGIA.md) |
| Ver os **fluxogramas** (linhagem Git, gates, estado, retomada) | [FLUXOGRAMAS.md](FLUXOGRAMAS.md) |
| **Retomar o trabalho** (A6, branches, riscos, integração) | [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md) |
| Saber exatamente o que o jar R3 é e não é | [BUILD-EM-USO.md](BUILD-EM-USO.md) |
| Conferir hashes e a política de binários | [ARTEFATOS.md](ARTEFATOS.md) |
| Ver erros passados, decisões e perguntas abertas | [HIPOTESES-E-INCIDENTES.md](HIPOTESES-E-INCIDENTES.md) |
| Saber a origem do código e os limites deste dossiê | [PROVENIENCIA.md](PROVENIENCIA.md) |
| Dados brutos (TSV) | [`data/`](data/) |
| Documentos originais preservados byte a byte | [`archive/INDEX.md`](archive/INDEX.md) |

## Em uma tabela: versões e estado

| Versão | Data | Estado |
|---|---|---|
| Baseline Forbidden 0.7.9 em RFG (`0c803472`) | 09/09 | Aceita |
| Garden Cloche Test 2 (`ad5e6201`) | 09/09 | **Aprovada em jogo** |
| Auto Workbench Test 1 (`e2ee4453`) | 10/09 | Rejeitado (OBJ) |
| Auto Workbench R2 (`97cea596`) | 10/09 | Rejeitado em 17/09 (estado/sync) |
| Alloy Kiln Test 1 (`a1376474`) | 12/09 | K4 PASS; gameplay pendente |
| Garden Cloche multiplayer Test 1 (`ac69028c`) | 16/09 | Estático PASS; multiplayer pendente |
| **Auto Workbench R3 (`5d467a61`)** | **17/09** | **Mais recente · A5.6 PASS · A6 pendente** |

## Branches e tags

- **Branch padrão: `1.7.10-backport`** — a linha R3 + este dossiê. As branches `feature/*`, `hardening/*`, `chief/*`, `audit/*` e `forbidden-backport` preservam as demais linhas **com o nome original**; `upstream-lineage/bogdan-g-1.7.10` é o ponto de partida upstream.
- **Atenção:** a branch `1.7.10` deste fork é a do BluSunrize (upstream) e **não foi tocada**.
- As 8 tags de candidato estão publicadas e são **imutáveis**: não as mova.
- Tabela completa de branches e o que cada uma contém: [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md#2-branches-e-o-que-cada-uma-contém).

## Honestidade sobre o que se sabe

Tudo aqui cita commit ou relatório. Esta sessão **não** refez builds nem testes de jogo; só conferiu o jar R3 anexado (idêntico ao candidato documentado) e testou, sem compilar, se os patches das linhas divergentes aplicam. Lacunas, incertezas e o que depende de conversas anteriores estão em [PROVENIENCIA.md](PROVENIENCIA.md#5-limites-e-ressalvas--leia-antes-de-citar-este-dossiê).

## Licença

O código-base 1.7.10 do BluSunrize não traz `LICENSE` nesta árvore; a "Blu's License of Common Sense" permite fork e proíbe redistribuir binários compilados sem permissão e monetizar. **Este repositório não publica jars**, apenas código, histórico, relatórios e SHA-256.
