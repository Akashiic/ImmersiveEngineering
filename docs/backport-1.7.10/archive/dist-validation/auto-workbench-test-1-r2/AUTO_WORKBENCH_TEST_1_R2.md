# Automated Engineer's Workbench — Test 1

## Objetivo
Validar em runtime a primeira build do backport do Auto Workbench antes de qualquer aprovação final.
O JAR de produção do modpack não deve ser sobrescrito automaticamente durante esta homologação.

## Regra de resultado
- PASS: comportamento observado coincide com o critério abaixo e não surge exceção relacionada.
- FAIL: crash, item perdido/duplicado, estrutura incorreta, renderer quebrado, estado que não persiste ou comportamento divergente.
- NÃO CONCLUSIVO: teste não pôde ser executado ou um mod externo impediu a observação.

Não converter NÃO CONCLUSIVO em PASS.

## Preparação
1. Faça backup do mundo usado para teste.
2. Remova/desative qualquer outro JAR do Immersive Engineering para não carregar duas cópias.
3. Use somente o candidato `auto-workbench-test-1` quando ele for fornecido.
4. Mantenha o restante do Forbidden Series igual ao ambiente real, inclusive Angelica.
5. Preserve logs de cliente e servidor desde o início do boot.
6. Se possível, grave vídeo das etapas de formação, animação e desmontagem.

## Materiais exatos da estrutura
- 5 Steel Scaffolding;
- 2 Treated Wood Slabs;
- 1 Redstone Engineering Block;
- 4 Light Engineering Blocks;
- 2 Heavy Engineering Blocks;
- 4 Conveyors normais.

## T0 — Boot e identidade
1. Inicie o cliente completo.
2. Inicie/conecte ao servidor dedicado usado para homologação.
3. Confirme que o IE aparece como `1.7.10-0.7.9 / Forbidden Series Edition`.
4. Procure por `AutoWorkbench`, `ModelFormatException`, `NoClassDefFoundError`, `ClassNotFoundException` e erros de texture/OBJ nos logs.
5. Não deve existir crash ou erro de classloading atribuído ao backport.

## T1 — Redstone Engineering Block
1. Localize `Redstone Engineering Block` no NEI/inventário criativo.
2. Confirme que a textura não substitui nenhuma Sheet Metal antiga.
3. Teste a receita oficial adaptada: `IRI / RCR / IRI`.
4. `I` = Iron Ingot, `R` = Redstone/Dust Redstone, `C` = Copper Ingot.
5. A receita deve produzir exatamente 2 blocos.
6. Reabra um mundo antigo e confirme que metas 0–2 do `metalDecoration2` continuam corretas.

## T2 — Engineer's Manual
1. Abra a categoria Machines.
2. Confirme a entrada `Automated Workbench`.
3. O exploded-view deve mostrar 18 componentes e os materiais acima.
4. Os três conveyors em linha devem apontar para SOUTH no espaço lógico do manual.
5. O quarto conveyor deve apontar para WEST.
6. Use o botão de estrutura formada; o modelo completo deve aparecer, sem desaparecer ou explodir a escala.

## T3 — Formação normal
1. Monte a estrutura exatamente como exibida no manual.
2. O bloco de trigger é o Treated Wood Slab que fica diretamente acima do Redstone Engineering Block.
3. Use o Engineer's Hammer nesse slab.
4. A estrutura deve formar sem remover/criar componentes extras.
5. Repita com a máquina orientada para NORTH, SOUTH, WEST e EAST.
6. Em todas as quatro direções, a geometria deve permanecer alinhada aos 18 blocos formados.
7. Verifique especialmente mesa, drill, press, lift e conveyor visual.

## T4 — Formação espelhada
1. Desmonte e reconstrua a estrutura com esquerda/direita espelhadas.
2. Use o mesmo slab diretamente acima do Redstone Engineering Block como trigger.
3. A estrutura deve reconhecer o layout espelhado.
4. O modelo não pode ficar invertido por dentro, com faces trocadas ou texturas espelhadas incorretamente.
5. Repita ao menos em duas direções; idealmente nas quatro.
6. A saída deve trocar de lado junto com o mirror.

## T5 — GUI e Blueprint
1. Abra a GUI por uma parte formada e confirme que ela resolve o master.
2. O slot isolado de Blueprint deve aceitar Engineer's Blueprint e ter limite 1.
3. Itens comuns não devem entrar no slot de Blueprint.
4. Os 16 slots de componentes devem aceitar materiais normalmente.
5. Insira Blueprint `bullet` e confirme os botões de receitas.
6. Clique em uma receita: ela deve ficar selecionada.
7. Clique novamente na mesma receita: seleção deve virar OFF (`-1`).
8. Selecione outra receita e confirme troca imediata sem crash/desync.

