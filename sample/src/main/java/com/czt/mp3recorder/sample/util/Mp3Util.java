package com.czt.mp3recorder.sample.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by Ming.Xiao on 2018/8/21.
 */

public class Mp3Util {

    FileOutputStream out;
    boolean isFinished = false;
    int i=0;

    public void appendNewFile(String appendFilepath) throws IOException {
        String fenLiData = fenLiData(appendFilepath);
        File file=new File(fenLiData);
        String luJing="/storage/emulated/0/"+"HH音乐播放器/合并/";
        if(out == null){
            out = new FileOutputStream(luJing +"(HH合并).mp3",true);
        }
        File f=new File(luJing);
        if(f == null || !f.exists()) {
            f.mkdirs();
        }
        FileInputStream in = new FileInputStream(appendFilepath);
        byte bs[]=new byte[1024*4];
        int len=0;

        while((len=in.read(bs))!=-1){
            out.write(bs,0,len);
        }


        in.close();
        i++;
        if(i == 3){
            endOut();
        }
    }

    private void endOut() throws IOException{
        out.close();
        out = null;
    }


    public  String heBingMp3(String path,String path1,String name) throws IOException{
        String fenLiData = fenLiData(path);
        String fenLiData2 = fenLiData(path1);
        File file=new File(fenLiData);
        File file1=new File(fenLiData2);
        String luJing="/storage/emulated/0/"+"HH音乐播放器/合并/";
        File f=new File(luJing);
        f.mkdirs();
        //生成处理后的文件
        File file2=new File(luJing+name+"(HH合并).mp3");
        FileInputStream in=new FileInputStream(file);
        FileOutputStream out=new FileOutputStream(file2);
        byte bs[]=new byte[1024*4];
        int len=0;
        //先读第一个
        while((len=in.read(bs))!=-1){
            out.write(bs,0,len);
        }

        in.close();
        out.close();
        //再读第二个
        in=new FileInputStream(file1);
        out=new FileOutputStream(file2,true);//在文件尾打开输出流
        len=0;

        byte bs1[]=new byte[1024*4];
        while((len=in.read(bs1))!=-1){
            out.write(bs1,0,len);
        }

        in.close();
        out.close();
        if(file.exists())file.delete();
        if(file1.exists())file1.delete();

        return file2.getAbsolutePath();
    }


    public  String fenLiData(String path) throws IOException {
        File file = new File(path);// 原文件
        File file1 = new File(path + "01");// 分离ID3V2后的文件,这是个中间文件，最后要被删除
        File file2 = new File(path + "001");// 分离id3v1后的文件
        RandomAccessFile rf = new RandomAccessFile(file, "rw");// 随机读取文件
        FileOutputStream fos = new FileOutputStream(file1);
        Log.v("xuebing","cccc");
        byte ID3[] = new byte[3];
        rf.read(ID3);
        String ID3str = new String(ID3);
        // 分离ID3v2
        if (ID3str.equals("ID3")) {
            rf.seek(6);
            byte[] ID3size = new byte[4];
            rf.read(ID3size);
            int size1 = (ID3size[0] & 0x7f) << 21;
            int size2 = (ID3size[1] & 0x7f) << 14;
            int size3 = (ID3size[2] & 0x7f) << 7;
            int size4 = (ID3size[3] & 0x7f);
            int size = size1 + size2 + size3 + size4 + 10;
            rf.seek(size);
            int lens = 0;
            byte[] bs = new byte[1024*4];
            while ((lens = rf.read(bs)) != -1) {
                fos.write(bs, 0, lens);
            }

            fos.close();
            rf.close();
        } else {// 否则完全复制文件
            int lens = 0;
            rf.seek(0);
            byte[] bs = new byte[1024*4];
            while ((lens = rf.read(bs)) != -1) {
                fos.write(bs, 0, lens);
            }
            fos.close();
            rf.close();
        }

        Log.v("xuebing","ddddddd");
        RandomAccessFile raf = new RandomAccessFile(file1, "rw");
        byte TAG[] = new byte[3];
        raf.seek(raf.length() - 128);
        raf.read(TAG);
        String tagstr = new String(TAG);
        if (tagstr.equals("TAG")) {
            FileOutputStream fs = new FileOutputStream(file2);
            raf.seek(0);
            byte[] bs=new byte[(int)(raf.length()-128)];
            raf.read(bs);
            fs.write(bs);
            raf.close();
            fs.close();
        } else {// 否则完全复制内容至file2
            FileOutputStream fs = new FileOutputStream(file2);
            raf.seek(0);
            byte[] bs = new byte[1024*4];
            int len = 0;
            while ((len = raf.read(bs)) != -1) {
                fs.write(bs, 0, len);
            }
            raf.close();
            fs.close();
        }

        Log.v("xuebing","eeeeeeeeee");
        if (file1.exists())// 删除中间文件
        {
            file1.delete();

        }
        return file2.getAbsolutePath();
    }

}
