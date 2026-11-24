package com.mt.chess;
public class NativeChessTest {
 static void ok(boolean v){if(!v)throw new AssertionError();}
 static void moves(Chess g,String... m){for(String s:m)if(!g.playCode(s))throw new AssertionError("illegal "+s);}
 static long perft(Chess g,int d){if(d==0)return 1;long n=0;for(Chess.Move m:g.legal()){Chess.State s=g.make(m);n+=perft(g,d-1);g.restore(s);}return n;}
 static Chess castlePosition(int side){Chess g=new Chess();java.util.Arrays.fill(g.b,0);g.b[60]=6;g.b[56]=4;g.b[63]=4;g.b[4]=-6;g.b[0]=-4;g.b[7]=-4;g.turn=side;g.keys.clear();g.keys.add(g.key());return g;}
 static void castleTests(){for(int side:new int[]{1,-1})for(boolean shortSide:new boolean[]{true,false}){Chess g=castlePosition(side);int f=side==1?60:4,rook=shortSide?f+3:f-4,t=shortSide?f+2:f-2,d=shortSide?f+1:f-1;String before=g.key();ok(g.castleTarget(f,rook)==t);ok(g.play(new Chess.Move(f,t)));ok(g.b[t]==side*6&&g.b[d]==side*4&&g.b[rook]==0&&g.b[f]==0);ok(g.notation.get(0).startsWith(shortSide?"O-O":"O-O-O"));ok(g.undo()&&g.key().equals(before));}
 Chess g=castlePosition(1);g.b[53]=1;g.b[61]=2;ok(!g.playCode("e1g1"));g=castlePosition(1);g.b[13]=-4;ok(!g.playCode("e1g1"));g=castlePosition(1);g.b[12]=-4;ok(!g.playCode("e1g1"));g=castlePosition(1);g.b[14]=-4;ok(!g.playCode("e1g1"));g=castlePosition(1);g.rights&=~1;ok(!g.playCode("e1g1"));ok(g.castleTarget(60,63)==63);g=castlePosition(1);moves(g,"h1h2","h8h7","h2h1","h7h8");ok(!g.playCode("e1g1"));}
 public static void main(String[] args){Chess g=new Chess();ok(perft(g,1)==20);ok(perft(g,2)==400);ok(perft(g,3)==8902);ok(perft(g,4)==197281);String k=g.key();moves(g,"e2e4","e7e5");ok(g.undo()&&g.undo()&&g.key().equals(k));moves(g,"f2f3","e7e5","g2g4","d8h4");ok(g.result().equals("black"));
 g=new Chess();moves(g,"e2e4","a7a6","e4e5","d7d5","e5d6");ok(g.b[Chess.index("d5")]==0);g.undo();ok(g.b[Chess.index("d5")]==-1);
 g=new Chess();moves(g,"e2e4","e7e5","g1f3","b8c6","f1c4","g8f6","e1g1");ok(g.b[61]==4&&g.b[62]==6);
 g=new Chess();java.util.Arrays.fill(g.b,0);g.b[60]=6;g.b[7]=-6;g.b[8]=1;g.rights=0;ok(g.playCode("a7a8n"));ok(g.b[0]==2);
 g=new Chess();moves(g,"g1f3","g8f6","f3g1","f6g8","g1f3","g8f6","f3g1","f6g8");ok(g.repeated());
 g=new Chess();for(int level=0;level<4;level++){Chess.Move m=new Chess(g).choose(level);ok(m!=null);ok(g.play(m));g.undo();}
 castleTests();System.out.println("Native chess tests passed (including all four castling variants and restrictions)");}
}
