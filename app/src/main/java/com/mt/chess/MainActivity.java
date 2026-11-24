package com.mt.chess;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;
public class MainActivity extends Activity {
 static final int BG=0xff081e16,CARD=0xff102e23,EDGE=0xff315744,TEXT=0xfffff6df,MUTED=0xffadc3b5,ACCENT=0xffefd28a,INK=0xff173320;
 final String[] LEVELS={"سهل","متوسط","صعب","خبير"};
 Chess game=new Chess();int mode=0,level=1,human=1,selected=-1,style=0;boolean hints=true,setupOpen=false;String ending="";
 BoardView board;LinearLayout root,content,blackCard,whiteCard,historyRows;LinearLayout blackCapt,whiteCapt;TextView blackName,whiteName,blackTurn,whiteTurn,status,modeText,undo,resign,draw,historyLabel;ScrollView historyScroll;
 final ExecutorService cpu=Executors.newSingleThreadExecutor();Future<?> task;int revision=0;
 android.content.SharedPreferences prefs;
 int dp(float v){return (int)(v*getResources().getDisplayMetrics().density+.5f);}
 @Override public void onCreate(Bundle saved){super.onCreate(saved);prefs=getSharedPreferences("native_chess",MODE_PRIVATE);boolean has=load();
  Window w=getWindow();w.setStatusBarColor(BG);w.setNavigationBarColor(BG);w.getDecorView().setSystemUiVisibility(0);
  if(Build.VERSION.SDK_INT>=30)w.setDecorFitsSystemWindows(false);
  root=new LinearLayout(this);root.setOrientation(1);root.setBackgroundColor(BG);root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
  if(Build.VERSION.SDK_INT>=30){root.setOnApplyWindowInsetsListener((v,in)->{android.graphics.Insets bars=in.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);return WindowInsets.CONSUMED;});}else root.setFitsSystemWindows(true);
  setContentView(root);root.requestApplyInsets();build();refresh();if(!has)setup(false);else schedule();
 }
 GradientDrawable bg(int color,int radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));if(stroke!=0)d.setStroke(dp(1),stroke);return d;}
 LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(0);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
 LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 TextView text(String s,int sp,int c,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(sp);t.setTextColor(c);t.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);t.setIncludeFontPadding(false);if(bold)t.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));return t;}
 TextView button(String s,boolean primary,Runnable click){TextView t=text(s,14,primary?INK:TEXT,true);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(45));t.setPadding(dp(12),dp(9),dp(12),dp(9));t.setBackground(new android.graphics.drawable.RippleDrawable(ColorStateList.valueOf(0x337d9983),bg(primary?ACCENT:CARD,14,primary?0:EDGE),null));t.setClickable(true);t.setFocusable(true);t.setOnClickListener(v->click.run());return t;}
 void setIcon(TextView v,int res,int size,boolean above){android.graphics.drawable.Drawable d=getDrawable(res).mutate();d.setBounds(0,0,dp(size),dp(size));v.setCompoundDrawablePadding(dp(above?4:6));if(above)v.setCompoundDrawables(null,d,null,null);else v.setCompoundDrawables(d,null,null,null);}
 void add(LinearLayout l,View v,int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(margin);l.addView(v,p);}
 void weighted(LinearLayout l,View v,int gap){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.leftMargin=dp(gap);l.addView(v,p);}
 void build(){
  LinearLayout header=row();header.setPadding(dp(14),dp(5),dp(14),dp(8));
  TextView newGame=button("مباراة جديدة",false,()->setup(true));setIcon(newGame,R.drawable.ui_new,27,false);newGame.setTextColor(ACCENT);header.addView(newGame,new LinearLayout.LayoutParams(-2,-2));
  LinearLayout brand=col();TextView name=text("MT Chess",25,TEXT,true);brand.addView(name);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1);bp.rightMargin=dp(10);header.addView(brand,bp);
  ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.brand_logo);header.addView(logo,new LinearLayout.LayoutParams(dp(43),dp(43)));root.addView(header,new LinearLayout.LayoutParams(-1,-2));
  ScrollView sc=new ScrollView(this);sc.setFillViewport(false);sc.setClipToPadding(false);sc.setVerticalScrollBarEnabled(false);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
  android.widget.FrameLayout frame=new android.widget.FrameLayout(this);sc.addView(frame,new ScrollView.LayoutParams(-1,-2));content=col();content.setPadding(dp(8),dp(2),dp(8),dp(16));
  int max=Math.min(getResources().getDisplayMetrics().widthPixels,dp(580));android.widget.FrameLayout.LayoutParams cp=new android.widget.FrameLayout.LayoutParams(max,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);frame.addView(content,cp);
  LinearLayout meta=row();modeText=text("",13,MUTED,true);weighted(meta,modeText,8);add(content,meta,0);
  blackCard=player(false);add(content,blackCard,7);
  board=new BoardView(this);board.listener=new BoardView.Listener(){public void touch(int sq){tap(sq);}public void drag(int from,int to){if(!canPlay())return;selected=from;tap(to);}};board.setBackground(bg(0xff23372b,12,0));board.setElevation(dp(5));add(content,board,6);
  whiteCard=player(true);add(content,whiteCard,6);
  status=text("",15,ACCENT,true);status.setGravity(Gravity.CENTER);status.setMinHeight(dp(36));status.setPadding(dp(8),dp(5),dp(8),dp(5));status.setBackground(bg(0xff23382b,11,0xff365446));add(content,status,7);
  LinearLayout actions=row();TextView settingsButton=button("إعدادات",false,()->settings());resign=button("استسلام",false,()->confirmResign());draw=button("تعادل",false,()->confirmDraw());undo=button("تراجع",false,()->undo());setIcon(settingsButton,R.drawable.ui_settings,32,true);setIcon(resign,R.drawable.ui_resign,32,true);setIcon(draw,R.drawable.ui_draw,32,true);setIcon(undo,R.drawable.ui_undo,32,true);for(TextView v:new TextView[]{settingsButton,draw,resign,undo}){v.setTextSize(12);v.setPadding(dp(2),dp(7),dp(2),dp(7));v.setMinHeight(dp(66));weighted(actions,v,v==settingsButton?0:5);}add(content,actions,8);
 }
 LinearLayout player(boolean white){LinearLayout l=row();l.setPadding(dp(9),dp(5),dp(8),dp(5));l.setBackground(bg(CARD,13,EDGE));TextView dot=text("●",13,ACCENT,true);dot.setGravity(Gravity.CENTER);l.addView(dot,new LinearLayout.LayoutParams(dp(19),dp(36)));
  LinearLayout data=col();TextView title=text("",15,TEXT,true);LinearLayout capt=row();capt.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);capt.setMinimumHeight(dp(20));data.addView(title);add(data,capt,2);weighted(l,data,6);
  ImageView icon=new ImageView(this);try(java.io.InputStream in=getAssets().open("pieces/"+(white?"wK":"bK")+".png")){icon.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(in,null));}catch(Exception e){}
  icon.setBackground(bg(0xff3b5147,11,0));icon.setPadding(dp(3),dp(3),dp(3),dp(3));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(40),dp(40));ip.leftMargin=dp(10);l.addView(icon,ip);
  if(white){whiteName=title;whiteCapt=capt;whiteTurn=dot;}else{blackName=title;blackCapt=capt;blackTurn=dot;}return l;}
 boolean load(){if(!prefs.contains("moves"))return false;try{mode=prefs.getInt("mode",0);level=prefs.getInt("level",1);human=prefs.getInt("human",1);style=prefs.getInt("style",0);hints=prefs.getBoolean("hints",true);ending=prefs.getString("ending","");for(String s:prefs.getString("moves","").split(" "))if(!s.isEmpty()&&!game.playCode(s))throw new Exception();return true;}catch(Exception e){game=new Chess();ending="";return false;}}
 void save(){StringBuilder b=new StringBuilder();for(Chess.Move m:game.history)b.append(m.code()).append(' ');prefs.edit().putString("moves",b.toString()).putInt("mode",mode).putInt("level",level).putInt("human",human).putInt("style",style).putBoolean("hints",hints).putString("ending",ending).apply();}
 String result(){return ending.isEmpty()?game.result():ending;}
 boolean thinking(){return mode==0&&game.turn!=human&&result().isEmpty();}
 boolean canPlay(){return !setupOpen&&result().isEmpty()&&!thinking();}
 String color(int s){return s==1?"الأبيض":"الأسود";}
 String name(int side){return mode==1?(side==1?"اللاعب الأول":"اللاعب الثاني"):(side==human?"أنت":"الكمبيوتر");}
 void enable(TextView v,boolean on){v.setEnabled(on);v.setAlpha(on?1f:.4f);}
 void captured(LinearLayout row,int side){row.removeAllViews();ArrayList<Integer> pieces=new ArrayList<>();int own=0,other=0;for(int p:game.captures){if(p*side<0){pieces.add(p);own+=Chess.VAL[Math.abs(p)];}else if(p*side>0)other+=Chess.VAL[Math.abs(p)];}Collections.sort(pieces,(a,b)->Integer.compare(Math.abs(b),Math.abs(a)));if(own>other){TextView advantage=text("+"+((own-other+50)/100),12,ACCENT,true);row.addView(advantage);}for(int p:pieces){ImageView icon=new ImageView(this);icon.setImageBitmap(board.images[p+6]);row.addView(icon,new LinearLayout.LayoutParams(dp(17),dp(20)));}if(pieces.isEmpty()){TextView blank=text("—",12,MUTED,false);row.addView(blank);}}
 void refresh(){String end=result();boolean turnWhite=end.isEmpty()&&game.turn==1,turnBlack=end.isEmpty()&&game.turn==-1;
  blackName.setText(name(-1)+" · الأسود");whiteName.setText(name(1)+" · الأبيض");captured(blackCapt,-1);captured(whiteCapt,1);blackTurn.setVisibility(turnBlack?View.VISIBLE:View.INVISIBLE);whiteTurn.setVisibility(turnWhite?View.VISIBLE:View.INVISIBLE);
  blackCard.setBackground(bg(CARD,13,turnBlack?ACCENT:EDGE));whiteCard.setBackground(bg(CARD,13,turnWhite?ACCENT:EDGE));modeText.setText(mode==0?"ضد الكمبيوتر · "+LEVELS[level]:"لاعبان · على نفس الجهاز");setIcon(modeText,mode==0?R.drawable.ui_cpu:R.drawable.ui_friend,24,false);
  String msg=end.equals("white")?"كش مات — فاز الأبيض":end.equals("black")?"كش مات — فاز الأسود":end.equals("stalemate")?"تعادل — لا توجد نقلات":end.equals("draw")?"انتهت المباراة بالتعادل":end.equals("resignWhite")?"استسلام — فاز الأسود":end.equals("resignBlack")?"استسلام — فاز الأبيض":end.equals("agreement")?"تعادل بالاتفاق":thinking()?"الكمبيوتر يفكر…":(game.check(game.turn)?"كش ملك · ":"")+"دور "+color(game.turn);
  if(end.isEmpty()&&selected>=0&&game.b[selected]==game.turn*Chess.K){for(Chess.Move m:game.legal())if(m.from==selected&&Math.abs(m.to-m.from)==2){msg="التبييت متاح · اختر القلعة أو مربع الملك";break;}}status.setText(msg);board.update(game,selected,hints,canPlay(),style);enable(undo,!game.history.isEmpty()&&!(mode==0&&human==-1&&game.history.size()==1));enable(resign,end.isEmpty()&&!thinking());enable(draw,end.isEmpty()&&mode==1);
 }
 void stop(){revision++;if(task!=null){task.cancel(true);task=null;}}
 void schedule(){if(!thinking()||setupOpen||task!=null)return;final int rev=++revision;Chess snapshot=new Chess(game);int difficulty=level;task=cpu.submit(()->{Chess.Move m=snapshot.choose(difficulty);runOnUiThread(()->{if(rev!=revision||isFinishing())return;task=null;if(thinking()&&m!=null)play(m);});});}
 void play(Chess.Move m){if(game.play(m)){selected=-1;save();refresh();board.animateMove(m.from,m.to,game.b[m.to]);schedule();}}
 void tap(int sq){if(!canPlay())return;if(selected>=0){sq=game.castleTarget(selected,sq);ArrayList<Chess.Move> matches=new ArrayList<>();for(Chess.Move m:game.legal())if(m.from==selected&&m.to==sq)matches.add(m);if(!matches.isEmpty()){if(matches.size()>1){promote(matches);return;}play(matches.get(0));return;}}selected=game.b[sq]*game.turn>0?(sq==selected?-1:sq):-1;refresh();}
 void promote(ArrayList<Chess.Move> m){LinearLayout tiles=row();tiles.setPadding(dp(12),dp(12),dp(12),dp(8));AlertDialog d=dialog("ترقية البيدق").setView(tiles).setNegativeButton("إلغاء",null).create();String[] names={"وزير","قلعة","فيل","حصان"};int[] types={5,4,3,2};for(int i=0;i<4;i++){final int p=types[i];LinearLayout tile=col();tile.setGravity(Gravity.CENTER);tile.setBackground(bg(CARD,12,EDGE));ImageView im=new ImageView(this);im.setImageBitmap(board.images[game.turn*p+6]);tile.addView(im,new LinearLayout.LayoutParams(dp(52),dp(52)));TextView label=text(names[i],13,TEXT,true);label.setGravity(Gravity.CENTER);add(tile,label,4);tile.setPadding(dp(2),dp(8),dp(2),dp(8));tile.setOnClickListener(v->{d.dismiss();for(Chess.Move x:m)if(x.promo==p){play(x);break;}});weighted(tiles,tile,i==0?0:5);}d.show();}
 void undo(){stop();board.clearPieceAnimation();if(game.history.isEmpty())return;game.undo();if(mode==0&&game.turn!=human&&!game.history.isEmpty())game.undo();ending="";selected=-1;save();refresh();schedule();}
 AlertDialog.Builder dialog(String title){return new AlertDialog.Builder(this).setTitle(title);}
 void confirmResign(){dialog("استسلام").setMessage("هل تريد إنهاء المباراة والاستسلام؟").setNegativeButton("إلغاء",null).setPositiveButton("استسلام",(d,w)->{ending=game.turn==1?"resignWhite":"resignBlack";stop();save();refresh();}).show();}
 void confirmDraw(){dialog("تعادل").setMessage("هل وافق اللاعبان على التعادل؟").setNegativeButton("إلغاء",null).setPositiveButton("تعادل",(d,w)->{ending="agreement";stop();save();refresh();}).show();}
 Spinner spinner(String[] values,int pos){Spinner s=new Spinner(this);ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,values);s.setAdapter(a);s.setSelection(pos);return s;}
 void setup(boolean ask){if(setupOpen)return;setupOpen=true;stop();LinearLayout l=col();l.setPadding(dp(24),dp(8),dp(24),dp(12));l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  LinearLayout choices=row();TextView computer=button("الكمبيوتر",false,()->{}),friend=button("مع صديق",false,()->{});setIcon(computer,R.drawable.ui_cpu,40,true);setIcon(friend,R.drawable.ui_friend,40,true);weighted(choices,computer,0);weighted(choices,friend,6);add(l,choices,0);final int[] chosen={mode};
  LinearLayout options=col();add(options,text("مستوى الكمبيوتر",14,MUTED,true),12);Spinner lev=spinner(LEVELS,level);add(options,lev,5);add(options,text("لون قطعك",14,MUTED,true),12);Spinner col=spinner(new String[]{"الأبيض","الأسود","عشوائي"},human==1?0:1);add(options,col,5);add(l,options,0);
  Runnable updateChoice=()->{int pos=chosen[0];options.setVisibility(pos==0?View.VISIBLE:View.GONE);computer.setTextColor(pos==0?ACCENT:TEXT);friend.setTextColor(pos==1?ACCENT:TEXT);computer.setBackground(new android.graphics.drawable.RippleDrawable(ColorStateList.valueOf(0x337d9983),bg(CARD,14,pos==0?ACCENT:EDGE),null));friend.setBackground(new android.graphics.drawable.RippleDrawable(ColorStateList.valueOf(0x337d9983),bg(CARD,14,pos==1?ACCENT:EDGE),null));};
  computer.setOnClickListener(v->{chosen[0]=0;updateChoice.run();});friend.setOnClickListener(v->{chosen[0]=1;updateChoice.run();});updateChoice.run();AlertDialog d=dialog("مباراة جديدة").setView(l).setNegativeButton("إلغاء",null).setPositiveButton("ابدأ اللعب",(x,y)->{mode=chosen[0];level=lev.getSelectedItemPosition();int c=col.getSelectedItemPosition();human=c==0?1:c==1?-1:new Random().nextBoolean()?1:-1;game=new Chess();ending="";selected=-1;board.clearPieceAnimation();save();}).create();d.setOnDismissListener(x->{setupOpen=false;refresh();schedule();});d.show();}
 void settings(){LinearLayout l=col();l.setPadding(dp(24),dp(8),dp(24),dp(12));l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);Switch show=new Switch(this);show.setText("إظهار النقلات القانونية");show.setTextColor(TEXT);show.setChecked(hints);setIcon(show,R.drawable.ui_hints,27,false);add(l,show,5);add(l,text("لون الرقعة",14,MUTED,true),16);Spinner s=spinner(new String[]{"أخضر كلاسيكي","خشبي دافئ","أزرق هادئ","رمادي عصري"},style);add(l,s,5);TextView rules=button("كيفية التبييت",false,()->dialog("التبييت").setMessage("اختر الملك ثم مربع التبييت (مربعان باتجاه القلعة) أو اضغط على القلعة. يجب ألا يكون الملك أو القلعة قد تحركا من قبل، وأن تكون المربعات بينهما فارغة، وألا يكون الملك في كش أو يعبر أو يصل إلى مربع مهدد.").setPositiveButton("فهمت",null).show());setIcon(rules,R.drawable.ui_castle,28,false);add(l,rules,12);dialog("الإعدادات").setView(l).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{hints=show.isChecked();style=s.getSelectedItemPosition();save();refresh();}).show();}
 @Override protected void onDestroy(){stop();cpu.shutdownNow();super.onDestroy();}
}
