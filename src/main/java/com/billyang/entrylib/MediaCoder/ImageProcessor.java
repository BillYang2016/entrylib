package com.billyang.entrylib.MediaCoder;

import com.billyang.entrylib.Config.UserIO;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ImageProcessor 类
 * 图片处理器
 * 实现对图片进行转义与反转义
 * @author Bill Yang
 */
public class ImageProcessor {

    String path;

    /**
     * 初始化
     * @param path 提供数据路径
     */
    public void init(String path) {
        this.path = path + "/images/";

        File file = new File(this.path);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 下载一个图片
     * 根据图片本身包含的下载地址，缓存到插件目录中
     * @param img 图片文件
     * @return 下载成功或否
     * @see Image
     */
    boolean downloadImage(Image img) {
        String imageId = img.getImageId();

        try {
            URL url = new URL(Image.queryUrl(img));

            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(path + imageId);

            byte[] buffer = new byte[1204];
            int byteread;

            while ((byteread = inStream.read(buffer)) != -1) fs.write(buffer, 0, byteread);

            inStream.close();
            fs.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 将图片 ID 转化为 Mirai 码
     * @param Id 图片 ID
     * @return Mirai 码
     */
    String Id2MiraiCode(String Id) {return "[mirai:image:" + Id + "]";}

    /**
     * 将图片 Mirai 码转化为 图片 ID
     * @param code Mirai 码
     * @return 图片 ID
     */
    String MiraiCode2Id(String code) {return code.replace("[mirai:image:", "").replace("]","");}

    /**
     * 将消息队列中的图片转义为作为 Mirai 码的字符串
     * 根据用户配置决定是否本地缓存图片
     * @param uio 用户配置
     * @param msgChain 消息队列
     * @return 转义后的消息队列
     * @see UserIO#getImageDownloadMode()
     */
    public MessageChain Image2PlainText(UserIO uio, MessageChain msgChain) { //图片转义
        MessageChainBuilder builder = new MessageChainBuilder();

        boolean download = uio.getImageDownloadMode(); //查询下载选项

        for(SingleMessage msg : msgChain) {
            if(msg instanceof Image) {
                Image img = (Image) msg;
                String imageId = img.getImageId();
                if(download) downloadImage(img);

                msg = new PlainText(Id2MiraiCode(imageId));
            }
            builder.append(msg);
        }

        return builder.build();
    }

    /**
     * 图片 Mirai 码的正则匹配式
     * @see Image
     */
    String regex = "\\[mirai:image:\\{[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\\}\\..{3,5}]";

    /**
     * 将消息队列中的图片 Mirai 码反转义为图片
     * 根据情况上传图片或请求服务器
     * 待优化（String 替换为 MessageChain）
     * @param g 原消息事件
     * @param msg 消息队列
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2Image(GroupMessageEvent g, String msg) {
        MessageChainBuilder builder = new MessageChainBuilder();

        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(msg);

        int start, end = 0, lastEnd = 0;

        while(mt.find()) {
            start = mt.start();
            end = mt.end();

            if(start >= 1) builder.append(new PlainText(msg.substring(lastEnd, start)));

            String imageId = MiraiCode2Id(msg.substring(start, end));
            File file = new File(path, imageId);

            if(file.exists()) { //存在文件
                try {
                    Image img = Contact.uploadImage(g.getGroup(), file);
                    builder.append(img);
                } catch (Exception e) {
                    builder.append(new PlainText("[图片尺寸过大]"));
                }
            } else { //不存在，尝试查询服务器
                try {
                    Image img = Image.fromId(imageId);
                    builder.append(img);
                } catch (Exception e) {
                    builder.append(new PlainText(msg.substring(start, end)));
                }
            }

            lastEnd = end;
        }

        builder.append(new PlainText(msg.substring(end)));

        return builder.build();
    }
}
