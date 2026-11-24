import {Chess} from 'chess.js';import {pieceSVG} from './pieces.js';
const $=id=>document.getElementById(id), key='mtchess.v1', levels={easy:'سهل',medium:'متوسط',hard:'صعب',expert:'خبير'};
let game=new Chess(),mode='cpu',level='medium',human='w',selected=null,pending=null,worker=null,ending='',hints=true;
function save(){try{localStorage.setItem(key,JSON.stringify({moves:game.history(),mode,level,human,ending,hints}));}catch(_){}}
function load(){try{let x=JSON.parse(localStorage.getItem(key));if(!x)return false;let g=new Chess();for(let m of x.moves)g.move(m);game=g;mode=x.mode==='friend'?'friend':'cpu';level=levels[x.level]?x.level:'medium';human=x.human==='b'?'b':'w';ending=typeof x.ending==='string'?x.ending:'';hints=x.hints!==false;return true;}catch(_){return false;}}
function done(){return !!ending||game.isGameOver();}
function thinking(){return mode==='cpu'&&!done()&&game.turn()!==human;}
function stop(){if(worker){worker.terminate();worker=null;}}
function status(){if(ending)return ending;if(game.isCheckmate())return 'كش مات — فاز '+(game.turn()==='w'?'الأسود':'الأبيض');if(game.isStalemate())return 'تعادل — لا توجد نقلات قانونية';if(game.isDraw())return 'انتهت المباراة بالتعادل';return thinking()?'الكمبيوتر يفكر…':(game.isCheck()?'كش ملك — ':'')+'دور '+(game.turn()==='w'?'الأبيض':'الأسود');}
const symbols={p:'♟',n:'♞',b:'♝',r:'♜',q:'♛',k:'♚'};
function render(){
 const h=game.history({verbose:true}),last=h[h.length-1],legal=selected?game.moves({square:selected,verbose:true}):[];
 const board=$('board');board.replaceChildren();
 // The board ALWAYS has White at the bottom; neither turns nor settings rotate it.
 for(let r=8;r>=1;r--)for(let f=0;f<8;f++){const sq='abcdefgh'[f]+r,p=game.get(sq),b=document.createElement('button');b.type='button';b.className='square'+((f+r)%2===1?' dark':'')+(last&&(last.from===sq||last.to===sq)?' last':'')+(selected===sq?' selected':'')+(p&&p.type==='k'&&p.color===game.turn()&&game.isCheck()?' checkKing':'');b.setAttribute('aria-label',sq+(p?' '+p.color+p.type:''));if(p)b.innerHTML=pieceSVG(p);if(hints&&legal.some(m=>m.to===sq)){const dot=document.createElement('span');dot.className=p?'ring':'dot';b.append(dot);}if(f===0){const c=document.createElement('span');c.className='coord rank';c.textContent=r;b.append(c);}if(r===1){const c=document.createElement('span');c.className='coord file';c.textContent='abcdefgh'[f];b.append(c);}b.addEventListener('click',()=>tap(sq));board.append(b);}
 $('blackName').textContent=mode==='cpu'?(human==='b'?'أنت · الأسود':'الكمبيوتر · الأسود'):'اللاعب الثاني · الأسود';
 $('whiteName').textContent=mode==='cpu'?(human==='w'?'أنت · الأبيض':'الكمبيوتر · الأبيض'):'اللاعب الأول · الأبيض';
 $('modeLabel').textContent=mode==='cpu'?'ضد الكمبيوتر · '+levels[level]:'لاعبان على نفس الشاشة';
 for(const c of ['w','b']){const id=c==='w'?'white':'black';$(id).classList.toggle('active',!done()&&game.turn()===c);$(id+'Capt').textContent=h.filter(m=>m.color===c&&m.captured).map(m=>symbols[m.captured]).join(' ');}
 $('status').textContent=status();$('hints').checked=hints;$('undo').disabled=!h.length||(mode==='cpu'&&human==='b'&&h.length===1);$('resign').disabled=done()||thinking();$('draw').disabled=done()||mode!=='friend';
 const hist=$('history');hist.replaceChildren();for(let i=0;i<h.length;i+=2){for(const [v,cls] of [[String(i/2+1)+'.','num'],[h[i].san,''],[h[i+1]?.san||'','']]){const el=document.createElement('span');el.textContent=v;el.className=cls;hist.append(el);}}hist.scrollTop=hist.scrollHeight;
}
function play(move){try{game.move(move);}catch(_){return;}selected=null;pending=null;save();render();schedule();}
function tap(sq){if(done()||thinking()||$('setup').open||$('promote').open)return;const p=game.get(sq);
 if(selected){const candidates=game.moves({square:selected,verbose:true}).filter(m=>m.to===sq);if(candidates.length){if(candidates[0].promotion){pending={from:selected,to:sq};$('promotions').replaceChildren();for(const type of ['q','r','b','n']){let b=document.createElement('button');b.innerHTML=pieceSVG({type,color:game.turn()});b.setAttribute('aria-label',type);b.onclick=()=>{const m={...pending,promotion:type};$('promote').close();play(m);};$('promotions').append(b);}$('promote').showModal();return;}play({from:selected,to:sq});return;}}
 selected=p&&p.color===game.turn()?(selected===sq?null:sq):null;render();
}
function schedule(){if(!thinking()||worker||$('setup').open)return;try{const w=new Worker('worker.js');worker=w;const fallback=()=>{if(worker!==w)return;stop();if(thinking()){const moves=game.moves();if(moves.length)play(moves[0]);}};w.onmessage=e=>{if(worker!==w)return;if(e.data.move){const m=e.data.move;stop();if(thinking())play(m);}else fallback();};w.onerror=fallback;w.postMessage({fen:game.fen(),level});}catch(_){const m=game.moves()[0];if(m)play(m);}}
function undo(){stop();if($('promote').open)$('promote').close();pending=null;selected=null;if(mode==='friend'){game.undo();}else{if(!game.history().length)return;game.undo();if(game.turn()!==human&&game.history().length)game.undo();}ending='';save();render();schedule();}
function showSetup(){stop();document.querySelector('input[name=mode][value='+mode+']').checked=true;$('level').value=level;$('color').value=human;options();$('setup').showModal();}
function options(){$('cpuOptions').hidden=document.querySelector('input[name=mode]:checked').value!=='cpu';}
document.querySelectorAll('input[name=mode]').forEach(x=>x.onchange=options);
$('setupForm').onsubmit=e=>{e.preventDefault();stop();mode=document.querySelector('input[name=mode]:checked').value;level=$('level').value;human=$('color').value;if(human==='random')human=Math.random()<.5?'w':'b';game=new Chess();selected=null;pending=null;ending='';$('setup').close();save();render();schedule();};
$('close').onclick=()=>$('setup').close();$('setup').addEventListener('close',schedule);
$('new').onclick=showSetup;$('undo').onclick=undo;$('cancelPromotion').onclick=()=>$('promote').close();$('promote').addEventListener('close',()=>{pending=null;});
$('hints').onchange=e=>{hints=e.target.checked;save();render();};
$('resign').onclick=()=>{if(confirm('هل تريد الاستسلام؟')){ending='استسلام — فاز '+(game.turn()==='w'?'الأسود':'الأبيض');stop();save();render();}};
$('draw').onclick=()=>{if(confirm('هل وافق اللاعبان على التعادل؟')){ending='تعادل بالاتفاق';save();render();}};
const restored=load();render();if(!restored)showSetup();else schedule();
