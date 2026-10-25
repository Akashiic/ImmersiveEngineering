# Hipóteses, incidentes e decisões

Registro do que deu errado, por quê, como foi tratado e o que continua em aberto. Cada linha aponta para a fonte. "Aberto" significa que **nenhuma fonte** registra fechamento.

## 1. Incidentes (defeitos e falhas de gate)

| ID | Quando | O que ocorreu | Causa provada | Tratamento | Estado |
|---|---|---|---|---|---|
| I-01 | 09/09 | Cloche Test 1 no jogo: texturas erradas, solo MFR sem efeito, integrações agrícolas ausentes | Inversão de V duplicada (conversor + `WavefrontObject`); solo MFR e adaptadores ainda não existiam | `1901538d`, `bdf75d13`, `2162392f`, `4d2b92a0`… → Test 2 | **Fechado** (aprovação funcional em 09/09 17:44) |
| I-02 | 10/09 | `auto-workbench-test-1`: `ModelFormatException: Invalid number of points for face (expected 4, found 3)` | `GroupObject` do Forge 1.7.10 trava o modo de desenho na 1ª face; `base_workbench` tinha 289 quads + 4 triângulos; o render estático do IE é um fluxo GL_QUADS único | Quadify (`A B C` → `A B C C`) só nos OBJ estáticos; tag rejeitada e mantida | **Fechado** no R2 |
| I-03 | 17/09 | R2 rejeitado na revisão adversarial A5.5 | (a) `markDirty()` ausente com processo estável → autosave podia persistir estado velho; (b) barra de RF da GUI sem canal de sync | R3 `5d467a61` | **Fechado** estaticamente; gameplay **aberto** |
| I-04 | 12/09 | 1ª auditoria do K4 reprovou o candidato | Worktree do Windows converteu 3 OBJ para CRLF; os blobs no Git eram LF | Normalização para LF e novo build; `.gitattributes` na linha Alloy/Mixer/rede | **Fechado** nessas linhas; **aberto na linha R3** até este dossiê adicionar o `.gitattributes` |
| I-05 | 10/09–12/09 | Harness de servidor dedicado de R2 e Alloy parou em "EULA not accepted" | O EULA não foi aceito automaticamente em nome do usuário | Prova dedicada ficou só por bytecode (0 refs de cliente em classes comuns) | **Aberto** |
| I-06 | 11/09 | Worktree `network-hardening` ganhou edições concorrentes durante a verificação | Trabalho paralelo | Verificação refeita em worktree limpo (`c5453e22`); edições ficaram como WIP | WIP arquivado em `archive/wip-uncommitted/` |
| I-07 | 25/10 | Ao montar este dossiê, 117 arquivos do clone apareceram como modificados | `core.autocrlf=true` na criação do clone (CRLF no disco, LF no índice); **0** diferenças de conteúdo ignorando EOL | Re-checkout dos arquivos | **Fechado**; mostra por que o `.gitattributes` importa |

## 2. Decisões de projeto (tomadas e justificadas)

| Decisão | Motivo | Fonte |
|---|---|---|
| Não colocar bitmask por índice de processo no R3 | O desencontro só ocorre com custos diferentes na mesma fila; `autoWorkbench_energyModifier/timeModifier` só mudam em `Config.init()` | A5.5 |
| Dividir o OBJ por grupo foi **rejeitado** como correção | `renderStaticWavefrontModel()` não usa o `glDrawingMode` por grupo | A5.1 |
| RF do Auto Workbench por **uma** propriedade de `ICrafting` | 32.000 RF < 32.767 (`short` assinado) | A5.5 |
| Manter a correção de crescimento escalonada a cada 32 ticks na Cloche | Igual ao Belljar oficial 1.12.2; nenhuma redução de vazão especulativa | Audit MP da Cloche |
| Não importar correções "porque são mais novas" | Só se patcha defeito provado; ordem de evidência: fonte/runtime 1.7.10 → Forge → histórico IE → inferência | Audit MP da Cloche |
| `metalMultiblock` meta 15 fica de reserva | Resta uma única meta no legado | REGISTRY-EXPANSION-ARCHITECTURE |
| Magical Crops derivado do BETA 5 exato, não do Mystical Agriculture | Versões exatas do pack | ARCHITECTURE §10 |
| Manyullyn do Alloy Kiln ausente | Netherite Scrap sem equivalente semântico provado | K4 |
| Sem merge em massa de forks 1.7.10 | Cada patch precisa de proveniência, aplicabilidade, conflito, teste e decisão | ROADMAP G4 |
| Sem reescrever fios/conveyors durante features | Alto raio de impacto; exige regressão própria | GAP-MATRIX |
| GeckoLib/GTNHLib só com ganho medido | Multiblocks rígidos preferem OBJ/procedural nativo | RUNTIME-STACK-CAPABILITY-MATRIX |
| Reembolso de ingredientes de processo em curso na desmontagem **não** implementado | Comportamento upstream; qualquer hardening é decisão explícita futura | T14 |
| Não publicar jars | Licença sem permissão de redistribuição | `ARTEFATOS.md` |

