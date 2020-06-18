package com.example.final_task;

public class Category {
    private int CIcon;
    private String CName;
    private String Color;


    public Category(){
    }
    public Category(int CIcon,String CName,String Color){
        this.CIcon=CIcon;
        this.CName=CName;
        this.Color=Color;
    }
    public int getCIcon() {
        return CIcon;
    }

    public void setCIcon(int CIcon) {
        this.CIcon = CIcon;
    }

    public String getCName() {
        return CName;
    }

    public void setCName(String CName) {
        this.CName = CName;
    }

    public String getColor() {
        return Color;
    }

    public void setColor(String color) {
        Color = color;
    }


}
