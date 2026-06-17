package com.tool.hookmodule;

import java.io.BufferedReader;
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

public class HookModule implements IXposedHookLoadPackage {

    private static final String TAG = "MasTk";
    public static boolean 日志开关_集 = true;
    private static final long DEDUP_CACHE_TIMEOUT = 5000;
    private final Map<String, Long> processedCache = new HashMap<>();

    // 淘宝尾缀：初始为 null，触发首次加载
    public static String 淘系_尾缀 = null;

    public static String 实际_平台 = null;
    public static String 实际_分身id = null;

    public static String 设备序列号_虚拟 = null;

    public static String 多多_尾缀 = "http://ma.132.tv:3079/api/rwb/action/item_upload?apikey=YOUR_API_KEY&uuid=YOUR_UUID&itemId=YOUR_ITEM_ID&username=YOUR_NAME&ids=YOUR_IDS&pt=拼多多&fs=0";
    public static String 测试内容 = null;









    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // ✅ 动态加载淘宝尾缀（仅一次）



        //log2(" 测试内容: " + 测试内容);
        // === 原有主逻辑 ===
        String packageName = lpparam.packageName;
        String processName = lpparam.processName;
        String apk路径= lpparam.appInfo.dataDir;
        实际_分身id = 文本_取出中间文本(apk路径,"/user/","/",null,true);

        设备序列号_虚拟=取设备序列号(实际_分身id);
        log2(" ========================================");
        log2(" 被调用 - 包名: " + packageName +"分身序号:"+实际_分身id+" 进程名: " + processName+"apk路径="+apk路径+"尾>"+ 淘系_尾缀);
        log2(" ========================================");
        log2("初始化",true);


        if (packageName.contains("taobao") || packageName.contains("pinduoduo")) {
            log2("发现目标相关应用: " + packageName+"分身id:"+实际_分身id);
        }


