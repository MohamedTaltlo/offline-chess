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
 static final int[][] COLORS={{0xffeee4cc,0xff426b58},{0xffeddbbb,0xff9b6745},{0xffdce5ea,0xff506f8b},{0xffe2e2dc,0xff696e74}};
 static final int[][] LIGHTS={{0xfffff5dc,0xff587e68},{0xfff8e8cc,0xffac7956},{0xffedf3f6,0xff6688a1},{0xfff1f1eb,0xff85898c}};
 static final int[][] DARKS={{0xffd5c5a4,0xff315341},{0xffd6bd94,0xff805037},{0xffc5d3db,0xff405a72},{0xffccccc4,0xff51565c}};
 Bitmap boardTexture;int textureTheme=-1,textureWidth=-1;
 void paintTexture(int width){
  if(boardTexture!=null&&textureWidth==width&&textureTheme==theme)return;
  if(boardTexture!=null)boardTexture.recycle();
  textureWidth=width;textureTheme=theme;boardTexture=Bitmap.createBitmap(width,width,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(boardTexture);Paint q=new Paint(Paint.ANTI_ALIAS_FLAG);
  float inset=width*.037f,unit=(width-2*inset)/8f;RectF outer=new RectF(0,0,width,width);
  q.setShader(new LinearGradient(0,0,width,width,new int[]{0xff3b5547,0xff152b22,0xff091b14},null,Shader.TileMode.CLAMP));c.drawRoundRect(outer,width*.037f,width*.037f,q);q.setShader(null);
  q.setStyle(Paint.Style.STROKE);q.setStrokeWidth(Math.max(1,width*.0032f));q.setColor(0xffb59b61);outer.inset(width*.005f,width*.005f);c.drawRoundRect(outer,width*.032f,width*.032f,q);
  q.setColor(0xff8a784d);q.setStrokeWidth(Math.max(1,width*.002f));c.drawRect(inset-1,inset-1,width-inset+1,width-inset+1,q);q.setStyle(Paint.Style.FILL);
  for(int row=0;row<8;row++)for(int col=0;col<8;col++){int parity=(row+col)&1;float x=inset+col*unit,y=inset+row*unit;
   q.setShader(new LinearGradient(x,y,x+unit,y+unit,new int[]{LIGHTS[theme][parity],COLORS[theme][parity],DARKS[theme][parity]},new float[]{0,.46f,1},Shader.TileMode.CLAMP));c.drawRect(x,y,x+unit,y+unit,q);q.setShader(null);
   q.setColor(parity==0?0x16ffffff:0x13ffffff);q.setStrokeWidth(1);c.drawLine(x,y,x+unit,y,q);c.drawLine(x,y,x,y+unit,q);
   q.setColor(0x13000000);c.drawLine(x,y+unit-1,x+unit,y+unit-1,q);c.drawLine(x+unit-1,y,x+unit-1,y+unit,q);
  }
  q.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));q.setColor(0xffe4cf9b);q.setTextSize(width*.021f);q.setTextAlign(Paint.Align.CENTER);
  for(int i=0;i<8;i++){float mid=inset+(i+.5f)*unit;c.drawText(""+(char)('a'+i),mid,width-inset*.24f,q);c.drawText(""+(8-i),inset*.49f,mid-(q.ascent()+q.descent())/2,q);}
 }

 BoardView(Context ctx){super(ctx);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setFocusable(true);setContentDescription("رقعة الشطرنج، الأبيض بالأسفل والأسود بالأعلى");
  for(int s:new int[]{1,-1})for(int k=1;k<=6;k++){String file=(s==1?"w":"b")+" PNBRQK".charAt(k)+".png";try(java.io.InputStream in=ctx.getAssets().open("pieces/"+file)){images[s*k+6]=BitmapFactory.decodeStream(in);}catch(Exception ex){throw new IllegalStateException("Missing chess art "+file,ex);}}}
 @Override protected void onMeasure(int w,int h){int width=MeasureSpec.getSize(w);setMeasuredDimension(width,width);}
 void update(Chess g,int sel,boolean show,boolean can,int style){game=g;selected=sel;hints=show;enabled=can;theme=Math.max(0,Math.min(COLORS.length-1,style));legal=sel<0?new ArrayList<>():g.legal();int n=g.history.size();lastFrom=n==0?-1:g.history.get(n-1).from;lastTo=n==0?-1:g.history.get(n-1).to;check=-1;if(g.check(g.turn))for(int i=0;i<64;i++)if(g.b[i]==g.turn*Chess.K)check=i;invalidate();}
 void animateMove(int from,int to,int piece){if(animator!=null)animator.cancel();animFrom=from;animTo=to;animPiece=piece;rookFrom=rookTo=-1;rookPiece=0;if(Math.abs(piece)==Chess.K&&Math.abs(to-from)==2){rookFrom=to>from?from+3:from-4;rookTo=to>from?from+1:from-1;rookPiece=piece>0?Chess.R:-Chess.R;}fraction=0;animator=ValueAnimator.ofFloat(0,1);animator.setDuration(rookTo>=0?260:175);animator.setInterpolator(new android.view.animation.DecelerateInterpolator());animator.addUpdateListener(a->{fraction=(float)a.getAnimatedValue();invalidate();});animator.start();}
 void clearPieceAnimation(){if(animator!=null)animator.cancel();fraction=1;invalidate();}
 void fill(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(l,t,r,b,p);}
 void piece(Canvas c,int val,float x,float y,float scale){Bitmap b=images[val+6];if(b==null)return;float inset=cell*(1-scale)/2;dst.set(x+inset,y+inset,x+cell-inset,y+cell-inset);p.setColor(Color.WHITE);p.setAlpha(255);p.setFilterBitmap(true);c.drawBitmap(b,null,dst,p);}
 @Override protected void onDraw(Canvas c){super.onDraw(c);if(game==null)return;int width=Math.min(getWidth(),getHeight());if(width<=0)return;paintTexture(width);float outerX=(getWidth()-width)/2f,outerY=(getHeight()-width)/2f;p.setColor(Color.WHITE);p.setFilterBitmap(true);c.drawBitmap(boardTexture,outerX,outerY,p);float inset=width*.037f;size=width-2*inset;cell=size/8f;left=outerX+inset;top=outerY+inset;c.save();c.clipRect(left,top,left+size,top+size);
  for(int i=0;i<64;i++){int r=i/8,col=i%8;float x=left+col*cell,y=top+r*cell;
   if(i==lastFrom||i==lastTo)fill(c,0x99d0a849,x,y,x+cell,y+cell);
   if(i==check){p.setShader(new RadialGradient(x+cell/2,y+cell/2,cell*.68f,new int[]{0xffed5b55,0x00ed5b55},null,Shader.TileMode.CLAMP));c.drawRect(x,y,x+cell,y+cell,p);p.setShader(null);}
   if(i==selected){fill(c,0x99e8bd54,x,y,x+cell,y+cell);p.setColor(0xffffdf85);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(cell*.045f);c.drawRect(x+2,y+2,x+cell-2,y+cell-2,p);p.setStyle(Paint.Style.FILL);}
   if(game.b[i]!=0&&!(dragging&&i==down)&&!(fraction<1&&(i==animTo||i==rookTo)))piece(c,game.b[i],x,y,1.0f);
   if(hints)for(Chess.Move m:legal)if(m.from==selected&&m.to==i){p.setColor(0x99533215);if(game.b[i]==0){c.drawCircle(x+cell/2,y+cell/2,cell*.13f,p);p.setColor(0xffedce83);c.drawCircle(x+cell/2,y+cell/2,cell*.08f,p);}else{p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(cell*.07f);c.drawCircle(x+cell/2,y+cell/2,cell*.435f,p);p.setStyle(Paint.Style.FILL);}break;}

  }
  if(fraction<1&&animPiece!=0){float x=left+(animFrom%8+(animTo%8-animFrom%8)*fraction)*cell,y=top+(animFrom/8+(animTo/8-animFrom/8)*fraction)*cell;piece(c,animPiece,x,y,1.0f);if(rookTo>=0){float rx=left+(rookFrom%8+(rookTo%8-rookFrom%8)*fraction)*cell,ry=top+rookTo/8*cell;piece(c,rookPiece,rx,ry,1.0f);}}
  if(hints&&selected>=0&&game.b[selected]==game.turn*Chess.K){for(Chess.Move m:legal)if(m.from==selected&&Math.abs(m.to-m.from)==2){int r=m.to>m.from?selected+3:selected-4;float cx=left+(r%8+.5f)*cell,cy=top+(r/8+.5f)*cell;p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(cell*.055f);p.setColor(0xffe8c76a);c.drawCircle(cx,cy,cell*.45f,p);p.setStyle(Paint.Style.FILL);}}if(dragging&&down>=0&&game.b[down]!=0)piece(c,game.b[down],dragX-cell/2,dragY-cell/2,1.1f);c.restore();}
 int at(float x,float y){int col=(int)Math.floor((x-left)/cell),r=(int)Math.floor((y-top)/cell);return Chess.inside(r,col)?r*8+col:-1;}
 @Override public boolean onTouchEvent(android.view.MotionEvent e){if(!enabled||game==null||cell<=0)return true;float x=e.getX(),y=e.getY();switch(e.getActionMasked()){case MotionEvent.ACTION_DOWN:down=at(x,y);downX=x;downY=y;dragging=false;if(down>=0&&game.b[down]*game.turn>0)getParent().requestDisallowInterceptTouchEvent(true);return true;case MotionEvent.ACTION_MOVE:if(down>=0&&game.b[down]*game.turn>0&&Math.hypot(x-downX,y-downY)>cell*.16){dragging=true;dragX=x;dragY=y;getParent().requestDisallowInterceptTouchEvent(true);invalidate();}return true;case MotionEvent.ACTION_UP:int to=at(x,y);boolean was=dragging;dragging=false;getParent().requestDisallowInterceptTouchEvent(false);if(listener!=null&&to>=0){if(was)listener.drag(down,to);else listener.touch(to);}down=-1;invalidate();performClick();return true;case MotionEvent.ACTION_CANCEL:dragging=false;down=-1;getParent().requestDisallowInterceptTouchEvent(false);invalidate();return true;}return true;}
 @Override public boolean performClick(){super.performClick();return true;}
}
