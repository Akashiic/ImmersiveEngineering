# Estado atual e guia de retomada

Data do levantamento: **25/10/2026**. Ponto mais recente trabalhado: **`auto-workbench-test-1-r3`** (commit `5d467a61`; HEAD documental `ac41e5e7`, 17/09/2026 01:53).

## 1. Resumo

- **Onde parou:** R3 construído, auditado e fumaçado; falta o **A6** — jogar a matriz T0–T18 no Forbidden Series com coleta de evidência. Nenhum resultado de A6 existe nas fontes.
- **O que está aprovado em jogo:** só a Garden Cloche Test 2.
- **A armadilha principal:** o repositório tem **duas linhas que não se encontram**. A linha R3 (`feature/auto-workbench`) tem a correção de estado do Auto Workbench e **não tem** Alloy Kiln, Mixer, hardening de rede nem hardening multiplayer da Cloche. A linha `feature/mixer` tem Alloy Kiln e Mixer e **carrega o Auto Workbench R2, que foi rejeitado**. Nenhum jar existente contém tudo.
- **O que não foi feito agora:** nenhum build, teste ou execução de jogo foi refeito para montar este dossiê. Os resultados abaixo são os dos relatórios originais, exceto o teste de aplicação de patches da seção 5 (que não compila nada).

## 2. Branches e o que cada uma contém

Todas existem neste fork sob o mesmo nome. "Ahead" = commits à frente do upstream `798ce21a` (Bogdan-G).

| Branch | Tip | Ahead | Conteúdo relevante |
|---|---|--:|---|
| `1.7.10-backport` | este dossiê | — | **Branch padrão.** Linha R3 + `docs/backport-1.7.10/` |
| `feature/auto-workbench` | `ac41e5e7` | 26 | **R3** (mais recente) |
| `feature/mixer` | `7b8863d0` | 32 | Alloy Kiln + Mixer M1 + fluido Concrete (+ AW **R2**) |
| `feature/alloy-kiln` | `ccaad104` | 29 | Alloy Kiln K4 (+ AW **R2**) |
| `chief/ie-1.21.1-masterplan` | `d7bf99e5` | 26 | Só documentação: charter 1.21.1, gap matrix, registry, áudio, render, rede, design de Alloy/Mixer |
| `hardening/network-gate` | `a1b811a6` | 25 | Rede C2S (+ AW R2) |
| `hardening/network-gate-chief` | `c5453e22` | 26 | + verificação do chefe (testes/ABI) |
| `hardening/auto-r2-cloche-mp` | `a55d6fa7` | 21 | Multiplayer da Cloche sobre AW R2 |
| `hardening/garden-cloche-multiplayer` | `ac69028c` | 10 | Multiplayer da Cloche sobre Cloche T2 |
| `feature/garden-cloche` | `ad5e6201` | 9 | Cloche Test 2 aprovada |
| `audit/modern-feature-gap` | `a36bb241` | 10 | + auditoria de lacunas e escolha do Auto Workbench |
| `forbidden-backport` | `0c803472` | 1 | Baseline 0.7.9 em RFG |
| `upstream-lineage/bogdan-g-1.7.10` | `798ce21a` | 0 | Ponto de partida upstream (nome seguro: a branch `1.7.10` deste fork é a do BluSunrize) |

### Matriz de inclusão (o que cada linha carrega)

| Recurso | R3 (`feature/auto-workbench`) | `feature/mixer` | `feature/alloy-kiln` | `hardening/network-gate*` | `hardening/auto-r2-cloche-mp` |
|---|:-:|:-:|:-:|:-:|:-:|
| Garden Cloche Test 2 | ✅ | ✅ | ✅ | ✅ | ✅ |
| Auto Workbench **R2** (rejeitado) | substituído | ⚠️ | ⚠️ | ⚠️ | ⚠️ |
| Auto Workbench **R3** (`5d467a61`) | ✅ | ❌ | ❌ | ❌ | ❌ |
| Alloy Kiln | ❌ | ✅ | ✅ | ❌ | ❌ |
| Mixer M1 / fluido Concrete | ❌ | ✅ | ❌ | ❌ | ❌ |
| Hardening de rede C2S | ❌ | ❌ | ❌ | ✅ | ❌ |
| Hardening multiplayer da Cloche | ❌ | ❌ | ❌ | ❌ | ✅ |
| `.gitattributes` (LF para OBJ/MTL) | ❌ | ✅ | ✅ | ✅ | ❌ |

