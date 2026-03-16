package com.tool.hookmodule;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HookModule_测试 implements IXposedHookLoadPackage {

    private static final String TAG = "UnifiedHookModule";
    private static final long DEDUP_CACHE_TIMEOUT = 5000;
    private final Map<String, Long> processedCache = new HashMap<>();

    // 记录找到的有效配置目录
    private String effectiveConfigDir = null;
    // 标记是否已进行路径测试
    private boolean pathTestCompleted = false;

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        String processName = lpparam.processName;

        XposedBridge.log(TAG + " ========================================");
        XposedBridge.log(TAG + " 模块被调用 - 包名: " + packageName + ", 进程名: " + processName);
        XposedBridge.log(TAG + " ========================================");

        // 只在第一次遇到目标应用时运行路径测试
        if (!pathTestCompleted && ("com.taobao.taobao".equals(packageName) ||
                "com.xunmeng.pinduoduo".equals(packageName))) {
            XposedBridge.log(TAG + " 🧪 开始全面检测存储路径权限...");
            testAllStoragePaths(packageName);
            pathTestCompleted = true;
        }

        if ("com.taobao.taobao".equals(packageName)) {
            XposedBridge.log(TAG + " ✅ 开始处理淘宝应用，进程: " + processName);
            hookTaobao(lpparam);
        } else if ("com.xunmeng.pinduoduo".equals(packageName)) {
            XposedBridge.log(TAG + " ✅ 开始处理拼多多应用，进程: " + processName);
            hookPinduoduo(lpparam);
        } else {
            XposedBridge.log(TAG + " ❌ 非目标应用，跳过处理: " + packageName);
        }
    }

    /**
     * 全面检测所有可能的存储路径权限
     */
    private void testAllStoragePaths(String packageName) {
        XposedBridge.log(TAG + " 🧪 ======== 开始全面路径权限检测 ========");

        // 定义要测试的所有可能路径
        String[] allPossiblePaths = {
                // 外部存储路径
                "/sdcard/flash/",
                "/storage/emulated/0/flash/",
                "/mnt/sdcard/flash/",

                // 应用私有目录路径
                "/data/data/" + packageName + "/files/flash/",
                "/data/user/0/" + packageName + "/files/flash/",

                // 应用外部私有目录
                "/storage/emulated/0/Android/data/" + packageName + "/files/flash/",

                // 系统目录
                "/data/local/tmp/flash/",
                "/cache/flash/",

                // 其他可能的公共目录
                "/sdcard/",
                "/storage/emulated/0/",
                "/mnt/sdcard/",

                // Android 10+ 作用域存储可能的位置
                "/sdcard/Android/data/" + packageName + "/files/flash/",
                "/storage/emulated/0/Android/data/" + packageName + "/cache/flash/",

                // 可能的可写系统目录
                "/data/data/" + packageName + "/cache/flash/",
                "/data/user/0/" + packageName + "/cache/flash/"
        };

        int totalPaths = allPossiblePaths.length;
        int accessiblePaths = 0;

        XposedBridge.log(TAG + " 📊 总共需要测试 " + totalPaths + " 个路径");
        XposedBridge.log(TAG + " 🔄 开始逐个测试...");

        for (int i = 0; i < allPossiblePaths.length; i++) {
            String path = allPossiblePaths[i];
            XposedBridge.log(TAG + " --- 测试路径 " + (i+1) + "/" + totalPaths + " ---");

            boolean isAccessible = testSinglePath(path, packageName);
            if (isAccessible) {
                accessiblePaths++;
                XposedBridge.log(TAG + " ✅✅✅ 路径 " + path + " 测试通过");
            } else {
                XposedBridge.log(TAG + " ❌❌❌ 路径 " + path + " 测试失败");
            }

            XposedBridge.log(TAG + " ---------------------------------");
        }

        // 总结报告
        XposedBridge.log(TAG + " 📈 ======== 路径检测总结报告 ========");
        XposedBridge.log(TAG + " 📈 总共测试路径: " + totalPaths + " 个");
        XposedBridge.log(TAG + " ✅ 可访问路径: " + accessiblePaths + " 个");
        XposedBridge.log(TAG + " ❌ 不可访问路径: " + (totalPaths - accessiblePaths) + " 个");
        XposedBridge.log(TAG + " 📈 可访问率: " + (accessiblePaths * 100 / totalPaths) + "%");

        if (accessiblePaths > 0) {
            XposedBridge.log(TAG + " 💡 建议使用测试通过的路径作为配置目录");
        } else {
            XposedBridge.log(TAG + " ⚠️ 警告：没有找到任何可访问的路径！");
            XposedBridge.log(TAG + " 💡 建议检查应用存储权限或尝试手动设置权限");
        }

        XposedBridge.log(TAG + " 🧪 ======== 路径检测完成 ========");
    }

    /**
     * 测试单个路径的读写权限
     */
    private boolean testSinglePath(String dirPath, String packageName) {
        XposedBridge.log(TAG + "  测试路径: " + dirPath);

        try {
            File testDir = new File(dirPath);
            String testFileName = packageName + "_test.txt";
            File testFile = new File(dirPath, testFileName);

            // 1. 检查目录是否存在
            if (!testDir.exists()) {
                XposedBridge.log(TAG + "   目录不存在，尝试创建...");
                if (!testDir.mkdirs()) {
                    XposedBridge.log(TAG + "   ❌ 创建目录失败");
                    return false;
                }
                XposedBridge.log(TAG + "   ✅ 目录创建成功");
            } else {
                XposedBridge.log(TAG + "   ✅ 目录已存在");
            }

            // 检查目录权限
            XposedBridge.log(TAG + "   目录权限检查:");
            XposedBridge.log(TAG + "     - 可读: " + testDir.canRead());
            XposedBridge.log(TAG + "     - 可写: " + testDir.canWrite());
            XposedBridge.log(TAG + "     - 可执行: " + testDir.canExecute());

            // 2. 测试写入权限
            XposedBridge.log(TAG + "   测试文件写入...");
            try (FileWriter writer = new FileWriter(testFile)) {
                String testContent = "这是测试内容_" + System.currentTimeMillis();
                writer.write(testContent);
                writer.flush();
                XposedBridge.log(TAG + "   ✅ 文件写入成功");
                XposedBridge.log(TAG + "     写入内容: " + testContent);
            } catch (Exception e) {
                XposedBridge.log(TAG + "   ❌ 文件写入失败: " + e.getMessage());
                return false;
            }

            // 3. 测试读取权限
            XposedBridge.log(TAG + "   测试文件读取...");
            try (BufferedReader reader = new BufferedReader(new FileReader(testFile))) {
                String content = reader.readLine();
                XposedBridge.log(TAG + "   ✅ 文件读取成功");
                XposedBridge.log(TAG + "     读取内容: " + content);
            } catch (Exception e) {
                XposedBridge.log(TAG + "   ❌ 文件读取失败: " + e.getMessage());

                // 尝试删除可能损坏的文件
                try {
                    testFile.delete();
                } catch (Exception ignored) {}
                return false;
            }

            // 检查文件权限
            XposedBridge.log(TAG + "   文件权限检查:");
            XposedBridge.log(TAG + "     - 可读: " + testFile.canRead());
            XposedBridge.log(TAG + "     - 可写: " + testFile.canWrite());
            XposedBridge.log(TAG + "     - 可执行: " + testFile.canExecute());
            XposedBridge.log(TAG + "     - 文件大小: " + testFile.length() + " 字节");

            // 4. 测试删除权限
            XposedBridge.log(TAG + "   测试文件删除...");
            if (testFile.delete()) {
                XposedBridge.log(TAG + "   ✅ 文件删除成功");
            } else {
                XposedBridge.log(TAG + "   ⚠️ 文件删除失败，但读写测试已通过");
            }

            // 5. 额外检查：尝试列出目录内容
            XposedBridge.log(TAG + "   测试列出目录内容...");
            String[] files = testDir.list();
            if (files != null) {
                XposedBridge.log(TAG + "   ✅ 可列出目录，包含 " + files.length + " 个文件/目录");
            } else {
                XposedBridge.log(TAG + "   ⚠️ 无法列出目录内容");
            }

            return true;

        } catch (Exception e) {
            XposedBridge.log(TAG + "   ❌ 测试过程异常: " + e.getMessage());
            return false;
        }
    }

    /**
     * 从配置文件读取URL
     */
    private String getTargetUrl(String packageName) {
        XposedBridge.log(TAG + " 🔍 开始读取配置文件，包名: " + packageName);

        // 首先测试各个目录的可读写性
        if (effectiveConfigDir == null) {
            XposedBridge.log(TAG + " 🧪 开始寻找可用的配置目录...");

            // 定义多个可能的工作目录
            String[] possibleDirs = {
                    "/sdcard/flash/",                    // 外部存储
                    "/storage/emulated/0/flash/",        // 另一种外部存储路径
                    "/mnt/sdcard/flash/",                // 旧设备可能用这个
                    "HookModule",  // 应用私有目录
                    "/data/user/0/" + packageName + "/files/flash/", // 多用户环境
                    "/storage/emulated/0/Android/data/" + packageName + "/files/flash/", // 应用外部私有目录
                    "/data/local/tmp/flash/",            // 系统临时目录
                    "/cache/flash/"                      // 缓存目录
            };

            for (String dirPath : possibleDirs) {
                if (testSinglePath(dirPath, packageName)) {
                    effectiveConfigDir = dirPath;
                    XposedBridge.log(TAG + " 🎯 找到有效的配置目录: " + effectiveConfigDir);
                    break;
                }
            }

            if (effectiveConfigDir == null) {
                XposedBridge.log(TAG + " ❌ 未找到任何可用的配置目录");
                return null;
            }
        } else {
            XposedBridge.log(TAG + " 📁 使用之前找到的有效目录: " + effectiveConfigDir);
        }

        String configFileName = packageName + ".txt";
        File configFile = new File(effectiveConfigDir, configFileName);
        String filePath = configFile.getAbsolutePath();

        XposedBridge.log(TAG + " 📂 最终配置文件路径: " + filePath);
        XposedBridge.log(TAG + " 文件是否存在: " + configFile.exists());
        XposedBridge.log(TAG + " 文件大小: " + (configFile.exists() ? configFile.length() + " 字节" : "文件不存在"));

        if (!configFile.exists()) {
            XposedBridge.log(TAG + " ❌ 配置文件不存在: " + filePath);
            XposedBridge.log(TAG + " ℹ️ 请创建配置文件: " + filePath);
            XposedBridge.log(TAG + " ℹ️ 配置文件内容示例: apikey=xxx&uuid=xxx&itemId=xxx&username=xxx&ids=xxx&pt=淘宝&fs=0");
            return null;
        }

        try {
            // 检查文件权限
            XposedBridge.log(TAG + " 文件可读: " + configFile.canRead());
            XposedBridge.log(TAG + " 文件可写: " + configFile.canWrite());
            XposedBridge.log(TAG + " 文件可执行: " + configFile.canExecute());

            if (!configFile.canRead()) {
                XposedBridge.log(TAG + " ❌ 配置文件不可读");

                // 尝试修复权限
                XposedBridge.log(TAG + " ⚙️ 尝试修复文件权限...");
                try {
                    String[] cmd = {"su", "-c", "chmod 644 " + filePath};
                    Process process = Runtime.getRuntime().exec(cmd);
                    int result = process.waitFor();
                    XposedBridge.log(TAG + " 权限修复结果: " + result);

                    if (configFile.canRead()) {
                        XposedBridge.log(TAG + " ✅ 权限修复成功，现在可以读取");
                    } else {
                        XposedBridge.log(TAG + " ❌ 权限修复失败");
                        return null;
                    }
                } catch (Exception e) {
                    XposedBridge.log(TAG + " ❌ 权限修复异常: " + e.getMessage());
                    return null;
                }
            }

            BufferedReader reader = new BufferedReader(new FileReader(configFile));
            String params = reader.readLine();
            reader.close();

            XposedBridge.log(TAG + " 读取配置文件内容: " + (params != null ? params : "null"));

            if (params == null) {
                XposedBridge.log(TAG + " ❌ 配置文件内容为null");
                return null;
            }

            String trimmedParams = params.trim();
            if (trimmedParams.isEmpty()) {
                XposedBridge.log(TAG + " ❌ 配置文件内容为空（只有空格或空行）");
                return null;
            }

            String fullUrl = "http://ma.132.tv:3079/api/rwb/action=item_upload?" + trimmedParams;
            XposedBridge.log(TAG + " ✅ 配置文件加载成功");
            XposedBridge.log(TAG + " 完整URL: " + fullUrl);

            // 验证URL格式
            try {
                new URL(fullUrl);
                XposedBridge.log(TAG + " ✅ URL格式验证通过");
            } catch (Exception e) {
                XposedBridge.log(TAG + " ❌ URL格式错误: " + e.getMessage());
                return null;
            }

            return fullUrl;
        } catch (Exception e) {
            XposedBridge.log(TAG + " ❌ 读取配置文件失败: " + e.getMessage());
            XposedBridge.log(TAG + " ❌ 异常详情: " + e.getClass().getName() + ": " + e.getMessage());
            return null;
        }
    }

    // ==================== 淘宝 Hook ====================
    private void hookTaobao(LoadPackageParam lpparam) {
        String processName = lpparam.processName;
        XposedBridge.log(TAG + " 🎯 开始初始化淘宝Hook，当前进程: " + processName);

        final String targetUrl = getTargetUrl("com.taobao.taobao");
        if (targetUrl == null) {
            XposedBridge.log(TAG + " ❌ 淘宝Hook初始化失败：未找到配置文件，跳过Hook");
            return;
        }

        XposedBridge.log(TAG + " ✅ 淘宝配置文件加载成功，URL: " + targetUrl);

        try {
            XposedBridge.log(TAG + " 🔍 正在查找淘宝类: mtopsdk.mtop.domain.MtopResponse");
            Class<?> mtopResponseClass = XposedHelpers.findClass(
                    "mtopsdk.mtop.domain.MtopResponse",
                    lpparam.classLoader
            );
            XposedBridge.log(TAG + " ✅ 成功找到淘宝类: " + mtopResponseClass.getName());

            XposedBridge.log(TAG + " 🔧 正在Hook方法: MtopResponse.setBytedata");
            XposedHelpers.findAndHookMethod(mtopResponseClass,
                    "setBytedata",
                    byte[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + " 📥 淘宝MtopResponse.setBytedata被调用");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + " 📤 淘宝MtopResponse.setBytedata调用完成");

                            byte[] data = (byte[]) param.args[0];
                            if (data == null) {
                                XposedBridge.log(TAG + " ⚠️  data参数为null");
                                return;
                            }

                            if (data.length == 0) {
                                XposedBridge.log(TAG + " ⚠️  data参数为空数组");
                                return;
                            }

                            XposedBridge.log(TAG + " 📄 收到数据，长度: " + data.length + " 字节");

                            String responseText = new String(data, StandardCharsets.UTF_8);
                            int previewLength = Math.min(500, responseText.length());
                            XposedBridge.log(TAG + " 📄 数据前" + previewLength + "字符: " + responseText.substring(0, previewLength));

                            // 判断是否为目标接口
                            boolean containsDetailApi = responseText.contains("mtop.taobao.detail.data.get");
                            boolean containsGroupProps = responseText.contains("groupProps");
                            boolean containsRedirectUrl = responseText.contains("redirectUrl");
                            boolean containsDescApi = responseText.contains("mtop.taobao.detail.getdesc");
                            boolean containsError1 = responseText.contains("::哎哟喂");
                            boolean containsError2 = responseText.contains("被挤爆啦");

                            XposedBridge.log(TAG + " 🔍 数据检查结果:");
                            XposedBridge.log(TAG + "   - 包含detail.data.get接口: " + containsDetailApi);
                            XposedBridge.log(TAG + "   - 包含groupProps: " + containsGroupProps);
                            XposedBridge.log(TAG + "   - 包含redirectUrl: " + containsRedirectUrl);
                            XposedBridge.log(TAG + "   - 包含detail.getdesc接口: " + containsDescApi);
                            XposedBridge.log(TAG + "   - 包含错误'::哎哟喂': " + containsError1);
                            XposedBridge.log(TAG + "   - 包含错误'被挤爆啦': " + containsError2);

                            boolean isDetail = containsDetailApi && (containsGroupProps || containsRedirectUrl);
                            boolean isDesc = containsDescApi;
                            boolean isError = containsError1 || containsError2;

                            XposedBridge.log(TAG + " 🔍 综合结果:");
                            XposedBridge.log(TAG + "   - 是商品详情数据: " + isDetail);
                            XposedBridge.log(TAG + "   - 是商品描述数据: " + isDesc);
                            XposedBridge.log(TAG + "   - 是错误数据: " + isError);

                            if (isDetail || isDesc || isError) {
                                XposedBridge.log(TAG + " ✅ 符合条件，检查去重");
                                if (!isDuplicate(responseText)) {
                                    XposedBridge.log(TAG + " 🚀 开始上传数据，长度: " + data.length);
                                    sendToServerAsync(responseText, targetUrl, "Xposed-Hook-Client");
                                } else {
                                    XposedBridge.log(TAG + " ⏭️ 数据重复，跳过上传");
                                }
                            } else {
                                XposedBridge.log(TAG + " ❌ 不符合条件，跳过处理");
                            }
                        }
                    });

            XposedBridge.log(TAG + " ✅ 淘宝Hook设置成功");
        } catch (Throwable t) {
            XposedBridge.log(TAG + " ❌ 淘宝Hook设置失败: " + t.getMessage());
            XposedBridge.log(TAG + " ❌ 异常详情: " + t.getClass().getName() + ": " + t.getMessage());
        }

        // 绕过安全检测
        try {
            XposedBridge.log(TAG + " 🔧 尝试绕过安全检测");
            bypassSecurityChecks(lpparam.classLoader);
        } catch (Throwable t) {
            XposedBridge.log(TAG + " ⚠️ 绕过安全检测失败: " + t.getMessage());
        }
    }

    // ==================== 拼多多 Hook ====================
    private void hookPinduoduo(LoadPackageParam lpparam) {
        String processName = lpparam.processName;
        XposedBridge.log(TAG + " 🎯 开始初始化拼多多Hook，当前进程: " + processName);

        final String targetUrl = getTargetUrl("com.xunmeng.pinduoduo");
        if (targetUrl == null) {
            XposedBridge.log(TAG + " ❌ 拼多多Hook初始化失败：未找到配置文件，跳过Hook");
            return;
        }

        XposedBridge.log(TAG + " ✅ 拼多多配置文件加载成功，URL: " + targetUrl);

        try {
            XposedBridge.log(TAG + " 🔍 正在查找Gson类: com.google.gson.Gson");
            Class<?> gsonClass = XposedHelpers.findClass(
                    "com.google.gson.Gson",
                    lpparam.classLoader
            );
            XposedBridge.log(TAG + " ✅ 成功找到Gson类: " + gsonClass.getName());

            XposedBridge.log(TAG + " 🔧 正在Hook方法: Gson.fromJson");
            XposedHelpers.findAndHookMethod(gsonClass,
                    "fromJson",
                    String.class,
                    java.lang.reflect.Type.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + " 📥 拼多多Gson.fromJson被调用");

                            String json = (String) param.args[0];
                            if (json == null) {
                                XposedBridge.log(TAG + " ⚠️  json参数为null");
                                return;
                            }

                            int jsonLength = json.length();
                            XposedBridge.log(TAG + " 📄 JSON长度: " + jsonLength + " 字符");

                            if (jsonLength < 2000) {
                                XposedBridge.log(TAG + " ⚠️  JSON长度小于2000，跳过处理");
                                return;
                            }

                            int previewLength = Math.min(200, jsonLength);
                            XposedBridge.log(TAG + " 📄 JSON前" + previewLength + "字符: " + json.substring(0, previewLength));

                            boolean containsQualityAssurance = json.contains("mall_quality_assurance.html");
                            boolean containsGoodsName = json.contains("goods_name");

                            XposedBridge.log(TAG + " 🔍 JSON检查结果:");
                            XposedBridge.log(TAG + "   - 包含mall_quality_assurance.html: " + containsQualityAssurance);
                            XposedBridge.log(TAG + "   - 包含goods_name: " + containsGoodsName);

                            if (containsQualityAssurance && containsGoodsName) {
                                XposedBridge.log(TAG + " ✅ 符合拼多多商品数据特征");
                                if (!isDuplicate(json)) {
                                    XposedBridge.log(TAG + " 🚀 开始上传拼多多数据，长度: " + jsonLength);
                                    sendToServerAsync(json, targetUrl, "PDD-Hook-Client");
                                } else {
                                    XposedBridge.log(TAG + " ⏭️ 拼多多数据重复，跳过上传");
                                }
                            } else {
                                XposedBridge.log(TAG + " ❌ 不符合拼多多商品数据特征，跳过处理");
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            XposedBridge.log(TAG + " 📤 拼多多Gson.fromJson调用完成");
                        }
                    });

            XposedBridge.log(TAG + " ✅ 拼多多Hook设置成功");
        } catch (Throwable t) {
            XposedBridge.log(TAG + " ❌ 拼多多Hook设置失败: " + t.getMessage());
            XposedBridge.log(TAG + " ❌ 异常详情: " + t.getClass().getName() + ": " + t.getMessage());
        }
    }

    // ==================== 工具方法 ====================
    private void sendToServerAsync(final String rawData, final String targetUrl, final String userAgent) {
        XposedBridge.log(TAG + " 🌐 开始异步发送数据到服务器");
        XposedBridge.log(TAG + " 目标URL: " + targetUrl);
        XposedBridge.log(TAG + " User-Agent: " + userAgent);
        XposedBridge.log(TAG + " 数据长度: " + rawData.length());
        int previewLength = Math.min(100, rawData.length());
        XposedBridge.log(TAG + " 数据前" + previewLength + "字符: " + rawData.substring(0, previewLength));

        new Thread(() -> {
            String threadName = Thread.currentThread().getName();
            XposedBridge.log(TAG + " 📡 网络请求线程启动: " + threadName);

            HttpURLConnection conn = null;
            try {
                URL url = new URL(targetUrl);
                XposedBridge.log(TAG + " 🔗 建立连接: " + url);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("User-Agent", userAgent);

                XposedBridge.log(TAG + " 📤 发送请求头: Content-Type=" + conn.getRequestProperty("Content-Type"));
                XposedBridge.log(TAG + " 📤 发送请求头: User-Agent=" + conn.getRequestProperty("User-Agent"));

                // 发送数据
                XposedBridge.log(TAG + " 📤 开始发送请求体数据");
                long startTime = System.currentTimeMillis();
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] dataBytes = rawData.getBytes(StandardCharsets.UTF_8);
                    os.write(dataBytes);
                    os.flush();
                }
                long sendTime = System.currentTimeMillis() - startTime;
                XposedBridge.log(TAG + " ✅ 请求体发送完成，耗时: " + sendTime + "ms");

                int code = conn.getResponseCode();
                long responseTime = System.currentTimeMillis() - startTime;
                XposedBridge.log(TAG + " 📥 收到响应，状态码: " + code + "，总耗时: " + responseTime + "ms");

                if (code == 200) {
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }

                        String responseStr = response.toString();
                        XposedBridge.log(TAG + " ✅ 上传成功，服务器返回完整数据长度: " + responseStr.length());
                        XposedBridge.log(TAG + " ✅ 服务器返回数据: " + responseStr);
                    }
                } else {
                    XposedBridge.log(TAG + " ❌ 上传失败，HTTP状态码: " + code);

                    // 尝试读取错误流
                    try (BufferedReader errorIn = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorIn.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        XposedBridge.log(TAG + " ❌ 错误响应: " + errorResponse.toString());
                    } catch (Exception e) {
                        XposedBridge.log(TAG + " ❌ 无法读取错误流: " + e.getMessage());
                    }
                }

                conn.disconnect();
                XposedBridge.log(TAG + " 🔌 连接已断开");
            } catch (Exception e) {
                XposedBridge.log(TAG + " ❌ 网络请求异常: " + e.getMessage());
                XposedBridge.log(TAG + " ❌ 异常详情: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                XposedBridge.log(TAG + " 🏁 网络请求线程结束: " + threadName);
            }
        }, "UploadThread-" + System.currentTimeMillis()).start();
    }

    private synchronized boolean isDuplicate(String content) {
        XposedBridge.log(TAG + " 🔍 开始检查数据去重");
        long now = System.currentTimeMillis();

        // 清理过期缓存
        int beforeSize = processedCache.size();
        processedCache.entrySet().removeIf(entry -> now - entry.getValue() > DEDUP_CACHE_TIMEOUT);
        int afterSize = processedCache.size();
        XposedBridge.log(TAG + " 🧹 清理过期缓存: " + beforeSize + " -> " + afterSize + " 条");

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            String hashStr = hex.toString();

            XposedBridge.log(TAG + " 🔢 数据MD5哈希: " + hashStr);
            XposedBridge.log(TAG + " 🔢 哈希长度: " + hashStr.length());

            if (processedCache.containsKey(hashStr)) {
                long cacheTime = processedCache.get(hashStr);
                long timeDiff = now - cacheTime;
                XposedBridge.log(TAG + " ⏭️ 数据重复，跳过处理");
                XposedBridge.log(TAG + " ⏰ 缓存时间: " + cacheTime + "，当前时间: " + now + "，时间差: " + timeDiff + "ms");
                XposedBridge.log(TAG + " ⏰ 缓存剩余有效时间: " + (DEDUP_CACHE_TIMEOUT - timeDiff) + "ms");
                return true;
            }

            processedCache.put(hashStr, now);
            XposedBridge.log(TAG + " ✅ 新数据，添加到缓存");
            XposedBridge.log(TAG + " 📊 当前缓存大小: " + processedCache.size());
            return false;
        } catch (Exception e) {
            XposedBridge.log(TAG + " ❌ MD5计算失败: " + e.getMessage());
            return false;
        }
    }

    private void bypassSecurityChecks(ClassLoader loader) {
        XposedBridge.log(TAG + " 🔒 开始尝试绕过安全检测");

        try {
            XposedBridge.log(TAG + " 🔍 查找类: com.mobile.auth.gatewayauth.utils.security.CheckRoot");
            // 绕过Root检测
            XposedHelpers.findAndHookMethod(
                    "com.mobile.auth.gatewayauth.utils.security.CheckRoot",
                    loader,
                    "checkRootPathSU",
                    XC_MethodReplacement.returnConstant(false)
            );
            XposedBridge.log(TAG + " ✅ 成功Hook Root检测方法");
        } catch (Throwable t) {
            XposedBridge.log(TAG + " ⚠️ 未找到Root检测类: " + t.getMessage());
        }

        try {
            XposedBridge.log(TAG + " 🔍 查找类: com.mobile.auth.gatewayauth.utils.security.CheckHook");
            // 绕过Hook检测
            Class<?> checkHookClass = XposedHelpers.findClass(
                    "com.mobile.auth.gatewayauth.utils.security.CheckHook",
                    loader
            );
            XposedBridge.log(TAG + " ✅ 找到Hook检测类: " + checkHookClass.getName());

            XposedHelpers.findAndHookMethod(checkHookClass,
                    "isHookByStack",
                    XC_MethodReplacement.returnConstant(false));
            XposedBridge.log(TAG + " ✅ 成功Hook isHookByStack方法");

            XposedHelpers.findAndHookMethod(checkHookClass,
                    "isHookByJar",
                    XC_MethodReplacement.returnConstant(false));
            XposedBridge.log(TAG + " ✅ 成功Hook isHookByJar方法");

            XposedBridge.log(TAG + " ✅ 安全检测绕过成功");
        } catch (Throwable t) {
            XposedBridge.log(TAG + " ⚠️ 未找到Hook检测类: " + t.getMessage());
        }
    }
}