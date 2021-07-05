package com.billyang.entrylib;

import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImageProcesser {

    String path;

    void init(String path) {
        this.path = path + "/images/";

        File file = new File(this.path);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

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

    String Id2MiraiCode(String Id) {return "[mirai:image:" + Id + "]";}

    String MiraiCode2Id(String code) {return code.replace("[mirai:image:", "").replace("]","");}

    MessageChain Image2PlainText(UserIO uio, MessageChain msgChain) { //图片转义
        MessageChainBuilder builder = new MessageChainBuilder();

        boolean download = uio.getDownloadMode(); //查询下载选项

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

    String regex = "\\[mirai:image:\\{[0-9a-fA-F]{8}-([0-9a-fA-F]{4}-){3}[0-9a-fA-F]{12}\\}\\..{3,5}]";

    MessageChain PlainText2Image(GroupMessageEvent g, String msg) { //图片反转义
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
