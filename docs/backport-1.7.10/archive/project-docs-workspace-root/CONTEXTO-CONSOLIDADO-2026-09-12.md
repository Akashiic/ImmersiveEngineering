# Immersive Engineering — contexto consolidado para continuidade

Snapshot recuperado em 12/09/2026. Horários da cronologia: America/Sao_Paulo.

Este documento reúne conversas recuperadas, commits, tags, relatórios locais e hashes recalculados. É um ponto de entrada para retomada; não substitui os relatórios de cada gate nem afirma que testes antigos foram repetidos nesta recuperação. O workspace pode continuar avançando depois deste snapshot.

## Objetivo vigente

Reconstruir progressivamente os recursos do Immersive Engineering 1.21.1 sobre Minecraft/Forge 1.7.10, na linhagem Forbidden Series 0.7.9: comportamento, máquinas, multiblocos, modelos 3D, texturas, animações, GUIs, sons, automação, equipamentos e integrações. Preservar mundos, registries e compatibilidade do pack. Uma máquina concluída é um marco intermediário, não a conclusão do projeto.

O objetivo inicial era uma continuação com recursos selecionados, começando pela Garden Cloche. Em 10/09 às 23h31, na conversa “Recuperar histórico do projeto”, o usuário explicitou o alvo integral 1.21.1 -> 1.7.10 e, às 23h34, reforçou a identidade visual moderna. Em 11/09 a frente formal de engenharia registrou esse objetivo.

O snapshot moderno documentado é `75a27f03e4243544243567e8d5c38d336f4f10f4`, IE 12.4.3 build 194. A linha 1.12.2 serve como ponte de implementação quando útil, sem substituir o alvo funcional 1.21.1.

## Cronologia reconstruída

| Data local | Marco | Evidência e limite |
|---|---|---|
| 09/09, 01h35 | Pedido inicial de fork, Garden Cloche, Magical Crops BETA 5 e AgriCraft | Mensagem inicial recuperada da conversa “Criar Fork Immersive Engineering”. |
| 09/09, 10h05 | Recuperação da baseline Forbidden 0.7.9 | Commit `0c803472`, tag `0.7.9-forbidden-baseline`; comparação semântica e build registradas. |
| 09/09, 12h49 | Garden Cloche core / Test 1 | Commit e tag `415ad44c` / `garden-cloche-test-1`; máquina, GUI e renderização implementados. |
| 09/09, primeira rodada no jogo | Usuário confirmou água, energia, fertilizante e funcionamento, mas reportou texturas incorretas, solo MFR sem efeito e integrações agrícolas ausentes | Vídeo e TesteLogs.zip constam como anexos históricos; não foram novamente inspecionados nesta recuperação. |
| 09/09, 14h21–16h47 | Correções e Test 2 | UV, MFR, Magical Crops, AgriCraft, casos de colheita e matriz de validação; tag `garden-cloche-test-2`, commit `ad5e6201`. |
| 09/09, 17h44m15s | Sucesso da Garden Cloche confirmado pelo usuário | “ChatGPT, temos uma build de sucesso, parabéns, você conseguiu um backport em 2 tentativas! ele está funcional e sem erros visiveis”. |
| 09/09, após aprovação | Método transformado em suíte de skills e escolha da feature seguinte | Relato histórico da suíte Akashic Legacy Backport Engineering; auditoria de lacunas selecionou Auto Workbench. Não foi reinstalada nem revalidada nesta recuperação. |
| 09/09, 19h14 em diante | Feature #2: Automated Engineer's Workbench | `a36bb241` seleciona a feature; commits seguintes implementam contrato de receitas, core, GUI, renderer, projeção de blueprint e correções. |
| 10/09 | Auto Workbench Test 1 rejeitado no smoke; substituído pelo R2 | Mistura de triângulos/quads incompatível com o loader legacy; correção `97cea596`. R2 pronto para A6, sem aprovação final de gameplay localizada. |
| 10/09 à noite–11/09 | Ampliação explícita para IE 1.21.1 e plano mestre | Branch `chief/ie-1.21.1-masterplan`, charter, inventário de features, áudio, rendering, rede e registries. |
| 11/09 | Hardening de rede e arquitetura de expansão de registry | Correção `a1b811a6`, verificação `c5453e22`; build/artefato/ABI PASS, runtime dedicado pendente. |
| 11–12/09 | Feature #3: Alloy Kiln | Implementação `a1376474`; tag `alloy-kiln-test-1`; K4 registrado em `ccaad104`. Candidato de teste, não release homologada. |
| 12/09, 02h57–03h31 | Mixer M1 | Infraestrutura `f7a4abd4`, relatório de aprovação `b1fab23f`: receitas, consumo exato por slot e tanque multifluido. |
| 12/09, 10h04 | Mixer / concreto líquido | HEAD observado `7b8863d0`: código de fluidos, ciclo de concreto, assets e self-test adicionados. Não foi localizado fechamento do gate M2. |

## O que a Garden Cloche realmente demonstrou

