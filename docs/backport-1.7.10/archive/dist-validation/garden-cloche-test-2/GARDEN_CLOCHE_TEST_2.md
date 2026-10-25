# Garden Cloche — Test 2

## Objetivo
Validar a segunda rodada do backport antes de fechar a Garden Cloche.
O JAR de produção do modpack não deve ser sobrescrito durante esta homologação.

## Gate visual
1. Coloque a Cloche em quatro orientações diferentes.
2. Observe base, vidro, topo, conexões e solo a curta e longa distância.
3. Compare também o item no inventário/NEI.
4. O vidro deve manter transparência normal, sem atlas embaralhado.

## Vanilla + MFR
1. Trigo + dirt: confirmar funcionamento base.
2. Trigo + MFR Fertile Soil: deve ser aceito e funcionar.
3. Tooltip deve mostrar Soil multiplier `2.00x`.
4. Total deve combinar solo e fertilizante sem substituir nenhum deles.
5. Cenoura, batata e stems devem seguir o mesmo bônus de solo `2.00x`.

## IE Hemp + MFR
1. IE Hemp deve aceitar MFR Fertile Soil como substituto de dirt.
2. Tooltip deve mostrar Soil multiplier `1.50x`.
3. Confirmar fibra/seeds sem perda de itens.

## Magical Crops 4.x
1. Diamond Seeds deve ser aceita diretamente pela Cloche.
2. Output esperado: Diamond Essence, não Diamond.
3. A seed permanece no slot e não deve ser duplicada na saída.
4. Com MFR Fertile Soil, Soil multiplier esperado: `2.81818x` (`31/11`).
5. Testar ao menos mais duas seeds base e uma seed de ModCompat disponível.

## AgriCraft evernife-v1
1. Testar uma seed genética `1/1/1` e uma `10/10/10` do mesmo plant.
2. Growth maior deve acelerar o ciclo segundo a fórmula do AgriCraft.
3. Gain maior deve alterar o yield via `getFruitsOnHarvest`.
4. Strength deve ser preservado no NBT; a Cloche não simula weeds/clippings.
5. Repetir com `analyzed=false`: stats reais do NBT devem continuar valendo.
6. Seed Magical Crops com stats AgriCraft deve usar o handler genético.
7. Nether Wart e crops com solo especial não devem aceitar solo incorreto.

## Robustez
1. Encha os quatro slots de output e confirme que o harvest para sem deletar itens.
2. Remova espaço de saída e confirme retomada normal.
3. Quebre master, dummy intermediário e topo; deve existir um único drop da máquina.
4. Reinicie mundo/cliente com uma Cloche ativa e verifique NBT, água, energia e growth.
5. Revisar `latest.log` e dedicated-server log por exceptions ligadas a Cloche/IE.
