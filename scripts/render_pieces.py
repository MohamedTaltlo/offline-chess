from pathlib import Path
import cairosvg, shutil
src=Path('art/chessnut')
out=Path('app/src/main/assets/pieces')
out.mkdir(parents=True, exist_ok=True)
for f in src.glob('*.svg'):
    cairosvg.svg2png(url=str(f),write_to=str(out/(f.stem+'.png')),output_width=320,output_height=320)
assert len(list(out.glob('*.png')))==12
for name in ('LICENSE.txt','NOTICE.txt'):
    shutil.copyfile(src/name,out/name)
