package com.billyang.entrylib.ui;

import com.billyang.entrylib.EntryLib;
import com.billyang.entrylib.UserIO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.InputStream;

class DigitOnlyKeyListener implements KeyListener {

    @Override
    public void keyTyped(KeyEvent e) {
        if(!Character.isDigit(e.getKeyChar()))e.consume();
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}

public class FloatingWindow extends JFrame {

    EntryLib entrylib;
    UserIO uio;

    JTabbedPane tabbedPane = new JTabbedPane();

    int getPageHeight(int index) {
        int height;

        switch (index) {
            case 1:
                height = 350;
                break;
            default:
                height = 300;
        }

        return height - 60;
    }

    int getPageWidth(int index) {
        switch (index) {
            case 1:
            default:
                return 500;
        }
    }

    public FloatingWindow(EntryLib entrylib) {
        this.entrylib = entrylib;
        uio = entrylib.uio;

        InputStream is = entrylib.getResourceAsStream("icon.jpg");
        if(is != null) {
            try {
                setIconImage(ImageIO.read(is));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        setTitle("EntryLib 控制台");
        setSize(500, 350);
        setLocationRelativeTo(null); //居中
        setResizable(false);
        setAlwaysOnTop(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        addGlobalConfigPage();

        tabbedPane.setSelectedIndex(0);

        setContentPane(tabbedPane);
    }

    void addGlobalConfigPage() { //全局配置页
        JPanel panel = new JPanel();

        panel.setLayout(null);
        int height = getPageHeight(1), width = getPageWidth(1);
        int borderHeight = (height - 10) / 13, contentHeight = borderHeight - 2;

        JCheckBox checkbox1 = new JCheckBox("查看模式", uio.getViewMode());
        checkbox1.setToolTipText("选中时，将\"view-mode\"修改为1，表示可以直接输入词条名来查看词条内容。反之，表示需要输入查看指令才可查看词条内容。");
        checkbox1.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("view-mode", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox2 = new JCheckBox("默认开关", uio.getDefaultSwitch());
        checkbox2.setToolTipText("选中时，将\"default-switch\"修改为1，表示所有群都默认启用本插件。反之，表示所有群都默认禁用本插件。");
        checkbox2.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("default-switch", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox3 = new JCheckBox("开关权限", uio.getSwitchPermission());
        checkbox3.setToolTipText("选中时，将\"switch-permission\"修改为1，表示只有群管等级以上的成员才有权力修改插件开关。反之，表示所有成员都可以修改。");
        checkbox3.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("switch-permission", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox4 = new JCheckBox("学习命令权限", uio.getLearnPermission());
        checkbox4.setToolTipText("选中时，将\"learn-permission\"修改为1，表示只有群管等级以上的成员才可以使用学习指令。反之，表示所有成员都可以使用。");
        checkbox4.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("learn-permission", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox5 = new JCheckBox("查看命令权限", uio.getViewPermission());
        checkbox5.setToolTipText("选中时，将\"view-permission\"修改为1，表示只有群管等级以上的成员才可以使用查看指令。反之，表示所有成员都可以使用。");
        checkbox5.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("view-permission", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox6 = new JCheckBox("历史命令权限", uio.getHistoryPermission());
        checkbox6.setToolTipText("选中时，将\"history-permission\"修改为1，表示只有群管等级以上的成员才可以使用历史指令。反之，表示所有成员都可以使用。");
        checkbox6.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("history-permission", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox7 = new JCheckBox("搜索命令权限", uio.getSearchPermission());
        checkbox7.setToolTipText("选中时，将\"search-permission\"修改为1，表示只有群管等级以上的成员才可以使用搜索指令。反之，表示所有成员都可以使用。");
        checkbox7.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("search-permission", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox8 = new JCheckBox("查询全部命令权限", uio.getAllPermission());
        checkbox8.setToolTipText("选中时，将\"all-permission\"修改为1，表示只有群管等级以上的成员才可以使用查询全部指令。反之，表示所有成员都可以使用。");
        checkbox8.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("all-permission", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox9 = new JCheckBox("删除命令权限", uio.getDeletePermission());
        checkbox9.setToolTipText("选中时，将\"delete-permission\"修改为1，表示只有群管等级以上的成员才可以使用删除指令。反之，表示所有成员都可以使用。");
        checkbox9.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("delete-permission", checkbox.isSelected() ? 1 : 0);
        });

        JCheckBox checkbox10 = new JCheckBox("缓存图片", uio.getDownloadMode());
        checkbox10.setToolTipText("选中时，将\"download-image\"修改为1，表示缓存接收到的图片。反之，表示不缓存。");
        checkbox10.addChangeListener(e -> {
            JCheckBox checkbox = (JCheckBox) e.getSource();
            uio.modifyGlobalConfig("download-image", checkbox.isSelected() ? 1 : 0);
        });

        checkbox1.setBounds(10, 5, 80, contentHeight);
        checkbox2.setBounds(10, 5 + borderHeight * 1, 80, contentHeight);
        checkbox3.setBounds(10, 5 + borderHeight * 2, 80, contentHeight);
        checkbox4.setBounds(10, 5 + borderHeight * 3, 110, contentHeight);
        checkbox5.setBounds(10, 5 + borderHeight * 4, 110, contentHeight);
        checkbox6.setBounds(10, 5 + borderHeight * 5, 110, contentHeight);
        checkbox7.setBounds(10, 5 + borderHeight * 6, 110, contentHeight);
        checkbox8.setBounds(10, 5 + borderHeight * 7, 130, contentHeight);
        checkbox9.setBounds(10, 5 + borderHeight * 8, 110, contentHeight);
        checkbox10.setBounds(10, 5 + borderHeight * 9, 80, contentHeight);
        panel.add(checkbox1);
        panel.add(checkbox2);
        panel.add(checkbox3);
        panel.add(checkbox4);
        panel.add(checkbox5);
        panel.add(checkbox6);
        panel.add(checkbox7);
        panel.add(checkbox8);
        panel.add(checkbox9);
        panel.add(checkbox10);

        JLabel label1 = new JLabel("历史命令单页最大回复量");
        label1.setToolTipText("表示对于历史指令，每页仅返回几个记录，请确保本数值为正整数。");

        JTextField textField1 = new JTextField(String.valueOf(uio.getHistoryMaxHeight()),2);
        textField1.addKeyListener(new DigitOnlyKeyListener());
        textField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                JTextField textField = (JTextField) e.getSource();
                uio.modifyGlobalConfig("history-max-height", Integer.parseInt(textField.getText()));
            }
        });

        JLabel label2 = new JLabel("搜索命令单页最大回复量");
        label2.setToolTipText("表示对于搜索指令，每页仅返回几个记录，请确保本数值为正整数。");

        JTextField textField2 = new JTextField(String.valueOf(uio.getSearchMaxHeight()),2);
        textField2.addKeyListener(new DigitOnlyKeyListener());
        textField2.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                JTextField textField = (JTextField) e.getSource();
                uio.modifyGlobalConfig("search-max-height", Integer.parseInt(textField.getText()));
            }
        });

        JLabel label3 = new JLabel("回复模式");
        label3.setToolTipText("0表示机器人将普通回复，1表示机器人回复时会@发送指令的成员，2表示机器人回复时会引用指令消息。");

        JTextField textField3 = new JTextField(String.valueOf(uio.getReplyMode()),2);
        textField3.addKeyListener(new DigitOnlyKeyListener());
        textField3.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                JTextField textField = (JTextField) e.getSource();
                uio.modifyGlobalConfig("reply-mode", Integer.parseInt(textField.getText()));
            }
        });

        label1.setBounds(10, 5 + borderHeight * 10, 150, contentHeight);
        textField1.setBounds(label1.getWidth() + 20, 5 + borderHeight * 10, 20, contentHeight);
        label2.setBounds(10, 5 + borderHeight * 11, 150, contentHeight);
        textField2.setBounds(label2.getWidth() + 20, 5 + borderHeight * 11, 20, contentHeight);
        label3.setBounds(10, 5 + borderHeight * 12, 55, contentHeight);
        textField3.setBounds(label3.getWidth() + 20, 5 + borderHeight * 12, 20, contentHeight);
        panel.add(label1);
        panel.add(textField1);
        panel.add(label2);
        panel.add(textField2);
        panel.add(label3);
        panel.add(textField3);

        InputStream is = entrylib.getResourceAsStream("gear.jpg"); //添加图标
        if(is == null) {
            entrylib.getLogger().warning("未找到资源文件gear.jpg");

            tabbedPane.addTab("全局配置", panel);
        } else {
            try {
                ImageIcon icon = new ImageIcon(ImageIO.read(is));

                tabbedPane.addTab("全局配置", icon, panel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}