A baseline foi recuperada a partir da cadeia documentada BluSunrize `c45a3a2` -> chaos234 -> Bogdan-G `798ce21` -> overlay Forbidden 0.7.9. O relatório apresenta 694 classes em ambos os artefatos, nenhuma classe exclusiva, 463 arquivos Java decompilados idênticos e 32 diferenças classificadas. Isso sustentou a recuperação semântica; não significa igualdade byte a byte com a compilação original.

A infraestrutura adotada foi RetroFuturaGradle, Forge 10.13.4.1614, MCP stable_12 e bytecode Java 8. O backport preservou a identidade 0.7.9/Forbidden Series e IDs existentes.

As principais correções entre as duas rodadas foram:

- **Texturas:** conversor e WavefrontObject faziam inversão V, anulando a transformação desejada; corrigido em `1901538d`.
- **MFR Fertile Soil:** multiplicadores por comportamento real de cada cultura, documentados como 2x vanilla, 1,5x Hemp e 31/11 para Magical Crops direto. GUI distingue solo, fertilizante e total.
- **Magical Crops:** o arquivo BETA 5 apresentava string interna BETA 4b; integração baseada na API/comportamento identificado. Diamond Seeds produz Diamond Essence; a semente matriz permanece no input.
- **AgriCraft evernife-v1:** Growth afeta velocidade, Gain afeta rendimento e Strength permanece no NBT, sem inventar efeito de crescimento. Sementes não analisadas conservam os atributos reais; a máquina não faz breeding.
- **Colheita:** simulação de todos os outputs antes de confirmar, bloqueio com inventário cheio e rejeição de colheita sem saída válida.

A confirmação do usuário é evidência de sucesso funcional observado na segunda rodada. Não equivale a um registro individual PASS para cada cenário da matriz, nem a benchmark, soak test ou homologação completa de todos os mods agrícolas.

## Identidades dos candidatos preservados

Os três SHA-256 abaixo foram recalculados nesta recuperação e coincidem com os registros locais.

| Candidato | Commit de runtime | SHA-256 |
|---|---|---|
| Garden Cloche Test 2 | `ad5e6201` | `51CF081E5A7C9E92DC9EDE65021BA2BEFBDD17971A070D7F26F8C83839C4AC2E` |
| Auto Workbench Test 1 R2 | `97cea596` | `C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97` |
| Alloy Kiln Test 1 | `a1376474` | `F03FC1EC9EA4C17DBB38E62D204EAD4853966D570D06E3258B0525DAA5DA8590` |

Tags anotadas possuem um objeto próprio; não confundir SHA do objeto tag, commit de runtime e hash do JAR. Os candidatos estão em `dist/` sob a raiz do workspace.

## Fronteira de progresso

| Frente | Estado sustentado pelas evidências |
|---|---|
| Garden Cloche | Aprovação funcional pelo usuário em 09/09; checkpoint de referência. Integrações adicionais e performance em escala continuam fora dessa aprovação. |
| Auto Workbench | Core, GUI, animação, blueprint, fila, RF, NBT e automação implementados; R2 passa build/artefato/smoke isolado. A6 real e A7/A8 não fechados nas fontes recuperadas. |
| Plano 1.21.1 | Alvo e frentes arquiteturais formalizados. Quantidades de assets/traduções não são contagem de features concluídas. |
| Rede | Hardening em branch separada; static/build/artefato/ABI aprovados. Runtime dedicado pendente. Não está na história direta do HEAD Mixer observado. |
| Alloy Kiln | Core, GUI, NEI, MineTweaker, receitas e assets incluídos no candidato K4. Gameplay K5 e dedicated runtime completo pendentes. |
| Mixer M1 | Gate da fundação multifluido/receitas aprovado no relatório, inclusive self-test e probe FML. Não aprova a máquina Mixer. |
| Mixer M2 | Código de fluidos/concreto presente no HEAD; falta localizar/produzir aprovação específica antes de considerar a fundação fechada. |
| Release geral | Paridade de mundos/máquinas antigas, integração total, multiplayer, performance e aprovação de release ainda abertas. |

Os harnesses dedicados de R2/Alloy pararam no aceite do EULA, segundo os relatórios. Isso limita a evidência de servidor; não é conclusão de defeito dessas features e não foi reexecutado aqui.

O percentual histórico de aproximadamente 14% aparece na conversa de continuidade. Não foi recalculado nesta recuperação e não deve ser apresentado como medição atual. Os 44% encontrados em “Sucesso técnico do projeto” referem-se ao Immersive Petroleum, não ao Engineering.

## Mapa de retomada

Raiz: `C:/Users/User/Desktop/Codex/immersiveengineering-backport`.

| Diretório sob work/ | Branch | HEAD observado |
|---|---|---|
| ImmersiveEngineering-Forbidden-Backport | feature/auto-workbench | `97cea596` |
| chief-1.21.1-masterplan | chief/ie-1.21.1-masterplan | `d7bf99e5` |
| alloy-kiln | feature/alloy-kiln | `ccaad104` |
| mixer | feature/mixer | `7b8863d0` |
| network-hardening | hardening/network-gate | `a1b811a6` |
| network-hardening-chief | hardening/network-gate-chief | `c5453e22` |

