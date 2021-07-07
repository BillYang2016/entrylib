package com.billyang.entrylib.ui;

import com.billyang.entrylib.EntryLib;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

public class Tray {

    EntryLib entrylib;
    private TrayIcon tray = null;
    FloatingWindow fw = null;

    public void create(EntryLib entrylib) {
        this.entrylib = entrylib;

        fw = new FloatingWindow(entrylib);

        if(!SystemTray.isSupported()) {
            entrylib.getLogger().warning("系统不支持托盘");
            return;
        }

        InputStream is = entrylib.getResourceAsStream("icon.jpg");

        if(is != null) {
            try {
                PopupMenu pop = new PopupMenu();

                MenuItem menu1 = new MenuItem("打开控制台");
                menu1.addActionListener(e -> {
                    fw.setVisible(true);
                });

                pop.add(menu1);

                pop.addSeparator();

                MenuItem menu2 = new MenuItem("关于");
                menu2.addActionListener(e -> JOptionPane.showMessageDialog(
                        null, "EntryLib " + entrylib.getVersion() +
                                "\nhttps://github.com/BillYang2016/entrylib" +
                                "\n遵循 AGPL-3.0 协议开源" +
                                "\n作者 Bill Yang" +
                                "\n插件图标版权所有",
                        "关于 EntryLib", JOptionPane.INFORMATION_MESSAGE
                ));

                pop.add(menu2);

                tray = new TrayIcon(ImageIO.read(is), "EntryLib 菜单", pop);
                tray.setImageAutoSize(true);
                tray.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if(e.getButton() != MouseEvent.BUTTON1) return; //需左键点击
                        if(e.getClickCount() != 2) return; //需要双击
                        fw.setVisible(true);
                    }
                });

                SystemTray systemTray = SystemTray.getSystemTray();
                systemTray.add(tray);
            } catch (IOException | AWTException e) {
                e.printStackTrace();
            }
        } else entrylib.getLogger().error("未找到资源文件icon.jpg，无法生成控制台");
    }

    public void remove() {
        if(tray != null)SystemTray.getSystemTray().remove(tray);
    }

}