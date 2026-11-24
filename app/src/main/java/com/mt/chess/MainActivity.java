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
 static final int BG=0xff111b1a,CARD=0xff1d2b29,EDGE=0xff31443e,TEXT=0xfff1f4ed,MUTED=0xffa6bbb1,ACCENT=0xffa8ddb4,INK=0xff15382b;
 final String[] LEVELS={"سهل","متوسط","صعب","خبير"};
 Chess game=new Chess();int mode=0,level=1,human=1,selected=-1,style=0;boolean hints=true,setupOpen=false;String ending="";
 BoardView board;LinearLayout root,content,blackCard,whiteCard,historyRows;TextView blackName,whiteName,blackCapt,whiteCapt,blackTurn,whiteTurn,status,modeText,undo,resign,draw,historyLabel;ScrollView historyScroll;
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
 TextView button(String s,boolean primary,Runnable click){TextView t=text(s,14,primary?INK:TEXT,true);t.setGravity(Gravity.CENTER);t.setMinHeight(dp(45));t.setPadding(dp(12),dp(9),dp(12),dp(9));t.setBackground(bg(primary?ACCENT:CARD,13,primary?0:EDGE));t.setClickable(true);t.setFocusable(true);t.setOnClickListener(v->click.run());return t;}
 void add(LinearLayout l,View v,int margin){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(margin);l.addView(v,p);}
 void weighted(LinearLayout l,View v,int gap){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,1);p.leftMargin=dp(gap);l.addView(v,p);}
 void build(){
  LinearLayout header=row();header.setPadding(dp(16),dp(10),dp(16),dp(10));
  TextView newGame=button("＋ مباراة جديدة",true,()->setup(true));header.addView(newGame,new LinearLayout.LayoutParams(-2,-2));
  LinearLayout brand=col();TextView name=text("MT Chess",25,TEXT,true);TextView sub=text("شطرنج • بلا إنترنت",12,MUTED,false);brand.addView(name);add(brand,sub,3);LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(0,-2,1);bp.rightMargin=dp(10);header.addView(brand,bp);
  ImageView logo=new ImageView(this);logo.setImageResource(com.mt.chess.R.drawable.chess_icon);header.addView(logo,new LinearLayout.LayoutParams(dp(47),dp(47)));root.addView(header,new LinearLayout.LayoutParams(-1,-2));
  ScrollView sc=new ScrollView(this);sc.setFillViewport(false);sc.setClipToPadding(false);sc.setVerticalScrollBarEnabled(false);root.addView(sc,new LinearLayout.LayoutParams(-1,0,1));
  android.widget.FrameLayout frame=new android.widget.FrameLayout(this);sc.addView(frame,new ScrollView.LayoutParams(-1,-2));content=col();content.setPadding(dp(14),dp(2),dp(14),dp(20));
  int max=Math.min(getResources().getDisplayMetrics().widthPixels,dp(620));android.widget.FrameLayout.LayoutParams cp=new android.widget.FrameLayout.LayoutParams(max,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);frame.addView(content,cp);
  LinearLayout meta=row();TextView offline=text("●  OFFLINE",11,ACCENT,true);offline.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);meta.addView(offline);modeText=text("",13,MUTED,true);weighted(meta,modeText,8);add(content,meta,0);
  blackCard=player(false);add(content,blackCard,12);
  board=new BoardView(this);board.listener=new BoardView.Listener(){public void touch(int sq){tap(sq);}public void drag(int from,int to){if(!canPlay())return;selected=from;tap(to);}};add(content,board,9);
  whiteCard=player(true);add(content,whiteCard,9);
  status=text("",15,ACCENT,true);status.setGravity(Gravity.CENTER);status.setMinHeight(dp(42));status.setBackground(bg(0xff203a30,12,0xff365446));add(content,status,10);
  LinearLayout actions=row();resign=button("⚑  استسلام",false,()->confirmResign());undo=button("↶  تراجع",false,()->undo());weighted(actions,resign,0);weighted(actions,undo,8);add(content,actions,9);
  LinearLayout settings=row();draw=button("تعادل",false,()->confirmDraw());TextView settingsButton=button("⚙  الإعدادات",false,()->settings());weighted(settings,draw,0);weighted(settings,settingsButton,8);add(content,settings,8);
  LinearLayout hist=col();hist.setPadding(dp(12),dp(12),dp(12),dp(12));hist.setBackground(bg(CARD,14,EDGE));
  historyLabel=text("سجل النقلات",16,TEXT,true);add(hist,historyLabel,0);historyScroll=new ScrollView(this);historyScroll.setNestedScrollingEnabled(true);historyRows=col();historyScroll.addView(historyRows);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,dp(132));hp.topMargin=dp(8);hist.addView(historyScroll,hp);add(content,hist,12);
  TextView footer=text("MT CHESS  •  2.0.0",11,MUTED,false);footer.setGravity(Gravity.CENTER);add(content,footer,16);
 }
 LinearLayout player(boolean white){LinearLayout l=row();l.setPadding(dp(11),dp(8),dp(10),dp(8));l.setBackground(bg(CARD,13,EDGE));TextView dot=text("●",13,ACCENT,true);dot.setGravity(Gravity.CENTER);l.addView(dot,new LinearLayout.LayoutParams(dp(19),dp(36)));
  LinearLayout data=col();TextView title=text("",15,TEXT,true);TextView capt=text("",14,MUTED,false);capt.setSingleLine(true);capt.setEllipsize(android.text.TextUtils.TruncateAt.END);data.addView(title);add(data,capt,4);weighted(l,data,6);
  ImageView icon=new ImageView(this);try(java.io.InputStream in=getAssets().open("pieces/"+(white?"wK":"bK")+".png")){icon.setImageDrawable(android.graphics.drawable.Drawable.createFromStream(in,null));}catch(Exception e){}
  icon.setBackground(bg(0xff3b5147,11,0));icon.setPadding(dp(3),dp(3),dp(3),dp(3));LinearLayout.LayoutParams ip=new LinearLayout.LayoutParams(dp(43),dp(43));ip.leftMargin=dp(10);l.addView(icon,ip);
  if(white){whiteName=title;whiteCapt=capt;whiteTurn=dot;}else{blackName=title;blackCapt=capt;blackTurn=dot;}return l;}
 boolean load(){if(!prefs.contains("moves"))return false;try{mode=prefs.getInt("mode",0);level=prefs.getInt("level",1);human=prefs.getInt("human",1);style=prefs.getInt("style",0);hints=prefs.getBoolean("hints",true);ending=prefs.getString("ending","");for(String s:prefs.getString("moves","").split(" "))if(!s.isEmpty()&&!game.playCode(s))throw new Exception();return true;}catch(Exception e){game=new Chess();ending="";return false;}}
 void save(){StringBuilder b=new StringBuilder();for(Chess.Move m:game.history)b.append(m.code()).append(' ');prefs.edit().putString("moves",b.toString()).putInt("mode",mode).putInt("level",level).putInt("human",human).putInt("style",style).putBoolean("hints",hints).putString("ending",ending).apply();}
 String result(){return ending.isEmpty()?game.result():ending;}
 boolean thinking(){return mode==0&&game.turn!=human&&result().isEmpty();}
 boolean canPlay(){return !setupOpen&&result().isEmpty()&&!thinking();}
 String color(int s){return s==1?"الأبيض":"الأسود";}
 String name(int side){return mode==1?(side==1?"اللاعب الأول":"اللاعب الثاني"):(side==human?"أنت":"الكمبيوتر");}
 void enable(TextView v,boolean on){v.setEnabled(on);v.setAlpha(on?1f:.4f);}
 String captured(int side){StringBuilder s=new StringBuilder();String glyph=" ♟♞♝♜♛♚";for(int p:game.captures)if(p*side<0)s.append(glyph.charAt(Math.abs(p))).append(' ');return s.length()==0?"—":s.toString();}
 void refresh(){String end=result();boolean turnWhite=end.isEmpty()&&game.turn==1,turnBlack=end.isEmpty()&&game.turn==-1;
  blackName.setText(name(-1)+" · الأسود");whiteName.setText(name(1)+" · الأبيض");blackCapt.setText(captured(-1));whiteCapt.setText(captured(1));blackTurn.setVisibility(turnBlack?View.VISIBLE:View.INVISIBLE);whiteTurn.setVisibility(turnWhite?View.VISIBLE:View.INVISIBLE);
  blackCard.setBackground(bg(CARD,13,turnBlack?ACCENT:EDGE));whiteCard.setBackground(bg(CARD,13,turnWhite?ACCENT:EDGE));modeText.setText(mode==0?"ضد الكمبيوتر · "+LEVELS[level]:"لاعبان · على نفس الجهاز");
  String msg=end.equals("white")?"كش مات — فاز الأبيض":end.equals("black")?"كش مات — فاز الأسود":end.equals("stalemate")?"تعادل — لا توجد نقلات":end.equals("draw")?"انتهت المباراة بالتعادل":end.equals("resignWhite")?"استسلام — فاز الأسود":end.equals("resignBlack")?"استسلام — فاز الأبيض":end.equals("agreement")?"تعادل بالاتفاق":thinking()?"الكمبيوتر يفكر…":(game.check(game.turn)?"كش ملك · ":"")+"دور "+color(game.turn);
  status.setText(msg);board.update(game,selected,hints,canPlay(),style);enable(undo,!game.history.isEmpty()&&!(mode==0&&human==-1&&game.history.size()==1));enable(resign,end.isEmpty()&&!thinking());enable(draw,end.isEmpty()&&mode==1);
  historyRows.removeAllViews();historyLabel.setText("سجل النقلات  ·  "+game.history.size());for(int i=0;i<game.notation.size();i+=2){LinearLayout r=row();TextView n=text((i/2+1)+".",12,MUTED,false);n.setGravity(Gravity.CENTER);r.addView(n,new LinearLayout.LayoutParams(dp(34),dp(30)));for(int j=0;j<2;j++){TextView m=text(i+j<game.notation.size()?game.notation.get(i+j):"—",14,TEXT,true);m.setGravity(Gravity.CENTER);m.setBackground(bg(0xff2a3a35,6,0));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(30),1);lp.leftMargin=dp(5);r.addView(m,lp);}add(historyRows,r,4);}historyScroll.post(()->historyScroll.fullScroll(View.FOCUS_DOWN));
 }
 void stop(){revision++;if(task!=null){task.cancel(true);task=null;}}
 void schedule(){if(!thinking()||setupOpen||task!=null)return;final int rev=++revision;Chess snapshot=new Chess(game);int difficulty=level;task=cpu.submit(()->{Chess.Move m=snapshot.choose(difficulty);runOnUiThread(()->{if(rev!=revision||isFinishing())return;task=null;if(thinking()&&m!=null)play(m);});});}
 void play(Chess.Move m){if(game.play(m)){selected=-1;save();refresh();board.animateMove(m.from,m.to,game.b[m.to]);schedule();}}
 void tap(int sq){if(!canPlay())return;if(selected>=0){ArrayList<Chess.Move> matches=new ArrayList<>();for(Chess.Move m:game.legal())if(m.from==selected&&m.to==sq)matches.add(m);if(!matches.isEmpty()){if(matches.size()>1){promote(matches);return;}play(matches.get(0));return;}}selected=game.b[sq]*game.turn>0?(sq==selected?-1:sq):-1;refresh();}
 void promote(ArrayList<Chess.Move> m){String[] names={"وزير","قلعة","فيل","حصان"};dialog("ترقية البيدق").setItems(names,(d,i)->{int p=new int[]{5,4,3,2}[i];for(Chess.Move x:m)if(x.promo==p){play(x);break;}}).setNegativeButton("إلغاء",null).show();}
 void undo(){stop();board.clearPieceAnimation();if(game.history.isEmpty())return;game.undo();if(mode==0&&game.turn!=human&&!game.history.isEmpty())game.undo();ending="";selected=-1;save();refresh();schedule();}
 AlertDialog.Builder dialog(String title){return new AlertDialog.Builder(this).setTitle(title);}
 void confirmResign(){dialog("استسلام").setMessage("هل تريد إنهاء المباراة والاستسلام؟").setNegativeButton("إلغاء",null).setPositiveButton("استسلام",(d,w)->{ending=game.turn==1?"resignWhite":"resignBlack";stop();save();refresh();}).show();}
 void confirmDraw(){dialog("تعادل").setMessage("هل وافق اللاعبان على التعادل؟").setNegativeButton("إلغاء",null).setPositiveButton("تعادل",(d,w)->{ending="agreement";stop();save();refresh();}).show();}
 Spinner spinner(String[] values,int pos){Spinner s=new Spinner(this);ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,values);s.setAdapter(a);s.setSelection(pos);return s;}
 void setup(boolean ask){if(setupOpen)return;setupOpen=true;stop();LinearLayout l=col();l.setPadding(dp(24),dp(8),dp(24),dp(12));l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
  add(l,text("طريقة اللعب",14,MUTED,true),0);Spinner m=spinner(new String[]{"ضد الكمبيوتر","لاعبان على نفس الشاشة"},mode);add(l,m,5);
  LinearLayout options=col();add(options,text("مستوى الكمبيوتر",14,MUTED,true),12);Spinner lev=spinner(LEVELS,level);add(options,lev,5);add(options,text("لون قطعك",14,MUTED,true),12);Spinner col=spinner(new String[]{"الأبيض","الأسود","عشوائي"},human==1?0:1);add(options,col,5);add(l,options,0);
  m.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> a){}public void onItemSelected(android.widget.AdapterView<?> a,View v,int pos,long id){options.setVisibility(pos==0?View.VISIBLE:View.GONE);}});
  AlertDialog d=dialog("مباراة جديدة").setView(l).setNegativeButton("إلغاء",null).setPositiveButton("ابدأ اللعب",(x,y)->{mode=m.getSelectedItemPosition();level=lev.getSelectedItemPosition();int c=col.getSelectedItemPosition();human=c==0?1:c==1?-1:new Random().nextBoolean()?1:-1;game=new Chess();ending="";selected=-1;board.clearPieceAnimation();save();}).create();d.setOnDismissListener(x->{setupOpen=false;refresh();schedule();});d.show();}
 void settings(){LinearLayout l=col();l.setPadding(dp(24),dp(8),dp(24),dp(12));l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);Switch show=new Switch(this);show.setText("إظهار النقلات القانونية");show.setTextColor(TEXT);show.setChecked(hints);add(l,show,5);add(l,text("لون الرقعة",14,MUTED,true),16);Spinner s=spinner(new String[]{"أخضر كلاسيكي","خشبي دافئ","أزرق هادئ"},style);add(l,s,5);add(l,text("القطع: Chessnut · Alexis Luengas / Apache 2.0",11,MUTED,false),18);dialog("الإعدادات").setView(l).setNegativeButton("إلغاء",null).setPositiveButton("حفظ",(d,w)->{hints=show.isChecked();style=s.getSelectedItemPosition();save();refresh();}).show();}
 @Override protected void onDestroy(){stop();cpu.shutdownNow();super.onDestroy();}
}