## 3. Perguntas e hipóteses em aberto

| ID | Pergunta | O que se sabe | Como fechar |
|---|---|---|---|
| H-01 | O R3 funciona em jogo (formação, GUI, fila, RF, redstone, automação, saída, NBT, desmontagem, render)? | Só prova estática e smoke de inicialização | Executar A6 (T0–T18) |
| H-02 | `markDirty()` por tick ativo tem custo mensurável com muitas máquinas? | Igual ao comportamento do IE 0.12-89 (`updateMasterBlock`), mas sem medição aqui | T17 com profiler/Spark em 1, 16, 64 estruturas |
| H-03 | A integração R3 + rede + Alloy/Mixer + MP da Cloche funciona? | Os patches **aplicam**; nada foi compilado | Seção 5 de `ESTADO-ATUAL-E-RETOMADA.md` |
| H-04 | O hardening de rede quebra algum uso legítimo (Sorter, Assembler/NEI, Auto Workbench)? | Estático PASS; runtime não rodou | Runtime dedicado: cliques rápidos, reconexão, payload malformado |
| H-05 | O servidor dedicado sobe com R3/Alloy? | Bytecode comum sem referências de cliente | Rodar com o EULA aceito pelo operador |
| H-06 | O que fazer com o DeveloperCapes (chamada remota no cliente)? | Herdado do Forbidden 0.7.9; ainda embutido | Decidir remover/ficar; teste de rede offline |
| H-07 | O ID de GUI 15 do Alloy Kiln é aceitável? | Faixa 15–31 é "reservada para continuações legacy"; alocação moderna começa em 32 | Decidir: manter com justificativa ou migrar antes de qualquer release |
| H-08 | O gate M2 do Mixer existe? | Há código (`7b8863d0`) e manifest de assets, **sem** relatório de aprovação | Escrever e rodar o gate; reconciliar a nomenclatura M2 |
| H-09 | Há conflito com Angelica / EntityCulling em renderers dinâmicos? | Smokes mínimos não carregam o pack inteiro | T15/T16 com o pack real |
| H-10 | Correções de forks 1.7.10 posteriores (quentin452, vladislemon…) valem para esta linhagem? | Gate G4 só planejado | Auditoria patch a patch |
| H-11 | Dá para reproduzir o jar R3 byte a byte? | Não testado | Rebuild em outra máquina e comparar entradas/hashes de assets |
| H-12 | Paridade de sons (54 eventos no 1.21.1 vs 9 no legado) | Faixa inteira ainda não iniciada | Trilha de áudio (design em `IE-1.21.1-AUDIO-RUNTIME-AUDIT.md`) |
| H-13 | Interação rede × `ContainerAutoWorkbench` alterado no R3 | O handler exige o container aberto para o tile exato; o R3 mexeu nesse container | Revisar na integração (seção 5) |

## 4. Armadilhas de fluxo de trabalho já vividas

- **Tag nunca se move.** Defeito → novo commit, nova tag, novo hash (R1 → R2 → R3).
- **Aprovação por compilação é proibida.** "Compila" não é "funciona".
- **Dois jars IE no `mods/` invalidam o teste.** O coletor A6 recusa começar nessa situação.
- **Falha de transporte remoto:** revalidar o último estado Git/arquivos antes de repetir qualquer escrita, build, limpeza, movimentação, remoção ou commit.
- **Windows e fim de linha:** `core.autocrlf=false` ou `.gitattributes` com `*.obj`/`*.mtl eol=lf`, senão os hashes dos assets auditados mudam.
