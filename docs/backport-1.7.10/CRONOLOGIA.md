# Cronologia — Immersive Engineering 1.7.10 (backport 1.21.1 → Forbidden Series 0.7.9)

> **Ponto mais recente trabalhado: `auto-workbench-test-1-r3` (R3)** — commit `5d467a61`, tag criada em **17/09/2026 01:22**, jar gerado às 01:24, documentação de fechamento em `ac41e5e7` (**17/09/2026 01:53**, HEAD da branch `feature/auto-workbench`).
> É o build que o autor está usando. Estado: **A5.6 PASS (estático + smoke de cliente); A6 (gameplay T0–T18) sem evidência registrada**.
> **Atenção:** o R3 está numa linha própria. Ele **não contém** Alloy Kiln, Mixer, hardening de rede nem hardening multiplayer da Cloche, e essas linhas, por sua vez, **não contêm** as correções do R3. Veja [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md).

Fuso de todos os horários do projeto: **America/Sao_Paulo (UTC−3)**. Fontes: histórico Git (`data/commits.tsv`, `data/tags.tsv`), relatórios de gate (`archive/`), contexto consolidado de 12/09 (`archive/project-docs-workspace-root/CONTEXTO-CONSOLIDADO-2026-09-12.md`). Cada afirmação aponta para commit ou documento; onde só existe a palavra de uma conversa anterior, isso está dito. Diagramas: [FLUXOGRAMAS.md](FLUXOGRAMAS.md).

## 1. Progressão das versões (visão de uma tela)

| # | Candidato (tag) | Quando | Commit | Status segundo as fontes | Contém R3? |
|---|---|---|---|---|---|
| 0 | `0.7.9-forbidden-baseline` | 09/09 10:05 | `0c803472` | Baseline recuperada em RFG; gates G1/G2 aceitos | — |
| 1 | `garden-cloche-test-1` | 09/09 12:49 | `415ad44c` | Substituída pelo Test 2 (tag leve, não anotada) | — |
| 2 | `garden-cloche-test-2` | 09/09 16:47 | `ad5e6201` | **Aprovada funcionalmente pelo usuário** (09/09 17:44) | ancestral do R3 |
| 3 | `auto-workbench-test-1` | 10/09 11:39 | `e2ee4453` | **REJEITADO** — `ModelFormatException` no OBJ (A5.1) | ancestral |
| 4 | `auto-workbench-test-1-r2` | 10/09 21:31 | `97cea596` | **REJEITADO em 17/09** — defeitos de estado (A5.5) | ancestral |
| 5 | `alloy-kiln-test-1` | 12/09 01:57 | `a1376474` | K4 PASS (estático + smoke de cliente); K5 (gameplay) pendente | não |
| 6 | `garden-cloche-multiplayer-test-1` | 16/09 23:09 | `ac69028c` | Estático/artefato PASS; runtime multiplayer pendente | não |
| 7 | **`auto-workbench-test-1-r3`** | **17/09 01:22** | **`5d467a61`** | **A5.6 PASS; A6 pendente — build em uso** | **é o R3** |

Sem tag, mas relevantes: hardening de rede (`a1b811a6`, verificação `c5453e22`), Mixer M1 (`b1fab23f`) e fluido Concrete do Mixer (`7b8863d0`, sem gate). Identidade completa dos jars (tamanho e SHA-256): [ARTEFATOS.md](ARTEFATOS.md).

## 2. Fase 0 — Ancestralidade (2016 → 2023)

