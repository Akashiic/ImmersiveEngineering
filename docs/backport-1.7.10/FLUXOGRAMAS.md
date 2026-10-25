# Fluxogramas — Immersive Engineering 1.7.10 (backport 1.21.1)

Doze diagramas Mermaid, renderizados direto pelo GitHub. Cada um também existe como arquivo `.mmd` em [`diagrams/`](diagrams/) para editar ou exportar (`mmdc -i arquivo.mmd -o arquivo.svg`). Todos foram validados com o Mermaid CLI antes da publicação.

**Legenda de cores nos diagramas de estado:** verde = aprovado/PASS; vermelho = rejeitado/bloqueado; laranja forte = **versão mais recente (R3) ou ponto de retomada**; azul = estático PASS com runtime pendente; cinza = histórico/superado.

Leia junto: [CRONOLOGIA.md](CRONOLOGIA.md) (datas e commits) e [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md) (o que fazer agora).

---

## 1. Linhagem Git e linhas divergentes

O diagrama mais importante para quem chega: mostra que o R3 (laranja) e a linha Alloy Kiln/Mixer/rede **saem do mesmo ponto (R2) e não se encontram**.

```mermaid
flowchart LR
  classDef ok fill:#d4edda,stroke:#2e7d32,color:#111
  classDef rej fill:#f8d7da,stroke:#c62828,color:#111
  classDef latest fill:#fff3cd,stroke:#ef6c00,stroke-width:3px,color:#111
  classDef pend fill:#e8eaf6,stroke:#3949ab,color:#111
  classDef up fill:#eceff1,stroke:#607d8b,color:#111

  subgraph ANC["Ancestralidade upstream"]
    direction TB
    U0["BluSunrize 1.7.10<br/>c45a3a2 · 2016-05-09<br/>Build and Release v0.7.7"]:::up
    U1["chaos234<br/>c38fd6b → 3cf2a38<br/>era 0.7.8"]:::up
    U2["Bogdan-G 1.7.10<br/>798ce21a · 2017-04-28<br/>XSTR · split de energia"]:::up
    U0 --> U1 --> U2
  end

  B0["0c803472 · 09/09<br/>Baseline Forbidden 0.7.9 em RFG<br/>tag 0.7.9-forbidden-baseline"]:::ok
  U2 --> B0

  subgraph CL["Garden Cloche"]
    direction TB
    C1["415ad44c<br/>tag garden-cloche-test-1<br/>substituída"]:::up
    C2["6 commits de correção<br/>UV · MFR · Magical Crops · AgriCraft"]:::up
    C3["ad5e6201 · 09/09 16:47<br/>tag garden-cloche-test-2<br/>APROVADA funcional"]:::ok
    C1 --> C2 --> C3
  end
  B0 --> C1

  MP["ac69028c · 16/09<br/>tag garden-cloche-multiplayer-test-1<br/>estático PASS · runtime MP pendente"]:::pend
  C3 --> MP

  subgraph AW["Auto Workbench · linha R3"]
    direction TB
    A0["9 commits · a36bb241 … f7d638cb<br/>A0–A4 · 09–10/09"]:::up
    A1["e2ee4453<br/>tag auto-workbench-test-1<br/>REJEITADO · OBJ"]:::rej
    A2["97cea596<br/>tag auto-workbench-test-1-r2<br/>REJEITADO · A5.5"]:::rej
    A3["44bbc72d → a4259bf1 → 53cfd2bf → e0e0aa4e<br/>docs A5.2/A5.3 · regressão · coletor A6"]:::up
    A4["5d467a61 · 17/09 01:21<br/>tag auto-workbench-test-1-r3<br/>MAIS RECENTE · A6 pendente"]:::latest
    A5["ac41e5e7 · 17/09 01:53<br/>HEAD feature/auto-workbench<br/>docs A5.6 + protocolo A6"]:::latest
    A0 --> A1 --> A2 --> A3 --> A4 --> A5
  end
  C3 --> A0

  HM["a55d6fa7 · 16/09<br/>hardening/auto-r2-cloche-mp<br/>MP da Cloche sobre o R2"]:::pend
  A2 --> HM

  subgraph CH["Plano mestre 1.21.1 · linha Alloy/Mixer/rede"]
    direction TB
    P1["94df992b → e0cd905b → 51ce9cc0<br/>charter · áudio · render/rede · 11/09"]:::up
    N1["e7c7e37a → a1b811a6<br/>hardening/network-gate<br/>C2S enfileirado"]:::pend
    N2["c5453e22<br/>hardening/network-gate-chief<br/>verificação estática"]:::pend
    R1["7ada56c1 → 431f3615<br/>gate de registry · Alloy A0–A2"]:::up
    K1["5784567a → 174a5228 → a1376474<br/>tag alloy-kiln-test-1<br/>K4 PASS"]:::ok
    K2["ccaad104<br/>HEAD feature/alloy-kiln"]:::up
    X1["c2ddc2bb → f7a4abd4 → b1fab23f<br/>Mixer A0–A2 + M1 PASS"]:::ok
    X2["7b8863d0 · 12/09 10:04<br/>HEAD feature/mixer<br/>fluido Concrete · sem gate M2"]:::pend
    D1["d7bf99e5<br/>HEAD chief/ie-1.21.1-masterplan"]:::up
    P1 --> N1 --> N2
    P1 --> R1 --> K1 --> K2
    K1 --> X1 --> X2
    R1 --> D1
  end
  A2 --> P1
```