## 3. Retomar o A6 (gameplay do R3) — passo a passo

Pré-requisitos: Windows, Python 3, o modpack Forbidden Series (launcher Technic) e **um mundo de teste com backup**.

1. Feche o Minecraft por completo e faça **backup do mundo**.
2. Em `…\.technic\modpacks\forbidden-series\mods\`, remova qualquer `ImmersiveEngineering*.jar` e instale **exatamente um**: `ImmersiveEngineering-auto-workbench-test-1-r3.jar`. Os jars não estão neste repositório (licença); gere-o a partir da tag ou use o que já tem.
3. Confira o SHA-256: `B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12` (3.806.758 bytes).
4. Na raiz do repositório, rode `python tools/collect_auto_workbench_a6_evidence.py --start --label auto-workbench-test-1-r3`. Ele **recusa** começar se houver mais de um jar IE, nome diferente ou hash diferente — e não cria marcador nesse caso.
   - **Ajuste de ambiente:** o script tem os caminhos do autor fixos no código (`ROOT = C:\Users\User\Desktop\Codex\immersiveengineering-backport`, `PACK = C:\Users\User\AppData\Roaming\.technic\modpacks\forbidden-series`). Em outra máquina, edite essas duas constantes. As evidências saem em `<ROOT>\evidence\auto-workbench-a6\<sessão>-<label>\`.
5. Abra o jogo e execute **T0–T18** de [`docs/AUTO_WORKBENCH_TEST_1.md`](../AUTO_WORKBENCH_TEST_1.md). Anote PASS / FAIL / NÃO CONCLUSIVO por item; **nunca** converta NÃO CONCLUSIVO em PASS. Observação: o texto de preparação da matriz diz "use somente o candidato `auto-workbench-test-1`"; leia como **R3** (o arquivo é idêntico nos três candidatos).
6. Saia do jogo normalmente e rode `python tools/collect_auto_workbench_a6_evidence.py --collect`. Ele revalida nome e hash antes de criar a pasta de evidência (logs filtrados, últimas 500 linhas de cada log, crash reports, manifesto JSON).
7. **Interpretação:** A6 só passa quando **jogo + logs da mesma sessão** são revisados juntos. Log sem exceção não prova semântica correta; se as duas fontes conflitam, A6 é **rejeitado** e vira defeito A7.
8. Resultado: PASS → A8 (aprovação e congelamento). FAIL → A7 (correção adversarial) → novo candidato **R4** com novo commit, tag e hash. **Não mova a tag R3.**
9. Rollback se o R3 falhar: restaurar o backup do mundo, remover o R3 e voltar ao último candidato aprovado (**Garden Cloche Test 2**, nunca o R2).

Itens da matriz que merecem atenção extra por serem os mais prováveis de falhar: T3/T4 (formação normal e espelhada nas quatro direções), T9 (fila e corte de RF), T12 (backpressure da saída), T13 (reinício com processo a 25/60/78%), T14 (desmontagem: o IE upstream **não** devolve ingredientes de processos em curso; registre sem classificar como regressão), T15/T16 (renderer com Angelica e regressão da Cloche), T17 (carga — sem limiar numérico definido).

## 4. Riscos e armadilhas conhecidos

1. **Linhas divergentes** (seção 2): qualquer build "completo" exige integração; o R3 não tem Alloy/Mixer, e a linha Mixer tem o AW rejeitado.
2. **Quebra de linha no Windows:** a linha R3 **não tem** `.gitattributes`. Num checkout com `core.autocrlf=true`, os OBJ viram CRLF e os hashes auditados deixam de bater (isso já reprovou uma auditoria no K4). Este fork adiciona `.gitattributes` (`*.obj`/`*.mtl` com `eol=lf`) na branch `1.7.10-backport`. Em outro clone: `git config core.autocrlf false` antes do checkout. Efeito colateral observado ao montar este dossiê: 117 arquivos apareceram "modificados" só por CRLF.
3. **Documentação desatualizada** (seção 8).
4. **A6 nunca executado**; A7/A8 bloqueados. Qualquer afirmação de que o Auto Workbench "funciona" é, hoje, hipótese amparada por prova estática e smoke de inicialização.
5. **Servidor dedicado nunca provado** para R2, Alloy ou rede: os harnesses pararam no aceite do EULA, que não foi aceito automaticamente. A segurança do lado dedicado se apoia em zero referências a `net.minecraft.client.*` no bytecode comum.
6. **DeveloperCapes** continua embutido no jar e faz chamada de rede remota no cliente — herança do Forbidden 0.7.9, não aprovada para a linha endurecida.
7. **Três texturas ausentes herdadas** (`revolver_rose`, `metal2_skycrateDispenser`, `revolver_tesla`) aparecem em todo smoke; já estavam ausentes no baseline aprovado e não são regressões.
8. **GUI ID 15** foi usado pelo Alloy Kiln, dentro da faixa 15–31 que o gate de registry reservou (alocação moderna deveria começar em 32). Decidir e documentar antes de integrar.
9. **Reprodutibilidade do jar:** o hash do jar R3 está registrado, mas **não foi testado** se um novo build a partir da tag gera bytes idênticos (carimbos de data entram no ZIP). Para auditoria, compare conteúdo, não só hash.
10. **Dependência do disco do autor:** jars, logs de smoke e `upstream/` vivem só na máquina local. Este repositório guarda o que é licenciável e pequeno; veja [ARTEFATOS.md](ARTEFATOS.md).

## 5. Integração das linhas divergentes

**Proposta; nada disso foi integrado ou compilado.**

Teste feito em 25/10/2026, em worktree descartável (removido), sem build: partindo de `feature/mixer` (`7b8863d0`) e aplicando `git cherry-pick -n` em sequência:

| Ordem | Commit | Resultado |
|---|---|---|
| 1 | `a55d6fa7` multiplayer da Cloche (base R2) | aplica limpo |
| 2 | `5d467a61` correção de estado do R3 | código aplica; **só `docs/engineering/ROADMAP.md` conflita** (documentação) |
| 3 | `a1b811a6` rede C2S | aplica limpo (toca o mesmo `TileEntityAutoWorkbench.java` do passo 2) |

Resultado do conjunto: 22 arquivos alterados, +1.281/−40. O `.gitattributes` não precisa ser reaplicado (já existe nessa base). O pino `e7c7e37a` fica redundante.

Isso prova **aplicabilidade textual**, não correção. Antes de chamar de candidato é preciso: build limpo; auditoria de artefato e ABI; revisão semântica da interação rede × Auto Workbench (o handler de rede valida `ContainerAutoWorkbench` aberto; o R3 alterou esse container); smoke de cliente e, no mínimo, A6 + K5; runtime dedicado com o EULA aceito pelo operador. O resultado deve receber **novo nome** (ex.: `integration-test-1`), nunca sobrescrever tag existente. Diagrama: [FLUXOGRAMAS.md](FLUXOGRAMAS.md#9-integração-proposta-das-linhas-divergentes).

## 6. Trabalho não commitado encontrado no workspace

Capturado em modo somente leitura em 25/10/2026; **sem revisão, sem teste, fora de qualquer tag**. Arquivos em [`archive/wip-uncommitted/`](archive/wip-uncommitted/).

| Worktree (HEAD) | Mudança | Interpretação |
|---|---|---|
| `network-hardening` (`a1b811a6`) | edições em `EventHandler`, `MessageMinecartShaderSync`, `MessageRequestBlockUpdate`; novo `MinecartShaderSyncRequestQueue.java`; dois scripts `apply_*_hardening.py` | Começo do hardening dos handlers C2S que o Gate 1 deixou de fora |
| `ie-auto-r2-cloche-mp` (`a55d6fa7`) | `tools/audit_garden_cloche_multiplayer.py` | Ajuste de auditoria |
| `runtime-alloy-kiln-smoke`, `runtime-mixer-m1-smoke`, `runtime-smoke-auto-workbench-r3` | `dependencies.gradle` (e probe do M1) | Harnesses descartáveis com opcionais em compile-only; **não** são linhas de entrega |

## 7. Mapa do workspace local (fonte de verdade do autor)

Raiz: `C:\Users\User\Desktop\Codex\immersiveengineering-backport\` — **não é um repositório Git**, é um agregador.

| Pasta | Conteúdo |
|---|---|
| `project-docs/` | 6 documentos de contexto (arquivados em `archive/project-docs-workspace-root/`) |
| `dist/` | pastas de candidatos com `VALIDATION.txt`, `SHA256.txt`, `TEST-INSTALL.txt`, jars e logs |
| `evidence/` | logs de smoke, probes, JSONs de decompilação |
| `tools/` | auditores e coletores Python |
| `work/` | worktrees Git (11). Principais: `ImmersiveEngineering-Forbidden-Backport` (o nome é enganoso: é a linha R3), `chief-1.21.1-masterplan`, `alloy-kiln`, `mixer`, `network-hardening`, `network-hardening-chief`, `ie-auto-r2-cloche-mp` e os `runtime-*` descartáveis |
| `upstream/` | clones de referência: IE oficial 1.7.10 e 1.21.1, Bogdan-G, chaos234, quentin452, vladislemon, referência GTNH, Cloche 1.10.2/1.12.2, FML, AquaTweaks, código do DeveloperCapes |

Nada mais novo que 17/09/2026 01:54 foi encontrado nessa árvore.

## 8. Documentos desatualizados (não corrigidos de propósito)

Os originais foram preservados; as divergências estão aqui para ninguém tropeçar:

- `PROJECT-CHARTER.md` ainda diz BASELINE RECOVERY — superado pelo charter 1.21.1.
- `CONTEXTO-CONSOLIDADO-2026-09-12.md` é um retrato de 12/09: lista o R2 como "pronto para A6" e o HEAD do workspace como `97cea596`; o R2 foi rejeitado depois e o HEAD é `ac41e5e7`. O "~14%" é histórico e não foi recalculado.
- O ROADMAP em checkouts R2 diz que falta rebuild/tag substituto — já resolvido pelo R2 e depois pelo R3.
- `AUTO_WORKBENCH_TEST_1.md` (e suas cópias `_R2`, `_R3`) é o mesmo texto e cita o candidato `auto-workbench-test-1`; a matriz `_R2` guarda ainda uma referência ao R1.
- O design do Mixer chama M2 de multiblock/registry; o fechamento M1 redefine M2 como fluidos e Concrete.
- Qualquer relatório que diga "R2 aprovado para A6" (A5.2, A5.3, A5.4) foi **superado** pelo A5.5.

## 9. Próximos passos, em ordem

1. **Executar o A6 no R3** (seção 3) e arquivar o resultado em `docs/backport-1.7.10/` (nova pasta `a6-evidence/`), sem editar os documentos históricos.
2. Se o A6 passar: A8 (congelar o Auto Workbench). Se falhar: A7 → R4.
3. Em paralelo (sem tocar o R3): decidir a estratégia de integração (seção 5) e o conflito do GUI ID 15.
4. Rodar o dedicado de verdade (aceitando o EULA conscientemente) para rede, Alloy e Auto Workbench.
5. K5 (gameplay do Alloy Kiln) e M2 do Mixer (escrever e executar o gate que falta).
6. Reconciliar a documentação (seção 8) numa atualização de planejamento.
7. Só então voltar ao plano mestre (Sawmill, contidos, equipamentos…).
