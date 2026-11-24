from pathlib import Path
import cairosvg
out=Path('app/src/main/assets/pieces')
out.mkdir(parents=True,exist_ok=True)

# Original Staunton silhouettes drawn in a 100x100 viewbox. Both sides
# share geometry, with individually lit ivory and polished ebony finishes.
def path(d,fill='url(#body)',extra=''):
    return f'<path d="{d}" fill="{fill}" {extra}/>'
def ellipse(cx,cy,rx,ry,fill='url(#rim)',extra=''):
    return f'<ellipse cx="{cx}" cy="{cy}" rx="{rx}" ry="{ry}" fill="{fill}" {extra}/>'
def line(d):
    return path(d,'none','stroke="url(#shine)" stroke-width="1.8" stroke-linecap="round"')
base=path('M24 74 Q29 69 34 68 H66 Q72 69 76 74 L79 82 Q79 88 50 89 Q21 88 21 82 Z')+ellipse(50,75,26,5)+path('M21 82 Q19 83 19 88 Q19 94 50 95 Q81 94 81 88 L79 82 Q70 87 50 87 Q30 87 21 82 Z','url(#base)')+line('M25 87 Q50 94 75 87')
stem=path('M39 39 Q40 56 31 69 Q30 74 50 75 Q70 74 69 69 Q60 56 61 39 Z')
collar=path('M32 37 Q30 40 33 44 Q50 49 67 44 Q70 40 68 37 Z','url(#rim)')+ellipse(50,37,18,4)
pieces={
'P': stem+collar+ellipse(50,27,15,15,'url(#head)')+line('M40 25 Q40 17 48 15'),
'R': path('M34 34 L37 60 Q34 66 30 70 Q31 76 50 76 Q69 76 70 70 Q66 65 63 60 L66 34 Z')+path('M27 19 L27 34 Q27 40 50 42 Q73 40 73 34 L73 19 L62 19 L62 27 L55 27 L55 19 L45 19 L45 27 L38 27 L38 19 Z')+line('M32 33 Q50 39 68 33')+ellipse(50,19,23,5,'url(#top)','stroke="none"')+path('M27 17 L38 17 L38 24 L45 24 L45 17 L55 17 L55 24 L62 24 L62 17 L73 17 L73 26 Q50 32 27 26 Z')+line('M39 44 L41 59'),
'N': path('M29 73 Q29 63 44 53 Q53 46 53 37 Q45 47 35 45 L25 49 L18 39 L33 23 L36 13 L43 19 L49 10 Q70 17 75 38 Q82 59 68 73 Z')+path('M49 10 Q72 17 77 41 Q81 61 68 73 L61 73 Q72 53 66 37 Q61 25 49 18 Z','url(#mane)')+line('M40 21 Q53 26 54 34')+path('M33 29 L42 27 L39 32 Z','url(#eye)','stroke="none"')+ellipse(25,39,2.4,1.6,'url(#eye)','stroke="none"')+line('M44 55 Q33 65 34 69'),
'B': stem+collar+path('M50 10 Q71 24 65 32 Q62 37 50 40 Q38 37 35 32 Q29 24 50 10 Z','url(#head)')+path('M52 16 L61 27 L55 30 L47 19 Z','url(#eye)','stroke="none"')+ellipse(50,8,4,4,'url(#head)'),
'Q': stem+collar+path('M30 35 L22 16 L38 27 L43 12 L50 26 L57 12 L62 27 L78 16 L70 35 Q50 44 30 35 Z')+ellipse(50,35,20,4)+''.join(ellipse(x,y,3.7,3.7,'url(#head)') for x,y in [(22,15),(43,11),(57,11),(78,15)])+line('M36 32 Q50 37 64 32'),
'K': stem+collar+path('M31 31 Q28 21 39 21 Q47 21 50 29 Q53 21 61 21 Q72 21 69 31 L65 38 Q50 44 35 38 Z')+path('M46 8 H54 V14 H61 V21 H54 V30 H46 V21 H39 V14 H46 Z','url(#rim)')+line('M36 34 Q50 40 64 34')
}
def gradient(name,colors,vertical=False):
    direction='x1="0" y1="0" x2="0" y2="1"' if vertical else 'x1="0" y1=".12" x2="1" y2=".8"'
    stops=''.join(f'<stop offset="{i/(len(colors)-1):.3f}" stop-color="#{c}"/>' for i,c in enumerate(colors))
    return f'<linearGradient id="{name}" {direction}>{stops}</linearGradient>'
palettes={
'w':{'body':['fffbef','f9e7bc','c5a46b','f3ddb0'],'rim':['fff9e6','e1bf82','b68d51','f8e1ac'],'base':['fff2cc','b58f54','e9cc95'],'head':['fffdf6','fff0cf','c3a36c'],'top':['f1dfb6','fff9df'],'mane':['f0d39a','b28a4d','e0bd7f'],'shine':['ffffff','fbe5b2'],'eye':['534328','211d18']},
'b':{'body':['778079','36463e','101c19','45534a'],'rim':['90968a','35443a','101b16','5e6c5c'],'base':['657063','111c16','455747'],'head':['9ca49a','44534a','101b17'],'top':['32443b','69796d'],'mane':['4e5c52','0c1711','34463b'],'shine':['ced5c2','70877a'],'eye':['e4d8b3','bba66c']}
}
for color,palette in palettes.items():
    defs=''.join(gradient(k,v,k=='base') for k,v in palette.items())
    outline='#80613d' if color=='w' else '#091511'
    for kind,body in pieces.items():
        svg=f'''<svg xmlns="http://www.w3.org/2000/svg" width="320" height="320" viewBox="0 0 100 100"><defs>{defs}</defs>
        <ellipse cx="50" cy="94" rx="33" ry="4" fill="#08110c" opacity=".25"/>
        <g stroke="{outline}" stroke-width="1.65" stroke-linecap="round" stroke-linejoin="round">{body}{base}</g></svg>'''
        cairosvg.svg2png(bytestring=svg.encode(),write_to=str(out/(color+kind+'.png')),output_width=320,output_height=320)
assert len(list(out.glob('*.png')))==12