| Data | Marco | Evidência |
|---|---|---|
| 09/05/2016 | BluSunrize `1.7.10` @ `c45a3a2` — "Build and Release v0.7.7". A tag remota `0.7.7` está numa história divergente e **não** é ancestral; o HEAD da branch é a referência oficial. | `archive/project-docs-workspace-root/PROVENANCE.md` §2 |
| 2016–2017 | Continuação do chaos234 (era 0.7.8), `c38fd6b → 3cf2a38`. O jar implantado aponta `contributorRevolvers.json` para o repositório do chaos234. | PROVENANCE §3 |
| 28/04/2017 | Bogdan-G `1.7.10` @ `798ce21a` (02:28 +03:00): split de energia, fix de fio em mundo force-loaded, crash de poção, XSTR, conversão GL/double→float. Três impressões independentes ligam esse commit ao jar. | PROVENANCE §4; `data/branches.tsv` |
| set/2023 | Carimbos do jar Forbidden Series 0.7.9 indicam recompilação Java 8 com DeveloperCapes e metadado "Forbidden Series Edition". SHA-256 do jar: `f68b4bef…92fb8`. | PROVENANCE §1, §5 |

Limite declarado: o repositório público literal do "Forbidden Series 0.7.9" **não foi localizado**; a última etapa da cadeia é reconstruída, não confirmada por fonte pública.

## 3. Fase 1 — Baseline e Garden Cloche (09/09/2026)

Todos os horários abaixo são de 09/09/2026.

| Hora | Evento | Commit / fonte |
|---|---|---|
| 01:35 | Pedido inicial de fork, Garden Cloche, Magical Crops BETA 5 e AgriCraft (mensagem recuperada de conversa anterior). | CONTEXTO-CONSOLIDADO |
| 10:05 | `0c803472` Recupera a baseline Forbidden 0.7.9 em RFG (54 arquivos). Tag anotada `0.7.9-forbidden-baseline`. 694 classes em ambos os artefatos, 463 `.java` decompilados idênticos, 32 diferenças classificadas. | `data/commits.tsv`; BASELINE-GATE-REPORT |
| 12:49 | `415ad44c` Garden Cloche core → `garden-cloche-test-1` (tag leve). | idem |
| — | Primeira rodada no jogo: água, energia e fertilizante funcionam; texturas erradas, solo MFR sem efeito, integrações agrícolas ausentes (relato do usuário; vídeo e logs não reinspecionados). | CONTEXTO-CONSOLIDADO |
| 14:21 | `1901538d` corrige a coordenada V do Wavefront (texturas). | commits |
| 14:28 | `bdf75d13` multiplicador do MFR Fertile Soil. | commits |
| 14:44 | `2162392f` adaptador Magical Crops v4. | commits |
| 16:30–16:44 | `4d2b92a0` AgriCraft (sementes genéticas); `d288da4d` semântica do solo; `69bf1d8d` bordas de colheita. | commits |
| 16:47 | `ad5e6201` matriz de validação; tag anotada `garden-cloche-test-2` às 16:47:29. | commits; `GARDEN_CLOCHE_TEST_2.md` |
| 17:44 | **Usuário confirma sucesso funcional** da Cloche ("funcional e sem erros visíveis"). Não equivale a PASS por cenário, benchmark ou soak test. | CONTEXTO-CONSOLIDADO |
| depois | Método consolidado em suíte de skills e auditoria de lacunas: Auto Workbench ranqueado primeiro (92/100); Alloy Kiln e Mixer (83/100). | ROADMAP G8.5; MODERN-FEATURE-GAP-AUDIT |

## 4. Fase 2 — Auto Workbench: Test 1 e R2 (09–10/09/2026)