        if ("com.taobao.taobao".equals(packageName)||"com.taobao.litetao".equals(packageName)||"com.tmall.wireless".equals(packageName)) {
            淘系_尾缀 = 读取apk配置文件(packageName,实际_分身id);
            if ("com.taobao.taobao".equals(packageName)){
                实际_平台="淘宝";
            }
            if ("com.taobao.litetao".equals(packageName)){
                实际_平台="淘特";
            }
            if ("com.tmall.wireless".equals(packageName)){
                实际_平台="天猫";
            }

            if (HookModule.淘系_尾缀 == null) {
                //TAOBAO_尾缀 = 读取apk配置文件();
                if (HookModule.淘系_尾缀 == null || HookModule.淘系_尾缀.trim().isEmpty()) {
                    // 回退到默认值
                    HookModule.淘系_尾缀 = "apikey=ZV63UZRACUUIMF1X&uuid=3880296&itemId=663120907824&username=默认公司_2组&ids=testtaobao&pt=淘宝&fs=0";
                    log2(" ⚠️ 使用默认淘宝尾缀");
                } else {
                    log2(" ✅ 成功加载自定义淘宝尾缀");
                }
            }
            //String 测试内容A = 网页访问_GET("http://item.132.tv:3079/api/rwb/action=item_get?apikey=X8J9FCN9RGJKYCIR&uuid=8302820&itemId=1023801162324");
            // 测试内容=测试内容A;


            log2(" ✅ 开始处理淘系应用，进程: " + processName+"尾缀>"+ HookModule.淘系_尾缀 +"设备序列号>"+ 设备序列号_虚拟);
            hookTaobao(lpparam);
        } else if ("com.xunmeng.pinduoduo".equals(packageName)) {
            多多_尾缀 = 读取apk配置文件(packageName,实际_分身id);
            if (HookModule.多多_尾缀 == null) {
                //TAOBAO_尾缀 = 读取apk配置文件();
                if (HookModule.多多_尾缀 == null || HookModule.多多_尾缀.trim().isEmpty()) {
                    // 回退到默认值
                    HookModule.多多_尾缀 = "apikey=ZV63UZRACUUIMF1X&uuid=3880296&itemId=6631209804&username=默认公司_3组&ids=testduoduo&pt=拼多多&fs=0";
                    log2(" ⚠️ 使用默认多动尾缀");
                } else {
                    log2(" ✅ 成功加载自定义多多尾缀");
                }
            }
            log2(" ✅ 开始处理拼多多应用，进程: " + processName);
            hookPinduoduo(lpparam);
        } else {
            log2(" ❌ 非目标应用，跳过处理: " + packageName);
        }




//        String sConfigFilePath = null;
//        if (sConfigFilePath == null) {
//            XposedHelpers.findAndHookMethod("android.app.Application", lpparam.classLoader,
//                    "onCreate", new XC_MethodHook() {
//                        @Override
//                        protected void afterHookedMethod(MethodHookParam param) {
//                            String sConfigFilePat = "abc";
//                            if (sConfigFilePat == null) {
//                                Context context = (Context) param.thisObject;
//                                File externalFilesDir = context.getExternalFilesDir(null);
//                                if (externalFilesDir != null) {
//                                    sConfigFilePat = externalFilesDir.getParentFile().getParent()
//                                            + "/" + lpparam.packageName + "/files/pz.txt";
//                                    log2("📁 动态配置路径: " + sConfigFilePat);
//                                }
//                            }
//                        }
//                    });
//        }
//
//        log2("📁 te特别查看对的`~~~~: " + sConfigFilePath+lpparam.packageName);
//
//
    }











    // ==================== 淘宝 Hook ====================
    private void hookTaobao(LoadPackageParam lpparam) {
        String processName = lpparam.processName;
        log2(" 🎯 开始初始化淘宝Hook，当前进程: " + processName);

        // ✅ 动态拼接 URL（使用最新 TAOBAO_尾缀）

        final String targetUrl = "http://ma.132.tv:3079/api/rwb/action=item_upload?" + 淘系_尾缀 +"&sjpt="+实际_平台+"&sjfsid="+实际_分身id+"&sbxlhxn="+设备序列号_虚拟;
        log2(" ✅ 使用动态淘宝URL: " + targetUrl);

        try {
            log2(" 🔍 正在查找淘宝类: MtopResponse");
            Class<?> mtopResponseClass = XposedHelpers.findClass(
                    "mtopsdk.mtop.domain.MtopResponse",
                    lpparam.classLoader
            );
            //log2(" ✅ 成功找到淘宝L: " + mtopResponseClass.getName());

            //log2(" 🔧 正在Hook方法: MtopResponse.setBytedata");
            XposedHelpers.findAndHookMethod(mtopResponseClass,
                    "setBytedata",
                    byte[].class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                           // log2(" 📥 淘宝MtopResponse.setBytedata被调用");
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            //log2(" 📤 淘宝MtopResponse.setBytedata调用完成");

                            byte[] data = (byte[]) param.args[0];
                            if (data == null) {
                                log2(" ⚠️  data参数为null");
                                return;
                            }

                            if (data.length == 0) {
                                log2(" ⚠️  data参数为空数组");
                                return;
                            }

                            log2(" 📄 收到数据，长度: " + data.length + " 字节");

                            String responseText = new String(data, StandardCharsets.UTF_8);
                            int previewLength = Math.min(50, responseText.length());
                            log2(" 📄 数据前" + previewLength + "字符: " + responseText.substring(0, previewLength));

                            // 判断是否为目标接口
                            boolean containsDetailApi = responseText.contains("mtop.taobao.detail.data.get");
                            boolean containsGroupProps = responseText.contains("groupProps");
                            boolean containsRedirectUrl = responseText.contains("redirectUrl");
                            boolean containsDescApi = responseText.contains("mtop.taobao.detail.getdesc");
                            boolean containsError1 = responseText.contains("::哎哟喂");
                            boolean containsError2 = responseText.contains("被挤爆啦");

                            //log2(" 🔍 数据检查结果:");
                          //  log2("   - 包含detail.data.get接口: " + containsDetailApi);
                           // log2("   - 包含groupProps: " + containsGroupProps);
                           // log2("   - 包含redirectUrl: " + containsRedirectUrl);
                           // log2("   - 包含detail.getdesc接口: " + containsDescApi);
                           // log2("   - 包含错误'::哎哟喂': " + containsError1);
                           // log2("   - 包含错误'被挤爆啦': " + containsError2);

                            boolean isDetail = containsDetailApi && (containsGroupProps || containsRedirectUrl);
                            boolean isDesc = containsDescApi;
                            boolean isError = containsError1 || containsError2;

                            log2(" 🔍 综合结果:");
                            log2("   - 是商品详情数据: " + isDetail);
                            log2("   - 是商品描述数据: " + isDesc);
                            log2("   - 是错误数据: " + isError);

                            if (isDetail || isDesc || isError) {
                                log2(" ✅ 符合条件，检查去重");
                                if (!isDuplicate(responseText)) {
                                    log2(" 🚀 开始上传数据，长度: " + data.length);
                                    sendToServerAsync(responseText, targetUrl, "Xposed-Hook-Client");
                                } else {
                                    log2(" ⏭️ 数据重复，跳过上传");
                                }
                            } else {
                                log2(" ❌ 不符合条件，跳过处理");
                            }
                        }
                    });

            log2(" ✅ 淘宝Hook设置成功");
        } catch (Throwable t) {
            log2(" ❌ 淘宝Hook设置失败: " + t.getMessage());
            log2(" ❌ 异常详情: " + t.getClass().getName() + ": " + t.getMessage());
        }

        // 绕过安全检测
        try {
            log2(" 🔧 尝试绕过安全检测");
            bypassSecurityChecks(lpparam.classLoader);
        } catch (Throwable t) {
            log2(" ⚠️ 绕过安全检测失败: " + t.getMessage());
        }
    }

    // ==================== 拼多多 Hook ====================
    private void hookPinduoduo(LoadPackageParam lpparam) {
        String processName = lpparam.processName;
        log2(" 🎯 开始初始化拼多多Hook，当前进程: " + processName);
        final String 多多全网址 = "http://ma.132.tv:3079/api/rwb/action=item_upload?" + 多多_尾缀+"&sjpt="+实际_平台+"&sjfsid="+实际_分身id+"&sbxlhxn="+设备序列号_虚拟;
       // final String targetUrl = PINDUODUO_TARGET_URL;
        log2(" ✅ 使用硬编码拼多多URL: " + 多多全网址);

        try {
            log2(" 🔍正在查找Gson类: com.google.gson.Gson");
            Class<?> gsonClass = XposedHelpers.findClass(
                    "com.google.gson.Gson",
                    lpparam.classLoader
            );
            log2(" ✅成功找到Gson类: " + gsonClass.getName());

            log2(" 🔧 正在Hook方法: Gson.fromJson");
            XposedHelpers.findAndHookMethod(gsonClass,
                    "fromJson",
                    String.class,
                    java.lang.reflect.Type.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log2(" 📥 拼多多Gson.fromJson被调用");

                            String json = (String) param.args[0];
                            if (json == null) {
                                log2(" ⚠️  json参数为null");
                                return;
                            }

                            int jsonLength = json.length();
                            log2(" 📄 JSON长度: " + jsonLength + " 字符");

                            if (jsonLength < 1200) {
                                log2(" ⚠️  JSON长度小于1200，跳过处理");
                                return;
                            }

                            int previewLength = Math.min(200, jsonLength);
                            log2(" 📄 JSON前" + previewLength + "字符: " + json.substring(0, previewLength));

                            boolean containsQualityAssurance = json.contains("mall_quality_assurance.html");
                            boolean containsGoodsName = json.contains("goods_name");
                            boolean 是否为oka = json.contains("disable_avatar_jump");

                            log2(" 🔍 JSON检查结果:");
                            log2("   - 包含mall_quality_assurance.html: " + containsQualityAssurance);
                            log2("   - 包含goods_name: " + containsGoodsName);

                            if (containsQualityAssurance && containsGoodsName) {
                                log2(" ✅ 符合拼多多商品数据特征");
                                if (!isDuplicate(json)) {
                                    log2(" 🚀 开始上传拼多多数据，长度: " + jsonLength);
                                    sendToServerAsync(json, 多多全网址, "PDD-Hook-Client");
                                } else {
                                    log2(" ⏭️ 拼多多数据重复，跳过上传");
                                }
                            } else {
                                log2(" ❌ 不符合拼多多商品数据特征，跳过处理");
                            }
                        }

                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            log2(" 📤 拼多多Gson.fromJson调用完成");
                        }
                    });

            log2(" ✅ 拼多多Hook设置成功");
        } catch (Throwable t) {
            log2(" ❌ 拼多多Hook设置失败: " + t.getMessage());
            log2(" ❌ 异常详情: " + t.getClass().getName() + ": " + t.getMessage());
        }
    }

    // ==================== 工具方法 ====================
    private void sendToServerAsync(final String rawData, final String targetUrl, final String userAgent) {
        if (rawData == null) {
            log2(" ❌ 要上传的数据为null，跳过上传");
            return;
        }

        if (targetUrl == null) {
            log2(" ❌ 目标URL为null，跳过上传");
            return;
        }

        log2(" 🌐 开始异步发送数据到服务器");
        log2(" 目标URL: " + targetUrl);
        log2(" User-Agent: " + userAgent);
        log2(" 数据长度: " + rawData.length());

        int previewLength = Math.min(100, rawData.length());
        if (previewLength > 0) {
            log2(" 数据前" + previewLength + "字符: " + rawData.substring(0, previewLength));
        } else {
            log2(" 数据为空");
        }

        new Thread(() -> {
            String threadName = Thread.currentThread().getName();
            log2(" 📡 网络请求线程启动: " + threadName);

            HttpURLConnection conn = null;
            try {
                URL url = new URL(targetUrl);
                log2(" 🔗 建立连接: " + url);

                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("User-Agent", userAgent);

                log2(" 📤 发送请求头: Content-Type=" + conn.getRequestProperty("Content-Type"));
                log2(" 📤 发送请求头: User-Agent=" + conn.getRequestProperty("User-Agent"));

                long startTime = System.currentTimeMillis();
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] dataBytes = rawData.getBytes(StandardCharsets.UTF_8);
                    os.write(dataBytes);
                    os.flush();
                }
                long sendTime = System.currentTimeMillis() - startTime;
                log2(" ✅ 请求体发送完成，耗时: " + sendTime + "ms");

                int code = conn.getResponseCode();
                long responseTime = System.currentTimeMillis() - startTime;
                log2(" 📥 收到响应，状态码: " + code + "，总耗时: " + responseTime + "ms");

                if (code == 200) {
                    try (BufferedReader in = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = in.readLine()) != null) {
                            response.append(line);
                        }
                        String responseStr = response.toString();
                        log2(" ✅ 上传成功，服务器返回完整数据长度: " + responseStr.length());
                        log2(" ✅ 服务器返回数据: " + responseStr);
                    }
                } else {
                    log2(" ❌ 上传失败，HTTP状态码: " + code);
                    try (BufferedReader errorIn = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorIn.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        log2(" ❌ 错误响应: " + errorResponse.toString());
                    } catch (Exception e) {
                        log2(" ❌ 无法读取错误流: " + e.getMessage());
                    }
                }

                conn.disconnect();
                log2(" 🔌 连接已断开");
            } catch (Exception e) {
                log2(" ❌ 网络请求异常: " + e.getMessage());
                log2(" ❌ 异常详情: " + e.getClass().getName() + ": " + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                log2(" 🏁 网络请求线程结束: " + threadName);
            }
        }, "UploadThread-" + System.currentTimeMillis()).start();
    }

    private synchronized boolean isDuplicate(String content) {
        if (content == null) {
            log2(" ⚠️ 去重检查：内容为null，跳过检查");
            return false;
        }

        log2(" 🔍 开始检查数据去重");
        long now = System.currentTimeMillis();

        int beforeSize = processedCache.size();
        processedCache.entrySet().removeIf(entry -> now - entry.getValue() > DEDUP_CACHE_TIMEOUT);
        int afterSize = processedCache.size();
        log2(" 🧹 清理过期缓存: " + beforeSize + " -> " + afterSize + " 条");

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            String hashStr = hex.toString();

            log2(" 🔢 数据MD5哈希: " + hashStr);

            if (processedCache.containsKey(hashStr)) {
                long cacheTime = processedCache.get(hashStr);
                long timeDiff = now - cacheTime;
                log2(" ⏭️ 数据重复，跳过处理");
                log2(" ⏰ 缓存时间: " + cacheTime + "，当前时间: " + now + "，时间差: " + timeDiff + "ms");
                log2(" ⏰ 缓存剩余有效时间: " + (DEDUP_CACHE_TIMEOUT - timeDiff) + "ms");
                return true;
            }

            processedCache.put(hashStr, now);
            log2(" ✅ 新数据，添加到缓存");
            log2(" 📊 当前缓存大小: " + processedCache.size());
            return false;
        } catch (Exception e) {
            log2(" ❌ MD5计算失败: " + e.getMessage());
            return false;
        }
    }

    private void bypassSecurityChecks(ClassLoader loader) {
        log2(" 🔒 开始尝试绕过安全检测");

        try {
            XposedHelpers.findAndHookMethod(
                    "com.mobile.auth.gatewayauth.utils.security.CheckRoot",
                    loader,
                    "checkRootPathSU",
                    XC_MethodReplacement.returnConstant(false)
            );
            log2(" ✅ 成功Hook Root检测方法");
        } catch (Throwable t) {
            log2(" ⚠️ 未找到Root检测类: " + t.getMessage());
        }

        try {
            Class<?> checkHookClass = XposedHelpers.findClass(
                    "com.mobile.auth.gatewayauth.utils.security.CheckHook",
                    loader
            );
            XposedHelpers.findAndHookMethod(checkHookClass, "isHookByStack", XC_MethodReplacement.returnConstant(false));
            XposedHelpers.findAndHookMethod(checkHookClass, "isHookByJar", XC_MethodReplacement.returnConstant(false));
            log2(" ✅ 安全检测绕过成功");
        } catch (Throwable t) {
            log2(" ⚠️ 未找到Hook检测类: " + t.getMessage());
        }
    }

    // ===== 从 /sdcard/flash/taobao.txt 读取淘宝尾缀 =====
    private static String 读取apk配置文件(String 包名,String 分身名) {
        String filePath = "/storage/emulated/"+分身名+"/Android/data/"+包名+"/files/pz.txt";
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            log2(" ⚠️ 配置文件不存在: " + filePath);
            return null;
        }

        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.FileReader(file, StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line != null) {
                line = line.trim();
                log2(" ✅ 成功读取尾缀: " + line.substring(0, Math.min(50, line.length())) + "...");
                return line;
            }
        } catch (Exception e) {
            log2(" ❌ 读取配置失败: " + e.getMessage() +   filePath);
        }
        log2(" ❌ 读取配置返回NULL: " + e.getMessage() +   filePath);
        return null;
    }


    private static String 取设备序列号(String 分身名) {
        try {
            String serial = android.os.Build.SERIAL;
            if (serial == null || serial.isEmpty() || "unknown".equals(serial)) {
                serial = android.os.Build.ID;
                log2(" ⚠️ 获取设备序列号败：" +serial + 分身名);
            }
            return serial != null ? serial : "unknown";
        } catch (Exception e) {
            log2( " ⚠️ 获取设备序列号失败：" + e.getMessage());
            return "unknown";
        }
    }



    // ==================== 工具方法 ====================

    // 统一的日志方法，受开关控制
    private static void log2(String message) {
        if (日志开关_集) XposedBridge.log(TAG+message);
        }
    
    private static void log2(String message, boolean force) {
    if (force || 日志开关_集) XposedBridge.log(TAG+ message);
        }


        private static String 网页访问_GET(String 网址) {
        try {
            URL url = new URL(网址);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); // 5秒超时
            conn.setReadTimeout(10000);

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();

                return sb.toString();
            }
        } catch (Exception e) {
            // Xposed 日志
            XposedBridge.log("HTTP GET failed: " + e.getMessage());
        }
        return null;
    }
    private static String 网页访问_POST(String urlStr, String jsonBody) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setDoOutput(true);

            // 发送 body
            OutputStream os = conn.getOutputStream();
            os.write(jsonBody.getBytes("UTF-8"));
            os.flush();
            os.close();

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                conn.disconnect();
                return sb.toString();
            }
        } catch (Exception e) {
            XposedBridge.log("HTTP POST failed: " + e.getMessage());
        }
        return null;
    }

    private static String 取key数据() {
        return 网页访问_GET("http://item.132.tv:3079/api/rwb/action=item_get?apikey=X8J9FCN9RGJKYCIR&uuid=8302820&itemId=1023801162324");
    }



