# Artefatos — identidade, hashes e política de binários

> **Nenhum `.jar`, `.zip` ou log grande é publicado neste repositório.** O código-base 1.7.10 do BluSunrize não traz `LICENSE` nesta árvore; a "Blu's License of Common Sense" permite fork, mas proíbe redistribuir código ou binários compilados sem permissão. Aqui ficam o código-fonte, o histórico, os relatórios e os **SHA-256**, que bastam para identificar um jar que você já tenha ou reconstruiu. Os assets modernos copiados do 1.21.1 seguem a licença do upstream; o charter exige revisão de licença antes de qualquer release público.

## 1. Candidatos (jars)

Hashes recalculados em 25/10/2026 a partir dos arquivos locais do autor; `data/artifacts.tsv` guarda a mesma tabela. O estado vem dos relatórios de gate, não de teste novo.

| Candidato (tag) | Commit | Bytes | SHA-256 | Estado segundo os relatórios |
|---|---|--:|---|---|
| `garden-cloche-test-1` | `415ad44c` | 3.683.093 | `E03A71CAE6D2BEE8B85D710C7980C520641C4C8BB87C7F4DF0492779ED50B480` | superseded by test-2 |
| `garden-cloche-test-2` | `ad5e6201` | 3.699.942 | `51CF081E5A7C9E92DC9EDE65021BA2BEFBDD17971A070D7F26F8C83839C4AC2E` | USER-APPROVED 2026-09-09 (functional) |
| `auto-workbench-test-1` | `e2ee4453` | 3.806.297 | `94547AA9A48BE6EE0A3E5C6B39F317973AAE14E0B9EC0FF4AA6CAADB1BB8DFAB` | REJECTED (OBJ parser crash) |
| `auto-workbench-test-1-r2` | `97cea596` | 3.806.303 | `C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97` | REJECTED 2026-09-17 (A5.5 state audit) |
| **`auto-workbench-test-1-r3`** | `5d467a61` | 3.806.758 | `B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12` | A5.6 PASS; A6 gameplay pending; IN USE by author |
| `alloy-kiln-test-1` | `a1376474` | 3.871.389 | `F03FC1EC9EA4C17DBB38E62D204EAD4853966D570D06E3258B0525DAA5DA8590` | K4 PASS (static/client smoke); K5 gameplay pending |
| `garden-cloche-multiplayer-test-1` | `ac69028c` | 3.701.436 | `17F6F3D507C7E16BEE595C62C1AE19ED41407B854B32590A8C0B1557222FC0FB` | static/artifact PASS; multiplayer runtime pending |

O R3 foi, além disso, **conferido contra o jar anexado ao pedido**: SHA-256, tamanho, 1.405 entradas, 747 classes major 52 e `mcmod.info` coincidem (ver [BUILD-EM-USO.md](BUILD-EM-USO.md)).

Hashes de jar documentados em outros relatórios e que **não** aparecem acima: o jar de produção original `ImmersiveEngineering-1.7.10-0.7.9.jar` (`f68b4bef165414ae751d00ef2e1eb2a9692b196239e45193a6bc82d37bb92fb8`); o artefato de build do Mixer M1 (`31576B492E563FFF62F1B0D27FD9E6086753E0FBBDB5ACC65B2281BAE7F8A623`); o de hardening de rede (`1D16936EB40ECE71DCF7CD61AC292FC13DF52F190F0177BFAC4E96A4225937E0`). Esses três não foram recalculados nesta sessão; vêm dos relatórios.

## 2. Evidências binárias e logs que ficaram fora do repositório

Existem no disco do autor; o SHA-256 permite saber se um arquivo recuperado é o mesmo. Os `.md`/`.txt`/`.json` pequenos **foram** arquivados em [`archive/`](archive/INDEX.md).

