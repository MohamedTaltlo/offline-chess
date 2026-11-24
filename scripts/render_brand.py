from pathlib import Path
from PIL import Image
out=Path('app/src/main/res/drawable-nodpi')
out.mkdir(parents=True,exist_ok=True)
a=Image.open('art/brand/primary.webp').convert('RGB')
b=Image.open('art/brand/secondary.webp').convert('RGB')
assert a.size==(128,208) and b.size==(96,96)
logo=a.crop((0,0,128,128))
logo.resize((384,384),Image.Resampling.LANCZOS).save(out/'brand_logo.png')
# Adaptive icon's central safe zone: keep the gold emblem within the launcher mask.
fg=Image.new('RGBA',(432,432),(0,0,0,0))
fg.alpha_composite(logo.resize((292,292),Image.Resampling.LANCZOS).convert('RGBA'),(70,70))
fg.save(out/'brand_foreground.png')
for i,name in enumerate(('new','undo','settings','draw','resign','offline')):
    x=(i%3)*40;y=128+(i//3)*40
    a.crop((x,y,x+40,y+40)).resize((128,128),Image.Resampling.LANCZOS).save(out/('ui_'+name+'.png'))
for i,name in enumerate(('cpu','friend','hints','castle')):
    x=(i%2)*48;y=(i//2)*48
    b.crop((x,y,x+48,y+48)).resize((128,128),Image.Resampling.LANCZOS).save(out/('ui_'+name+'.png'))
assert len(list(out.glob('ui_*.png')))==10
