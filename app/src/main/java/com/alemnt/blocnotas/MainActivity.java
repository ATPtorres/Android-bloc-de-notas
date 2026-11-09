package com.alemnt.blocnotas;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    static class Note {
        String id, title, body, updated;
        Note(String i, String t, String b, String u) { id=i; title=t; body=b; updated=u; }
    }
    ArrayList<Note> notes = new ArrayList<>();
    Note current;
    LinearLayout base, top, editorPanel;
    EditText titleInput, bodyInput, searchInput;
    TextView status;
    ListView list;
    ArrayAdapter<String> adapter;
    ArrayList<Note> shown = new ArrayList<>();
    boolean dark = false, loading = false;
    final int CREATE_FILE=41, OPEN_FILE=42;
    String exportBody="", exportName="nota.txt";

    String now() { return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date()); }
    String makeId() { return UUID.randomUUID().toString(); }

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        loadNotes();
        showHome();
    }

    int bg() { return dark ? 0xFF15171B : 0xFFF5F6F8; }
    int panel() { return dark ? 0xFF22262D : 0xFFFFFFFF; }
    int fg() { return dark ? 0xFFF1F3F5 : 0xFF20242A; }
    int muted() { return dark ? 0xFFADB5C0 : 0xFF697386; }
    int accent() { return 0xFF4169E1; }

    TextView text(String s, int size, boolean bold) {
        TextView v=new TextView(this); v.setText(s); v.setTextSize(size); v.setTextColor(fg());
        if(bold) v.setTypeface(null, Typeface.BOLD);
        v.setGravity(Gravity.CENTER_VERTICAL); return v;
    }
    GradientButton button(String label, Runnable action) {
        GradientButton b=new GradientButton(this); b.setText(label); b.setTextSize(14);
        b.setTextColor(0xFFFFFFFF); b.setAllCaps(false); b.setPadding(14,10,14,10);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent()));
        b.setOnClickListener(v -> action.run()); return b;
    }
    EditText edit(String hint, boolean multi) {
        EditText e=new EditText(this); e.setHint(hint); e.setTextColor(fg()); e.setHintTextColor(muted());
        e.setTextSize(16); e.setPadding(14,12,14,12); e.setBackgroundColor(panel());
        if(multi) { e.setGravity(Gravity.TOP|Gravity.START); e.setMinLines(12); e.setInputType( android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE | android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES); }
        else e.setSingleLine(true);
        return e;
    }
    void initBase() {
        base=new LinearLayout(this); base.setOrientation(LinearLayout.VERTICAL); base.setPadding(16,12,16,12); base.setBackgroundColor(bg());
        setContentView(base);
    }
    void addGap(LinearLayout p, int h) { View v=new View(this); p.addView(v,new LinearLayout.LayoutParams(1,h)); }
    void showHome() {
        current=null; initBase();
        LinearLayout header=new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL);
        TextView heading=text("Mis notas",25,true); header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
        header.addView(button("＋ Nueva", () -> newNote()));
        header.addView(button("⋮", () -> menu()));
        base.addView(header); addGap(base,12);
        searchInput=edit("Buscar notas…",false); base.addView(searchInput);
        list=new ListView(this); list.setDividerHeight(8); list.setBackgroundColor(bg());
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,0,1); lp.topMargin=8; base.addView(list,lp);
        status=text(notes.size()+" notas guardadas · Solo en este teléfono",12,false); status.setTextColor(muted()); base.addView(status);
        searchInput.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){ refreshList(s.toString()); }
            public void afterTextChanged(Editable e){}
        });
        refreshList("");
    }
    void refreshList(String query) {
        shown.clear(); ArrayList<String> labels=new ArrayList<>();
        for(Note n:notes) if(n.title.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)) || n.body.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))) {
            shown.add(n); String preview=n.body.replace('\n',' ').trim(); if(preview.length()>90) preview=preview.substring(0,90)+"…";
            labels.add((n.title.trim().isEmpty()?"Sin título":n.title)+"\n"+preview+"\n"+n.updated);
        }
        adapter=new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2, android.R.id.text1, labels) {
            @Override public View getView(int pos, View cv, android.view.ViewGroup parent) {
                LinearLayout row=new LinearLayout(MainActivity.this); row.setOrientation(LinearLayout.VERTICAL);
                row.setPadding(14,12,14,12); row.setBackgroundColor(panel());
                TextView a=text(shown.get(pos).title.isEmpty()?"Sin título":shown.get(pos).title,17,true);
                TextView b=text(labels.get(pos).split("\n",3).length>1?labels.get(pos).split("\n",3)[1]:"",13,false); b.setTextColor(muted());
                TextView c=text(shown.get(pos).updated,11,false); c.setTextColor(muted());
                row.addView(a); row.addView(b); row.addView(c); return row;
            }
        };
        list.setAdapter(adapter);
        list.setOnItemClickListener((p,v,pos,id)->openNote(shown.get(pos)));
        list.setOnItemLongClickListener((p,v,pos,id)->{ Note n=shown.get(pos); new AlertDialog.Builder(this).setTitle("Nota").setItems(new String[]{"Exportar","Eliminar"},(d,w)->{if(w==0) exportNote(n); else deleteNote(n);}).show(); return true;});
    }
    void newNote() { Note n=new Note(makeId(),"","",now()); notes.add(0,n); saveNotes(); openNote(n); }
    void openNote(Note n) {
        current=n; loading=true; initBase();
        LinearLayout bar=new LinearLayout(this); bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.addView(button("‹ Notas",()->{saveCurrent(); hideKeyboard(); showHome();}));
        TextView spacer=text(" ",12,false); bar.addView(spacer,new LinearLayout.LayoutParams(0,1,1));
        bar.addView(button("Exportar",()->{saveCurrent(); exportNote(current);}));
        bar.addView(button("⋮",()->noteMenu()));
        base.addView(bar); addGap(base,10);
        titleInput=edit("Título",false); titleInput.setText(n.title); base.addView(titleInput); addGap(base,8);
        bodyInput=edit("Empieza a escribir…",true); bodyInput.setText(n.body);
        bodyInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE|android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1); base.addView(bodyInput,bp);
        addGap(base,8); TextView info=text("Guardado automáticamente en este dispositivo",12,false); info.setTextColor(muted()); base.addView(info);
        TextWatcher watcher=new TextWatcher(){ public void beforeTextChanged(CharSequence s,int st,int c,int a){} public void onTextChanged(CharSequence s,int st,int before,int count){ if(!loading) saveCurrent(); } public void afterTextChanged(Editable e){} };
        titleInput.addTextChangedListener(watcher); bodyInput.addTextChangedListener(watcher); loading=false;
    }
    void saveCurrent() {
        if(current==null || loading) return;
        current.title=titleInput.getText().toString(); current.body=bodyInput.getText().toString(); current.updated=now(); saveNotes();
    }
    void deleteNote(Note n) {
        new AlertDialog.Builder(this).setTitle("Eliminar nota").setMessage("¿Eliminar esta nota definitivamente?")
            .setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->{notes.remove(n); saveNotes(); showHome();}).show();
    }
    void noteMenu() {
        String[] items={"Exportar como archivo…","Importar texto en esta nota","Eliminar nota","Cambiar tema"};
        new AlertDialog.Builder(this).setItems(items,(d,w)->{
            if(w==0){saveCurrent();exportNote(current);}
            if(w==1) openFile();
            if(w==2) deleteNote(current);
            if(w==3){dark=!dark;saveCurrent();openNote(current);}
        }).show();
    }
    void menu() {
        new AlertDialog.Builder(this).setTitle("Opciones").setItems(new String[]{"Nueva nota","Importar archivo de texto","Cambiar tema","Información"},(d,w)->{
            if(w==0)newNote(); if(w==1)openFile(); if(w==2){dark=!dark;saveNotes();showHome();}
            if(w==3)new AlertDialog.Builder(this).setMessage("Bloc de notas 1.0\nTus notas se guardan localmente en la aplicación. No necesita Internet ni crea una cuenta.").setPositiveButton("Aceptar",null).show();
        }).show();
    }
    void exportNote(Note n) {
        exportBody=n.body; String name=n.title.trim().isEmpty()?"nota":n.title.trim();
        name=name.replaceAll("[\\\\/:*?\"<>|]","_"); exportName=name+".txt";
        final EditText input=edit("Nombre del archivo, incluida la extensión",false); input.setText(exportName);
        new AlertDialog.Builder(this).setTitle("Guardar archivo").setMessage("Puedes cambiar la extensión: por ejemplo .md, .html, .csv o .json.")
            .setView(input).setNegativeButton("Cancelar",null).setPositiveButton("Elegir ubicación",(d,w)->{
                String chosen=input.getText().toString().trim(); if(chosen.isEmpty()) chosen="nota.txt";
                exportName=chosen; Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*"); i.putExtra(Intent.EXTRA_TITLE,exportName); startActivityForResult(i,CREATE_FILE);
            }).show();
    }
    void openFile() { Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*"); startActivityForResult(i,OPEN_FILE); }
    @Override protected void onActivityResult(int req,int res,Intent data) {
        super.onActivityResult(req,res,data); if(res!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try {
            if(req==CREATE_FILE) {
                OutputStream out=getContentResolver().openOutputStream(uri);
                out.write(exportBody.getBytes(StandardCharsets.UTF_8)); out.close();
                Toast.makeText(this,"Archivo guardado",Toast.LENGTH_LONG).show();
            } else if(req==OPEN_FILE) {
                InputStream in=getContentResolver().openInputStream(uri); ByteArrayOutputStream out=new ByteArrayOutputStream();
                byte[] buf=new byte[4096]; int len; while((len=in.read(buf))>0)out.write(buf,0,len); in.close();
                String content=out.toString("UTF-8");
                if(current==null) { Note n=new Note(makeId(),"Importado",content,now()); notes.add(0,n); saveNotes(); openNote(n); }
                else { bodyInput.setText(content); saveCurrent(); }
                Toast.makeText(this,"Texto importado",Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e) { Toast.makeText(this,"No se pudo procesar el archivo: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
    }
    void hideKeyboard() { try { ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(base.getWindowToken(),0); } catch(Exception ignored){} }
    void saveNotes() {
        JSONArray arr=new JSONArray();
        try { for(Note n:notes){JSONObject o=new JSONObject();o.put("id",n.id);o.put("title",n.title);o.put("body",n.body);o.put("updated",n.updated);arr.put(o);} }
        catch(Exception ignored){}
        getSharedPreferences("notes",MODE_PRIVATE).edit().putString("data",arr.toString()).putBoolean("dark",dark).apply();
    }
    void loadNotes() {
        android.content.SharedPreferences p=getSharedPreferences("notes",MODE_PRIVATE); dark=p.getBoolean("dark",false);
        try { JSONArray a=new JSONArray(p.getString("data","[]")); for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);notes.add(new Note(o.optString("id",makeId()),o.optString("title",""),o.optString("body",""),o.optString("updated",now())));} }
        catch(Exception ignored){}
    }
    // Small helper so this file remains dependency-free.
    static class GradientButton extends Button {
        GradientButton(Context c){super(c);}
    }
}
