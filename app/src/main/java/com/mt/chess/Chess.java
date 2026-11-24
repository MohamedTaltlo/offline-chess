package com.mt.chess;
import java.util.*;
final class Chess {
 static final int P=1,N=2,B=3,R=4,Q=5,K=6;
 static final int[] VAL={0,100,320,335,500,900,20000};
 static final int[][] KN={{1,2},{2,1},{-1,2},{-2,1},{1,-2},{2,-1},{-1,-2},{-2,-1}};
 static final int[][] DIR={{1,0},{-1,0},{0,1},{0,-1},{1,1},{1,-1},{-1,1},{-1,-1}};
 final int[] b=new int[64];
 int turn=1,rights=15,ep=-1,half=0,full=1;
 static final class Move {
  final int from,to,promo;
  Move(int f,int t,int p){from=f;to=t;promo=p;}
  Move(int f,int t){this(f,t,0);}
  String code(){return square(from)+square(to)+(promo==0?"":String.valueOf(" pnbrqk".charAt(promo)));}
 }
 static final class State {
  final int[] board; final int turn,rights,ep,half,full;
  State(Chess g){board=g.b.clone();turn=g.turn;rights=g.rights;ep=g.ep;half=g.half;full=g.full;}
 }
 final ArrayList<Move> history=new ArrayList<>();
 final ArrayList<State> previous=new ArrayList<>();
 final ArrayList<String> notation=new ArrayList<>();
 final ArrayList<Integer> captures=new ArrayList<>();
 final ArrayList<String> keys=new ArrayList<>();
 Chess(){int[] back={R,N,B,Q,K,B,N,R};for(int i=0;i<8;i++){b[i]=-back[i];b[8+i]=-P;b[48+i]=P;b[56+i]=back[i];}keys.add(key());}
 Chess(Chess other){restore(new State(other));}
 static boolean inside(int r,int c){return r>=0&&r<8&&c>=0&&c<8;}
 static String square(int i){return ""+(char)('a'+i%8)+(8-i/8);}
 static int index(String s){return (8-(s.charAt(1)-'0'))*8+s.charAt(0)-'a';}
 void restore(State s){System.arraycopy(s.board,0,b,0,64);turn=s.turn;rights=s.rights;ep=s.ep;half=s.half;full=s.full;}
 State make(Move m){State s=new State(this);int p=b[m.from],victim=b[m.to],kind=Math.abs(p),side=turn;
  if(kind==P&&m.to==ep&&victim==0&&m.from%8!=m.to%8){b[m.to+side*8]=0;victim=-side*P;}
  b[m.from]=0;b[m.to]=m.promo!=0?side*m.promo:p;
  if(kind==K&&Math.abs(m.to-m.from)==2){int rook=m.to>m.from?m.from+3:m.from-4, dest=m.to>m.from?m.from+1:m.from-1;b[dest]=b[rook];b[rook]=0;}
  if(kind==K)rights&=side==1?~3:~12;
  if(m.from==63||m.to==63)rights&=~1;if(m.from==56||m.to==56)rights&=~2;
  if(m.from==7||m.to==7)rights&=~4;if(m.from==0||m.to==0)rights&=~8;
  ep=kind==P&&Math.abs(m.to-m.from)==16?(m.to+m.from)/2:-1;
  half=kind==P||victim!=0?0:half+1;if(side==-1)full++;turn=-side;return s;
 }
 boolean attacked(int sq,int side){int r=sq/8,c=sq%8,pr=r+side;for(int dc:new int[]{-1,1})if(inside(pr,c+dc)&&b[pr*8+c+dc]==side*P)return true;
  for(int[] d:KN)if(inside(r+d[0],c+d[1])&&b[(r+d[0])*8+c+d[1]]==side*N)return true;
  for(int i=0;i<DIR.length;i++){int[] d=DIR[i];for(int n=1;n<8;n++){int rr=r+d[0]*n,cc=c+d[1]*n;if(!inside(rr,cc))break;int p=b[rr*8+cc];if(p==0)continue;if(p==side*Q||p==side*(i<4?R:B)||n==1&&p==side*K)return true;break;}}
  return false;
 }
 boolean check(int side){for(int i=0;i<64;i++)if(b[i]==side*K)return attacked(i,-side);return true;}
 void pawn(ArrayList<Move> a,int f,int t){if(t/8==0||t/8==7)for(int p:new int[]{Q,R,B,N})a.add(new Move(f,t,p));else a.add(new Move(f,t));}
 ArrayList<Move> pseudo(){ArrayList<Move>a=new ArrayList<>();for(int f=0;f<64;f++){int p=b[f];if(p*turn<=0)continue;int k=Math.abs(p),r=f/8,c=f%8;
  if(k==P){int rr=r-turn;if(inside(rr,c)){int t=rr*8+c;if(b[t]==0){pawn(a,f,t);if(r==(turn==1?6:1)&&b[t-turn*8]==0)a.add(new Move(f,t-turn*8));}
   for(int dc:new int[]{-1,1})if(inside(rr,c+dc)){t=rr*8+c+dc;if((b[t]*turn<0&&Math.abs(b[t])!=K)||(t==ep&&b[t]==0&&b[t+turn*8]==-turn*P))pawn(a,f,t);}}continue;}
  if(k==N){for(int[] d:KN){int rr=r+d[0],cc=c+d[1];if(inside(rr,cc)){int t=rr*8+cc;if(b[t]*turn<=0&&Math.abs(b[t])!=K)a.add(new Move(f,t));}}continue;}
  int start=k==B?4:0,end=k==R?4:8;
  for(int i=start;i<end;i++){int[]d=DIR[i];for(int n=1;n<=(k==K?1:7);n++){int rr=r+d[0]*n,cc=c+d[1]*n;if(!inside(rr,cc))break;int t=rr*8+cc;if(b[t]*turn>0)break;if(Math.abs(b[t])!=K)a.add(new Move(f,t));if(b[t]!=0)break;}}
  if(k==K&&f==(turn==1?60:4)&&!check(turn)){int mask=turn==1?1:4;
    if((rights&mask)!=0&&b[f+1]==0&&b[f+2]==0&&b[f+3]==turn*R&&!attacked(f+1,-turn)&&!attacked(f+2,-turn))a.add(new Move(f,f+2));
    if((rights&(mask*2))!=0&&b[f-1]==0&&b[f-2]==0&&b[f-3]==0&&b[f-4]==turn*R&&!attacked(f-1,-turn)&&!attacked(f-2,-turn))a.add(new Move(f,f-2));}
 }return a;}
 ArrayList<Move> legal(){ArrayList<Move>a=new ArrayList<>();int side=turn;for(Move m:pseudo()){State s=make(m);boolean ok=!check(side);restore(s);if(ok)a.add(m);}return a;}
 String key(){StringBuilder s=new StringBuilder();for(int p:b)s.append((char)(p+65));int legalEp=-1;if(ep>=0){for(Move m:pseudo())if(m.to==ep&&Math.abs(b[m.from])==P&&m.from%8!=m.to%8){int side=turn;State st=make(m);boolean ok=!check(side);restore(st);if(ok){legalEp=ep;break;}}}return s.append('/').append(turn).append('/').append(rights).append('/').append(legalEp).toString();}
 boolean play(Move m){ArrayList<Move> legal=legal();Move chosen=null;for(Move x:legal)if(x.from==m.from&&x.to==m.to&&x.promo==m.promo){chosen=x;break;}if(chosen==null)return false;m=chosen;
  int kind=Math.abs(b[m.from]),capt=b[m.to];if(kind==P&&m.to==ep&&capt==0&&m.from%8!=m.to%8)capt=-turn*P;
  String san;if(kind==K&&Math.abs(m.to-m.from)==2)san=m.to>m.from?"O-O":"O-O-O";else{
   StringBuilder s=new StringBuilder();if(kind!=P){s.append(" PNBRQK".charAt(kind));boolean sameFile=false,sameRank=false,other=false;for(Move x:legal)if(x.from!=m.from&&x.to==m.to&&b[x.from]==b[m.from]){other=true;sameFile|=x.from%8==m.from%8;sameRank|=x.from/8==m.from/8;}if(other){if(!sameFile)s.append((char)('a'+m.from%8));else if(!sameRank)s.append(8-m.from/8);else s.append(square(m.from));}}
   else if(capt!=0)s.append((char)('a'+m.from%8));if(capt!=0)s.append('x');s.append(square(m.to));if(m.promo!=0)s.append('=').append(" PNBRQK".charAt(m.promo));san=s.toString();}
  previous.add(make(m));history.add(m);captures.add(capt);keys.add(key());if(check(turn))san+=legal().isEmpty()?"#":"+";notation.add(san);return true;
 }
 boolean playCode(String s){try{int p=s.length()==5?" pnbrqk".indexOf(s.charAt(4)):0;return play(new Move(index(s.substring(0,2)),index(s.substring(2,4)),p));}catch(Exception e){return false;}}
 boolean undo(){int n=history.size()-1;if(n<0)return false;restore(previous.remove(n));history.remove(n);notation.remove(n);captures.remove(n);keys.remove(n+1);return true;}
 boolean insufficient(){int minors=0,bishopColor=-1;boolean allB=true;for(int i=0;i<64;i++){int p=Math.abs(b[i]);if(p==0||p==K)continue;if(p!=B&&p!=N)return false;minors++;if(p==N)allB=false;else{int c=(i/8+i%8)&1;if(bishopColor<0)bishopColor=c;else if(bishopColor!=c)allB=false;}}return minors<=1||allB;}
 boolean repeated(){String k=keys.get(keys.size()-1);int n=0;for(String s:keys)if(s.equals(k)&&++n>=3)return true;return false;}
 String result(){if(legal().isEmpty())return check(turn)?(turn==1?"black":"white"):"stalemate";if(half>=100||insufficient()||repeated())return "draw";return "";}
 int evaluate(){int score=0;for(int i=0;i<64;i++){int p=b[i],k=Math.abs(p);if(p==0)continue;int side=p>0?1:-1,r=i/8,c=i%8,center=7-Math.abs(2*c-7)/2-Math.abs(2*r-7)/2,advance=side==1?6-r:r-1;int v=VAL[k]+(k==P?advance*12+center*4:k==N||k==B?center*14:k==K?-center*6:center*4);score+=side*v;}return score*turn;}
 int priority(Move m){int p=b[m.to];return (p==0?0:10*VAL[Math.abs(p)]-VAL[Math.abs(b[m.from])])+(m.promo==0?0:VAL[m.promo]);}
 ArrayList<Move> ordered(){ArrayList<Move>a=legal();Collections.sort(a,(x,y)->Integer.compare(priority(y),priority(x)));return a;}
 static final class TimeUp extends RuntimeException {}
 long deadline;boolean aborted(){return Thread.currentThread().isInterrupted()||System.nanoTime()>deadline;}
 int search(int d,int a,int z,int ply){if(aborted())throw new TimeUp();ArrayList<Move> moves=ordered();if(moves.isEmpty())return check(turn)?-100000+ply:0;if(half>=100||insufficient())return 0;if(d==0)return evaluate();
  for(Move m:moves){State st=make(m);int v;try{v=-search(d-1,-z,-a,ply+1);}finally{restore(st);}if(v>=z)return v;if(v>a)a=v;}return a;}
 Move choose(int level){ArrayList<Move> moves=ordered();if(moves.isEmpty())return null;Random rng=new Random();if(level==0&&rng.nextInt(4)==0)return moves.get(rng.nextInt(moves.size()));int[] depths={1,2,3,5};long[] ms={180,500,1300,3000};level=Math.max(0,Math.min(3,level));deadline=System.nanoTime()+ms[level]*1000000L;Move best=moves.get(0);
  for(int d=1;d<=depths[level];d++){int vbest=-1000000;Move candidate=best;try{moves.remove(best);moves.add(0,best);for(Move m:moves){State st=make(m);int v;try{v=-search(d-1,-1000000,-vbest,1);}finally{restore(st);}if(v>vbest){vbest=v;candidate=m;}}best=candidate;}catch(TimeUp ex){break;}}return best;}
}
