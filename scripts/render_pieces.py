from pathlib import Path
from PIL import Image
import shutil
src=Path('art/staunton-metal')
out=Path('app/src/main/assets/pieces')
out.mkdir(parents=True,exist_ok=True)
names=[c+k for c in 'wb' for k in 'PNBRQK']
images={n:Image.open(src/(n+'.png')).convert('RGBA') for n in names}
# Normalize the shared source coordinate system so relative piece sizes are preserved.
# Crop excess empty space using a common bounding box and fit the art into a square.
assert len({im.size for im in images.values()})==1
boxes=[im.getchannel('A').point(lambda x:255 if x>12 else 0).getbbox() for im in images.values()]
box=(min(v[0] for v in boxes),min(v[1] for v in boxes),max(v[2] for v in boxes),max(v[3] for v in boxes))
for name,im in images.items():
    cropped=im.crop(box)
    cropped.thumbnail((308,308),Image.Resampling.LANCZOS)
    canvas=Image.new('RGBA',(320,320))
    canvas.alpha_composite(cropped,((320-cropped.width)//2,(320-cropped.height)//2))
    canvas.save(out/(name+'.png'),optimize=True)
for n in ('LICENSE.txt','NOTICE.txt'):
    shutil.copyfile(src/n,out/n)
assert len(list(out.glob('*.png')))==12
