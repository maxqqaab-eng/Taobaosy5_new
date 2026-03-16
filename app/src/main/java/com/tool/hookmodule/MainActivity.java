package com.tool.hookmodule;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class MainActivity extends AppCompatActivity {

    private static final String 变量_配置目录 = "/sdcard/flash/";
    private static final int 变量_权限请求码 = 100;

    // 支持的包名
    private static final String[] 变量_支持的包名 = {
            "com.taobao.taobao",
            "com.xunmeng.pinduoduo"
    };

    private static final String[] 变量_包名标签 = {
            "🛒 淘宝",
            "🍊 拼多多"
    };

    private TextView 变量_设备序列号文本;
    private TextView 变量_权限状态文本;
    private TextView 变量_模块状态文本;
    private TextView 变量_配置列表文本;
    private TextView 变量_API配置文本; // 修正：去掉空格

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        变量_设备序列号文本 = findViewById(R.id.tv_device_serial);
        变量_权限状态文本 = findViewById(R.id.tv_permission_status);
        变量_模块状态文本 = findViewById(R.id.tv_module_status);
        变量_配置列表文本 = findViewById(R.id.tv_config_list);
        变量_API配置文本 = findViewById(R.id.tv_api_config); // 修正：变量名一致
        Button 变量_刷新按钮 = findViewById(R.id.btn_refresh);
        Button 变量_创建示例按钮 = findViewById(R.id.btn_create_sample);

        变量_刷新按钮.setOnClickListener(v -> refreshConfigStatus());
        变量_创建示例按钮.setOnClickListener(v -> createSampleConfigs());

        // 显示设备序列号和权限状态
        显示设备信息();

        // 请求存储权限
        requestStoragePermission();

        // 刷新状态
        refreshConfigStatus();

        // 显示 API 配置
        显示API配置();
    }

    @Override
    protected void onResume() {
        super.onResume();
        显示设备信息();
        refreshConfigStatus();
        显示API配置();
    }

    private void 显示API配置 () {
        try {
            StringBuilder 变量_配置信息 = new StringBuilder();

            // 直接从 HookModule 获取全局变量
            //String 变量_淘宝尾缀 = HookModule.TAOBAO_尾缀;
            String 变量_淘宝尾缀 = "测试";
            变量_配置信息.append("🔑 淘宝尾缀：\n").append(变量_淘宝尾缀);
            变量_API配置文本.setText(变量_配置信息.toString());
        } catch (Exception e) {
            变量_API配置文本.setText("❌ 读取失败：" + e.getMessage());
        }
    }

    private void 显示设备信息() {
        // 获取设备序列号
        String 变量_序列号 = Build.SERIAL;
        if (变量_序列号 == null || 变量_序列号.isEmpty() || "unknown".equals(变量_序列号)) {
            变量_序列号 = Build.ID;
        }
        变量_设备序列号文本.setText("📱 设备序列号：" + 变量_序列号);

        // 检查文件读取权限
        StringBuilder 变量_权限状态 = new StringBuilder();
        boolean 变量_有读权限 = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        boolean 变量_有写权限 = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            boolean 变量_有管理权限 = Environment.isExternalStorageManager();
            变量_权限状态.append("⚙️ 存储权限：");
            if (变量_有管理权限) {
                变量_权限状态.append("✅ 已授予所有文件访问权限");
                变量_权限状态文本.setBackgroundColor(0xFFE8F5E9);
                变量_权限状态文本.setTextColor(0xFF2E7D32);
            } else {
                变量_权限状态.append("❌ 未授予所有文件访问权限，请点击刷新或手动授权");
                变量_权限状态文本.setBackgroundColor(0xFFFFF3E0);
                变量_权限状态文本.setTextColor(0xFFE65100);
            }
        } else {
            变量_权限状态.append("⚙️ 存储权限：");
            if (变量_有读权限 && 变量_有写权限) {
                变量_权限状态.append("✅ 已授予读写权限");
                变量_权限状态文本.setBackgroundColor(0xFFE8F5E9);
                变量_权限状态文本.setTextColor(0xFF2E7D32);
            } else {
                变量_权限状态.append("❌ 缺少读写权限");
                变量_权限状态文本.setBackgroundColor(0xFFFFF3E0);
                变量_权限状态文本.setTextColor(0xFFE65100);
            }
        }

        变量_权限状态文本.setText(变量_权限状态.toString());
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 需要 MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                try {
                    android.content.Intent intent = new android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } catch (Exception e) {
                    android.content.Intent intent = new android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    startActivity(intent);
                }
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                        }, 变量_权限请求码);
            }
        }
    }

    private void refreshConfigStatus() {
        // 检查配置目录
        File 变量_配置目录对象 = new File(变量_配置目录);
        StringBuilder 变量_内容构建器 = new StringBuilder();

        if (!变量_配置目录对象.exists()) {
            变量_内容构建器.append("⚠️ 配置目录不存在：").append(变量_配置目录).append("\n");
            变量_内容构建器.append("点击「创建示例配置文件」自动创建目录");
            变量_配置列表文本.setText(变量_内容构建器.toString());
            变量_模块状态文本.setText("📊 状态：未找到配置文件");
            变量_模块状态文本.setBackgroundColor(0xFFFFF3E0);
            变量_模块状态文本.setTextColor(0xFFE65100);
            return;
        }

        int 变量_配置总数 = 0;

        for (int 变量_p = 0; 变量_p < 变量_支持的包名.length; 变量_p++) {
            String 变量_包名 = 变量_支持的包名[变量_p];
            String 变量_标签 = 变量_包名标签[变量_p];
            boolean 变量_是否有配置 = false;

            变量_内容构建器.append(变量_标签).append("：\n");

            // 扫描分身配置文件 0-99
            for (int 变量_i = 0; 变量_i <= 99; 变量_i++) {
                String 变量_shsjj = 变量_包名 + "_" + 变量_i + ".txt";
                File 变量_配置文件 = new File(变量_配置目录对象, 变量_shsjj);
                if (变量_配置文件.exists()) {
                    变量_是否有配置 = true;
                    变量_配置总数++;
                    String 变量_内容 = readFileContent(变量_配置文件);
                    String 变量_显示内容 = 变量_内容.length() > 80 ? 变量_内容.substring(0, 80) + "..." : 变量_内容;
                    变量_内容构建器.append("  ✅ 分身").append(变量_i).append(": ").append(变量_shsjj).append("\n");
                    变量_内容构建器.append("     ").append(变量_显示内容).append("\n");
                }
            }

            if (!变量_是否有配置) {
                变量_内容构建器.append("  ❌ 未找到配置文件\n");
            }

            变量_内容构建器.append("\n");
        }

        变量_配置列表文本.setText(变量_内容构建器.toString().trim());

        if (变量_配置总数 > 0) {
            变量_模块状态文本.setText("📊 状态：已找到 " + 变量_配置总数 + " 个配置文件，重启目标APP 后生效");
            变量_模块状态文本.setBackgroundColor(0xFFE8F5E9);
            变量_模块状态文本.setTextColor(0xFF2E7D32);
        } else {
            变量_模块状态文本.setText("📊 状态：未找到配置文件，请通过 ADB 写入配置");
            变量_模块状态文本.setBackgroundColor(0xFFFFF3E0);
            变量_模块状态文本.setTextColor(0xFFE65100);
        }
    }

    private void createSampleConfigs() {
        try {
            File 变量_配置目录对象 = new File(变量_配置目录);
            if (!变量_配置目录对象.exists()) {
                变量_配置目录对象.mkdirs();
            }

            // 创建淘宝示例配置
            String 变量_淘宝示例 = "apikey=YOUR_KEY&uuid=YOUR_UUID&itemId=YOUR_ITEM_ID&username=YOUR_NAME&ids=DEVICE_ID&pt=淘宝&fs=0";
            File 变量_淘宝文件 = new File(变量_配置目录对象, "com.taobao.taobao_0.txt");
            if (!变量_淘宝文件.exists()) {
                writeFile(变量_淘宝文件, 变量_淘宝示例);
            }

            // 创建拼多多示例配置
            String 变量_拼多多示例 = "apikey=YOUR_KEY&uuid=YOUR_UUID&itemId=YOUR_ITEM_ID&username=YOUR_NAME&ids=DEVICE_ID&pt=duoduo&fs=0";
            File 变量_拼多多文件 = new File(变量_配置目录对象, "com.xunmeng.pinduoduo_0.txt");
            if (!变量_拼多多文件.exists()) {
                writeFile(变量_拼多多文件, 变量_拼多多示例);
            }

            Toast.makeText(this, "✅ 示例配置文件已创建到 " + 变量_配置目录 + "\n请修改为实际参数值", Toast.LENGTH_LONG).show();
            refreshConfigStatus();
        } catch (Exception e) {
            Toast.makeText(this, "❌ 생성 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String readFileContent(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            reader.close();
            return line != null ? line.trim() : "";
        } catch (Exception e) {
            return "읽기 실패: " + e.getMessage();
        }
    }

    private void writeFile(File file, String content) throws Exception {
        FileWriter writer = new FileWriter(file);
        writer.write(content);
        writer.close();
    }
}