# Proveniência, método e limites deste dossiê

## 1. Quem fez o quê

- O trabalho de engenharia (baseline, Garden Cloche, Auto Workbench, Alloy Kiln, Mixer, hardening) está nos commits do autor (`Akashic`, 09/09 a 17/09/2026). Conversas anteriores do projeto — em outra ferramenta de IA — são citadas nos documentos originais; **não** foram reabertas nem verificadas aqui.
- Este dossiê foi montado em **25/10/2026** por uma sessão do Claude, a pedido do autor (Gabriel, GitHub `Akashiic`), a partir de: histórico Git das 12 branches, 8 tags, relatórios de gate, documentos de projeto do workspace local, e inspeção do jar R3 anexado.
- Nenhum build, teste de jogo ou auditoria foi **refeito**. O único experimento novo foi um teste de aplicação de `cherry-pick` em worktree descartável (sem compilar), descrito em [ESTADO-ATUAL-E-RETOMADA.md](ESTADO-ATUAL-E-RETOMADA.md#5-integração-das-linhas-divergentes).

## 2. Cadeia de código

Diagrama: [FLUXOGRAMAS.md](FLUXOGRAMAS.md#6-cadeia-de-proveniência-do-código). Texto original: [`archive/project-docs-workspace-root/PROVENANCE.md`](archive/project-docs-workspace-root/PROVENANCE.md).

| Elo | Evidência | Confiança |
|---|---|---|
| BluSunrize `1.7.10` @ `c45a3a2577a4751afdc78e3109f2358b10b0f397` ("Build and Release v0.7.7", 09/05/2016) | Clone oficial. A tag remota `0.7.7` está em história **divergente** e não é ancestral do HEAD. | Verificada |
| chaos234 `c38fd6b → 3cf2a38` (era 0.7.8) | O jar implantado troca a URL `contributorRevolvers.json` para o repositório do chaos234. | Verificada |
| Bogdan-G `798ce21a1dda0bba6be0a3931c2d0e26b495673c` (28/04/2017) | Três impressões digitais independentes (split de energia no `TileEntityConnectorLV`, `XSTR.class`, família float/GL). | Verificada |
| Overlay Forbidden 0.7.9 (versão, Java 8, DeveloperCapes embutido, chamada remota de capes) | Diferença entre o jar implantado e o HEAD do Bogdan-G. | Recuperada |
| Repositório público literal do "Forbidden Series 0.7.9" | Não localizado. | **Ausente** |
| Baseline `0c803472` em RFG | 694 classes em ambos os artefatos; nenhuma classe exclusiva; 463 `.java` decompilados idênticos; 32 diferenças classificadas. Comparação binária bruta superestima o delta: ~678 classes diferem por compilador, mas após CFR + normalização só 42 arquivos Java diferem e 14 classes são acrescentadas (quase todas DeveloperCapes e XSTR). | Recuperação semântica aceita; **não** é igualdade byte a byte |

SHA-256 do jar implantado de referência: `f68b4bef165414ae751d00ef2e1eb2a9692b196239e45193a6bc82d37bb92fb8` (`mods/ImmersiveEngineering-1.7.10-0.7.9.jar`; carimbos de arquivo em torno de set/2023).

## 3. Fontes usadas e como são citadas

| Tipo | Onde está neste repositório |
|---|---|
| Histórico Git (45 commits, 8 tags, 12 branches) | `data/commits.tsv`, `data/tags.tsv`, `data/branches.tsv`; permalinks no GitHub |
| Evolução de cada documento (44 caminhos) | `data/docs-history.tsv` |
| Arquivos alterados por etapa de feature | `data/files-by-feature.tsv` |
| Hashes dos jars | `data/artifacts.tsv`; `ARTEFATOS.md` |
| Documentos que só existiam no workspace | `archive/` (82 arquivos, 324 KB, SHA-256 em `archive/INDEX.md`, sem edição) |
| Relatórios de gate | `docs/engineering/` em cada branch; cópias das pastas `dist/` em `archive/dist-validation/` |
| Binários e logs grandes | **não incluídos**; ver `data/external-evidence-not-archived.tsv` |

## 4. Método

1. Fork de `BluSunrize/ImmersiveEngineering` para `Akashiic/ImmersiveEngineering`.
2. Clone **isolado** do repositório de trabalho do autor (a pasta original não foi alterada; nada foi movido ou apagado nela).
3. Extração automatizada de metadados do Git (datas em UTC−3, pais, branches que contêm cada commit, arquivos e linhas).
4. Leitura integral dos relatórios de gate das linhas R3, Alloy, Mixer, rede e multiplayer, e dos documentos-mestre.
5. Cópia verbatim dos documentos que estavam fora do Git e SHA-256 de cada um.
6. Verificação independente do jar R3 anexado (hash, tamanho, entradas, bytecode, ausência de classes de outras features).
7. Redação em português, com os diagramas validados por renderização (Mermaid CLI).

## 5. Limites e ressalvas — leia antes de citar este dossiê

- **Gameplay não verificado.** A única feature aprovada em jogo é a Garden Cloche Test 2, e isso é registro de uma confirmação do usuário em 09/09/2026, não de matriz por cenário.
- **O que aconteceu entre 12/09 10:04 e 16/09 23:09, e depois de 17/09 01:54, não está documentado.** Se houve testes do R2/R3 no jogo nesse período, as observações não estão em nenhuma fonte acessível.
- **Uso do R3 pelo autor** é afirmação dele; o que se verificou foi que o jar é idêntico ao candidato.
- **Conversas anteriores** (recuperadas em `CONTEXTO-CONSOLIDADO-2026-09-12.md`) são secundárias: horários como 01:35 e 17:44 de 09/09 e 23:31/23:34 de 10/09 vêm dali, não de commits.
- **Porcentagem de progresso:** o "~14%" histórico não foi recalculado e não deve ser citado como medição atual.
- **Reprodutibilidade de build:** hashes de jar não foram reproduzidos por nova compilação.
- **Linhagem:** o último elo (Forbidden 0.7.9) é reconstrução, não confirmação por repositório público.
- **Lacuna de 1.21.1:** contagens como "398 blocos / 314 itens" são superfície bruta; não são número de features faltantes (há falsos positivos por metas, renomeações e variantes geradas).

## 6. Licença e avisos

Veja a seção 9 de [CONTEXTO.md](CONTEXTO.md#9-licença), [ARTEFATOS.md](ARTEFATOS.md) e `THIRD_PARTY_NOTICES.md`. Este fork não republica binários. Alterações em documentação e relatórios feitas nesta branch não alteram a licença do código.
