package com.dp.dnsc.util;

import java.util.Scanner;

public class RBTreeTestNew {
    public static void main(String[] args) {
        RBTreeNew rbt=new RBTreeNew();
        Scanner sc=new Scanner(System.in);
        while (true){
            System.out.println("������order:");
            String order=sc.next();
            System.out.println("������key:");
            if (order.equals("1")){
                String key=sc.next();
                System.out.println("������value:");
                String value = sc.next();
                rbt.insert(key,value);
            }else if (order.equals("2")){
                String key=sc.next();
                System.out.println("������value:");
                String value = sc.next();
//                rbt.delete(key);
            }
            TreeOperationNew.show(rbt.getRoot());
        }
    }
}
