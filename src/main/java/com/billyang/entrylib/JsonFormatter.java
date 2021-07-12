package com.billyang.entrylib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * JsonFormatter 类
 * Json 格式校准器
 * 将 json 格式按照缩进标准化
 * @author Bill Yang
 */
public class JsonFormatter {
    /**
     * 将 json 格式按照缩进标准化
     * @param jsonStr json 字符串
     * @return 标准化 json 字符串
     */
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

    /**
     * 换行并向右移动缩进
     * 仅内部使用
     * @param space 缩进空格数量
     * @param ch 当前字符
     * @param out 输出流
     * @param first 是否是第一个字符
     * @return 缩进空格数量
     * @throws IOException 抛出 IO异常
     */
    public static int outputAndRightMove(int space, char ch, ByteArrayOutputStream out, boolean first) throws IOException {
        if(!first) outputNewline(out); //换行
        out.write(getBlankingStringBytes(space)); //向右缩进
        out.write(ch);
        outputNewline(out);
        space += 2; //再向右缩进多两个字符
        out.write(getBlankingStringBytes(space));
        return space;
    }

    /**
     * 换行并向左移动缩进
     * 仅内部使用
     * @param space 缩进空格数量
     * @param ch 当前字符
     * @param out 输出流
     * @return 缩进空格数量
     * @throws IOException 抛出 IO异常
     */
    public static int outputAndLeftMove(int space, char ch, ByteArrayOutputStream out) throws IOException{
        outputNewline(out);
        space -= 2;
        out.write(getBlankingStringBytes(space));
        out.write(ch);
        return space;
    }

    /**
     * 获取空行
     * 仅内部使用
     * @param space 缩进空格数量
     * @return 空行
     */
    public static byte[] getBlankingStringBytes(int space){
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < space; i++)sb.append(" ");
        return sb.toString().getBytes();
    }

    /**
     * 输出换行
     * 仅内部使用
     * @param out 输出流
     */
    public static void outputNewline(ByteArrayOutputStream out){
        out.write('\n');
    }
}