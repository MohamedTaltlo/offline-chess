package com.mt.chess;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.animation.ValueAnimator;
import java.util.*;
final class BoardView extends View {
 interface Listener{void touch(int square);void drag(int from,int to);}
 Listener listener;
 Chess game;int selected=-1,lastFrom=-1,lastTo=-1,check=-1;
 boolean hints=true,enabled=true;
 int theme=0;
 ArrayList<Chess.Move> legal=new ArrayList<>();
 final Paint p=new Paint(3);final Bitmap[] images=new Bitmap[13];final RectF dst=new RectF();float size,cell,left,top;
 int down=-1;float downX,downY,dragX,dragY;boolean dragging;
 int animFrom=-1,animTo=-1,animPiece=0,rookFrom=-1,rookTo=-1,rookPiece=0;float fraction=1;ValueAnimator animator;
 static final int[][] COLORS={{0xffeee7d4,0xff638a73},{0xfff6dfb9,0xffa46b43},{0xffe0e9f1,0xff67849e},{0xffe5e5dd,0xff62676d}};
 BoardView(Context ctx){super(ctx);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setFocusable(true);setContentDescription("رقعة الشطرنج، الأبيض بالأسفل والأسود بالأعلى");
  for(int s:new int[]{1,-1})for(int k=1;k<=6;k++){String file=(s==1?"w":"b")+" PNBRQK".charAt(k)+".png";try(java.io.InputStream in=ctx.getAssets().open("pieces/"+file)){images[s*k+6]=BitmapFactory.decodeStream(in);}catch(Exception ex){throw new IllegalStateException("Missing chess art "+file,ex);}}}
 @Override protected void onMeasure(int w,int h){int width=MeasureSpec.getSize(w);setMeasuredDimension(width,width);}
 void update(Chess g,int sel,boolean show,boolean can,int style){game=g;selected=sel;hints=show;enabled=can;theme=Math.max(0,Math.min(COLORS.length-1,style));legal=sel<0?new ArrayList<>():g.legal();int n=g.history.size();lastFrom=n==0?-1:g.history.get(n-1).from;lastTo=n==0?-1:g.history.get(n-1).to;check=-1;if(g.check(g.turn))for(int i=0;i<64;i++)if(g.b[i]==g.turn*Chess.K)check=i;invalidate();}
 void animateMove(int from,int to,int piece){if(animator!=null)animator.cancel();animFrom=from;animTo=to;animPiece=piece;rookFrom=rookTo=-1;rookPiece=0;if(Math.abs(piece)==Chess.K&&Math.abs(to-from)==2){rookFrom=to>from?from+3:from-4;rookTo=to>from?from+1:from-1;rookPiece=piece>0?Chess.R:-Chess.R;}fraction=0;animator=ValueAnimator.ofFloat(0,1);animator.setDuration(rookTo>=0?260:175);animator.setInterpolator(new android.view.animation.DecelerateInterpolator());animator.addUpdateListener(a->{fraction=(float)a.getAnimatedValue();invalidate();});animator.start();}
 void clearPieceAnimation(){if(animator!=null)animator.cancel();fraction=1;invalidate();}
 void fill(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(l,t,r,b,p);}
 void piece(Canvas c,int val,float x,float y,float scale){Bitmap b=images[val+6];if(b==null)return;float inset=cell*(1-scale)/2;dst.set(x+inset,y+inset,x+cell-inset,y+cell-inset);p.setColor(Color.WHITE);p.setAlpha(255);p.setFilterBitmap(true);c.drawBitmap(b,null,dst,p);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(game==null)return;size=Math.min(getWidth(),getHeight());cell=size/8f;left=(getWidth()-size)/2;top=(getHeight()-size)/2;Path clip=new Path();clip.addRoundRect(new RectF(left,top,left+size,top+size),cell*.17f,cell*.17f,Path.Direction.CW);c.save();c.clipPath(clip);
  for(int i=0;i<64;i++){int r=i/8,col=i%8;float x=left+col*cell,y=top+r*cell;int base=COLORS[theme][(r+col)%2];fill(c,base,x,y,x+cell,y+cell);
   if(i==lastFrom||i==lastTo)fill(c,0x8896cd54,x,y,x+cell,y+cell);
   if(i==check){p.setShader(new RadialGradient(x+cell/2,y+cell/2,cell*.68f,new int[]{0xffed5b55,0x00ed5b55},null,Shader.TileMode.CLAMP));c.drawRect(x,y,x+cell,y+cell,p);p.setShader(null);}
   if(i==selected){fill(c,0x889dd453,x,y,x+cell,y+cell);p.setColor(0xffd8ec9d);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(cell*.045f);c.drawRect(x+2,y+2,x+cell-2,y+cell-2,p);p.setStyle(Paint.Style.FILL);}
   if(game.b[i]!=0&&!(dragging&&i==down)&&!(fraction<1&&(i==animTo||i==rookTo)))piece(c,game.b[i],x,y,.94f);
   if(hints)for(Chess.Move m:legal)if(m.from==selected&&m.to==i){p.setColor(0x80405b43);if(game.b[i]==0)c.drawCircle(x+cell/2,y+cell/2,cell*.115f,p);else{p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(cell*.07f);c.drawCircle(x+cell/2,y+cell/2,cell*.435f,p);p.setStyle(Paint.Style.FILL);}break;}
   p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextSize(cell*.17f);p.setColor(COLORS[theme][1-(r+col)%2]);p.setTextAlign(Paint.Align.LEFT);if(col==0)c.drawText(""+(8-r),x+cell*.055f,y+cell*.18f,p);if(r==7){p.setTextAlign(Paint.Align.RIGHT);c.drawText(""+(char)('a'+col),x+cell*.94f,y+cell*.94f,p);}
  }
  if(fraction<1&&animPiece!=0){float x=left+(animFrom%8+(animTo%8-animFrom%8)*fraction)*cell,y=top+(animFrom/8+(animTo/8-animFrom/8)*fraction)*cell;piece(c,animPiece,x,y,.94f);if(rookTo>=0){float rx=left+(rookFrom%8+(rookTo%8-rookFrom%8)*fraction)*cell,ry=top+rookTo/8*cell;piece(c,rookPiece,rx,ry,.94f);}}
  if(hints&&selected>=0&&game.b[selected]==game.turn*Chess.K){for(Chess.Move m:legal)if(m.from==selected&&Math.abs(m.to-m.from)==2){int r=m.to>m.from?selected+3:selected-4;float cx=left+(r%8+.5f)*cell,cy=top+(r/8+.5f)*cell;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(cell*.055f);p.setColor(0xffe8c76a);c.drawCircle(cx,cy,cell*.45f,p);p.setStyle(Paint.Style.FILL);}}if(dragging&&down>=0&&game.b[down]!=0)piece(c,game.b[down],dragX-cell/2,dragY-cell/2,1.1f);c.restore();}
 int at(float x,float y){int col=(int)Math.floor((x-left)/cell),r=(int)Math.floor((y-top)/cell);return Chess.inside(r,col)?r*8+col:-1;}
 @Override public boolean onTouchEvent(android.view.MotionEvent e){if(!enabled||game==null||cell<=0)return true;float x=e.getX(),y=e.getY();switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:down=at(x,y);downX=x;downY=y;dragging=false;if(down>=0&&game.b[down]*game.turn>0)getParent().requestDisallowInterceptTouchEvent(true);return true;case MotionEvent.ACTION_MOVE:if(down>=0&&game.b[down]*game.turn>0&&Math.hypot(x-downX,y-downY)>cell*.16){dragging=true;dragX=x;dragY=y;getParent().requestDisallowInterceptTouchEvent(true);invalidate();}return true;case MotionEvent.ACTION_UP:int to=at(x,y);boolean was=dragging;dragging=false;getParent().requestDisallowInterceptTouchEvent(false);if(listener!=null&&to>=0){if(was)listener.drag(down,to);else listener.touch(to);}down=-1;invalidate();performClick();return true;case MotionEvent.ACTION_CANCEL:dragging=false;down=-1;getParent().requestDisallowInterceptTouchEvent(false);invalidate();return true;}return true;}
 @Override public boolean performClick(){super.performClick();return true;}
}
