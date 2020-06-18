package com.example.final_task;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class edit_memo extends AppCompatActivity {
    Toolbar toolbar;
    private EditText memoText;
    private TextView titleTxt;
    private SQLiteDatabase db;
    private  DBOpenHelper dbOpenHelper;
    private Context mContext;
    private int flag;//判断是修改还是新建备忘录  0修改 1新建
    private memo my_memo;//从主机面传过来的memo对象
    private String tempID;
    private Spinner spin_category;
    private boolean dateFlag;//EditText是否被修改过 1修改过 0未修改
    private boolean timeFlag;
    private MyAdapter myAdapter;
    private ArrayList<Category>mData;
    private String memoType;
    private ArrayList<String> categoryNames;
    private ArrayList<String> colorNames;
    private ImageView timeImage;
    private AlarmManager alarmManager;
    private PendingIntent pi;
    private int alert_year,alert_month,alert_day,alert_min,alert_second,alert_hour;
    private TextView alarmText;
    private Button clearAlarmBtn;
    private String alarmResult;
    private Calendar cale;
    private Intent alert_intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_memo);
        mContext=getApplicationContext();

        toolbar_init();//导航栏初始化
        widget_init();//控件初始化
        get_intentMemo();//输入文本框初始化
        Toast.makeText(mContext, "memo:"+memoType, Toast.LENGTH_SHORT).show();
        bindViews();
        alert_init();
    }


    private void toolbar_init(){
        //导航栏初始化
        toolbar =findViewById(R.id.toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.inflateMenu(R.menu.menu);
        //setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(edit_memo.this,Main2Activity.class);
                startActivity(intent);
            }
        });
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                        case R.id.action_delete_note:
                            AlertDialog.Builder dialog = new AlertDialog.Builder(edit_memo.this);
                            dialog.setMessage("是否删除此笔记？");
                            dialog.setCancelable(false);//dialog弹出后会点击屏幕或物理返回键，dialog不消失
                            dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            dialog.setPositiveButton("删除id为"+tempID+"的记录？", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dbOpenHelper = new DBOpenHelper(mContext, "My_memo.db", null, 1);
                                    db = dbOpenHelper.getWritableDatabase();
                                    db.execSQL("DELETE FROM memo_1 WHERE id = ?", new String[]{tempID});
                                    Toast.makeText(mContext, "删除完毕"+tempID, Toast.LENGTH_SHORT).show();
                                    Intent intent=new Intent(edit_memo.this,Main2Activity.class);
                                    startActivity(intent);
                                }
                            });
                            dialog.show();
                            return true;
                        case R.id.memo_check:
                            //获取数据库操作对象
                            dbOpenHelper = new DBOpenHelper(mContext, "My_memo.db", null, 1);
                            db = dbOpenHelper.getWritableDatabase();

                            //获取用户界面输入
                            String title = getFirstLine(memoText.getText().toString());//获取多行文本框的第一行内容作为标题
                            String content = memoText.getText().toString();//获取多行文本框的全部文本
                            String CurrentTime = getCurrentTime();//获取当前提交的时间
                            //增、改
                            if(flag==1) {//新建备忘信息
                                ContentValues values1 = new ContentValues();
                                values1.put("title", title);//备忘录标题
                                values1.put("content", content);//备忘录内容
                                values1.put("modified_date", CurrentTime);//修改日期
                                values1.put("memoType", memoType);//备忘录类型
                                values1.put("alarm_date",alarmResult);//提醒时间字符串

                                //参数依次是：表名，强行插入null值得数据列的列名，一行记录的数据
                                db.insert("memo_1", null, values1);
                                Toast.makeText(mContext, "插入完毕~", Toast.LENGTH_SHORT).show();
                                if(timeFlag&&dateFlag){
                                    String tempTitle=getFirstLine(memoText.getText().toString());
                                    System.out.println("titleTest:"+tempTitle);
alert_intent.putExtra("title",tempTitle);
pi = PendingIntent.getActivity(edit_memo.this, 0, alert_intent, 0);
alarmManager.set(AlarmManager.RTC_WAKEUP, cale.getTimeInMillis(),pi);
Toast.makeText(getApplicationContext(),"设置闹钟成功",Toast.LENGTH_SHORT).show();
                                }
                            }
                            else if(flag==0){//修改备忘信息
                                db.execSQL("UPDATE memo_1 SET title = ?,content = ?, modified_date= ?,memoType= ?," +
                                                "alarm_date= ?" + "WHERE id = ? ",
                                        new String[]{title,content,CurrentTime,memoType,alarmResult,tempID});
                                Toast.makeText(mContext, "更新完毕"+tempID, Toast.LENGTH_SHORT).show();

                            }
                            Intent intent=new Intent(edit_memo.this,Main2Activity.class);
                            startActivity(intent);
                            return true;
                    }
                return false;
            }
         });
    }
    private void widget_init(){
        //控件初始化
        memoText=findViewById(R.id.editText2);
        titleTxt=findViewById(R.id.titleTxt);
        alarmText=(TextView)findViewById(R.id.alarmText);
        clearAlarmBtn=(Button)findViewById(R.id.clearAlarmBtn);
        clearAlarmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmText.setText("未设置闹钟");
                alarmManager.cancel(pi);
                dbOpenHelper = new DBOpenHelper(mContext, "My_memo.db", null, 1);
                db = dbOpenHelper.getWritableDatabase();
                db.execSQL("UPDATE memo_1 SET alarm_date= ?" + "WHERE id = ? ",new String[]{null,tempID});
                dateFlag=false;
                timeFlag=false;
                alarmResult="";
                Toast.makeText(edit_memo.this, "闹钟已取消", Toast.LENGTH_SHORT)
                        .show();
                clearAlarmBtn.setVisibility(View.INVISIBLE);
            }
        });
    }
    private String getFirstLine(String s){
        String res="";
        for(int i=0;i<s.length();i++) {
            char c=s.charAt(i);
            if(c!='\n')res+=c;
            else break;
        }
        return res;
    }
    private String getCurrentTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");//Date指定格式：yyyy-MM-dd HH:mm:ss:SSS
        Date date = new Date(System.currentTimeMillis());;//创建一个date对象保存当前时间
        String res=simpleDateFormat.format(date);
        return res;
    }