## 2. Cronologia visual

### 2a. Ancestralidade do código

```mermaid
timeline
  title Ancestralidade do código (upstream → Forbidden 0.7.9)
  2016-05-09 : BluSunrize 1.7.10 c45a3a2 "Build and Release v0.7.7" : linha oficial de referência
  2016–2017 : chaos234 continua o ramo 1.7.10 (era 0.7.8) : o jar implantado aponta contributorRevolvers.json para chaos234
  2017-04-28 : Bogdan-G 1.7.10 798ce21a : split de energia · fix de fio em mundo force-loaded · crash de poção · XSTR · conversão para float
  2023-09 : jar Forbidden Series 0.7.9 recompilado em Java 8 : DeveloperCapes embutido · metadado "Forbidden Series Edition"
  2026-09-09 : baseline recuperada em RFG (0c803472) : início do backport 1.21.1 → 1.7.10
```

### 2b. 09–12/09/2026

As barras vão de um commit ao seguinte; **não** medem esforço.

```mermaid
gantt
  title 09–12/09/2026 · Cloche, Auto Workbench R2, plano mestre, rede, Alloy Kiln, Mixer
  dateFormat YYYY-MM-DD HH:mm
  axisFormat %d/%m %Hh
  tickInterval 6hour

  section Baseline e Cloche
  Baseline 0.7.9 recuperada (0c803472)              :done, s1, 2026-09-09 01:35, 2026-09-09 10:05
  Cloche Test 1 → Test 2 (ad5e6201)                 :done, s2, 2026-09-09 10:05, 2026-09-09 16:47
  Aprovação funcional da Cloche às 17h44            :milestone, m1, 2026-09-09 17:44, 0m

  section Auto Workbench
  A0–A4 implementação (9 commits)                   :done, a1, 2026-09-09 19:14, 2026-09-10 01:35
  Test 1 (e2ee4453) rejeitado → R2 (97cea596)       :done, a2, 2026-09-10 11:39, 2026-09-10 21:31

  section Plano mestre e rede
  Charter 1.21.1 · áudio · render (94df992b…)       :done, p1, 2026-09-11 15:21, 2026-09-11 16:11
  Hardening C2S (a1b811a6 → c5453e22)               :done, p2, 2026-09-11 18:22, 2026-09-11 19:53
  Gate de registry (7ada56c1)                       :milestone, p3, 2026-09-11 19:50, 0m

  section Alloy Kiln
  A0–A2 → implementação → K4 (ccaad104)             :done, k1, 2026-09-11 22:23, 2026-09-12 01:57

  section Mixer
  A0–A2 e M1 PASS (b1fab23f)                        :done, x1, 2026-09-12 02:08, 2026-09-12 03:31
  Fluido Concrete sem gate (7b8863d0)               :milestone, x2, 2026-09-12 10:04, 0m
```

