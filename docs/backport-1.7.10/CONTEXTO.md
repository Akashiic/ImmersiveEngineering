# Contexto sólido — Immersive Engineering 1.7.10 (backport do 1.21.1)

Documento de entrada para quem vai trabalhar neste fork. Ele reúne o objetivo, a stack, o que foi construído, as regras do projeto e o que ainda **não** está provado. Todo fato aponta para um commit ou relatório deste repositório; o que depende só de conversas anteriores está marcado. Para datas, veja [CRONOLOGIA.md](CRONOLOGIA.md); para diagramas, [FLUXOGRAMAS.md](FLUXOGRAMAS.md); para continuar de onde parou, [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md).

## 1. Em dez linhas

1. O projeto reconstrói, sobre Minecraft/Forge **1.7.10**, recursos do Immersive Engineering **1.21.1**, partindo da linhagem **Forbidden Series 0.7.9** (IE `1.7.10-0.7.9`).
2. A baseline 0.7.9 foi **recuperada** a partir de uma cadeia documentada (BluSunrize → chaos234 → Bogdan-G → overlay Forbidden); o repositório público literal do Forbidden 0.7.9 não foi encontrado.
3. Stack de build: RetroFuturaGradle, Forge 10.13.4.1614, MCP stable_12, saída Java 8 (classes major 52).
4. **Garden Cloche** foi a primeira feature e a única **aprovada** em jogo (Test 2, 09/09/2026).
5. **Automated Engineer's Workbench** é a feature #2. Passou por três candidatos: Test 1 e R2 foram **rejeitados**; o **R3 é o mais recente** (17/09/2026) e passou em todos os gates estáticos, mas o gate de gameplay **A6 nunca foi executado com evidência**.
6. **Alloy Kiln** (feature #3) e **Mixer** (feature #4, só fundação) estão em outra linha Git, que ainda carrega o Auto Workbench R2 rejeitado.
7. Existem ainda hardening de **rede** (C2S) e de **multiplayer da Cloche**, ambos só com prova estática.
8. O plano mestre mira a **paridade integral com o IE 1.21.1** (398 blocos, 314 itens, 25 multiblocks, 54 sons), em fases dependentes — não é uma lista de promessas.
9. Regra central: **compilar não é aprovar**. Todo recurso passa por uma máquina de estados com evidência por gate.
10. Os jars **não** são publicados (licença); o repositório guarda código-fonte, histórico, relatórios e hashes.

## 2. Objetivo e escopo

Reconstruir, de forma progressiva, o comportamento, máquinas, multiblocks, modelos 3D, texturas, animações, GUIs, sons, automação, equipamentos e integrações do IE 1.21.1 sobre o 1.7.10, preservando mundos, registries e a compatibilidade do modpack Forbidden Series. Uma máquina concluída é marco intermediário, não o fim do projeto.

O escopo começou menor (continuar a linha com recursos selecionados, a partir da Garden Cloche) e foi ampliado pelo usuário em 10/09/2026 às 23:31 (alvo integral 1.21.1) e 23:34 (identidade visual moderna), formalizado em 11/09 pelo `IE-1.21.1-BACKPORT-MASTER-CHARTER.md`.

Não é um downgrade de código: é uma **tradução arquitetural que preserva comportamento**, de NeoForge para contratos do Forge 1.7.10. Snapshot upstream de referência: `BluSunrize/ImmersiveEngineering`, branch `1.21.1`, commit `75a27f03e4243544243567e8d5c38d336f4f10f4` (IE 12.4.3, build 194). Para features próximas da era 1.12, usa-se o snapshot `0.12-89` como ponte de implementação.

## 3. Stack técnica

| Item | Valor | Fonte |
|---|---|---|
| Minecraft / Forge | 1.7.10 / 10.13.4.1614 | `BASELINE-GATE-REPORT.md`, `ROADMAP.md` G2 |
| Build | RetroFuturaGradle; `gradlew clean build --no-daemon` | gates A5.x |
| Mappings | MCP stable_12 | ROADMAP G2 |
| Bytecode | Java 8 (major 52); 747/747 classes no R3 | VALIDATION do R3 |
| Identidade do mod | modid `ImmersiveEngineering`, versão `1.7.10-0.7.9`, "Forbidden Series Edition" | `mcmod.info` |
| Build local | Windows + PowerShell; worktrees Git em `work/` | `ESTADO-ATUAL-E-RETOMADA.md` |
| Opcionais compile-only nos smokes | Magical Crops, AgriCraft, MFR, etc. (somente em worktrees descartáveis) | A5.1, K4 |

**Stack de runtime do pack Forbidden Series** (fatos do inventário congelado, não dependências do IE): UniMixins 0.3.1, Angelica 2.2.8, ArchaicFix 0.8.0, CoreTweaks 0.3.4.7-GTNH, EntityCulling 1.6.4, GeckoLib Unofficial 1.0.4, GTNHLib 0.11.41, Hodgepodge 2.7.189, ParallelMipMap 1.0. Política: Angelica é alvo de teste obrigatório; GTNHLib e GeckoLib só com ganho medido; o mod deve ser correto por conta própria (`RUNTIME-STACK-CAPABILITY-MATRIX.md`).

## 4. Linhagem do código

```text
BluSunrize 1.7.10 @ c45a3a2 (2016-05-09, v0.7.7)
  → chaos234 (era 0.7.8; c38fd6b … 3cf2a38)
    → Bogdan-G 1.7.10 @ 798ce21a (2017-04-28)
      → overlay Forbidden Series 0.7.9 (Java 8, versão, DeveloperCapes)   [reconstruído]
        → 0c803472 baseline recuperada em RFG (09/09/2026)
```

Detalhes e impressões digitais em [PROVENIENCIA.md](PROVENIENCIA.md). DeveloperCapes é um subsistema externo que depende de rede e **não** está aprovado automaticamente para a linha endurecida; está preservado como evidência histórica (e o jar R3 ainda o contém).

## 5. O que existe — por feature

### 5.1 Garden Cloche — APROVADA funcionalmente (Test 2)

Multiblock de três blocos, GUI ID **13**. Crescimento simulado dentro da máquina (sem alterar o mundo), servidor autoritativo. Inventário de 7 slots, tanque de 4.000 mB, energia de 16.000 RF; saída automática de até 16 itens por slot a cada 8 ticks; correção de crescimento sincronizada a cada 32 ticks escalonados.

- **Texturas:** o conversor e o `WavefrontObject` invertiam V em duplicidade; corrigido em `1901538d`.
- **MFR Fertile Soil:** multiplicador por comportamento real — 2,00× vanilla, 1,50× IE Hemp, 31/11 (2,81818×) para Magical Crops direto; a GUI separa solo, fertilizante e total.
- **Magical Crops:** alvo exato `magicalcrops-4.0.0_PUBLIC_BETA_5` (o arquivo traz string interna BETA 4b). Diamond Seeds produz Diamond Essence; a semente permanece.
- **AgriCraft `1.5.2-evernife-v1`:** Growth acelera, Gain altera rendimento, Strength fica no NBT; a máquina **não** faz breeding nem melhora genética.
- **Colheita:** simula todas as saídas antes de confirmar; inventário cheio pausa sem apagar itens.

Limite: o sucesso foi confirmado pelo usuário em jogo (09/09 17:44). Não equivale a PASS individual por cenário, benchmark, soak test ou homologação de todos os mods agrícolas.

### 5.2 Automated Engineer's Workbench — R3 é o mais recente

Backport do multiblock do IE 1.12/1.21 (referência `0.12-89`). Forma-se com **18 peças**: 5 Steel Scaffolding, 2 Treated Wood Slab (um deles é o gatilho, sobre o Redstone Engineering Block), 1 Redstone Engineering Block, 4 Light Engineering, 2 Heavy Engineering, 4 conveyors. Aceita layout espelhado e as quatro orientações.

| Item | Valor |
|---|---|
| Registry | multiblock em `metalMultiblock` meta **14** (Metal Press permanece 13); Redstone Engineering Block em `metalDecoration2` meta **3** (metas 0–2 preservadas); GUI ID **14** |
| Posições lógicas | mestre `pos 13`, hatch de itens + RF `pos 9`, controle de redstone `pos 1` |
| Inventário | slot 0 = Engineer's Blueprint (limite 1, fora de automação); slots 1–16 = componentes |
| Energia | buffer 32.000 RF; entrada só pela parte `pos 9` |
| Processo (padrão) | 180 ticks, 23.040 RF, 128 RF/t nominais; fila de até **3** processos; novo processo só após ≥ 43,75% do anterior; troca visual de input para output após 78% |
| Saída | dropada uma vez a 2 blocos do mestre, ou inserida no inventário adjacente; resto real vira drop, nunca é apagado |
| Redstone | sinal pausa a máquina; Engineer's Hammer inverte a lógica; persistido em NBT |
| Renderer | OBJ estático normal/espelhado + animado; projeção do blueprint sobre a mesa; formato OBJ adaptado ao Forge 1.7 |
| Rede | recebe a seleção de receita via `MessageTileSync`, hoje no caminho legado (veja 5.5) |

**Por que existem três candidatos:**

- **Test 1** (`e2ee4453`): `ModelFormatException` — o `GroupObject` do Forge trava o modo de desenho na primeira face, e o grupo `base_workbench` misturava 289 quads com 4 triângulos. Correção: cada triângulo `A B C` vira o quad degenerado `A B C C` (área zero, normal inalterada).
- **R2** (`97cea596`): passou build, artefato, smoke de cliente e smoke no modpack real, mas a revisão adversarial A5.5 achou (1) **estado sem `markDirty()`** durante processo estável — um autosave podia limpar o bit de "modificado" e perder progresso/RF — e (2) **barra de RF da GUI sem canal de sincronização**.
- **R3** (`5d467a61`): `markDirty()` uma vez por tick em que ao menos um processo avança; propriedade `0` de `ICrafting` carrega o RF para a GUI (capacidade 32.000 cabe no `short` assinado de 32.767).

Decisão registrada sobre o índice de processos: não se adicionou bitmask por processo, porque o desencontro só seria alcançável com custos diferentes na mesma fila, e os modificadores só mudam em `Config.init()`.

Matriz de gameplay **A6**: `docs/AUTO_WORKBENCH_TEST_1.md`, T0–T18 (boot, bloco, manual, formação normal e espelhada, GUI, receita inválida, `bullet`, `electrode`, fila e energia, redstone, automação, saída/backpressure, NBT/restart, desmontagem, renderer, compatibilidade com o pack, carga/performance, evidências). Nenhum desses itens tem resultado registrado.

### 5.3 Alloy Kiln — `alloy-kiln-test-1`, K4 PASS

Multiblock "Alloy Smelter/Alloy Kiln" do 1.21.1, com GUI ID **15**, blocos em `stoneDecoration2` e `stoneDevice2`, TileEntity novo, NEI e MineTweaker. 23 assets gerados/copiados do commit 1.21.1 congelado (manifest com SHA-256). Receitas de Bronze e Invar por entradas dimensionadas; **Manyullyn ausente de propósito** (Netherite Scrap não tem equivalente semântico provado no 1.7.10). Textura do Insulating Glass substituída pela versão 16×16 RGBA, sem mudar identidade/meta.

K4 provou build, artefato, ABI e smoke de cliente. **Não** provou servidor dedicado (parou no EULA, não aceito automaticamente) nem gameplay (K5).

### 5.4 Mixer — apenas fundação

- **M1 (PASS):** `MultiFluidTank` (multifluido ordenado com capacidade), `MixerRecipe` (fluido + item), planejamento exato de consumo entre slots (resolve casos de ingredientes sobrepostos que o planejador guloso rejeitava), revisão de lista de receitas. Autoteste `MIXER_M1_SELF_TEST_PASS` e probe FML `MIXER_M1_RUNTIME_PROBE_PASS amount=875 types=3`. **Não aprova a máquina Mixer.**
- **M2 (código, sem gate):** `7b8863d0` adiciona identidades de fluido, ciclo do concreto, assets e autoteste. Nenhum relatório de aprovação M2 foi localizado. O design inicial chama o M2 de "multiblock/registry"; o fechamento M1 o redefine como fluidos e Concrete antes do multiblock — nomenclatura a reconciliar.

### 5.5 Hardening de rede C2S — estático PASS, runtime pendente

O handler legado de `MessageTileSync` mutava o mundo na thread do Netty e confiava em dimensão/coordenadas/NBT do cliente. A correção (`a1b811a6`): o Netty só guarda um payload limitado (≤ 32.769 bytes codificados) e o servidor executa em `ServerTickEvent.START` com limites de **64 pendentes globais, 4 por jogador, 16 ações por tick**, NBT inflado com limite de **256 KiB**, e valida jogador autenticado, mesma dimensão, distância ≤ 64² blocos, chunk já carregado e container aberto no tile exato. Consumidores: Conveyor Sorter, Assembler (índices e slots validados) e Auto Workbench.

Ainda **fora** deste gate e pendentes: `MessageRequestBlockUpdate` e o sync do shader de minecart — há trabalho **não commitado** sobre isso arquivado em `archive/wip-uncommitted/network-hardening/`.

### 5.6 Hardening multiplayer da Cloche — estático PASS, runtime MP pendente

Três defeitos provados e corrigidos (`ac69028c`): (1) `S31PacketWindowProperty` serializa ID e valor em `short` — valores acima de 32.767 chegavam negativos; agora vão em duas metades de 16 bits; (2) GUI e render usavam a config **local** do cliente para duração do fertilizante e modificador de crescimento; agora o servidor sincroniza ambos; (3) o container reconstruía todo tanque como água; agora preserva o `FluidStack` sincronizado. Itens históricos conferidos e já cobertos estão listados em `GARDEN-CLOCHE-MULTIPLAYER-AUDIT.md`.

## 6. Registry e save-compatibility

Estado auditado em 11/09 (`REGISTRY-EXPANSION-ARCHITECTURE.md`): `metalMultiblock` metas 0–14 ocupadas (**só a 15 livre**); `metalDevice2` 0–12; `metalDecoration2` 0–3 (depois do Auto Workbench); GUI IDs 0–14 ocupados; 61 TileEntities registradas. Regras:

- nunca remapear uma meta existente; metas livres só com prova de que nunca foram usadas;
- meta 15 de `metalMultiblock` é **reserva estratégica**; novos multiblocks vão para famílias novas (`metalMultiblock2`, `metalDevice3`…);
- GUI IDs **0–14 congelados; 15–31 reservados; alocação moderna começa em 32**. *Inconsistência real:* o Alloy Kiln usou o ID 15 — dentro da faixa reservada. Veja [HIPOTESES-E-INCIDENTES.md](HIPOTESES-E-INCIDENTES.md);
- TileEntities novas exigem auditoria de colisão de ID; nada de reutilizar string antiga;
- `tools/audit_registry_surface.py` regenera `IE-1.7.10-REGISTRY-SURFACE.json`.

## 7. Gates e governança

Roadmap G0–G11 (`docs/engineering/ROADMAP.md`, estado no tip da linha R3):

| Gate | Estado |
|---|---|
| G0 evidência congelada | PASS |
| G1 proveniência / G2 build moderno | PASS |
| G3 paridade de runtime | **ativo/parcial** — release-parity aberta |
| G4 hardening legacy (quentin452, vladislemon…) | planejado |
| G5 framework de backport | PASS parcial |
| G6 Garden Cloche, G7 Magical Crops, G8 AgriCraft | PASS (Test 2) |
| G8.5 auditoria de lacunas modernas | PASS (09/09) |
| G8.6 Auto Workbench | A5.6 PASS; **A6 pronto após instalar o R3**; A7/A8 bloqueados |
| G9 ecossistema amplo | adiado |
| G10 performance e render / G11 release | abertos; distribuição pública só após revisão de licença |

Máquina de estados por feature: `DISCOVERED → PROVENANCE_LOCKED → DESIGNED → IMPLEMENTED → STATIC_PASS → RUNTIME_TEST_1 → ADVERSARIAL_FIX → RUNTIME_TEST_2 → APPROVED` (ou `DEFERRED`/`REJECTED`). Falha de gate volta à **menor camada responsável**; "NOT TESTED", "NON-CONCLUSIVE" e "ASSUMED" nunca valem PASS.

Fases do plano mestre: P0 baseline → P1 fechar Auto Workbench → P2 classificar toda a superfície 1.21.1 → P3 Alloy Kiln e Mixer → P4 Sawmill e processamento moderno → P5 equipamentos → P6 conveyors/redstone/fluidos → P7 fios/conectores/feedthrough → P8 decoração → P9 Radio Tower e sistemas distantes → P10 auditoria de paridade e release. A ordem é por dependência, **não** promessa de aceitar tudo.

## 8. Regras recuperadas para continuidade

- Qualidade técnica e evidência vencem velocidade.
- Separar hipótese, código, teste automatizado, inicialização e gameplay observado.
- Nunca sobrescrever o jar de produção durante desenvolvimento; candidatos congelados são imutáveis (tag nunca se move).
- Evoluir por famílias completas, com visual/som quando aplicável.
- Usar versões exatas do pack; não deduzir compatibilidade pelo nome.
- Sem merge em massa de forks: cada patch tem proveniência, aplicabilidade, conflito, teste e aprovação/rejeição registrada.
- Contenção de escopo: falha em AgriCraft não autoriza reescrever a rede de fios.
- Cliente nunca é fonte de verdade; todo pacote tem discriminador, limites, validação de lado/dimensão/chunk/distância e nada de I/O bloqueante no Netty.
- Nenhuma máquina varre área ilimitada por tick; renderer não reconstrói geometria invariável por frame.

## 9. Licença

O código-base 1.7.10 do BluSunrize não traz arquivo `LICENSE` nesta árvore; a "Blu's License of Common Sense" permite fork mas proíbe redistribuir código ou binários compilados sem permissão e monetizar. Por isso este repositório **não publica jars**, só código-fonte, histórico, documentação e SHA-256. Os assets modernos copiados do 1.21.1 seguem a licença do upstream; o charter exige revisão de licença/permissão **antes** de qualquer release público. Veja `THIRD_PARTY_NOTICES.md`.

## 10. Glossário

| Termo | Significado |
|---|---|
| A0–A8 | gates do Auto Workbench (arqueologia → aprovação) |
| K4 / K5 | Alloy Kiln: verificação estática e smoke / gameplay real |
| M1 / M2 | Mixer: fundação multifluido e receitas / fluidos e concreto |
| Gate | critério de aprovação com evidência; falhar volta à menor camada |
| Candidato | jar de teste com commit, tag e SHA-256 imutáveis |
| C2S | pacote cliente → servidor |
| RFG | RetroFuturaGradle |
| Forbidden Series | modpack/edição do IE 0.7.9 alvo do projeto |
| R1/R2/R3 | primeira, segunda e terceira tentativa do Auto Workbench (R1 = `auto-workbench-test-1`) |
| Quadify | conversão de triângulo `A B C` em quad degenerado `A B C C` |

## 11. Onde está cada coisa

| Quer… | Vá para |
|---|---|
| Datas, commits, tags | [CRONOLOGIA.md](CRONOLOGIA.md) |
| Diagramas | [FLUXOGRAMAS.md](FLUXOGRAMAS.md), [`diagrams/`](diagrams/) |
| Retomar o trabalho | [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md) |
| Saber exatamente o que o jar R3 contém | [BUILD-EM-USO.md](BUILD-EM-USO.md) |
| Hashes de todos os jars | [ARTEFATOS.md](ARTEFATOS.md) |
| Origem do código, método, limites | [PROVENIENCIA.md](PROVENIENCIA.md) |
| Problemas conhecidos e hipóteses | [HIPOTESES-E-INCIDENTES.md](HIPOTESES-E-INCIDENTES.md) |
| Dados brutos | [`data/`](data/) |
| Documentos originais, byte a byte | [`archive/`](archive/INDEX.md) |
| Relatórios de gate por feature | `docs/engineering/` na branch de cada feature (veja a tabela de branches) |
