package com.example.final_task;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuView;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private SQLiteDatabase db;
    private DBOpenHelper dbOpenHelper;
    private Context mContext;
    private ListView list_memo;
    private ListView list_Category;
    private MyAdapter<memo> myAdapter = null;
    private ArrayList<memo> mData = null;
    private MyAdapter<Category> categoryAdapter = null;
    private ArrayList<Category>categoryData=null;
    //private Button addBtn;
    private Toolbar toolbar;
    private DrawerLayout drawer;
    private FloatingActionButton addFab;
    private NavigationView navigationView;
    private ArrayList<String> categoryNames;
    private AlertDialog alert = null;
    private AlertDialog.Builder builder = null;
    private boolean[] checkItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mContext = Main2Activity.this;

        init();

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
      // NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //return NavigationUI.navigateUp(navController, mAppBarConfiguration)
          //      || super.onSupportNavigateUp();
        return true;
    }

    private void init() {
        //数据库变量初始化
        dbOpenHelper = new DBOpenHelper(mContext, "My_memo.db", null, 1);
        db = dbOpenHelper.getWritableDatabase();

        //导航栏+侧滑菜单
        toolbar = findViewById(R.id.main_toolbar);
       // setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);//
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        //动态加载侧滑菜单
        updateDrawLayout();
//        categoryNames=getCategory();
//        for(int i=0;i<categoryNames.size();i++){
//            navigationView.getMenu().add(1,2+i,0,categoryNames.get(i));
//            navigationView.getMenu().findItem(2+i)
//                    .setIcon(R.mipmap.ic_launcher) ;
//        }

        //侧滑菜单监听事件
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
                if(itemId==R.id.all_note){
                    bindViews("");
                    toolbar.setTitle("全部笔记");
                }
                else if(itemId==R.id.item_add){
                    alert_edit();//弹出新建笔记对话框

                }
                else if(itemId==R.id.item_del){
                    final String[] categories = categoryNames.toArray(new String[categoryNames.size()]) ;
                    //定义一个用来记录个列表项状态的boolean数组
                    checkItems = new boolean[categoryNames.size()];
                    alert = null;
                    builder = new AlertDialog.Builder(mContext);
                    alert = builder.setIcon(R.mipmap.delete_icon)
                            .setTitle("请选择要删除的分组(分组内的笔记也会被删除！)")
                            .setMultiChoiceItems(categories, checkItems, new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    checkItems[which] = isChecked;
                                }
                            })
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String result = "";
                                    for (int i = 0; i < checkItems.length; i++) {
                                        if (checkItems[i]){
                                            //sql语句删除两张表中的信息
                                            db.execSQL("DELETE FROM category WHERE memoType=? ",
                                                    new String[]{categories[i]});
                                            db.execSQL("DELETE FROM memo_1 WHERE memoType=? ",
                                                    new String[]{categories[i]});
                                            updateDrawLayout();
                                            result+=categories[i]+" ";
                                        }
                                    }
                                    //更新侧滑界面

                                    //重新绑定主界面
                                    Toast.makeText(getApplicationContext(), "您删除了:" + result, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .show();
                }
                else{
                    String tempTitle=menuItem.getTitle().toString();
                    bindViews(tempTitle);
                    toolbar.setTitle(tempTitle);
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        //浮动按钮单击事件
        addFab = findViewById(R.id.fab);
        addFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Main2Activity.this, edit_memo.class);
                intent.putExtra("flag", 1);
                startActivity(intent);
            }
        });


        //数据初始化，绑定数据
        bindViews("");

        //ListView单击事件，跳转到当前备忘信息
        list_memo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //传递数据到edit_memo
                memo my_memo = mData.get(position);
                System.out.println("Intent's id:" + my_memo.getId());
                Intent intent = new Intent(Main2Activity.this, edit_memo.class);
                intent.putExtra("memo_data", my_memo);
                intent.putExtra("flag", 0);
                startActivity(intent);

            }
        });
    }

    private void reFlashListView(ArrayList<memo> mData,String categoryName) {
        //参数依次是:表名，列名，where约束条件，where中占位符提供具体的值，指定group by的列，进一步约束
        Cursor cursor;
        if(categoryName.equals("")){
            cursor = db.rawQuery("SELECT id,title,content,modified_date,memoType,alarm_date FROM memo_1 " ,null);
            //Toast.makeText(getApplicationContext(),"??",Toast.LENGTH_SHORT).show();
        }
        else {
            cursor = db.rawQuery("SELECT id,title,content,modified_date,memoType,alarm_date FROM memo_1 " +
                    "WHERE memoType=? ", new String[]{categoryName});
        }
        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String date = cursor.getString(cursor.getColumnIndex("modified_date"));
                String memoType = cursor.getString(cursor.getColumnIndex("memoType"));
                String alarm_date = cursor.getString(cursor.getColumnIndex("alarm_date"));
                System.out.println("main:" + memoType);

                mData.add(new memo(id, title, content, date, memoType,alarm_date));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
    //根据类别刷新界面的函数
    //调用reFlashListView(String categoryName)
    private void bindViews(String categoryName) {
        mData = new ArrayList<memo>();
        reFlashListView(mData,categoryName);
        myAdapter = new MyAdapter<memo>((ArrayList) mData, R.layout.item) {
            @Override
            public void bindView(MyAdapter.ViewHolder holder, memo obj) {
                holder.setText(R.id.itemTitle, obj.getTitle());
                holder.setText(R.id.itemDate, obj.getDate());
            }
        };
        list_memo = (ListView) findViewById(R.id.list_memo);
        list_memo.setAdapter(myAdapter);
    }

    //获取已有的分类名
    private ArrayList<String>  getCategory(){
        ArrayList<String> categoryNames=new ArrayList<String>();
        Cursor cursor = db.rawQuery("SELECT DISTINCT memoType FROM category ",null);
        if (cursor.moveToFirst()) {
            do {
                String temp = cursor.getString(0);
                categoryNames.add(temp);
                System.out.println("names:"+temp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  categoryNames;
    }
    public void alert_edit(){
        final EditText et = new EditText(this);
        new AlertDialog.Builder(this).setTitle("新建笔记分类")
                .setIcon(android.R.drawable.sym_def_app_icon)
                .setView(et)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //按下确定键后的事件
                        String name=et.getText().toString();
                        if(!categoryNames.contains(name)) {
                            categoryNames.add(name);
                            navigationView.getMenu().add(1,categoryNames.size(),0,name);
                            navigationView.getMenu().findItem(categoryNames.size())
                                    .setIcon(R.mipmap.ic_launcher) ;
                            db.execSQL("INSERT INTO category(memoType) values(?)",
                                    new String[]{name});
                            Toast.makeText(getApplicationContext(),"新建成功！",Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "笔记分类名称已经存在！新建失败".toString(),Toast.LENGTH_LONG).show();
                        }

                    }
                }).setNegativeButton("取消",null).show();
    }
    private  void updateDrawLayout(){ //更新侧滑界面
        categoryNames=getCategory();
        Menu menu= navigationView.getMenu();
        menu.removeGroup(1);
        for(int i=0;i<categoryNames.size();i++){
            menu.add(1,2+i,0,categoryNames.get(i));
            menu.findItem(2+i).setIcon(R.mipmap.ic_launcher) ;
        }
    }

}