### 2c. 16–17/09/2026

```mermaid
gantt
  title 16–17/09/2026 · Hardening MP da Cloche e fechamento do Auto Workbench R3
  dateFormat YYYY-MM-DD HH:mm
  axisFormat %d/%m %Hh
  tickInterval 1hour

  section Multiplayer da Cloche
  Patch MP (ac69028c · a55d6fa7)                    :done, h1, 2026-09-16 23:09, 2026-09-16 23:18

  section Auto Workbench
  Gate A5.2 do R2 (44bbc72d)                        :done, a1, 2026-09-16 23:18, 2026-09-16 23:58
  A5.3 smoke em produção (a4259bf1)                 :done, a2, 2026-09-16 23:58, 2026-09-17 00:06
  A5.4 regressão R2 20/20 (53cfd2bf)                :done, a3, 2026-09-17 00:06, 2026-09-17 01:00
  Coletor A6 (e0e0aa4e)                             :done, a4, 2026-09-17 01:00, 2026-09-17 01:04
  A5.5 R2 rejeitado → R3 (5d467a61)                 :crit, done, a5, 2026-09-17 01:04, 2026-09-17 01:21
  Tag R3 às 01h22 · jar às 01h24                    :milestone, a6, 2026-09-17 01:22, 0m
  Docs A5.6 (ac41e5e7)                              :done, a7, 2026-09-17 01:22, 2026-09-17 01:53
```

Entre 2b e 2c há um intervalo de 4 dias e 13 horas sem commits; depois de 2c, nada mais foi commitado até 25/10/2026.

## 3. Fluxo de gates do Auto Workbench (A0 → A8)

Mostra os dois ciclos de rejeição (OBJ misto no Test 1; estado/sincronização no R2) e onde o R3 está: **A6 pendente**.

```mermaid
flowchart TD
  classDef pass fill:#d4edda,stroke:#2e7d32,color:#111
  classDef fail fill:#f8d7da,stroke:#c62828,color:#111
  classDef pend fill:#fff3cd,stroke:#ef6c00,stroke-width:3px,color:#111
  classDef block fill:#eceff1,stroke:#90a4ae,color:#555,stroke-dasharray:4 3

  A0["A0 arqueologia de fonte e assets<br/>PASS"]:::pass --> A1["A1 registry · meta · TileEntity · GUI-ID<br/>PASS"]:::pass
  A1 --> A2["A2 arquitetura legacy · NBT · transação de processo<br/>PASS"]:::pass
  A2 --> A3["A3 implementação · compile · auditoria estática<br/>PASS"]:::pass
  A3 --> A4["A4 GUI · render · assets · lado dedicado<br/>PASS"]:::pass
  A4 --> A5["A5 artefato do tag auto-workbench-test-1<br/>PASS estático"]:::pass
  A5 --> A51{"A5.1 smoke de cliente isolado<br/>ModelFormatException no OBJ"}
  A51 -- "FALHOU: grupo com 289 quads + 4 triângulos" --> REJ1["auto-workbench-test-1<br/>REJEITADO · tag imutável"]:::fail
  REJ1 --> FIX1["quadify: triângulo A B C vira quad A B C C"]
  FIX1 --> A52["A5.2 R2 · build limpo · 1.405 entradas<br/>PASS"]:::pass
  A52 --> A53["A5.3 smoke no modpack real<br/>8,7 M de linhas · 0 erros de parser<br/>PASS"]:::pass
  A53 --> A54["A5.4 regressão reprodutível 20/20<br/>PASS"]:::pass
  A54 --> A55{"A5.5 revisão adversarial de estado"}
  A55 -- "markDirty ausente + RF da GUI sem canal de sync" --> REJ2["auto-workbench-test-1-r2<br/>REJEITADO em 17/09 · tag imutável"]:::fail
  REJ2 --> FIX2["R3: markDirty por tick ativo<br/>+ propriedade 0 em ICrafting"]
  FIX2 --> A56["A5.6 R3 · build limpo 2m55s<br/>artefato 21/21 · smoke 4 mods<br/>PASS"]:::pass
  A56 --> A6{{"A6 gameplay T0–T18 no Forbidden Series<br/>PENDENTE · sem evidência registrada<br/>VOCÊ ESTÁ AQUI"}}:::pend
  A6 -- "PASS + logs revisados" --> A8["A8 aprovação e congelamento"]:::block
  A6 -- "FAIL ou conflito logs × jogo" --> A7["A7 correção adversarial · Test 2"]:::block
  A7 --> A6
```

