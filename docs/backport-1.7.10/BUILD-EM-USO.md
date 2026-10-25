# Build em uso: `ImmersiveEngineering-auto-workbench-test-1-r3`

Este é o jar que o autor está usando e o ponto mais recente do trabalho. Esta página diz **exatamente** o que ele é, o que contém, o que **não** contém e quanto dele está provado.

## 1. Identidade

| Campo | Valor |
|---|---|
| Arquivo | `ImmersiveEngineering-auto-workbench-test-1-r3.jar` |
| SHA-256 | `B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12` |
| Tamanho | 3.806.758 bytes |
| Tag / commit de runtime | `auto-workbench-test-1-r3` → `5d467a612605c1201a13bde52b54e3550477ac05` (17/09/2026 01:21–01:22) |
| Jar gerado | 17/09/2026 01:24 |
| HEAD documental | `ac41e5e7` (17/09/2026 01:53), nada de código depois da tag |
| `mcmod.info` | `Immersive Engineering` · `1.7.10-0.7.9` · "Forbidden Series Edition" · mcversion 1.7.10 |
| Build de referência | `gradlew clean build --no-daemon` → `BUILD SUCCESSFUL in 2m 55s`; 23 tarefas (22 executadas, 1 atualizada) |

**Conferência feita em 25/10/2026** no jar anexado ao pedido de montagem deste dossiê: SHA-256 e tamanho idênticos; 1.405 entradas ZIP sem duplicatas; 747 classes, todas major 52 (Java 8); `mcmod.info` conforme acima; carimbo interno de `mcmod.info` em 17/09/2026 01:24:46. Ou seja: o jar em uso **é** o candidato R3 documentado, e não um build sujo.

## 2. O que o jar contém

- Toda a linhagem recuperada **Forbidden 0.7.9** (Bogdan-G + overlay), incluindo `org/bogdang/modifications/random/XSTR.class` e as classes de `com/jadarstudios/developercapes` (17 entradas). O DeveloperCapes faz chamada remota no cliente.
- **Garden Cloche** na versão do **Test 2** (aprovada em jogo): 10 entradas com "GardenCloche" no nome (`GuiGardenCloche`, `TileRenderGardenCloche`, `TileEntityGardenCloche`, `ContainerGardenCloche`…), adaptadores de Vanilla/Hemp/Magical Crops/AgriCraft/MFR.
- **Automated Engineer's Workbench** completo: 22 entradas, entre elas `TileEntityAutoWorkbench` (+ `$AutoWorkbenchProcess`), `ContainerAutoWorkbench` (+ `$1`), `MultiblockAutoWorkbench`, `GuiAutoWorkbench` (+ `$RecipeButton`), `TileRenderAutoWorkbench` (+ `$1`, `$2`), `AutoWorkbenchBlueprintRenderer` (+ classes internas) e os três OBJ (`autoWorkbench.obj`, `autoWorkbenchMirrored.obj`, `autoWorkbenchAnimated.obj`).
- Identidades de registry preservadas e acrescentadas: Metal Press meta 13 → **Auto Workbench meta 14**; Steel Sheet meta 2 → **Redstone Engineering Block meta 3**; GUI **13** (Cloche) e **14** (Auto Workbench). Nenhuma meta ou GUI ID existente foi remapeado.
- APIs de terceiros empacotadas nas pastas de topo `cofh`, `mods`, `invtweaks`, `WayofTime`, `powercrystals`, como na linhagem original.

## 3. O que o jar **não** contém (verificado por busca de classes)

| Recurso | Classes encontradas no jar | Onde está |
|---|--:|---|
| Alloy Kiln / Alloy Smelter | 0 | `feature/alloy-kiln`, `feature/mixer` |
| Mixer (`MultiFluidTank`, `MixerRecipe`, fluido Concrete) | 0 | `feature/mixer` |
| Hardening de rede C2S (`ServerNetworkTaskQueue`) | 0 | `hardening/network-gate*` |
| Hardening multiplayer da Cloche | ausente (a Cloche é a do Test 2) | `hardening/garden-cloche-multiplayer`, `hardening/auto-r2-cloche-mp` |

Consequência prática: nesse jar a seleção de receita do Auto Workbench trafega pelo `MessageTileSync` **legado** (sem a fila e a validação do hardening de rede), e a Cloche tem o comportamento do Test 2 em multiplayer (sem as correções de `short` assinado).

## 4. O que mudou do R2 para o R3

Segundo o relatório A5.6: 1.405 → 1.405 entradas; **0 adicionadas, 0 removidas, 4 classes alteradas** (`TileEntityAutoWorkbench`, `TileEntityAutoWorkbench$AutoWorkbenchProcess`, `ContainerAutoWorkbench`, `ContainerAutoWorkbench$1` — as duas internas só mudam metadados de linha); 0 membros públicos/protegidos removidos; referências a `net.minecraft.client.*` em classes comuns/API: 55 → 55; os 8 assets do Auto Workbench e do Redstone Engineering Block são byte-idênticos aos do R2.

Código-fonte da mudança: commit `5d467a61` (5 arquivos, +142/−4). Dois efeitos:

1. `TileEntityAutoWorkbench.tickServerProcesses()` chama `markDirty()` uma vez por tick em que ao menos um processo avança.
2. `ContainerAutoWorkbench` envia a energia pela propriedade `0` de `ICrafting` quando muda com a GUI aberta; o cliente aplica com `EnergyStorage.setEnergyStored()`.

## 5. Evidência que sustenta o R3 — e o que ela não cobre

| Prova | Resultado | Fonte |
|---|---|---|
| Build limpo da tag | PASS (2m 55s) | A5.6 |
| Auditoria de artefato | 21/21 PASS (`tools/audit_auto_workbench_r3_artifact.py`) | A5.6 |
| Auditoria de sincronização de estado (fonte) | 16/16 PASS (`tools/audit_auto_workbench_r3_state_sync.py`) | A5.5 |
| Smoke de cliente mínimo (2 inicializações) | "Forge Mod Loader has successfully loaded 4 mods"; 0 `ModelFormatException`; sem exceção de renderer/classloading do Auto Workbench | A5.6; log SHA `5E6FD1E7…5365` |
| Teste do coletor A6 | sandbox positivo PASS; gate negativo no pack real PASS (com R2 instalado recusou iniciar) | `A6-COLLECTOR-SELFTEST.txt` |
| **Gameplay T0–T18 (A6)** | **sem evidência** | — |
| **Servidor dedicado** | **não provado** (o harness parou no EULA) | A5.1 |
| **Multiplayer real, performance em escala (T17), Angelica com o pack completo** | **não provados** | — |

Ruído herdado conhecido nos smokes: três texturas ausentes (`revolver_rose`, `metal2_skycrateDispenser`, `revolver_tesla`), já ausentes no baseline aprovado.

## 6. Como usar com segurança

- Instale **um** jar IE por vez; nunca R2 e R3 juntos. Faça backup do mundo antes.
- Este build ainda **não** é candidato a produção. Não o promova a "estável".
- Veja o passo a passo de teste em [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md#3-retomar-o-a6-gameplay-do-r3--passo-a-passo).
- Em caso de falha: restaurar o backup do mundo e voltar à Garden Cloche Test 2 (nunca ao R2).
