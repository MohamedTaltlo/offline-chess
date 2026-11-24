import { Chess } from 'chess.js';
const value={p:100,n:320,b:335,r:500,q:900,k:0};
function evalBoard(g){let s=0;for(const row of g.board())for(const p of row)if(p){let f=p.square.charCodeAt(0)-97,r=Number(p.square[1])-1,center=7-Math.abs(f-3.5)-Math.abs(r-3.5),adv=p.color==='w'?r:7-r;let v=value[p.type]+(p.type==='p'?adv*9+center*2:p.type==='n'||p.type==='b'?center*12:p.type==='k'?-center*5:center*3);s+=(p.color==='w'?1:-1)*v;}return (g.turn()==='w'?1:-1)*s;}
function order(g){return g.moves({verbose:true}).sort((a,b)=>score(b)-score(a));}
function score(m){return (m.captured?10*value[m.captured]-value[m.piece]:0)+(m.promotion?value[m.promotion]:0)+(m.san.includes('+')?35:0);}
export function chooseMove(fen,level='medium',budgetMs){
 const g=new Chess(fen),moves=order(g);if(!moves.length)return null;
 if(level==='easy'&&Math.random()<.38)return moves[Math.floor(Math.random()*moves.length)].san;
 const depth={easy:1,medium:2,hard:3,expert:4}[level]||2;
 const deadline=Date.now()+(budgetMs||({easy:200,medium:500,hard:1400,expert:3000}[level]||500));
 let best=moves[0].san;const timeout={};
 function search(d,a,b,ply){
  if(Date.now()>deadline)throw timeout;
  if(g.isCheckmate())return -100000+ply;
  if(g.isDraw())return 0;
  if(d===0)return evalBoard(g);
  for(const m of order(g)){g.move(m.san);let v;try{v=-search(d-1,-b,-a,ply+1);}finally{g.undo();}if(v>=b)return v;if(v>a)a=v;}return a;
 }
 for(let d=1;d<=depth;d++){let vbest=-Infinity,movebest=best;try{const roots=order(g);roots.sort((a,b)=>Number(b.san===best)-Number(a.san===best));for(const m of roots){g.move(m.san);let v;try{v=-search(d-1,-Infinity,-vbest,1);}finally{g.undo();}if(v>vbest){vbest=v;movebest=m.san;}}best=movebest;}catch(e){if(e!==timeout)throw e;break;}}
 return best;
}