private void get_intentMemo(){
    my_memo=(memo)getIntent().getSerializableExtra("memo_data");
    flag=getIntent().getIntExtra("flag",-1);//-1是默认返回值
    if(flag==0){
        titleTxt.setText("编辑备忘信息");
        memoText.setText(my_memo.getContent());
        tempID = String.valueOf(my_memo.getId());
        memoType=my_memo.getMemoType();
        if(my_memo.getAlarm_date()==null){
            clearAlarmBtn.setVisibility(View.INVISIBLE);
            alarmText.setText("未设置提醒时间");
        }
        else{
            clearAlarmBtn.setVisibility(View.VISIBLE);
            alarmText.setText("");
        }
    }else if(flag==1){
        titleTxt.setText("添加备忘信息");
        memoType="未分类";
    }
}

    private void bindViews() {
        spin_category = (Spinner) findViewById(R.id.spinner_category);
        mData = new ArrayList<Category>();
        categoryNames=getCategory();
        for(int i=0;i<categoryNames.size();i++){
            mData.add(new Category(R.mipmap.ic_launcher,categoryNames.get(i),colorNames.get(i)));
        }
       // mData.add(new Category( R.mipmap.staricon,"新建")

        myAdapter = new MyAdapter<Category>(mData, R.layout.item_spin_class) {
            @Override
            public void bindView(ViewHolder holder, Category obj) {
                holder.setImageResource(R.id.img_icon, obj.getCIcon());
                holder.setText(R.id.txt_name, obj.getCName());

            }
        };

        spin_category.setAdapter(myAdapter);
        int pos = 0;
        //查找被选中的类别的编号
        for (int i = 0; i < mData.size(); i++) {
            String tempValue = mData.get(i).getCName();
            System.out.println(memoType);
            if (tempValue.equals(memoType)) {
                pos = i;
                break;
            }
        }
       // Toast.makeText(getApplicationContext(),"当前默认序号是"+String.valueOf(pos),Toast.LENGTH_SHORT).show();
        spin_category.setSelection(pos, true);
        spin_category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = (TextView) findViewById(R.id.txt_name);
                String myCategory = textView.getText().toString();//获取当前选中的类别名
//                if(myCategory=="新建"){
//
//                }
                Toast.makeText(getApplicationContext(), "您选择的类别是："
                        + myCategory, Toast.LENGTH_SHORT).show();
                memoType=myCategory;

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private ArrayList<String>  getCategory(){
        categoryNames=new ArrayList<String>();
        colorNames=new ArrayList<String>();
        dbOpenHelper = new DBOpenHelper(mContext, "My_memo.db", null, 1);
        db = dbOpenHelper.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT DISTINCT memoType,color FROM category ",null);
        if (cursor.moveToFirst()) {
            do {
                String temp = cursor.getString(0);
                String color = cursor.getString(1);
                categoryNames.add(temp);
                colorNames.add(color);
                System.out.println("names:"+temp);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return  categoryNames;
    }


    //定时提醒模块
    private void alert_init(){
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        timeImage=(ImageView)findViewById(R.id.timeImage);


        alert_intent = new Intent(edit_memo.this, ClockActivity.class);
        timeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dateFlag=false;
                //timeFlag=false;
                Calendar currentTime = Calendar.getInstance();
                cale = Calendar.getInstance();
                cale.setTimeInMillis(System.currentTimeMillis());
                DatePickerDialog picker=new DatePickerDialog(edit_memo.this,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        //这里获取到的月份需要加上1哦~

                        alert_year=year;
                        alert_month=monthOfYear;
                        alert_day=dayOfMonth;

                        alarmResult+= "\n"+(monthOfYear+1)+"-"+dayOfMonth+"";
                        dateFlag=true;
                        cale.set(alert_year,alert_month,alert_day, alert_hour,alert_min,alert_second);
                        alarmText.setText(alarmResult);
                        clearAlarmBtn.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), alarmResult, Toast.LENGTH_SHORT).show();
                    }
                }
                        ,cale.get(Calendar.YEAR)
                        ,cale.get(Calendar.MONTH)
                        ,cale.get(Calendar.DAY_OF_MONTH));
                picker.show();
                new TimePickerDialog(edit_memo.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        alarmResult="";
                       alarmResult += hourOfDay+"时"+minute+"分";
                       Toast.makeText(getApplicationContext(), alarmResult, Toast.LENGTH_SHORT).show();
                       alert_min=minute;
                       alert_hour=hourOfDay;
                       alert_second=0;
                       timeFlag=true;

                    }

                }, cale.get(Calendar.HOUR_OF_DAY), cale.get(Calendar.MINUTE), true)
                        .show();

            }
        });

        alarmText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                cale = Calendar.getInstance();
                cale.setTimeInMillis(System.currentTimeMillis());
                DatePickerDialog picker=new DatePickerDialog(edit_memo.this,new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        //这里获取到的月份需要加上1哦~

                        alert_year=year;
                        alert_month=monthOfYear;
                        alert_day=dayOfMonth;

                        alarmResult+= "\n"+(monthOfYear+1)+"-"+dayOfMonth+"";
                        dateFlag=true;
                        cale.set(alert_year,alert_month,alert_day, alert_hour,alert_min,alert_second);
                        alarmText.setText(alarmResult);
                        clearAlarmBtn.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), alarmResult, Toast.LENGTH_SHORT).show();
                    }
                }
                        ,cale.get(Calendar.YEAR)
                        ,cale.get(Calendar.MONTH)
                        ,cale.get(Calendar.DAY_OF_MONTH));
                picker.show();
                new TimePickerDialog(edit_memo.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        alarmResult="";
                        alarmResult += hourOfDay+"时"+minute+"分";
                        Toast.makeText(getApplicationContext(), alarmResult, Toast.LENGTH_SHORT).show();
                        alert_min=minute;
                        alert_hour=hourOfDay;
                        alert_second=0;
                        timeFlag=true;

                    }
                }, cale.get(Calendar.HOUR_OF_DAY), cale.get(Calendar.MINUTE), true)
                        .show();

            }
        });
//        System.out.println("xx:"+timeFlag);
//        if(timeFlag && dateFlag){
//            //如果两个对话框都确认了
//            //alarmManager.set(AlarmManager.RTC_WAKEUP, cale.getTimeInMillis(),pi);
//            Toast.makeText(getApplicationContext(),"设置定时器成功",Toast.LENGTH_SHORT).show();
//
//        }

    }

}