## 4. Máquina de estados de aprovação de uma feature

Definida em `IE-1.21.1-BACKPORT-MASTER-CHARTER.md` (branch `feature/mixer`).

```mermaid
stateDiagram-v2
  direction LR
  [*] --> DISCOVERED
  DISCOVERED --> PROVENANCE_LOCKED: fonte e licença verificadas
  PROVENANCE_LOCKED --> DESIGNED: A0 a A2
  DESIGNED --> IMPLEMENTED: código compila
  IMPLEMENTED --> STATIC_PASS: build, artefato, ABI, smoke
  STATIC_PASS --> RUNTIME_TEST_1: jogo real
  RUNTIME_TEST_1 --> ADVERSARIAL_FIX: falha comprovada
  ADVERSARIAL_FIX --> RUNTIME_TEST_2: novo candidato
  RUNTIME_TEST_1 --> APPROVED: evidência completa
  RUNTIME_TEST_2 --> APPROVED: evidência completa
  RUNTIME_TEST_2 --> ADVERSARIAL_FIX: nova falha
  DISCOVERED --> DEFERRED: custo ou risco
  DESIGNED --> REJECTED: licença ou arquitetura
  APPROVED --> [*]
  note right of STATIC_PASS
    Compilar não é aprovar.
    NOT TESTED, NON-CONCLUSIVE
    e ASSUMED nunca valem PASS.
  end note
```

## 5. Estado de cada frente nessa máquina

```mermaid
flowchart LR
  classDef stage fill:#eceff1,stroke:#607d8b,color:#111
  classDef ok fill:#d4edda,stroke:#2e7d32,color:#111
  classDef latest fill:#fff3cd,stroke:#ef6c00,stroke-width:3px,color:#111
  classDef pend fill:#e8eaf6,stroke:#3949ab,color:#111

  S1["DESIGNED"]:::stage --> S2["IMPLEMENTED"]:::stage --> S3["STATIC_PASS"]:::stage --> S4["RUNTIME_TEST_1"]:::stage --> S5["APPROVED"]:::stage

  F1["Garden Cloche Test 2<br/>aprovada funcional 09/09"]:::ok -.-> S5
  F2["Auto Workbench R3<br/>A5.6 PASS · A6 pendente<br/>MAIS RECENTE"]:::latest -.-> S3
  F3["Alloy Kiln Test 1<br/>K4 PASS · K5 pendente"]:::pend -.-> S3
  F4["Cloche multiplayer<br/>estático PASS · MP pendente"]:::pend -.-> S3
  F5["Hardening de rede C2S<br/>estático PASS · runtime pendente"]:::pend -.-> S3
  F6["Mixer M1 fundação<br/>PASS só da fundação"]:::pend -.-> S3
  F7["Mixer M2 fluido Concrete<br/>código sem gate"]:::pend -.-> S2
  F8["Sawmill e demais famílias<br/>fila do plano mestre"]:::pend -.-> S1
```