| Quando | Evento | Commit / fonte |
|---|---|---|
| 09/09 19:14 | Seleciona o Auto Workbench como feature #2. | `a36bb241` |
| 09/09 19:34–20:17 | Gates de arqueologia/design; Redstone Engineering Block; contrato de receitas; núcleo do multiblock (+1.023 linhas). | `9697b2f5`, `70868d8a`, `0b201ff6`, `6b6578aa` |
| 09/09 22:31–23:12 | GUI e interação; renderer animado (+3.458 linhas); projeção do blueprint. | `f2cd3dd3`, `b9aa06af`, `7a61866b` |
| 10/09 01:35 | Endurece renderer e paridade manual. | `f7d638cb` |
| 10/09 11:39 | Prepara o candidato → tag `auto-workbench-test-1`. A5 (artefato) passa. | `e2ee4453` |
| 10/09 | **Smoke de cliente isolado falha**: grupo OBJ com 289 quads + 4 triângulos → `ModelFormatException`. Candidato **rejeitado**; tag imutável. | `AUTO-WORKBENCH-A5.1-RUNTIME-SMOKE-GATE.md` |
| 10/09 21:31 | Correção "quadify" (triângulo A B C → quad A B C C) → **R2**, tag às 21:31:51. Smoke corrigido às 21:38–21:39 mostra ciclo de vida do servidor sem erro. | `97cea596`; A5.1; A5.2 |
| 10/09 23:31–23:34 | Usuário amplia o alvo para a paridade integral com o IE 1.21.1 e reforça a identidade visual moderna. | CONTEXTO-CONSOLIDADO (conversa anterior) |

## 5. Fase 3 — Plano mestre 1.21.1, rede e registry (11/09/2026)

| Hora | Evento | Commit |
|---|---|---|
| 15:21 | Charter-mestre 1.21.1 → 1.7.10; snapshot upstream congelado `75a27f03` (IE 12.4.3, build 194); 398 blocos / 314 itens / 25 multiblocks / 54 sons modernos. | `94df992b` |
| 15:34 | Design do runtime de áudio. | `e0cd905b` |
| 16:11 | Auditoria de render moderno e rede legacy. | `51ce9cc0` |
| 18:22 | Pino de LF para OBJ/MTL (`.gitattributes`). | `e7c7e37a` |
| 18:28 | **Hardening de rede C2S**: fila no servidor (cap 64 global / 4 por jogador / 16 por tick), NBT limitado a 256 KiB, validação por container. | `a1b811a6` |
| 19:50 | Gate de expansão de registry: metas legacy congeladas; GUI IDs novos a partir de 32. | `7ada56c1` |
| 19:53 | Verificação do chefe: build limpo, artefato (1.410 entradas), ABI sem remoções. **Runtime pendente.** | `c5453e22` |

## 6. Fase 4 — Alloy Kiln (11–12/09/2026)

| Quando | Evento | Commit |
|---|---|---|
| 11/09 22:23 | Gates de arqueologia e arquitetura. | `431f3615` |
| 11/09 22:45 | Contrato de receitas e assets modernos (manifest com 23 saídas). | `174a5228` |
| 12/09 01:17 | Implementação completa (23 arquivos, +2.156): núcleo, GUI, NEI, MineTweaker. | `a1376474` |
| 12/09 01:57 | Tag `alloy-kiln-test-1` (01:57:06) e gate K4 (01:57:57): build 2m28s, 1.446 entradas, 766 classes, ABI sem remoções, smoke de cliente. GUI ID 15. **Dedicado não rodou** (parou no EULA). | `ccaad104`; K4 |

## 7. Fase 5 — Mixer (12/09/2026)

| Hora | Evento | Commit |
|---|---|---|
| 02:08–02:09 | Gates de arqueologia/arquitetura do Mixer. | `d7bf99e5`, `c2ddc2bb` |
| 02:57 | Fundação: `MultiFluidTank` e `MixerRecipe`. | `f7a4abd4` |
| 03:31 | **M1 PASS** (só a fundação): autoteste puro e probe FML (`MIXER_M1_RUNTIME_PROBE_PASS amount=875 types=3`). | `b1fab23f` |
| 10:04 | Código de fluidos e ciclo do concreto (16 arquivos, +605). **Não existe gate M2 registrado.** | `7b8863d0` |
| 12/09 | Snapshot `CONTEXTO-CONSOLIDADO-2026-09-12.md` (porcentagem histórica de ~14% **não** recalculada). | `archive/project-docs-workspace-root/` |

## 8. Fase 6 — Intervalo sem commits (12/09 10:04 → 16/09 23:09)

