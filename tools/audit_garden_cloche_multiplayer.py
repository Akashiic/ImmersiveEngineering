import math
import struct
from pathlib import Path

ROOT = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\garden-cloche-multiplayer")

def signed_short(value):
    value &= 0xffff
    return value - 0x10000 if value >= 0x8000 else value

def split_int(value):
    return signed_short(value), signed_short(value >> 16)

def combine(low, high):
    return ((high & 0xffff) << 16) | (low & 0xffff)

def to_signed_int(value):
    return value - 0x100000000 if value >= 0x80000000 else value

for value in [0, 1, 32767, 32768, 65535, 65536, 6000, 70000, 2**31-1, -1]:
    low, high = split_int(value)
    rebuilt = to_signed_int(combine(low, high))
    assert rebuilt == value, (value, low, high, rebuilt)
    print("INT_ROUNDTRIP", value, low, high, "PASS")
def float_bits(value):
    return struct.unpack(">I", struct.pack(">f", value))[0]

def bits_float(bits):
    return struct.unpack(">f", struct.pack(">I", bits & 0xffffffff))[0]

for value in [1.0, 1.25, 2.81818, 100.0, 32768.5, 1.0e-4]:
    bits = float_bits(value)
    low, high = split_int(bits)
    rebuilt_bits = combine(low, high)
    assert rebuilt_bits == bits, (value, bits, rebuilt_bits)
    rebuilt = bits_float(rebuilt_bits)
    assert float_bits(rebuilt) == bits
    print("FLOAT_BITS_ROUNDTRIP", value, hex(bits), "PASS")

tile = (ROOT / "src/main/java/blusunrize/immersiveengineering/common/blocks/metal/TileEntityGardenCloche.java").read_text(encoding="utf-8")
container = (ROOT / "src/main/java/blusunrize/immersiveengineering/common/gui/ContainerGardenCloche.java").read_text(encoding="utf-8")
gui = (ROOT / "src/main/java/blusunrize/immersiveengineering/client/gui/GuiGardenCloche.java").read_text(encoding="utf-8")

ids = {}
for line in tile.splitlines():
    line = line.strip()
    if line.startswith("public static final int SYNC_"):
        left, right = line.rstrip(";").split(" = ")
        ids[left.split()[-1]] = int(right)
assert sorted(ids.values()) == list(range(12)), ids
assert len(set(ids.values())) == len(ids)
print("SYNC_ID_GATE=PASS", ids)
assert "new FluidStack(net.minecraftforge.fluids.FluidRegistry.WATER" not in tile
assert "SYNC_FLUID_ID" not in tile
assert "FluidRegistry" not in container
assert "applySyncedFluidAmount" in tile
assert "FluidStack updated = current.copy();" in tile
assert "getFertilizerDurationForDisplay()" in gui
assert "common.Config" not in gui
assert "receiveMessageFromClient" not in tile
assert "worldObj.isRemote" in tile
print("MULTIPLAYER_STATIC_SYNC_GATE=PASS")

# S31PacketWindowProperty in Forge 1.7.10 carries property ID and value as signed shorts.
packet = Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport\build\rfg\minecraft-src\java\net\minecraft\network\play\server\S31PacketWindowProperty.java")
packet_text = packet.read_text(encoding="utf-8")
assert "data.writeShort(this.field_149184_b);" in packet_text
assert "data.writeShort(this.field_149185_c);" in packet_text
assert "this.field_149185_c = data.readShort();" in packet_text
print("FORGE_S31_SHORT_CONTRACT_GATE=PASS")

print("GARDEN_CLOCHE_MULTIPLAYER_AUDIT=PASS")