| Caminho relativo à raíz do workspace | Bytes | Data | SHA-256 |
|---|--:|---|---|
| `artifacts/garden-cloche-test-1/ImmersiveEngineering-garden-cloche-test-1.jar` | 3.683.093 | 2026-09-09 12:51 | `E03A71CAE6D2BEE8B85D710C7980C520641C4C8BB87C7F4DF0492779ED50B480` |
| `dist/alloy-kiln-test-1/CLIENT-SMOKE-FML.log` | 82.403 | 2026-09-12 01:40 | `0C93603B8A91E892742827C5CF820B1A86DF518031279046E294EEFD14104501` |
| `dist/alloy-kiln-test-1/ImmersiveEngineering-alloy-kiln-test-1.jar` | 3.871.389 | 2026-09-12 01:29 | `F03FC1EC9EA4C17DBB38E62D204EAD4853966D570D06E3258B0525DAA5DA8590` |
| `dist/auto-workbench-test-1/ImmersiveEngineering-auto-workbench-test-1.jar` | 3.806.297 | 2026-09-10 11:43 | `94547AA9A48BE6EE0A3E5C6B39F317973AAE14E0B9EC0FF4AA6CAADB1BB8DFAB` |
| `dist/auto-workbench-test-1-r2/ImmersiveEngineering-auto-workbench-test-1-r2.jar` | 3.806.303 | 2026-09-10 21:35 | `C72F7C4AAD54491F83EA6073799DBEAFF44C260B8B4CBDE33249F1351820FA97` |
| `dist/auto-workbench-test-1-r2/RUNTIME-SMOKE-CORRECTED.log` | 96.052 | 2026-09-10 21:39 | `DC30458BA5D108AE95B24AECBE367C4CC8A223C0AE280041B633C86C7ACED8E5` |
| `dist/auto-workbench-test-1-r3/ImmersiveEngineering-auto-workbench-test-1-r3.jar` | 3.806.758 | 2026-09-17 01:24 | `B91A12EBC1D424915CB8E3DE5F3267D5699FA564BA605FAFDD9635E61CFB0A12` |
| `dist/auto-workbench-test-1-r3/R3-MINIMAL-CLIENT-SMOKE.log` | 162.546 | 2026-09-17 01:34 | `5E6FD1E78430549BC84138BF08E44FFEEB234A0B1AA9FB5D4492159A5DC55365` |
| `dist/auto-workbench-test-1-r3-package.zip` | 3.557.850 | 2026-09-17 01:54 | `501502E84CE6D2019BB6F68AA2F295C2C96AE077BECAA4AD78FD4DDCB9586951` |
| `dist/garden-cloche-multiplayer-test-1/ImmersiveEngineering-garden-cloche-multiplayer-test-1.jar` | 3.701.436 | 2026-09-16 23:12 | `17F6F3D507C7E16BEE595C62C1AE19ED41407B854B32590A8C0B1557222FC0FB` |
| `dist/garden-cloche-test-2/ImmersiveEngineering-garden-cloche-test-2.jar` | 3.699.942 | 2026-09-09 16:48 | `51CF081E5A7C9E92DC9EDE65021BA2BEFBDD17971A070D7F26F8C83839C4AC2E` |
| `evidence/alloy-kiln-runtime-smoke/CLIENT-SMOKE-FML.log` | 82.403 | 2026-09-12 01:40 | `0C93603B8A91E892742827C5CF820B1A86DF518031279046E294EEFD14104501` |
| `evidence/auto-workbench-runtime-fix/corrected-client-smoke-fml.log` | 96.052 | 2026-09-10 21:39 | `DC30458BA5D108AE95B24AECBE367C4CC8A223C0AE280041B633C86C7ACED8E5` |
| `evidence/cfr-0.7.7.log` | 74.900 | 2026-09-09 02:08 | `061A97E97E1F52480FBF4FB5AB2A772CE576382548B6DD517C2304A4AE65F0D8` |
| `evidence/cfr-0.7.9.log` | 76.540 | 2026-09-09 02:08 | `F4A7ACFAA05050905F3DC9B5D35F064C8D0D800B945BCE8ECA2DFDCC2E40EF60` |
| `evidence/cfr-built-recovered-0.7.9.log` | 75.216 | 2026-09-09 02:56 | `072718A729AFFB6A0C9B7A41D8455BE99C968FBD4E5A37DA7C49226CD4784859` |
| `evidence/mixer-m1-runtime-probe/fml-client-latest.log` | 79.937 | 2026-09-12 03:22 | `D3DB65BC471CB1F6A9DB702210A9EB8BF87A8A822CCD1407F76568386A856BDC` |
| `tools/cfr-0.152.jar` | 2.162.315 | 2026-09-09 02:06 | `F686E8F3DED377D7BC87D216A90E9E9512DF4156E75B06C655A16648AE8765B2` |

## 3. Como obter ou verificar um jar

1. Faça checkout da **tag** do candidato (as tags são imutáveis) com `core.autocrlf=false` (veja [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md#4-riscos-e-armadilhas-conhecidos), item 2).
2. `gradlew clean build --no-daemon` (RetroFuturaGradle; o resultado de referência do R3 foi `BUILD SUCCESSFUL in 2m 55s`). O build **não foi refeito** ao montar este dossiê.
3. Compare **conteúdo** (entradas, classes, hashes de assets) e não apenas o SHA do arquivo: a identidade bit a bit entre builds de máquinas diferentes não está provada.
4. Para jar já existente: `Get-FileHash <jar> -Algorithm SHA256` (Windows) ou `sha256sum <jar>` e compare com a tabela.

## 4. Pacote de distribuição do R3

Em `dist/auto-workbench-test-1-r3/` (local): jar, `VALIDATION.txt`, `TEST-INSTALL.txt`, `SHA256.txt`, `A6-COLLECTOR-SELFTEST.txt`, os gates A5.5/A5.6 e o protocolo A6 (todos arquivados aqui exceto o jar e o log de smoke). Há também `dist/auto-workbench-test-1-r3-package.zip` (3.557.850 bytes, 17/09 01:54); seu `.sha256.txt` está arquivado. O pacote **não autoriza redistribuição pública**.