## 6. Cadeia de proveniência do código

```mermaid
flowchart TD
  classDef verified fill:#d4edda,stroke:#2e7d32,color:#111
  classDef recovered fill:#fff3cd,stroke:#ef6c00,color:#111
  classDef ours fill:#e8eaf6,stroke:#3949ab,color:#111
  classDef warn fill:#f8d7da,stroke:#c62828,color:#111

  O["BluSunrize/ImmersiveEngineering<br/>branch 1.7.10 @ c45a3a2<br/>Build and Release v0.7.7 · 2016-05-09"]:::verified
  T["tag remota 0.7.7<br/>HISTÓRIA DIVERGENTE<br/>não é ancestral do HEAD"]:::warn
  C["chaos234/ImmersiveEngineering<br/>c38fd6b → 3cf2a38 · era 0.7.8"]:::verified
  B["Bogdan-G/ImmersiveEngineering<br/>798ce21a · 2017-04-28"]:::verified
  FA["Impressão A: split de energia no ConnectorLV"]:::verified
  FB["Impressão B: org/bogdang/.../XSTR.class"]:::verified
  FC["Impressão C: família float/GL no Matrix4 e renderers"]:::verified
  D["Overlay Forbidden Series 0.7.9<br/>Java 8 · versão · DeveloperCapes embutido"]:::recovered
  J["Jar implantado<br/>SHA f68b4bef…92fb8"]:::recovered
  X["Repositório público literal do Forbidden 0.7.9<br/>NÃO LOCALIZADO"]:::warn
  R["0c803472 · baseline recuperada em RFG<br/>694 classes · 463 .java idênticos · 32 diferenças classificadas"]:::ours
  O -. "tag fora da linhagem" .- T
  O --> C --> B
  B --- FA
  B --- FB
  B --- FC
  B --> D --> J
  J --> R
  X -. "ausente" .- D
```

## 7. Arquitetura em camadas

```mermaid
flowchart TB
  subgraph L5["Backports de recursos"]
    direction LR
    GC["Garden Cloche"]
    AW["Auto Workbench"]
    AK["Alloy Kiln"]
    MX["Mixer em fundação"]
    NX["Sawmill e próximas famílias"]
  end
  subgraph L4["Camada de adaptadores de integração"]
    direction LR
    V["Vanilla"]
    H["IE Hemp"]
    MC["Magical Crops BETA 5"]
    AC["AgriCraft evernife-v1"]
    MFR["MFR Fertile Soil"]
  end
  subgraph L3["Fachada de compatibilidade legacy"]
    direction LR
    REG["registry · metas congeladas"]
    NBT["NBT versionado"]
    EN["energia RF"]
    FL["fluidos"]
    NET["rede C2S com fila no servidor"]
  end
  subgraph L2["IE 1.7.10 endurecido"]
    BASE["0.7.9 recuperado + correções legacy auditadas"]
  end
  subgraph L1["Build e testes"]
    BLD["RetroFuturaGradle · Forge 10.13.4.1614 · MCP stable_12 · saída Java 8"]
  end
  L5 --> L4 --> L3 --> L2 --> L1
```

## 8. Árvore de decisão para retomar

