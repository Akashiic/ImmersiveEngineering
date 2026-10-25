from pathlib import Path
import zipfile, hashlib
root=Path(r"C:\Users\User\Desktop\Codex\immersiveengineering-backport\work")
curdir=root/'ImmersiveEngineering-Forbidden-Backport'/'build'/'libs'
basedir=root/'audit-baseline-079'/'build'/'libs'
def mainjar(d):
    a=[p for p in d.glob('*.jar') if not p.name.endswith('-dev.jar') and not p.name.endswith('-sources.jar')]
    assert len(a)==1,(d,a)
    return a[0]
cur=mainjar(curdir); base=mainjar(basedir)
print('CURRENT',cur.name); print('BASELINE',base.name)
with zipfile.ZipFile(cur) as a, zipfile.ZipFile(base) as b:
    A=set(a.namelist()); B=set(b.namelist())
    added=sorted(A-B); removed=sorted(B-A)
    common=sorted(A&B)
    changed=[]
    for n in common:
        if hashlib.sha256(a.read(n)).digest()!=hashlib.sha256(b.read(n)).digest(): changed.append(n)
    print('ADDED',len(added)); [print('+',x) for x in added]
    print('REMOVED',len(removed)); [print('-',x) for x in removed]
    print('CHANGED',len(changed)); [print('*',x) for x in changed]