## T6 — Recipe inválida / ausência de materiais
1. Com Blueprint válido, selecione uma receita sem fornecer ingredientes suficientes.
2. A máquina não deve consumir nada, não deve criar processo e não deve crashar.
3. Remova todos os componentes e repita.
4. Troque o Blueprint por outro de categoria válida durante idle; a GUI deve reconstruir os botões.
5. Se tiver acesso a um Blueprint com categoria NBT inválida/vazia, teste-o.
6. Categoria inválida/vazia deve resultar em zero receita, sem exception e sem consumo.

## T7 — Craft base: Blueprint `bullet`
1. Forneça os materiais mostrados para uma receita `bullet` realmente disponível no seu pack.
2. Selecione essa receita.
3. Forneça RF ao hatch/parte de energia e componentes ao inventário.
4. Com modificadores default, um processo usa 23.040 RF em 180 ticks.
5. O consumo nominal é 128 RF/t por processo ativo.
6. Ingredientes devem ser debitados uma única vez quando o processo entra na fila.
7. Observe lift, drill, press e deslocamento do item durante todo o ciclo.
8. Após 78% do ciclo, a representação visual deve mudar dos inputs para o output.
9. O item final deve sair uma única vez.

## T8 — Craft independente: Blueprint `electrode`
1. Insira o Blueprint `electrode` existente na baseline.
2. Forneça 4 `ingotHOPGraphite` para um craft.
3. Selecione Graphite Electrode.
4. Confirme consumo de quatro ingots e exatamente um output de Graphite Electrode.
5. Esse teste deve passar independentemente do teste `bullet`.

## T9 — Fila e energia
1. Coloque materiais para pelo menos 4 crafts da mesma receita.
2. Mantenha energia suficiente para múltiplos processos simultâneos.
3. A fila nunca deve conter mais de 3 processos.
4. Um novo processo só deve entrar após o processo anterior atingir ao menos 43,75%.
5. Com três processos avançando, o consumo nominal default pode chegar a 384 RF/t.
6. Corte a alimentação de RF no meio do ciclo: o progresso deve pausar sem perder processo ou ingredientes.
7. Restaure RF: os processos elegíveis devem continuar do ponto salvo.
8. Tentar alimentar um quarto processo não deve apagar nem sobrescrever nenhum dos três existentes.
## T10 — Controle por redstone
1. Sem sinal de redstone, confirme processamento normal com a lógica default.
2. Energize a posição de controle/Redstone Engineering Block: a máquina deve pausar.
3. Remova o sinal: o processamento deve retomar do mesmo estado.
4. Use o Engineer's Hammer na posição de controle para inverter a lógica.
5. Após a inversão, confirme que sinal presente permite e sinal ausente bloqueia.
6. Repita com processo ativo e com fila existente.
7. Nenhum processo, ingrediente reservado ou seleção de receita pode desaparecer ao alternar o controle.

## T11 — Automação de input e RF
1. Conecte hopper/tubo/item transport à parte `pos 9`, o hatch de input/energia.
2. Itens devem entrar somente nos slots 1–16 de componentes.
3. Automação externa não deve inserir nem extrair o Blueprint do slot 0.
4. Conecte RF à mesma parte `pos 9` por uma face disponível.
5. Confirme que essa parte abastece o buffer interno de 32.000 RF.
6. Nenhuma outra parte do multiblock deve funcionar como porta de RF do Auto Workbench.
7. Remova temporariamente o master/chunk de referência durante um teste controlado: o dummy deve falhar fechado, sem criar inventário ou energia independentes.

## T12 — Output e backpressure
1. Execute um craft sem inventário adjacente na saída.
2. O output deve ser dropado exatamente uma vez no lado correto, a 2 blocos do master.
3. Coloque um inventário vazio na saída e repita: o output deve ser inserido, sem drop duplicado.
4. Use um inventário parcialmente ocupado que aceite somente parte do stack produzido.
5. A parte inserível deve entrar; somente o remainder real pode ser dropado.
6. Use um inventário totalmente cheio: o output completo deve ser preservado como drop, nunca deletado.
7. Repita em estrutura espelhada e confirme que a saída muda para o lado oposto previsto pelo mirror.
8. Repita vários ciclos e conte inputs/outputs para detectar perda ou duplicação acumulativa.

## T13 — Persistência NBT / restart
1. Inicie um processo e salve/reinicie próximo de 25% do ciclo.
2. Repita próximo de 60% e novamente após o ponto visual de 78%.
3. Após cada restart, confirme Blueprint, 16 slots, seleção, energia, fila e progresso.
4. O processo deve continuar do progresso persistido, sem voltar ao início.
5. Inputs já reservados não podem reaparecer no inventário após reload.
6. O output deve ser produzido exatamente uma vez ao completar.
7. Teste com 2–3 processos simultaneamente na fila.
8. A ordem e o progresso individual dos processos devem sobreviver ao restart.
9. Inverta o controle de redstone, reinicie e confirme persistência da inversão.

