from pathlib import Path
repo=Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work\ImmersiveEngineering-Forbidden-Backport")

def patch(rel,old,new):
    p=repo/rel; b=p.read_bytes(); nl=b"\r\n" if b"\r\n" in b else b"\n"
    ob=old.replace("\n",nl.decode()).encode(); nb=new.replace("\n",nl.decode()).encode()
    assert b.count(ob)==1,(rel,b.count(ob)); p.write_bytes(b.replace(ob,nb,1))

patch('src/main/java/blusunrize/immersiveengineering/common/blocks/metal/BlockMetalDevices2.java',
'''\t\telse if(te instanceof TileEntityGardenCloche && !player.isSneaking())
\t\t{
\t\t\tTileEntityGardenCloche cloche = ((TileEntityGardenCloche)te).getMaster();
\t\t\tif(cloche!=null)
\t\t\t{
\t\t\t\tif(!world.isRemote)
\t\t\t\t\tplayer.openGui(ImmersiveEngineering.instance, Lib.GUIID_GardenCloche, world, cloche.xCoord, cloche.yCoord, cloche.zCoord);
\t\t\t\treturn true;
\t\t\t}
\t\t}
''',
'''\t\telse if(te instanceof TileEntityGardenCloche)
\t\t{
\t\t\tTileEntityGardenCloche cloche = ((TileEntityGardenCloche)te).getMaster();
\t\t\tif(cloche!=null)
\t\t\t{
\t\t\t\tif(Utils.isHammer(player.getCurrentEquippedItem()))
\t\t\t\t{
\t\t\t\t\tif(!world.isRemote)
\t\t\t\t\t\tcloche.rotateFacing(player.isSneaking());
\t\t\t\t\treturn true;
\t\t\t\t}
\t\t\t\tif(!player.isSneaking())
\t\t\t\t{
\t\t\t\t\tif(Utils.fillFluidHandlerWithPlayerItem(world, cloche, player))
\t\t\t\t\t\treturn true;
\t\t\t\t\tif(!world.isRemote)
\t\t\t\t\t\tplayer.openGui(ImmersiveEngineering.instance, Lib.GUIID_GardenCloche, world, cloche.xCoord, cloche.yCoord, cloche.zCoord);
\t\t\t\t\treturn true;
\t\t\t\t}
\t\t\t}
\t\t}
''')
patch('src/main/java/blusunrize/immersiveengineering/common/blocks/metal/TileEntityGardenCloche.java',
'''\tpublic float getGrowth()
\t{
\t\treturn growth;
\t}
''',
'''\tpublic void rotateFacing(boolean reverse)
\t{
\t\tTileEntityGardenCloche master = getMaster();
\t\tif(master==null || master.worldObj==null || master.worldObj.isRemote)
\t\t\treturn;
\t\tint next;
\t\tif(reverse)
\t\t\tnext = master.facing==2?4:master.facing==4?3:master.facing==3?5:2;
\t\telse
\t\t\tnext = master.facing==2?5:master.facing==5?3:master.facing==3?4:2;
\t\tfor(int i=0; i<3; i++)
\t\t{
\t\t\tTileEntity part = master.worldObj.getTileEntity(master.xCoord, master.yCoord+i, master.zCoord);
\t\t\tif(part instanceof TileEntityGardenCloche)
\t\t\t{
\t\t\t\tTileEntityGardenCloche clochePart = (TileEntityGardenCloche)part;
\t\t\t\tclochePart.facing = next;
\t\t\t\tclochePart.markDirty();
\t\t\t\tmaster.worldObj.markBlockForUpdate(clochePart.xCoord, clochePart.yCoord, clochePart.zCoord);
\t\t\t}
\t\t}
\t}

\tpublic float getGrowth()
\t{
\t\treturn growth;
\t}
''')
print('cloche UX patch applied')
