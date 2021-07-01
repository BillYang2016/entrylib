package com.billyang.entrylib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class JsonFormater {
    public static String format(String jsonStr){
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(jsonStr.getBytes());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read, space = 0;
            boolean first = true;

            while((read = in.read()) > 0){
                char ch = (char)read;
                switch (ch) {
                    case '{': {
                        space = outputAndRightMove(space, ch, out, first);
                        break;
                    }
                    case '[': {
                        out.write(ch);
                        space += 2;
                        break;
                    }
                    case '}':
                    case ']': {
                        space = outputAndLeftMove(space, ch, out);
                        break;
                    }
                    case ',': {
                        out.write(ch);
                        outputNewline(out);
                        out.write(getBlankingStringBytes(space));
                        break;
                    }
                    default: {
                        out.write(ch);
                        break;
                    }
                }
                first = false;
            }
            return out.toString();
        } catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public static int outputAndRightMove(int space, char ch, ByteArrayOutputStream out, boolean first) throws IOException {
        if(!first) outputNewline(out); //换行
        out.write(getBlankingStringBytes(space)); //向右缩进
        out.write(ch);
        outputNewline(out);
        space += 2; //再向右缩进多两个字符
        out.write(getBlankingStringBytes(space));
        return space;
    }

    public static int outputAndLeftMove(int space, char ch, ByteArrayOutputStream out) throws IOException{
        outputNewline(out);
        space -= 2;
        out.write(getBlankingStringBytes(space));
        out.write(ch);
        return space;
    }

    public static byte[] getBlankingStringBytes(int space){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < space; i++)sb.append(" ");
        return sb.toString().getBytes();
    }

    public static void outputNewline(ByteArrayOutputStream out){
        out.write('\n');
    }
}