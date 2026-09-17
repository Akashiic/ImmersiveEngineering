#!/usr/bin/env python3
from pathlib import Path
import re
import sys

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport")
TILE = ROOT / "src/main/java/blusunrize/immersiveengineering/common/blocks/metal/TileEntityAutoWorkbench.java"
CONTAINER = ROOT / "src/main/java/blusunrize/immersiveengineering/common/gui/ContainerAutoWorkbench.java"
CONFIG = ROOT / "src/main/java/blusunrize/immersiveengineering/common/Config.java"
ENERGY = ROOT / "src/main/java/cofh/api/energy/EnergyStorage.java"

tile = TILE.read_text(encoding="utf-8")
container = CONTAINER.read_text(encoding="utf-8")
config = CONFIG.read_text(encoding="utf-8")
energy = ENERGY.read_text(encoding="utf-8")
checks = []
def check(name, condition):
    checks.append((name, bool(condition)))
    print(name + "=" + ("PASS" if condition else "FAIL"))
tick_method = re.search(r"private boolean tickServerProcesses\(\)\s*\{(.*?)\n\t\}", tile, re.S)
check("tick_method_found", tick_method is not None)
if tick_method:
    body = tick_method.group(1)
    check("active_tick_marks_dirty", "if(ticked>0)\n\t\t\tmarkDirty();" in body)
    check("process_progress_increments", "process.processTick++;" in body)
    check("energy_extraction_present", "energyStorage.extractEnergy(energy, false);" in body)

check("container_imports_icrafting", "import net.minecraft.inventory.ICrafting;" in container)
check("container_tracks_last_energy", "private int lastEnergy = -1;" in container)
check("container_detects_changes", "public void detectAndSendChanges()" in container)
check("container_sends_energy_property", "sendProgressBarUpdate(this, 0, energy)" in container)
check("container_receives_energy_property", "public void updateProgressBar(int id, int value)" in container)
check("client_sets_energy_storage", "tile.energyStorage.setEnergyStored(value);" in container)

capacity = re.search(r"new EnergyStorage\((\d+)\)", tile)
check("energy_capacity_found", capacity is not None)
if capacity:
    check("energy_fits_signed_short", int(capacity.group(1)) <= 32767)
check("energy_storage_has_sync_setter", "allow for server -&gt; client sync" in energy and "void setEnergyStored(int energy)" in energy)
check("energy_modifier_single_init", config.count('setDouble("autoWorkbench_energyModifier"') == 1)
check("time_modifier_single_init", config.count('setDouble("autoWorkbench_timeModifier"') == 1)
other_sources = []
for source in (ROOT / "src/main/java").rglob("*.java"):
    if source == CONFIG:
        continue
    text = source.read_text(encoding="utf-8", errors="replace")
    if 'setDouble("autoWorkbench_energyModifier"' in text or 'setDouble("autoWorkbench_timeModifier"' in text:
        other_sources.append(str(source.relative_to(ROOT)))
check("no_runtime_modifier_mutators", not other_sources)

failed = [name for name, ok in checks if not ok]
print("TOTAL_PASS=%d/%d" % (len(checks)-len(failed), len(checks)))
if failed:
    print("FAILED=" + ",".join(failed))
    raise SystemExit(1)
print("AUTO_WORKBENCH_R3_STATE_SYNC_GATE=PASS")