## T14 — Desmontagem e restauração da estrutura
1. Com a máquina idle, quebre o master e confirme restauração das outras 17 partes.
2. Repita quebrando a posição de redstone, a posição de input/RF e uma posição de conveyor.
3. Cada parte restante deve voltar somente se ainda pertencer ao mesmo master.
4. Um bloco estranho colocado no lugar de uma parte ausente não deve ser sobrescrito/recriado pela desmontagem.
5. Com `doTileDrops=true`, o componente realmente quebrado deve dropar uma única vez.
6. Com `doTileDrops=false`, o componente quebrado não deve ser forçado a dropar pelo Auto Workbench.
7. Os conveyors restaurados devem recuperar a orientação histórica correta, inclusive no layout espelhado.
8. Não deve permanecer TileEntity fantasma, estrutura `formed=true` ou inventário dummy independente.

### Processo ativo durante desmontagem
O IE upstream 0.12 não devolve os ingredientes já consumidos por processos em curso quando o multiblock é desmontado.
Para Test 1, isso é comportamento upstream conhecido e não deve ser classificado automaticamente como regressão do backport.
Ainda assim, registre exatamente o que ocorreu; qualquer hardening futuro para reembolso de processos será uma decisão explícita e separada.

## T15 — Renderer e Blueprint projection
1. Observe a estrutura formada em NORTH, SOUTH, WEST e EAST, normal e espelhada.
2. Compare curta e longa distância e vários ângulos de câmera.
3. Workbench, conveyors e partes herdadas do Assembler devem usar suas texturas corretas, sem atlas embaralhado.
4. Não deve haver faces invertidas, UVs verticalmente trocados, z-fighting anormal ou partes deslocadas.
5. Durante craft, observe lift, drill, press, pressLift e o item transportado.
6. A orientação do item deve acompanhar a convenção visual das máquinas 1.7.10.
7. Com Blueprint/receita selecionada, a projeção desenhada sobre a mesa deve aparecer quando o output possuir textura legível.
8. Receita/textura não renderizável deve falhar silenciosamente, sem derrubar o TESR/cliente.

## T16 — Compatibilidade do pack e regressões
1. Execute o renderer com Angelica e o conjunto real de mods do Forbidden Series ativos.
2. Faça um ciclo rápido da Garden Cloche Test 2 para confirmar que a feature aprovada continua funcional.
3. Forme/observe ao menos o Metal Press e o Assembler existentes.
4. Confirme que seus modelos, GUIs e processamento básico não foram alterados pelo novo renderer/GUI.
5. Reabra um mundo criado com a build Garden Cloche Test 2.
6. Confirme ausência de `MissingMappings`, remapeamentos de metas antigas ou blocos substituídos.
7. Verifique especialmente `metalDecoration2` metas 0–2 e `metalMultiblock` metas 0–13 já existentes.
8. Procure exceptions relacionadas a `AutoWorkbench`, `Blueprint`, OBJ/model, GUI, packets, inventory ou energy.

## T17 — Carga/performance
1. Observe 1 máquina formada e idle por alguns minutos.
2. Repita com 16 máquinas formadas e idle, se o ambiente de teste permitir.
3. Repita com múltiplas máquinas processando simultaneamente.
4. Se prático, faça uma rodada com 64 estruturas para procurar comportamento não linear evidente.
5. Colete profiler/Spark quando disponível, em vez de estimar custo por percepção visual.
6. Não existe threshold numérico de aprovação definido ainda; registre TPS, tempo de tick e sintomas observados.
7. Qualquer crescimento patológico deve voltar para análise antes do Test 2.

## T18 — Evidências para retorno
1. Marque cada seção T0–T17 como PASS, FAIL ou NÃO CONCLUSIVO.
2. Preserve `latest.log` do cliente e logs do servidor dedicado da mesma sessão.
3. Anexe crash report caso exista, mesmo que o jogo consiga reiniciar depois.
4. Envie vídeo ou screenshots de formação normal, mirror, GUI, animação, output e desmontagem.
5. Informe qualquer mod externo que tenha impedido um teste específico.
6. Não precisamos repetir testes que tenham evidência clara; a rodada seguinte atacará apenas falhas comprovadas e regressões necessárias.

## Apêndice — mapa lógico da estrutura
As linhas abaixo usam `l0..l2` da frente/trigger para o fundo e três posições laterais por linha.

Camada inferior (`h=-1`):
- `l0`: Light Engineering | Redstone Engineering | Steel Scaffolding
- `l1`: Light Engineering | Steel Scaffolding | Steel Scaffolding
- `l2`: Steel Scaffolding | Steel Scaffolding | Heavy Engineering

Camada superior (`h=0`):
- `l0`: Light Engineering | **Treated Wood Slab (trigger)** | Treated Wood Slab
- `l1`: Light Engineering | Conveyor | Conveyor
- `l2`: Conveyor | Conveyor | Heavy Engineering

O trigger fica diretamente acima do Redstone Engineering Block.
O mapa é lógico: mirroring troca o eixo lateral no mundo sem alterar os índices internos `pos` usados pelo TileEntity.