Quatro dias e 13 horas sem commits em nenhuma branch. As fontes não registram o que ocorreu nesse intervalo (testes no jogo, se houve, não deixaram evidência no repositório nem nos documentos do workspace).

## 9. Fase 7 — Multiplayer da Cloche e fechamento do R3 (16–17/09/2026)

| Quando | Evento | Commit |
|---|---|---|
| 16/09 23:09 | **Hardening multiplayer da Cloche**: propriedades de janela em 16 bits assinados (`S31PacketWindowProperty`), config do servidor autoritativa para visuais, tipo de fluido nunca "adivinhado". Tag `garden-cloche-multiplayer-test-1` (23:09:29). | `ac69028c` |
| 16/09 23:18 | Mesmo patch reaplicado sobre a linha R2. | `a55d6fa7` |
| 16/09 23:58 | Gate A5.2 do R2 (build 1m46s, delta +25 entradas vs Cloche T2). | `44bbc72d` |
| 17/09 00:06 | A5.3: R2 instalado no modpack real; 8.716.747 linhas de log varridas; 0 erros de parser. | `a4259bf1` |
| 17/09 01:00 | A5.4: regressão reprodutível 20/20. | `53cfd2bf` |
| 17/09 01:04 | Ferramenta de coleta de evidência A6 (aborta se não houver exatamente um jar R3 com o hash esperado). | `e0e0aa4e` |
| 17/09 | **A5.5 — revisão adversarial rejeita o R2**: (1) estado do processo não chamava `markDirty()` → autosave podia persistir estado velho; (2) energia da GUI sem canal de sincronização → barra de RF obsoleta. | A5.5 |
| 17/09 01:21 | **`5d467a61` — correção do R3** (5 arquivos, +142/−4): `markDirty()` uma vez por tick com processo ativo; propriedade `0` de `ICrafting` sincroniza o RF (32.000 < 32.767). Tag `auto-workbench-test-1-r3` às 01:22:10; jar às 01:24. | `5d467a61` |
| 17/09 01:34 | Smoke de cliente mínimo do R3: 4 mods carregados, 0 `ModelFormatException`. | `R3-MINIMAL-CLIENT-SMOKE.log` (não arquivado; SHA em `data/`) |
| 17/09 01:53 | Fecha o gate A5.6 e endurece a coleta A6 → **HEAD da linha R3**. Pacote `.zip` às 01:54. | `ac41e5e7` |

R3 vs R2 (A5.6): 1.405 → 1.405 entradas; 4 classes alteradas (`TileEntityAutoWorkbench`, `$AutoWorkbenchProcess`, `ContainerAutoWorkbench`, `$1`); 0 membros públicos removidos; 8 assets idênticos.

## 10. Fase 8 — De 17/09 até hoje (25/10/2026)

- Nenhum arquivo mais novo que **17/09/2026 01:54** foi encontrado no workspace local (varredura completa em 25/10/2026).
- O autor informa estar usando `ImmersiveEngineering-auto-workbench-test-1-r3`. O jar anexado a esta tarefa tem SHA-256 `B91A12EB…0A12`, 3.806.758 bytes, 1.405 entradas, 747 classes (todas major 52): **idêntico ao candidato R3 documentado**.
- **Não há registro de execução do A6.** Observações de jogo feitas depois de 17/09 não estão em nenhum documento; devem ser adicionadas por quem as tem.
- 25/10/2026: este dossiê é montado e publicado na branch `1.7.10-backport` do fork.

## 11. Todos os commits (45), em ordem cronológica

Tabela gerada de `data/commits.tsv`. Cada hash abre o commit no fork.

