package com.hope;

import com.hope.utils.AliOSSUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileUploadGUI extends JFrame {
    private JLabel filePathLabel;
    private JTextField filePathField;
    private JButton browseButton;
    private JButton uploadButton;
    private JTextArea resultArea;
    private File selectedFile;
    private AliOSSUtil ossUtil;
    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;
    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    public FileUploadGUI() {
        // 初始化OSS工具类，实际应用中可能需要通过Spring容器获取
        ossUtil = new AliOSSUtil();
        // 这里需要手动设置OSS参数，实际项目中会通过@Value自动注入
        initOSSUtil();

        initializeUI();
        setupEventListeners();
    }

    private void initOSSUtil() {
        // 注意：在实际项目中，这些参数会通过配置文件注入
        // 这里只是为了GUI演示而手动设置，实际使用时应该移除
        try {
            // 通过反射设置私有属性，实际项目中不需要这样做
            setFieldValue(ossUtil, "endpoint", "https://oss-cn-shenzhen.aliyuncs.com");
            setFieldValue(ossUtil, "accessKeyId", accessKeyId);
            setFieldValue(ossUtil, "accessKeySecret", accessKeySecret);
            setFieldValue(ossUtil, "bucketName", "chufala");
        } catch (Exception e) {
            showErrorMessage("初始化OSS工具失败: " + e.getMessage());
        }
    }

    private void setFieldValue(Object obj, String fieldName, String value) throws Exception {
        java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(obj, value);
    }

    private void initializeUI() {
        setTitle("文件上传到阿里云OSS");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // 居中显示

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 文件选择区域
        JPanel fileSelectionPanel = new JPanel(new BorderLayout(10, 10));
        filePathLabel = new JLabel("选择文件:");
        filePathField = new JTextField();
        filePathField.setEditable(false);
        browseButton = new JButton("浏览...");
        uploadButton = new JButton("上传文件");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.add(browseButton);
        buttonPanel.add(uploadButton);

        fileSelectionPanel.add(filePathLabel, BorderLayout.WEST);
        fileSelectionPanel.add(filePathField, BorderLayout.CENTER);
        fileSelectionPanel.add(buttonPanel, BorderLayout.EAST);

        // 结果显示区域
        JPanel resultPanel = new JPanel(new BorderLayout(10, 10));
        JLabel resultLabel = new JLabel("上传结果:");
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        resultPanel.add(resultLabel, BorderLayout.NORTH);
        resultPanel.add(scrollPane, BorderLayout.CENTER);

        // 组装主面板
        mainPanel.add(fileSelectionPanel, BorderLayout.NORTH);
        mainPanel.add(resultPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void setupEventListeners() {
        // 浏览文件按钮事件
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                int result = fileChooser.showOpenDialog(FileUploadGUI.this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    filePathField.setText(selectedFile.getAbsolutePath());
                    resultArea.append("已选择文件: " + selectedFile.getName() + "\n");
                }
            }
        });

        // 上传文件按钮事件
        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedFile == null || !selectedFile.exists()) {
                    showErrorMessage("请先选择一个有效的文件");
                    return;
                }

                uploadFileToOSS();
            }
        });
    }

    private void uploadFileToOSS() {
        try {
            resultArea.append("开始上传文件: " + selectedFile.getName() + "\n");

            // 将本地文件转换为MultipartFile
            FileInputStream input = new FileInputStream(selectedFile);
            MultipartFile multipartFile = new MockMultipartFile(
                    "file",
                    selectedFile.getName(),
                    null,
                    input
            );

            // 调用OSS工具类上传文件
            String fileUrl = ossUtil.upload(multipartFile);

            resultArea.append("文件上传成功!\n");
            resultArea.append("文件访问URL: " + fileUrl + "\n\n");

            input.close();
        } catch (IOException ex) {
            resultArea.append("文件上传失败: " + ex.getMessage() + "\n\n");
            ex.printStackTrace();
        } catch (Exception ex) {
            resultArea.append("发生错误: " + ex.getMessage() + "\n\n");
            ex.printStackTrace();
        }
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileUploadGUI().setVisible(true);
            }
        });
    }
}

