# Forbidden IE 0.7.9 — Legacy Network / Netty Security Audit

Date: 2026-09-11
Status: BLOCKER FOUND — HARDEN BEFORE NETWORK SURFACE EXPANSION
Scope: current recovered baseline plus Auto Workbench R2 source

## Proven FML execution model
The exact FML 1.7.10 source used as platform reference shows `SimpleChannelHandlerWrapper.channelRead0()` constructing `MessageContext` and invoking `messageHandler.onMessage()` directly from the Netty channel handler.

The current IE handlers therefore must not assume they are already executing on the authoritative server tick thread.

## Current channel surface
The legacy mod owns one `SimpleNetworkWrapper` channel named `ImmersiveEngineering` and currently registers eight message discriminators.

The broadest client-to-server packet is `MessageTileSync`.
Its wire payload is:
- dimension id;
- x/y/z;
- arbitrary `NBTTagCompound`.

The server handler resolves `DimensionManager.getWorld(message.dimension)`, loads the TileEntity at the supplied coordinates and forwards the NBT to any `TileEntityIEBase` without consulting the sending player.
## Confirmed authorization gap
`MessageContext` exposes `getServerHandler()`, but the current IE network package does not use it.

Consequently `MessageTileSync` currently does not verify:
- that the sender is in the requested dimension;
- that the sender is near the target;
- that the sender has the relevant GUI/container open;
- that the sender is allowed to operate that machine;
- that the target chunk/tile belongs to the sender's current interaction context.

Because the message accepts an arbitrary dimension id, a client can address a loaded IE TileEntity in a different server dimension if its coordinates are known. The downstream TileEntity receives no sender identity, so it cannot perform player-specific authorization.

This is an architectural trust failure even when a particular TileEntity validates its numeric fields.

## Confirmed malformed-input risk — Assembler
`TileEntityAssembler.receiveMessageFromClient()` validates `buttonID`, but its `patternSync` path uses client-provided values directly:
- `patterns[r]` where `r = message.getInteger("recipe")`;
- `pattern.inv[itemTag.getInteger("slot")]` for every supplied item tag.

No bounds check is performed on either index in that path.

A malformed client packet can therefore drive array indexing outside the valid recipe/slot ranges. This is classified as a server-crash/security risk until a runtime proof disproves exploitability.
## Current consumer review
### Auto Workbench
The R2 receiver resolves the master, requires the `recipe` key and clamps/validates the selected recipe against the installed Blueprint category.

This is good value validation, but it still inherits the generic sender/distance/dimension trust gap.

### Conveyor Sorter
The receiver replaces `sideFilter` with the client-provided integer array without enforcing the expected six entries or allowed flag mask.

The examined query helpers bounds-check side indices, so a short array is not currently proven to crash that path. Nevertheless the packet can remotely mutate the sorter configuration and persist a structurally invalid-length state.

### Assembler
The `buttonID` path is range-checked. The `patternSync` path is not and is the highest-priority concrete malformed-input defect found in this round.

## Thread-affinity risk
The same handlers resolve worlds, fetch TileEntities and mutate machine state directly from `onMessage()`.

Since FML's exact 1.7.10 `SimpleChannelHandlerWrapper` invokes the handler from its Netty channel callback, new code must explicitly marshal world mutations onto a server-tick-owned queue rather than expanding the current pattern.

This finding is independent of whether a historical race is easy to reproduce; the architectural ownership is wrong for a larger modern packet surface.
## Hardening architecture
### Compatibility layer for existing packets
Keep the discriminator layout stable for current clients while hardening server handling.

For client-to-server machine actions:
1. decode only primitive/bounded envelope data on the network thread;
2. capture the authenticated server-side player from `MessageContext`;
3. enqueue a small immutable command to an IE-owned server-tick queue;
4. on the server thread, revalidate player/world/dimension/target state;
5. reject unloaded or invalid targets without forcing chunk loads;
6. enforce interaction distance consistent with the machine's own `isUseableByPlayer` contract;
7. validate action-specific numeric ranges, array lengths and tag types;
8. mutate state only after all checks pass.

### Long-term modern packets
Do not add new features by stuffing more keys into generic `MessageTileSync`.

New GUI/action families should use typed packet classes carrying only the data required for that action. Examples:
- select recipe index;
- toggle redstone mode;
- configure one router side/mask;
- set bounded frequency/channel;
- rename a storage slot with bounded text.

Server logic resolves authoritative inventory/recipes/state; the client never sends authoritative machine contents.
## Payload / denial-of-service rules
Outer transport limits are not a substitute for application bounds.

Every decoder/handler introduced by the backport must define explicit maxima for:
- list/array entry count;
- text length;
- recipe/frequency/index ranges;
- optional NBT depth/shape where NBT is unavoidable;
- number of actions accepted per player in a bounded interval when an action can trigger meaningful server work.

Packet handling must never:
- force-load a chunk merely because a client named its coordinates;
- allocate an array/list directly from an untrusted length without checking it;
- perform recipe scans, world scans or expensive graph traversals on the Netty callback;
- trust client-provided ItemStacks as authoritative inventory state;
- execute a machine mutation before validating all fields.

## Compatibility strategy
The R2 Auto Workbench runtime candidate remains frozen for A6; this audit does not retroactively mutate the tagged artifact.

Network hardening should be developed on a separate branch/worktree, built and tested independently, then merged only through an explicit regression gate.

The long-term goal is to retire generic client-authoritative NBT writes for new features while maintaining backwards compatibility for existing GUI actions until their typed replacements are proven.

## Approval
Current legacy client->server network architecture: **REJECT FOR EXPANSION**.

Existing gameplay may continue under frozen-test conditions, but no new 1.21.1 GUI/control feature is approved to reuse the current generic handler unchanged.

Required next gate:
- implement server-thread marshalling;
- add sender/dimension/distance validation;
- harden Assembler and Sorter field bounds;
- add malformed-packet tests;
- verify all existing GUI/NEI flows still function;
- run dedicated-server regression before merge.
