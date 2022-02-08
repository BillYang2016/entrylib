package com.billyang.entrylib.MiraiCodeParser;

import net.mamoe.mirai.contact.AudioSupported;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.message.data.*;
import net.mamoe.mirai.utils.ExternalResource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AudioProcessor 类
 * 语音处理器
 * 实现对语音进行转义与反转义
 * @author Bill Yang
 */
public class AudioParser {

    String path;

    /**
     * 初始化
     * @param path 提供数据路径
     */
    public void init(String path) {
        this.path = path + "/audios/";

        File file = new File(this.path);
        if(!file.exists()) {
            file.mkdirs();
        }
    }

    /**
     * 构造函数
     * 自动初始化
     */
    public AudioParser(String path) {
        init(path);
    }

    /**
     * 下载一个语音
     * 需要提供 [OnlineAudio]
     * 根据语音包含的下载地址，缓存到插件目录中
     * @param audio 语音文件
     * @return 下载成功或否
     * @see Audio
     */
    boolean downloadAudio(OnlineAudio audio) {
        String audioName = audio.getFilename();

        try {
            URL url = new URL(audio.getUrlForDownload());

            URLConnection conn = url.openConnection();
            InputStream inStream = conn.getInputStream();
            FileOutputStream fs = new FileOutputStream(new File(path, audioName));

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
     * 将语音名转化为 Mirai 码
     * Audio 暂无官方 Mirai 码
     * 暂用 [mirai:audio:${filename}]
     * @param fileName 语音名
     * @return Mirai 码
     */
    public static String Name2MiraiCode(String fileName) {return "[mirai:audio:" + fileName + "]";}

    /**
     * 将语音 Mirai 码转化为 语音名
     * Audio 暂无官方 Mirai 码
     * 暂用 [mirai:audio:${filename}]
     * @param code Mirai 码
     * @return 语音名
     */
    public static String MiraiCode2Name(String code) {return code.replace("[mirai:audio:", "").replace("]","");}

    /**
     * 将消息队列中的语音转义为作为 Mirai 码的字符串
     * 始终本地缓存
     * @param msgChain 消息队列
     * @return 转义后的消息队列
     */
    public MessageChain Audio2PlainText(MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage msg : msgChain) {
            if(msg instanceof OnlineAudio) {
                OnlineAudio audio = (OnlineAudio) msg;
                downloadAudio(audio);

                msg = new PlainText(Name2MiraiCode(audio.getFilename()));
            } else if(msg instanceof OfflineAudio) {
                OfflineAudio audio = (OfflineAudio) msg;

                msg = new PlainText(Name2MiraiCode(audio.getFilename()));
            }
            builder.append(msg);
        }

        return builder.build();
    }

    /**
     * 语音 Mirai 码的正则匹配式
     */
    public static String regex = "\\[mirai:audio:.+]";

    /**
     * 将纯文本中的语音 Mirai 码反转义为语音
     * 将语音上传到服务器
     * @param g 原消息事件
     * @param msg 纯文本
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2Audio(GroupMessageEvent g, String msg) {
        MessageChainBuilder builder = new MessageChainBuilder();

        Pattern pt = Pattern.compile(regex);
        Matcher mt = pt.matcher(msg);

        int start, end = 0, lastEnd = 0;

        while(mt.find()) {
            start = mt.start();
            end = mt.end();

            if(start >= 1) builder.append(new PlainText(msg.substring(lastEnd, start)));

            String audioName = MiraiCode2Name(msg.substring(start, end));
            File file = new File(path, audioName);

            if(file.exists()) { //存在文件
                try {
                    ExternalResource resource = ExternalResource.create(file);
                    AudioSupported contact = g.getGroup();
                    OfflineAudio audio = contact.uploadAudio(resource);
                    builder.append(audio);
                    resource.close();
                } catch (Exception e) {
                    builder.append(new PlainText("[语音格式错误或文件过大]"));
                }
            } else { //不存在，不进行转义
                builder.append(new PlainText(msg.substring(start, end)));
            }

            lastEnd = end;
        }

        builder.append(new PlainText(msg.substring(end)));

        return builder.build();
    }

    /**
     * 将消息队列中的语音 Mirai 码反转义为语音
     * 将语音上传到服务器
     * @param g 原消息事件
     * @param msgChain 消息队列
     * @return 反转义后的消息队列
     */
    public MessageChain PlainText2Audio(GroupMessageEvent g, MessageChain msgChain) {
        MessageChainBuilder builder = new MessageChainBuilder();

        for(SingleMessage message: msgChain) {
            if(message instanceof PlainText) builder.append(PlainText2Audio(g, message.contentToString()));
            else builder.append(message);
        }

        return builder.build();
    }
}