///  //////////文本处理
    public static String 文本_取右边(
            String 被查找的文本,
            String 欲寻找的文本,
            Integer 起始寻找位置,
            Boolean 是否不区分大小写) {

        // 容错处理
        if (被查找的文本 == null || 欲寻找的文本 == null || 欲寻找的文本.isEmpty()) {
            return "";
        }

        // 设置默认参数
        int 起始位置 = (起始寻找位置 != null) ? 起始寻找位置 : 1;
        boolean 忽略大小写 = (是否不区分大小写 != null) && 是否不区分大小写;

        String 主文本 = 被查找的文本;
        String 子文本 = 欲寻找的文本;

        if (忽略大小写) {
            主文本 = 主文本.toLowerCase();
            子文本 = 子文本.toLowerCase();
        }

        int pos = -1;

        if (起始位置 > 0) {
            // 从右往左查找（lastIndexOf）
            // 起始位置 1 表示从末尾开始，2 表示倒数第二个字符前...
            int fromIndex = Math.max(0, 主文本.length() - 起始位置);
            pos = 主文本.lastIndexOf(子文本, fromIndex + 子文本.length() - 1);
        } else {
            // 从左往右查找（indexOf）
            pos = 主文本.indexOf(子文本);
        }

        if (pos == -1) {
            return ""; // 未找到
        }

        // 返回匹配位置之后的文本（使用原字符串，非小写版）
        return 被查找的文本.substring(pos + 欲寻找的文本.length());
    }



    /**
     * 子程序名：文本_取出中间文本
     * 在全文本中，找出位于“前面文本”之后、“后面文本”之前的中间内容。
     *
     * 示例：文本_取出中间文本("12345", "2", "4", null, null) → 返回 "3"
     *
     * @param 欲取全文本         要处理的完整字符串，如 "12345"
     * @param 前面文本           起始标记，如 "2"
     * @param 后面文本           结束标记，如 "4"
     * @param 起始搜寻位置       可为 null，默认为 0（从头开始）
     * @param 是否不区分大小写   可为 null，默认为 false（区分大小写）
     * @return 找到则返回中间文本；否则返回 ""
     */
    public static String 文本_取出中间文本(
            String 欲取全文本,
            String 前面文本,
            String 后面文本,
            Integer 起始搜寻位置,
            Boolean 是否不区分大小写) {
        // 容错：任一关键参数为 null 或空，直接返回空
        if (欲取全文本 == null || 前面文本 == null || 后面文本 == null ||
                前面文本.isEmpty() || 后面文本.isEmpty()) {
            return "";
        }
        // 设置默认参数
        int 起始位置 = (起始搜寻位置 != null) ? Math.max(0, 起始搜寻位置) : 0;
        boolean 忽略大小写 = (是否不区分大小写 != null) && 是否不区分大小写;
        String 主文本 = 欲取全文本;
        String 前 = 前面文本;
        String 后 = 后面文本;
        if (忽略大小写) {
            主文本 = 主文本.toLowerCase();
            前 = 前.toLowerCase();
            后 = 后.toLowerCase();
        }
        // 第一步：查找“前面文本”的位置（从起始位置开始）
        int 前位置 = 主文本.indexOf(前, 起始位置);
        if (前位置 == -1) {
            return ""; // 未找到前面文本
        }
        // 第二步：从“前面文本”结束处开始，查找“后面文本”
        int 后开始搜索位置 = 前位置 + 前.length();
        int 后位置 = 主文本.indexOf(后, 后开始搜索位置);
        if (后位置 == -1) {
            return ""; // 未找到后面文本
        }
        // 第三步：截取中间内容（使用原始字符串，非小写版）
        return 欲取全文本.substring(前位置 + 前.length(), 后位置);
    }

}