| # | Commit | Data (UTC-3) | Arq. | +/- | Assunto | Tag |
|--:|---|---|--:|---|---|---|
| 1 | [`0c803472`](https://github.com/Akashiic/ImmersiveEngineering/commit/0c803472) | 09/09 10:05 | 54 | +2315/-204 | Recover Forbidden 0.7.9 baseline on RFG | `0.7.9-forbidden-baseline` |
| 2 | [`415ad44c`](https://github.com/Akashiic/ImmersiveEngineering/commit/415ad44c) | 09/09 12:49 | 16 | +2311/-5 | Backport Garden Cloche core for 1.7.10 | `garden-cloche-test-1` |
| 3 | [`1901538d`](https://github.com/Akashiic/ImmersiveEngineering/commit/1901538d) | 09/09 14:21 | 1 | +162/-162 | fix(cloche): correct Wavefront V coordinate handling |  |
| 4 | [`bdf75d13`](https://github.com/Akashiic/ImmersiveEngineering/commit/bdf75d13) | 09/09 14:28 | 5 | +159/-41 | feat(cloche): support MFR fertile soil multiplier |  |
| 5 | [`2162392f`](https://github.com/Akashiic/ImmersiveEngineering/commit/2162392f) | 09/09 14:44 | 3 | +140/-0 | feat(cloche): add Magical Crops v4 adapter |  |
| 6 | [`4d2b92a0`](https://github.com/Akashiic/ImmersiveEngineering/commit/4d2b92a0) | 09/09 16:30 | 2 | +364/-0 | feat(cloche): integrate AgriCraft genetic seeds |  |
| 7 | [`d288da4d`](https://github.com/Akashiic/ImmersiveEngineering/commit/d288da4d) | 09/09 16:37 | 2 | +45/-8 | fix(cloche): refine fertile soil crop semantics |  |
| 8 | [`69bf1d8d`](https://github.com/Akashiic/ImmersiveEngineering/commit/69bf1d8d) | 09/09 16:44 | 2 | +29/-8 | fix(cloche): harden AgriCraft stats and harvest edges |  |
| 9 | [`ad5e6201`](https://github.com/Akashiic/ImmersiveEngineering/commit/ad5e6201) | 09/09 16:47 | 1 | +46/-0 | docs(cloche): add test 2 validation matrix | `garden-cloche-test-2` |
| 10 | [`a36bb241`](https://github.com/Akashiic/ImmersiveEngineering/commit/a36bb241) | 09/09 19:14 | 3 | +413/-8 | docs(backport): select Auto Workbench as feature 2 |  |
| 11 | [`9697b2f5`](https://github.com/Akashiic/ImmersiveEngineering/commit/9697b2f5) | 09/09 19:34 | 1 | +131/-0 | docs(auto-workbench): close archaeology and design gates |  |
| 12 | [`70868d8a`](https://github.com/Akashiic/ImmersiveEngineering/commit/70868d8a) | 09/09 19:38 | 2 | +6/-2 | feat(auto-workbench): add redstone engineering block |  |
| 13 | [`0b201ff6`](https://github.com/Akashiic/ImmersiveEngineering/commit/0b201ff6) | 09/09 19:46 | 2 | +40/-0 | feat(auto-workbench): add automation recipe contract |  |
| 14 | [`6b6578aa`](https://github.com/Akashiic/ImmersiveEngineering/commit/6b6578aa) | 09/09 20:17 | 4 | +1023/-1 | feat(auto-workbench): implement legacy core multiblock |  |
| 15 | [`f2cd3dd3`](https://github.com/Akashiic/ImmersiveEngineering/commit/f2cd3dd3) | 09/09 22:31 | 7 | +285/-4 | feat(auto-workbench): add gui and interaction layer |  |
| 16 | [`b9aa06af`](https://github.com/Akashiic/ImmersiveEngineering/commit/b9aa06af) | 09/09 22:58 | 9 | +3458/-0 | feat(auto-workbench): backport animated renderer |  |
| 17 | [`7a61866b`](https://github.com/Akashiic/ImmersiveEngineering/commit/7a61866b) | 09/09 23:12 | 2 | +480/-0 | feat(auto-workbench): restore blueprint projection |  |
| 18 | [`f7d638cb`](https://github.com/Akashiic/ImmersiveEngineering/commit/f7d638cb) | 10/09 01:35 | 10 | +361/-20 | fix(auto-workbench): harden renderer and manual parity |  |
| 19 | [`e2ee4453`](https://github.com/Akashiic/ImmersiveEngineering/commit/e2ee4453) | 10/09 11:39 | 4 | +441/-3 | docs(auto-workbench): prepare test 1 candidate | `auto-workbench-test-1` |
| 20 | [`97cea596`](https://github.com/Akashiic/ImmersiveEngineering/commit/97cea596) | 10/09 21:31 | 6 | +167/-24 | fix(auto-workbench): adapt static OBJ faces for Forge 1.7 | `auto-workbench-test-1-r2` |
| 21 | [`94df992b`](https://github.com/Akashiic/ImmersiveEngineering/commit/94df992b) | 11/09 15:21 | 5 | +1193/-0 | docs(backport): establish 1.21.1 master engineering track |  |
| 22 | [`e0cd905b`](https://github.com/Akashiic/ImmersiveEngineering/commit/e0cd905b) | 11/09 15:34 | 1 | +144/-0 | docs(audio): design bounded modern sound runtime |  |
| 23 | [`51ce9cc0`](https://github.com/Akashiic/ImmersiveEngineering/commit/51ce9cc0) | 11/09 16:11 | 4 | +576/-0 | docs(backport): audit modern rendering and legacy network |  |
| 24 | [`e7c7e37a`](https://github.com/Akashiic/ImmersiveEngineering/commit/e7c7e37a) | 11/09 18:22 | 1 | +4/-0 | build: pin OBJ and MTL resources to LF |  |
| 25 | [`a1b811a6`](https://github.com/Akashiic/ImmersiveEngineering/commit/a1b811a6) | 11/09 18:28 | 13 | +769/-13 | fix(network): harden legacy tile GUI C2S path |  |
| 26 | [`c5453e22`](https://github.com/Akashiic/ImmersiveEngineering/commit/c5453e22) | 11/09 19:53 | 3 | +151/-0 | test(network): verify hardened C2S artifact and ABI |  |
| 27 | [`7ada56c1`](https://github.com/Akashiic/ImmersiveEngineering/commit/7ada56c1) | 11/09 19:50 | 3 | +449/-0 | docs(backport): define registry expansion gate |  |
| 28 | [`431f3615`](https://github.com/Akashiic/ImmersiveEngineering/commit/431f3615) | 11/09 22:23 | 3 | +537/-0 | docs(alloy-kiln): close archaeology and architecture gates |  |
| 29 | [`5784567a`](https://github.com/Akashiic/ImmersiveEngineering/commit/5784567a) | 11/09 22:24 | 1 | +4/-0 | build: pin OBJ and MTL resources to LF |  |
| 30 | [`174a5228`](https://github.com/Akashiic/ImmersiveEngineering/commit/174a5228) | 11/09 22:45 | 4 | +525/-0 | feat(alloy-kiln): add recipe contract and modern assets |  |
| 31 | [`a1376474`](https://github.com/Akashiic/ImmersiveEngineering/commit/a1376474) | 12/09 01:17 | 23 | +2156/-9 | feat(alloy-kiln): implement modern kiln for legacy runtime | `alloy-kiln-test-1` |
| 32 | [`ccaad104`](https://github.com/Akashiic/ImmersiveEngineering/commit/ccaad104) | 12/09 01:57 | 3 | +402/-0 | test(alloy-kiln): record K4 verification gates |  |
| 33 | [`d7bf99e5`](https://github.com/Akashiic/ImmersiveEngineering/commit/d7bf99e5) | 12/09 02:08 | 3 | +478/-0 | docs(mixer): close archaeology and architecture gates |  |
| 34 | [`c2ddc2bb`](https://github.com/Akashiic/ImmersiveEngineering/commit/c2ddc2bb) | 12/09 02:09 | 3 | +478/-0 | docs(mixer): close archaeology and architecture gates |  |
| 35 | [`f7a4abd4`](https://github.com/Akashiic/ImmersiveEngineering/commit/f7a4abd4) | 12/09 02:57 | 5 | +622/-4 | feat(mixer): add fluid and recipe foundations |  |
| 36 | [`b1fab23f`](https://github.com/Akashiic/ImmersiveEngineering/commit/b1fab23f) | 12/09 03:31 | 2 | +191/-0 | test(mixer): close M1 foundation gate |  |
| 37 | [`7b8863d0`](https://github.com/Akashiic/ImmersiveEngineering/commit/7b8863d0) | 12/09 10:04 | 16 | +605/-0 | feat(mixer): add concrete fluid runtime |  |
| 38 | [`ac69028c`](https://github.com/Akashiic/ImmersiveEngineering/commit/ac69028c) | 16/09 23:09 | 6 | +375/-27 | fix(cloche): harden multiplayer state synchronization | `garden-cloche-multiplayer-test-1` |
| 39 | [`a55d6fa7`](https://github.com/Akashiic/ImmersiveEngineering/commit/a55d6fa7) | 16/09 23:18 | 6 | +375/-27 | fix(cloche): harden multiplayer state synchronization |  |
| 40 | [`44bbc72d`](https://github.com/Akashiic/ImmersiveEngineering/commit/44bbc72d) | 16/09 23:58 | 2 | +105/-5 | docs(auto-workbench): close R2 artifact gate |  |
| 41 | [`a4259bf1`](https://github.com/Akashiic/ImmersiveEngineering/commit/a4259bf1) | 17/09 00:06 | 2 | +56/-1 | docs(auto-workbench): record production startup smoke |  |
| 42 | [`53cfd2bf`](https://github.com/Akashiic/ImmersiveEngineering/commit/53cfd2bf) | 17/09 01:00 | 3 | +141/-2 | test(auto-workbench): add R2 regression gate |  |
| 43 | [`e0e0aa4e`](https://github.com/Akashiic/ImmersiveEngineering/commit/e0e0aa4e) | 17/09 01:04 | 3 | +149/-2 | test(auto-workbench): prepare A6 evidence capture |  |
| 44 | **[`5d467a61`](https://github.com/Akashiic/ImmersiveEngineering/commit/5d467a61)** | 17/09 01:21 | 5 | +142/-4 | fix(auto-workbench): harden process persistence and GUI energy sync | `auto-workbench-test-1-r3` |
| 45 | **[`ac41e5e7`](https://github.com/Akashiic/ImmersiveEngineering/commit/ac41e5e7)** | 17/09 01:53 | 6 | +308/-21 | docs(auto-workbench): close R3 gate and harden A6 evidence |  |

## 12. Tags (8)

| Tag | Tipo | Commit | Data |
|---|---|---|---|
| `0.7.9-forbidden-baseline` | anotada | `0c803472` | 09/09 10:05 |
| `garden-cloche-test-1` | leve | `415ad44c` | 09/09 12:49 |
| `garden-cloche-test-2` | anotada | `ad5e6201` | 09/09 16:47 |
| `auto-workbench-test-1` | anotada | `e2ee4453` | 10/09 11:39 |
| `auto-workbench-test-1-r2` | anotada | `97cea596` | 10/09 21:31 |
| `alloy-kiln-test-1` | anotada | `a1376474` | 12/09 01:57 |
| `garden-cloche-multiplayer-test-1` | anotada | `ac69028c` | 16/09 23:09 |
| `auto-workbench-test-1-r3` | anotada | `5d467a61` | 17/09 01:22 |

Regra do projeto: **tags de candidato são imutáveis**; um defeito gera novo commit, nova tag e novo hash (R1→R2→R3). Não mova nem recrie tags existentes.
