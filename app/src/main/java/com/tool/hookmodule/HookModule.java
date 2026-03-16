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

    private static final String TAG = "UnifiedHookModule";
    private static final long DEDUP_CACHE_TIMEOUT = 5000;
    private final Map<String, Long> processedCache = new HashMap<>();

    // 淘宝的固定URL
    public static  String TAOBAO_尾缀 = "apikey=ZV63UZRACUUIMF1X&uuid=3880294&itemId=663120907804&username=芝麻公司_2组&ids=ksdhfkd&pt=淘宝&fs=0";
    public static  String TAOBAO_TARGET_URL = "http://ma.132.tv:3079/api/rwb/action=item_upload?"+TAOBAO_尾缀;


    // 拼多多的固定URL
    private static final String PINDUODUO_TARGET_URL = "http://ma.132.tv:3079/api/rwb/action=item_upload?apikey=YOUR_API_KEY&uuid=YOUR_UUID&itemId=YOUR_ITEM_ID&username=YOUR_NAME&ids=YOUR_IDS&pt=拼多多&fs=0";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        String packageName = lpparam.packageName;
        String processName = lpparam.processName;

        XposedBridge.log(TAG + " ========================================");
        XposedBridge.log(TAG + " 模块被调用 - 包名: " + packageName + ", 进程名: " + processName);
        XposedBridge.log(TAG + " ========================================");

        if (packageName.contains("taobao") || packageName.contains("pinduoduo")) {
            XposedBridge.log(TAG + " 发现目标相关应用: " + packageName);
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

    // ==================== 淘宝 Hook ====================
    private void hookTaobao(LoadPackageParam lpparam) {
        String processName = lpparam.processName;
        XposedBridge.log(TAG + " 🎯 开始初始化淘宝Hook，当前进程: " + processName);

        final String targetUrl = TAOBAO_TARGET_URL;
        XposedBridge.log(TAG + " ✅ 使用硬编码淘宝URL: " + targetUrl);

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

        final String targetUrl = PINDUODUO_TARGET_URL;
        XposedBridge.log(TAG + " ✅ 使用硬编码拼多多URL: " + targetUrl);

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
        // 添加空值检查
        if (rawData == null) {
            XposedBridge.log(TAG + " ❌ 要上传的数据为null，跳过上传");
            return;
        }

        if (targetUrl == null) {
            XposedBridge.log(TAG + " ❌ 目标URL为null，跳过上传");
            return;
        }

        XposedBridge.log(TAG + " 🌐 开始异步发送数据到服务器");
        XposedBridge.log(TAG + " 目标URL: " + targetUrl);
        XposedBridge.log(TAG + " User-Agent: " + userAgent);
        XposedBridge.log(TAG + " 数据长度: " + rawData.length());

        int previewLength = Math.min(100, rawData.length());
        if (previewLength > 0) {
            XposedBridge.log(TAG + " 数据前" + previewLength + "字符: " + rawData.substring(0, previewLength));
        } else {
            XposedBridge.log(TAG + " 数据为空");
        }

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
        if (content == null) {
            XposedBridge.log(TAG + " ⚠️ 去重检查：内容为null，跳过检查");
            return false;
        }

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