```mermaid
flowchart TD
  classDef go fill:#d4edda,stroke:#2e7d32,color:#111
  classDef stop fill:#f8d7da,stroke:#c62828,color:#111
  classDef act fill:#fff3cd,stroke:#ef6c00,color:#111

  S(["Quero retomar o IE 1.7.10"]) --> Q1{"O que quero fazer?"}
  Q1 -- "Testar o que existe" --> J1["Instalar EXATAMENTE 1 jar<br/>auto-workbench-test-1-r3 · SHA B91A12EB…0A12"]:::act
  J1 --> J2["python tools/collect_auto_workbench_a6_evidence.py --start --label auto-workbench-test-1-r3"]:::act
  J2 --> J3["Executar T0 a T18 de docs/AUTO_WORKBENCH_TEST_1.md"]:::act
  J3 --> J4["Fechar o jogo e rodar --collect"]:::act
  J4 --> J5{"Logs e jogo concordam?"}
  J5 -- "sim" --> J6["A6 PASS → A8 aprovação"]:::go
  J5 -- "não" --> J7["registrar defeito → A7 correção → R4"]:::stop
  Q1 -- "Continuar desenvolvendo" --> Q2{"Qual linha?"}
  Q2 -- "Auto Workbench" --> B1["branch feature/auto-workbench · tag R3<br/>não mover tags existentes"]:::act
  Q2 -- "Alloy Kiln ou Mixer" --> B2["branch feature/mixer contém Alloy<br/>mas carrega o Auto Workbench R2 REJEITADO"]:::stop
  Q2 -- "Integrar tudo" --> B3["ver 09-integracao-proposta<br/>cherry-picks aplicam limpo · build não executado"]:::act
  Q1 -- "Publicar um release" --> P1["bloqueado: licença sem permissão<br/>G10 performance e G11 abertos"]:::stop
```

## 9. Integração proposta das linhas divergentes

**Proposta, não execução.** Um teste de aplicação em worktree descartável (`git cherry-pick -n`, sem build) mostrou que o código aplica; apenas `docs/engineering/ROADMAP.md` conflita. Detalhes e limites em [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md#5-integração-das-linhas-divergentes).

```mermaid
flowchart TD
  classDef base fill:#eceff1,stroke:#607d8b,color:#111
  classDef pick fill:#fff3cd,stroke:#ef6c00,color:#111
  classDef todo fill:#f8d7da,stroke:#c62828,color:#111

  M["BASE: origin/feature/mixer · 7b8863d0<br/>contém Cloche T2 + Auto Workbench R2 + Alloy Kiln + Mixer M1/M2 parcial<br/>já contém o pino LF de OBJ (5784567a)"]:::base
  P1["cherry-pick a55d6fa7<br/>multiplayer da Cloche sobre linha R2<br/>dry-run: aplica limpo"]:::pick
  P2["cherry-pick 5d467a61<br/>correção de estado do R3<br/>dry-run: só ROADMAP.md conflita (doc)"]:::pick
  P3["cherry-pick a1b811a6<br/>rede C2S enfileirada<br/>dry-run: aplica limpo · toca o mesmo TileEntityAutoWorkbench.java"]:::pick
  G["Pendente: build limpo · auditoria de artefato · ABI · smoke de cliente · A6 · K5 · runtime dedicado"]:::todo
  M --> P1 --> P2 --> P3 --> G
```

## 10. Candidatos e substituições

```mermaid
flowchart LR
  classDef ok fill:#d4edda,stroke:#2e7d32,color:#111
  classDef rej fill:#f8d7da,stroke:#c62828,color:#111
  classDef latest fill:#fff3cd,stroke:#ef6c00,stroke-width:3px,color:#111
  classDef pend fill:#e8eaf6,stroke:#3949ab,color:#111
  classDef old fill:#eceff1,stroke:#90a4ae,color:#444

  G1["garden-cloche-test-1<br/>E03A71CA…<br/>superada"]:::old --> G2["garden-cloche-test-2<br/>51CF081E…<br/>APROVADA funcional"]:::ok
  G2 --> GM["garden-cloche-multiplayer-test-1<br/>17F6F3D5…<br/>estático PASS"]:::pend
  G2 --> W1["auto-workbench-test-1<br/>94547AA9…<br/>REJEITADO"]:::rej
  W1 --> W2["auto-workbench-test-1-r2<br/>C72F7C4A…<br/>REJEITADO 17/09"]:::rej
  W2 --> W3["auto-workbench-test-1-r3<br/>B91A12EB…<br/>MAIS RECENTE · A6 pendente"]:::latest
  W2 --> K1["alloy-kiln-test-1<br/>F03FC1EC…<br/>K4 PASS · K5 pendente"]:::pend
```
