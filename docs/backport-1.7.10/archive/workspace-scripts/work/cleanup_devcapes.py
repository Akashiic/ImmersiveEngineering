from pathlib import Path
root = Path(r'C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\src\main\java\com\jadarstudios\developercapes')
count = 0
for path in root.rglob('*.java'):
    raw = path.read_text(encoding='utf-8')
    out = []
    for line in raw.splitlines():
        stripped = line.lstrip(' \t')
        prefix = line[:len(line)-len(stripped)].expandtabs(4)
        out.append((prefix + stripped).rstrip(' \t'))
    path.write_text('\r\n'.join(out) + '\r\n', encoding='utf-8', newline='')
    count += 1
print('normalized', count, 'DeveloperCapes source files')