As quatro primeiras árvores estavam limpas na consulta. Os diretórios runtime-* são harnesses descartáveis em detached HEAD, não linhas principais de entrega.

O caminho com nome ImmersiveEngineering-Forbidden-Backport preserva R2 e não representa sozinho o progresso mais recente. A raiz é um workspace agregador, não um repositório Git. O histórico direto de Mixer contém Alloy e R2, mas não os commits do hardening de rede; integrar exige revisão e validação, não copiar arquivos entre árvores.

## Divergências documentais identificadas

- `project-docs/PROJECT-CHARTER.md` ainda diz BASELINE RECOVERY. É documento inicial, superado pelo charter 1.21.1 para o objetivo vigente.
- O ROADMAP no checkout R2 ainda diz que falta o rebuild/tag substituto. `dist/auto-workbench-test-1-r2/VALIDATION.txt`, tag e hash mostram que R2 já foi entregue para A6.
- A matriz `AUTO_WORKBENCH_TEST_1_R2.md` conserva uma referência interna ao candidato R1. Para identidade do candidato, usar VALIDATION.txt e TEST-INSTALL.txt do diretório R2.
- O design inicial do Mixer chama M2 de multiblock/registry; o fechamento M1 redefine o próximo gate como identidades de fluidos e ciclo do Concrete antes do multiblock. Essa nomenclatura deve ser reconciliada na próxima atualização de planejamento.

Essas divergências foram registradas aqui; os documentos históricos não foram reescritos nesta recuperação.

## Regras recuperadas para continuidade

- Qualidade técnica e evidência prevalecem sobre velocidade da resposta.
- Separar hipótese, código implementado, teste automatizado, inicialização e gameplay observado.
- Preservar a Cloche aprovada e candidatos congelados; não sobrescrever JAR de produção durante desenvolvimento.
- Evoluir por famílias completas, incluindo aspecto visual/sonoro quando aplicável.
- Falha de gate retorna à menor camada responsável; não autoriza reescrita de sistemas alheios.
- Usar versões exatas do pack; não deduzir compatibilidade apenas do nome ou string de versão.
- Preservar registries antigos; novos conteúdos usam expansão auditada.
- Seguir avançando em trabalho independente enquanto homologações específicas permanecem pendentes, mantendo esses limites explícitos.

Próxima retomada técnica: confirmar novamente HEAD/status/atividade concorrente; examinar o código M2 em Mixer e seus testes; reconciliar o gate de fluidos/concreto; manter A6 Auto Workbench, K5 Alloy e runtime de rede visíveis como homologações pendentes. Esta recuperação não iniciou implementação, build, merge ou instalação.

## Fontes consultadas

Conversas recuperadas pelo aplicativo:

- “Criar Fork Immersive Engineering”, ID `6aa0e1e5-d0cc-83e9-9e7e-ab734c40e790`: pedido inicial, primeira rodada, Test 2 e confirmação. Mensagem de aprovação: `8a25f88d-a582-4af4-bb78-392f9a55b552`.
- “Recuperar histórico do projeto”, ID `6aa35f27-49c4-83e9-815f-576e9e9f91d5`: ampliação explícita do escopo, R2, hardening, Alloy e começo do Mixer. Consulta dos dez turnos mais recentes, complementada pelos registros locais posteriores.
- “Sucesso técnico do projeto”, ID `6aa1cbc3-d334-83e9-b71b-9ce6aca76220`: usado somente para distinguir o progresso do Petroleum.

Documentos locais centrais, caminhos relativos à raiz do workspace:

- `work/ImmersiveEngineering-Forbidden-Backport/docs/engineering/BASELINE-GATE-REPORT.md` e `ROADMAP.md`.
- `dist/garden-cloche-test-2/GARDEN_CLOCHE_TEST_2.md`.
- `dist/auto-workbench-test-1-r2/VALIDATION.txt` e `TEST-INSTALL.txt`.
- `work/chief-1.21.1-masterplan/docs/engineering/IE-1.21.1-BACKPORT-MASTER-CHARTER.md`.
- `work/network-hardening-chief/docs/engineering/NETWORK-HARDENING-GATE-1-CHIEF-VERIFY.md`.
- `work/alloy-kiln/docs/engineering/ALLOY-KILN-K4-GATE.md`.
- `work/mixer/docs/engineering/MIXER-M1-GATE.md` e `MIXER-A0-A2-DESIGN.md`.
- Histórico Git, worktree list, status e JARs em dist consultados diretamente em 12/09/2026.

Não foram rerodados builds, testes de jogo nem auditorias de bytecode. Os resultados históricos permanecem atribuídos às conversas e relatórios que os registraram. A verificação atual cobriu a recuperação documental, estado Git e identidade dos três JARs.
