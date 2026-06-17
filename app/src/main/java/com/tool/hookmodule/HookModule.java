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
    public static String 软件版本 = "3.7";
    private static final String TAG = "MasTk";
    public static boolean 日志开关_集 = false;
    private static final long DEDUP_CACHE_TIMEOUT = 5000;
    private final Map<String, Long> processedCache = new HashMap<>();
    // 淘宝尾缀：初始为 null，触发首次加载
    public static String 淘系_尾缀 = null;

    public static String 实际_平台 = null;
    public static String 实际_分身id = null;
    public static String 设备序列号_虚拟 = null;
    public static String 多多_尾缀 = "http://ma.132.tv:3079/api/rwb/action=item_upload?apikey=YOUR_API_KEY&uuid=YOUR_UUID&itemId=YOUR_ITEM_ID&username=YOUR_NAME&ids=YOUR_IDS&pt=拼多多&fs=0";
    public static String 测试内容 = null;
    public static String 多多账号名_集 = null;


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
        log2("  " + packageName +"分身序号:"+实际_分身id+" 进程名: " + processName+"apk路径="+apk路径+"尾>"+ 淘系_尾缀);
        log2(" ========================================");
        log2("初始化"+软件版本+">"+日志开关_集,true);


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
            if (!"com.xunmeng.pinduoduo".equals(lpparam.processName)) {
                // 如果不是主进程，直接返回，不执行后面的逻辑
                log2(" ⚠️ 非主进程A>"+processName,true);
                return;
            }
            log2(" ⚠️ 主进程B>"+processName,true);

            多多_尾缀 = 读取apk配置文件(packageName,实际_分身id);
            if (HookModule.多多_尾缀 == null) {
                //TAOBAO_尾缀 = 读取apk配置文件();
                if (HookModule.多多_尾缀 == null || HookModule.多多_尾缀.trim().isEmpty()) {
                    // 回退到默认值
                    HookModule.多多_尾缀 = "apikey=ZV63UZRACUIMF1X&uuid=3880296&itemId=6631209804&username=默认公司_3组&ids=testduoduo&pt=拼多多&fs=0";

                    log2(" ⚠️ 使用默认加载",true);
                } else {
                    log2(" ✅ 成功加载DD",true);
                    HookModule.多多账号名_集= 文本_取出中间文本(HookModule.多多_尾缀,"username=","&",null,true);
                }
            }
            String 模式选择 = "新版本";
            if (多多_尾缀 != null && 多多_尾缀.indexOf("芝麻公司") != -1){
                模式选择 = "老版本";
                log2(" ✅老版本", true);
            } else {
                模式选择 = "新版本";
                 log2(" ❌ 新版本: ", true);
            }

            纯静默检测敏感类(lpparam);
            独立模块_测试泰坦网络拦截(lpparam);
            独立模块_屏蔽WiFi网络关联探测(lpparam);

            log2(" ✅ 开始处理拼多多应用，进程: " + processName);
            if ("老版本".equals(模式选择)) {
                 独立模块_解密Titan网络响应为明文(lpparam);

             }
             else{hookPinduoduo(lpparam);}

            //
        } else {
           // log2(" ❌ 非目标应用，跳过处理b: " + packageName);
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




    // =================================================================
// 🎯 [完全独立测试模块 - 升级闭环版] 泰坦长连接双向网络数据流监控引擎
// =================================================================
    private void 独立模块_测试泰坦网络拦截(LoadPackageParam lpparam) {
        // 🌟 在内部完成闭环静默探测，不再依赖外部变量
        boolean 内部检测_是否存在TitanApiCall = false;
        boolean 内部检测_是否存在混淆类_j = false;

        // 1. 探测核心网络类
        try {
            XposedHelpers.findClass("com.xunmeng.basiccomponent.titan.api.TitanApiCall", lpparam.classLoader);
            内部检测_是否存在TitanApiCall = true;
        } catch (Throwable 忽略) {
            // 静默捕获，不在系统日志中报错
        }

        // 2. 探测核心解压混淆类
        try {
            XposedHelpers.findClass("e.t.y.y1.n.j", lpparam.classLoader);
            内部检测_是否存在混淆类_j = true;
        } catch (Throwable 忽略) {}

        // 3. 安全前置检查：如果没有检测到 TitanApiCall 类，说明当前不是网络子进程，直接默默退出
        if (!内部检测_是否存在TitanApiCall) {
            return;
        }

        try {
            log2(" 🧪 [独立模块] 检测到网络进程，泰坦网络长连接监控启动...", true);

            // -----------------------------------------------------------------
            // 【A 通道】 拦截请求发送 (对应 Frida 中的 TitanApiCall.b)
            // -----------------------------------------------------------------
            XposedHelpers.findAndHookMethod(
                    "com.xunmeng.basiccomponent.titan.api.TitanApiCall",
                    lpparam.classLoader,
                    "b",
                    Object.class,         // request 对象
                    Object.class,         // callback
                    java.util.Map.class,  // map
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log2("\n==================== 📡 TITAN REQUEST ====================", true);
                            Object requestObj = param.args[0];
                            if (requestObj != null) {
                                try {
                                    Object urlVal = XposedHelpers.getObjectField(requestObj, "url");
                                    log2("[URL]    : " + (urlVal != null ? urlVal.toString() : "null"));
                                } catch (Throwable ignored) {}

                                try {
                                    Object methodVal = XposedHelpers.getObjectField(requestObj, "method");
                                    log2("[Method] : " + (methodVal != null ? methodVal.toString() : "null"));
                                } catch (Throwable ignored) {}

                                try {
                                    byte[] bodyBytes = (byte[]) XposedHelpers.callMethod(requestObj, "getBodyBytes");
                                    if (bodyBytes != null && bodyBytes.length > 0) {
                                        log2("[Request Body]: " + new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8));
                                    }
                                } catch (Throwable ignored) {}
                            }
                        }
                    }
            );

            // -----------------------------------------------------------------
            // 【B 通道】 拦截响应回调 (对应 Frida 中的 TitanApiCall$a.onResponse)
            // -----------------------------------------------------------------
            // 声明为 final 确保内部类可以正常访问
            final boolean 可靠解压标志 = 内部检测_是否存在混淆类_j;
            XposedHelpers.findAndHookMethod(
                    "com.xunmeng.basiccomponent.titan.api.TitanApiCall$a",
                    lpparam.classLoader,
                    "onResponse",
                    int.class,     // errorCode
                    String.class,  // errorMsg
                    Object.class,  // response 对象
                    int.class,     // i3
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            log2("\n******************** 📥 TITAN RESPONSE ********************", true);
                            Object responseObj = param.args[2]; // 第三个参数是 response

                            if (responseObj != null) {
                                try {
                                    byte[] bodyBytes = (byte[]) XposedHelpers.callMethod(responseObj, "getBodyBytes");
                                    if (bodyBytes != null && bodyBytes.length > 0) {

                                        boolean isGzip = XposedHelpers.getBooleanField(responseObj, "isGizpCompressed");
                                        String responseData = "";

                                        // 如果被 Gzip 压缩了，且之前探测发现混淆解压工具类存在，则实施强行解压
                                        if (isGzip && 可靠解压标志) {
                                            try {
                                                byte[] uncompressed = (byte[]) XposedHelpers.callStaticMethod(
                                                        XposedHelpers.findClass("e.t.y.y1.n.j", lpparam.classLoader),
                                                        "a",
                                                        (Object) bodyBytes
                                                );
                                                if (uncompressed != null) {
                                                    responseData = new String(uncompressed, java.nio.charset.StandardCharsets.UTF_8);
                                                    log2("[Content]: (Gzip Uncompressed JSON)", true);
                                                }
                                            } catch (Throwable gzipError) {
                                                responseData = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                                            }
                                        } else {
                                            responseData = new String(bodyBytes, java.nio.charset.StandardCharsets.UTF_8);
                                        }

                                        log2(responseData, true);
                                    }
                                } catch (Throwable ignored) {}
                            } else {
                                log2("[Content]: Empty Response", true);
                            }
                            log2("*******************************************************\n", true);
                        }
                    }
            );

            log2(" ✅ [独立模块] Titan 双向测试钩子已完全闭合注入成功！", true);

        } catch (Throwable e) {
            log2(" ❌ [独立模块] 泰坦监控挂载失败: " + e.getMessage(), true);
        }
    }




    // ==================== 淘宝 Hook ====================
    private void hookTaobao(LoadPackageParam lpparam) {
        String processName = lpparam.processName;
        log2(" 🎯 开始初始化淘宝Hook，当前进程: " + processName);

        // ✅ 动态拼接 URL（使用最新 TAOBAO_尾缀）

        final String targetUrl = "http://ma.132.tv:3079/api/rwb/action=item_upload?" + 淘系_尾缀 +"&sjpt="+实际_平台+"&sjfsid="+实际_分身id+"&sbxlhxn="+设备序列号_虚拟+"&ver="+软件版本;
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

                            log2(" 📄 收到数据，长度A: " + data.length + " 字节");

                            String responseText = new String(data, StandardCharsets.UTF_8);
                            int previewLength = Math.min(50, responseText.length());
                            log2(" 📄 A数据前" + previewLength + "字符: " + responseText.substring(0, previewLength));

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

                           // log2(" 🔍 综合结果:");
                            log2("   - 是商品详情数据: " + isDetail);
                            log2("   - 是商品描述数据: " + isDesc);
                            log2("   - 是错误数据: " + isError);

                            if (isDetail || isDesc || isError) {
                                log2(" ✅ 符合条件，检查去重");
                                if (!isDuplicate(responseText)) {
                                    log2(" 🚀 PUT，长度: " + data.length,true);
                                    上传数据到服务器(responseText, targetUrl, "Xposed-Hook-Client");
                                } else {
                                    log2(" ⏭️ 数据重复，跳过上传");
                                }
                            } else {
                               // log2(" ❌ 不符合条件，跳过处理");
                            }
                        }
                    });

            log2(" ✅ 淘宝Hook设置完毕");
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
        String 多多_临时网址 = "";
        String 多多全网址 = "";

        if (日志开关_集 = true) {//多多_尾缀 != null && 多多_尾缀.indexOf("芝麻公司") != -1
            多多_临时网址 = "http://ma.132.tv:3079";
            log2(" ✅调试", true);
        }
        else {
            多多_临时网址 = "http://ma.132.tv:43079";
            log2(" ✅ma43", true);
        }
        if (多多_尾缀 != null && 多多_尾缀.indexOf("芝麻公司") != -1){
            多多_临时网址 = "http://yx.132.tv:3079";
        log2(" ✅yx3079", true);
    }

         多多全网址=  多多_临时网址+"/api/rwb/action=item_upload?" + 多多_尾缀+"&sjpt="+实际_平台+"&sjfsid="+实际_分身id+"&sbxlhxn="+设备序列号_虚拟+"&ver="+软件版本;
        final String 安全的网址变量 = 多多全网址;
        //final String 多多全网址 = "http://ma.132.tv:3079/api/rwb/action=item_upload?" + 多多_尾缀+"&sjpt="+实际_平台+"&sjfsid="+实际_分身id+"&sbxlhxn="+设备序列号_虚拟;
       // final String targetUrl = PINDUODUO_TARGET_URL;
        log2(" ✅ 使用硬编码拼多多URL: " + 多多全网址);

        try {
            log2(" 🔍正在查找Gso: goGson");
            Class<?> gsonClass = XposedHelpers.findClass(
                    "com.google.gson.Gson",
                    lpparam.classLoader
            );
            log2(" ✅成功加载类", true);// + gsonClass.getName()

            log2(" 🔧 正在Hook: Gson");




            XposedHelpers.findAndHookMethod(gsonClass,
                    "fromJson",
                    String.class,
                    java.lang.reflect.Type.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                           //log2(" 📥 拼多多Gson被调用");

                            String json = (String) param.args[0];
                            if (json == null) {
                                log2(" ⚠️  json参数为null");
                                return;
                            }

                            int jsonLength = json.length();
                            //log2(" 📄 JSON长度: " + jsonLength + " 字符");

                            if (jsonLength <330){
                                if(jsonLength>7000){
                                  //  log2(" ⚠️  JSON长度大于1000小于7200，跳过处理c"+jsonLength);

                                }

                                return;
                            }

                            int previewLength = Math.min(9000, jsonLength);
                            if(jsonLength>2000){
                                log2(" ⚠️  JSON长度大于1000小于7200，跳过处理c"+jsonLength);
                                log2(" 长度"+jsonLength+"📄 多JSON前" + previewLength + "字符: " + json.substring(0, previewLength));
                            }

//https://mobile.yangkeduo.com/https://mobile.yangkeduo.com/goods.html?goods_id=825392713666&force_use_web_bundle=1&_oak_rcto=YWIAHHeL0-6AmEjLj_jKOTwcjynTm9813euYUDjBkmkvOM7Xoujbh4qQ&_oak_gallery=https%3A%2F%2Fimg.pddpic.com%2Fmms-material-img%2F2025-08-19%2Fb2225ea8-69bf-42ba-b126-c36f96526274.jpeg.a.jpeg&_oak_gallery_token=dd43524bdd1ee1740a1db444203787aa&_oc_refer_ad=0
                            boolean 是否包含这个页 = json.contains("using_secondary_bottom_section_order");//mall_quality_assurance.html
                            boolean 是否包含这个名 = json.contains("goods_id");
                            boolean 是否包含这个名_备用 = json.contains("grp_lite_paid");
                            boolean 是否大于N体积 = jsonLength>18000;

                            boolean 是否为oka = json.contains("disable_avatar_jump");
                            boolean 是否为收藏列表 = json.contains("fav_goods_hash");
                            boolean 是否为搜索列表 = json.contains("search_ext");
                            boolean 是否为收藏下的推荐列表 = json.contains("brec") || json.contains("arec");
                           // log2(" 🔍 JSON检查结果:");
                           // log2("   - 包含mall_quality_assurance.html: " + 是否包含这个页);
                           // log2("   - 包含goods_name: " + 是否包含这个名);

                           //og2(" ✅ "+"是否包含这个页>"+是否包含这个页);

                         // log2(" ✅ "+"是否包含这个名>"+是否包含这个名);

                            if ( 是否包含这个名) {
                                log2(" ✅ "+"特殊处理");
                                log2(" 长度"+jsonLength+"📄 多JSON前" + previewLength + "字符: " + json.substring(0, previewLength));

                            }
                            int previewLength2 = Math.min(200, jsonLength);
                            if ( json.contains("570126767")) {

                                log2("✅"+"终点位置=======");
                                log2(" 长度"+jsonLength+"📄 多JSON前" + previewLength2 + "字符: " + json.substring(0, previewLength2));

                            }
                           // log2(" ✅ "+"是否为收藏下的推荐列表"+是否为收藏下的推荐列表);
                            if (是否包含这个页 && 是否包含这个名 ) {
                                log2(" ✅ 符合XQ", true);
                                if (!isDuplicate(json)) {
                                    log2(" 🚀 开始上传拼多多数据，长度: " + jsonLength);
                                    备份数据到本地("com.xunmeng.pinduoduo", 实际_分身id, json);
                                    上传数据到服务器(json, 安全的网址变量, "PDD-Hook-Client");
                                } else {
                                    log2(" ⏭️ 拼多多数据重复，跳过上传");
                                }
                            } else {
                               // log2(" ❌ 不符合拼多多商品数据特征，跳过处理");
                            }


                            if (是否为搜索列表 ) {
                                log2(" ✅ 符合SS", true);
                                if (!isDuplicate(json)) {
                                    log2(" 🚀 开始上传拼多多搜索，长度: " + jsonLength);

                                    //备份数据到本地("com.xunmeng.pinduoduo", 实际_分身id, json);

                                    上传数据到服务器(json, 安全的网址变量, "PDD-Hook-Client");


                                } else {
                                    log2(" ⏭️ 拼多多数据搜索，跳过上传");
                                }
                            } else {
                                // log2(" ❌ 不符合拼多多商品数据特征，跳过处理");
                            }


// ==================================================
                            // 🌟 核心逻辑：收藏列表 与 推荐列表 统一合并篡改
                            // ==================================================
                            if (是否为收藏列表 || 是否为收藏下的推荐列表 ) {
                                String 列表类型名字 = 是否为收藏列表 ? "【收藏列表】" : "【推荐列表】";
                                log2(" ✅ 命中 " + 列表类型名字 + " 数据，准备检查动态配置");

                                try {
                                    // 1. 实时读取本地配置
                                    String[] 动态配置结果 = 读取动态配置_带ID("com.xunmeng.pinduoduo", 实际_分身id);
                                    String 目标网址_集 = "";
                                    String 提取商品ID_集 = "";
                                    boolean 配置有效 = false;

                                    if (动态配置结果 != null &&
                                            动态配置结果[0] != null && !动态配置结果[0].isEmpty() &&
                                            动态配置结果[1] != null && !动态配置结果[1].isEmpty()) {
                                        目标网址_集 = 动态配置结果[0];
                                        提取商品ID_集 = 动态配置结果[1];
                                        配置有效 = true;
                                        log2(" ✅ 成功加载自定义配置 [URL+ID]，准备替换");
                                    } else {
                                        log2(" ⚠️ 动态配置缺失，进入【保留原样+拼接原ID】模式");
                                    }

                                    // 2. 解析原始 JSON 根节点
                                    org.json.JSONObject 根节点 = new org.json.JSONObject(json);
                                    boolean 数据是否被修改 = false;

                                    // 3. 针对【收藏列表】的节点解析处理 (解析 goods_list[0])
                                    if (是否为收藏列表 && 根节点.has("goods_list")) {
                                        org.json.JSONArray 商品列表 = 根节点.getJSONArray("goods_list");
                                        if (商品列表.length() > 0) {
                                            // 按照你的要求，只改第一个商品 list[0] (如需全改，可换成 for 循环)
                                            org.json.JSONObject 单个商品 = 商品列表.getJSONObject(0);

                                            // 调用上面的公共模块 (收藏列表的ID在base_goods_info里，店名在mall_info.mall_name)
                                            统一篡改单商品数据(单个商品, 配置有效, 提取商品ID_集, 目标网址_集, "goods_id", "mall_info.mall_name");
                                            数据是否被修改 = true;
                                        }
                                    }

                                    // 4. 针对【推荐列表】的节点解析处理 (解析 list[1] 和 list[2])
                                    if (是否为收藏下的推荐列表 && 根节点.has("list")) {
                                        org.json.JSONArray 推荐列表 = 根节点.getJSONArray("list");

                                        // 篡改 list[1] 的数据 (修改它的链接、以及修改店名 sales_tip)
                                        if (推荐列表.length() > 0) {
                                            org.json.JSONObject 商品_1 = 推荐列表.getJSONObject(0);
                                            统一篡改单商品数据(商品_1, 配置有效, 提取商品ID_集, 目标网址_集, "id", "sales_tip");
                                            数据是否被修改 = true;
                                        }

                                        // 篡改 list[2] 的数据 (你的要求是 list[2].id 也是商品id，同样进行覆盖)
                                        if (推荐列表.length() > 1) {
                                            org.json.JSONObject 商品_2 = 推荐列表.getJSONObject(1);
                                            统一篡改单商品数据(商品_2, 配置有效, 提取商品ID_集, 目标网址_集, "id", "sales_tip");
                                            数据是否被修改 = true;
                                        }
                                    }



// 情况二：严格匹配 data.goods_list[x].data 结构
                                    else if (根节点.has("data")) {
                                        org.json.JSONObject data节点 = 根节点.getJSONObject("data");

                                        if (data节点.has("goods_list")) {
                                            org.json.JSONArray 商品列表 = data节点.getJSONArray("goods_list");

                                            // 1. 严格篡改 data.goods_list[0].data
                                            if (商品列表.length() > 0) {
                                                org.json.JSONObject 外层商品 = 商品列表.getJSONObject(0); // 对应 goods_list[0]

                                                if (外层商品.has("data")) {
                                                    org.json.JSONObject 核心商品数据 = 外层商品.getJSONObject("data"); // 对应 goods_list[0].data

                                                    // 🚀 第一步：调用公共方法，把这个最内层的 data 传进去改掉 link_url 和 goods_id
                                                    // 因为传进去的就是内层 data，所以 id键名 是 "goods_id"，店名键名 是 "mall_name"
                                                    统一篡改单商品数据(核心商品数据, 配置有效, 提取商品ID_集, 目标网址_集, "goods_id", "goods_name");

                                                    // 🚀 第二步：【关键核心】把改完的内层 data，重新覆盖回外层商品
                                                    外层商品.put("data", 核心商品数据);

                                                    // 🚀 第三步：【关键核心】把改完的外层商品，重新塞回 JSON 数组对应的 0 号位置
                                                    商品列表.put(0, 外层商品);

                                                    数据是否被修改 = true;
                                                }
                                            }

                                            // 2. 严格篡改 data.goods_list[1].data
                                            if (商品列表.length() > 1) {
                                                org.json.JSONObject 外层商品_2 = 商品列表.getJSONObject(1); // 对应 goods_list[1]

                                                if (外层商品_2.has("data")) {
                                                    org.json.JSONObject 核心商品数据_2 = 外层商品_2.getJSONObject("data"); // 对应 goods_list[1].data

                                                    // 修改内层
                                                    统一篡改单商品数据(核心商品数据_2, 配置有效, 提取商品ID_集, 目标网址_集, "goods_id", "goods_name");

                                                    // 手动一层层打包塞回去
                                                    外层商品_2.put("data", 核心商品数据_2);
                                                    商品列表.put(1, 外层商品_2);

                                                    数据是否被修改 = true;
                                                }
                                            }

                                            // 🚀 第四步：把整条改好的商品列表数组，重新塞回 data 节点，再塞回根节点
                                            data节点.put("goods_list", 商品列表);
                                            根节点.put("data", data节点);
                                        }
                                    }















                                    // 5. 将修改后的数据重新打包写回系统环境中
                                    if (数据是否被修改) {
                                        param.args[0] = 根节点.toString();
                                        log2(" 🚀 " + 列表类型名字 + " 篡改数据并写回环境成功！");
                                    }

                                } catch (Exception e) {
                                    log2(" ❌ 篡改 " + 列表类型名字 + " 失败: " + e.getMessage());
                                }
                            }






                            if (1 == 2) {//是否为收藏列表_备份
                                log2(" ✅ 命中收藏列表数据，准备检查动态配置");
                                try {
                                    // 1. 🌟 实时读取本地配置
                                    String[] 动态配置结果 = 读取动态配置_带ID("com.xunmeng.pinduoduo", 实际_分身id);

                                    String 目标网址_集 = "";
                                    String 提取商品ID_集 = "";
                                    boolean 配置有效 = false;

                                    if (动态配置结果 != null &&
                                            动态配置结果[0] != null && !动态配置结果[0].isEmpty() &&
                                            动态配置结果[1] != null && !动态配置结果[1].isEmpty()) {

                                        目标网址_集 = 动态配置结果[0];
                                        提取商品ID_集 = 动态配置结果[1];
                                        配置有效 = true;
                                        log2(" ✅ 成功加载自定义配置 [URL+ID]，准备替换");
                                    } else {
                                        log2(" ⚠️ 动态配置(URL或ID)缺失或为空，进入【保留原样+拼接原ID】模式");
                                    }

                                    // 2. 解析原始 JSON
                                    org.json.JSONObject 根节点 = new org.json.JSONObject(json);

                                    if (根节点.has("goods_list")) {
                                        org.json.JSONArray 商品列表 = 根节点.getJSONArray("goods_list");

                                        if (商品列表.length() > 0) {
                                            org.json.JSONObject 单个商品 = 商品列表.getJSONObject(0);

                                            // --- 核心修改逻辑开始 ---

                                            // 3. 处理店铺名 (无论配置是否存在，都要改店名)
                                            if (单个商品.has("mall_info")) {
                                                org.json.JSONObject 店铺信息 = 单个商品.getJSONObject("mall_info");
                                                if (店铺信息.has("mall_name")) {
                                                    String 原始店名 = 店铺信息.getString("mall_name");
                                                    String 拼接ID = "";
                                                    String 标识名 = "SOK";
                                                    if (配置有效) {
                                                        // 如果配置有效，用配置里的 ID
                                                        拼接ID = 提取商品ID_集;
                                                    } else {
                                                        标识名 = "YOK";
                                                        // 🌟 如果配置为空，从原始 JSON 的 base_goods_info 里提取商品 ID
                                                        if (单个商品.has("base_goods_info")) {
                                                            org.json.JSONObject 基础信息 = 单个商品.getJSONObject("base_goods_info");
                                                            if (基础信息.has("goods_id")) {
                                                                拼接ID = 基础信息.get("goods_id").toString(); // 拿到原始 ID
                                                            }
                                                        }
                                                    }

                                                    String 新店名 = 拼接ID+ "#"+标识名 +软件版本 +"|"+原始店名;
                                                    店铺信息.put("mall_name", 新店名);
                                                    log2(" ✍️ 已修改店铺名为: " + 新店名);
                                                }
                                            }

                                            // 4. 只有在【配置有效】的情况下，才篡改跳转链接和商品ID
                                            if (配置有效) {
                                                // 修改标题
                                                if (单个商品.has("goods_name")) {
                                                    单个商品.put("goods_name", "["+软件版本+"]" + 单个商品.getString("goods_name"));
                                                }

                                                if (单个商品.has("base_goods_info")) {
                                                    org.json.JSONObject 基础商品信息 = 单个商品.getJSONObject("base_goods_info");

                                                    // 修改 goods_id 为配置里的数字 ID
                                                    if (!提取商品ID_集.isEmpty()) {
                                                        try {
                                                            long 纯数字商品ID = Long.parseLong(提取商品ID_集.trim());
                                                            基础商品信息.put("goods_id", 纯数字商品ID);
                                                        } catch (Exception e) {
                                                            基础商品信息.put("goods_id", 提取商品ID_集);
                                                        }
                                                    }

                                                    // 修改链接
                                                    基础商品信息.put("link_url", 目标网址_集);
                                                    基础商品信息.put("detail_url", 目标网址_集);
                                                    log2(" ✍️ 已完成链接与数字ID的篡改");
                                                }
                                            } else {
                                                log2(" ℹ️ 配置为空，仅完成店名拼接，未触动跳转链接");
                                            }

                                            // --- 核心修改逻辑结束 ---

                                            // 6. 将修改后的数据写回
                                            param.args[0] = 根节点.toString();
                                            log2(" 🚀 篡改处理完毕！");
                                        }
                                    }
                                } catch (Exception e) {
                                    log2(" ❌ 篡改收藏列表失败: " + e.getMessage());
                                }
                            }















                        }




                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            //log2(" 📤 拼多多Gson.fromJson调用完成");
                        }
                    });

            log2(" ✅ 多多设置完毕", true);

        } catch (Throwable t) {
            log2(" ❌ 拼多多Hook设置失败: " + t.getMessage());
            log2(" ❌ 多多设置失败: " ,true);
            log2(" ❌ 异常详情CC: " + t.getClass().getName() + ": " + t.getMessage(),true);
        }
    }








    private void 独立模块_解密Titan网络响应为明文(final LoadPackageParam lpparam) {
        try {
            log2(" 🚀 [Titan网络] 开始挂载 TitanApiCall 响应明文修复器...", true);

            XposedHelpers.findAndHookMethod(
                    "com.xunmeng.basiccomponent.titan.api.TitanApiCall$1",
                    lpparam.classLoader,
                    "onResponse",
                    "com.xunmeng.basiccomponent.titan.api.TitanApiRequest",
                    int.class,
                    String.class,
                    "com.xunmeng.basiccomponent.titan.api.TitanApiResponse",
                    int.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Object titanApiRequest = param.args[0];
                            Object titanApiResponse = param.args[3];

                            if (titanApiRequest != null && titanApiResponse != null) {
                                String url = (String) XposedHelpers.callMethod(titanApiRequest, "getUrl");

                                // 🎯 精确过滤目标 URL
                                if (url != null && url.contains("/api/oak/integration/render")) {
                                    log2(" 🔔 [Titan网络] 捕获到目标加密/压缩网络响应！URL: " + url, true);

                                    // 1. 拿到原始的字节流
                                    byte[] bodyBytes = (byte[]) XposedHelpers.callMethod(titanApiResponse, "getBodyBytes");

                                    if (bodyBytes != null) {
                                        // 2. 🛡️ 【标准 Xposed 安全判定机制】判定是否为 Gzip 压缩
                                        boolean isGzip = false;

                                        // 尝试读取拼写错误的字段 isGizpCompressed
                                        try {
                                            isGzip = XposedHelpers.getBooleanField(titanApiResponse, "isGizpCompressed");
                                        } catch (Throwable ignored) {
                                            // 如果找不到，尝试读取拼写正确的字段 isGzipCompressed
                                            try {
                                                isGzip = XposedHelpers.getBooleanField(titanApiResponse, "isGzipCompressed");
                                            } catch (Throwable ignored2) {
                                                // 两个都没找到，说明字段被重新混淆了，默认当作普通数据或退回原逻辑
                                            }
                                        }

                                        // 3. 如果是 Gzip，强行解压并还原为明文
                                        if (isGzip) {
                                            Class<?> unzipUtil = XposedHelpers.findClass("com.xunmeng.basekit.util.p", lpparam.classLoader);
                                            byte[] unzippedBytes = (byte[]) XposedHelpers.callStaticMethod(unzipUtil, "b", new Object[]{bodyBytes});

                                            if (unzippedBytes != null) {
                                                // 把解压后的“纯明文 JSON 字节流”重新塞回响应体
                                                XposedHelpers.callMethod(titanApiResponse, "setBodyBytes", new Object[]{unzippedBytes});

                                                // 🌟 强行将 Gzip 压缩标志位强行设置为 false
                                                try { XposedHelpers.setBooleanField(titanApiResponse, "isGizpCompressed", false); } catch (Throwable ignored) {}
                                                try { XposedHelpers.setBooleanField(titanApiResponse, "isGzipCompressed", false); } catch (Throwable ignored) {}

                                                log2(" 🎉 [Titan网络] 目标 Gzip 响应已成功强行解压并还原为 [纯明文 JSON] 投递给业务层！", true);

                                                // 🌟【新增逻辑】打印解压后的 JSON 明文
                                                try {
                                                    String jsonStr = new String(unzippedBytes, "UTF-8");
                                                    log2(" 📄 [Titan JSON(Gzip解压后)]:\n" + jsonStr, true);
                                                } catch (Throwable t) {
                                                    log2(" ❌ [Titan网络] 解压后转 JSON 字符串失败: " + t.getMessage(), true);
                                                }
                                            }
                                        } else {
                                            log2(" ℹ️ [Titan网络] 该响应本身就是明文，无需解压，直接放行给业务层处理", true);

                                            // 🌟【新增逻辑】本身是明文时直接打印 JSON
                                            try {
                                                String jsonStr = new String(bodyBytes, "UTF-8");
                                                log2(" 📄 [Titan JSON(原始明文)]:\n" + jsonStr, true);
                                            } catch (Throwable t) {
                                                log2(" ❌ [Titan网络] 原始明文转 JSON 字符串失败: " + t.getMessage(), true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            );

            log2(" ✅ [Titan网络] 响应明文修复器挂载成功！", true);
        } catch (Throwable e) {
            log2(" ❌ [Titan网络] 模块挂载异常 (可能混淆类名变动): " + e.getMessage(), true);
        }
    }





    // ==================== 工具方法 ====================
    private void 上传数据到服务器(final String rawData, final String targetUrl, final String userAgent) {
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
            //log2(" B数据前" + previewLength + "字符: " + rawData.substring(0, previewLength));
        } else {
            log2(" 数据为空");
        }

        new Thread(() -> {
            String threadName = Thread.currentThread().getName();
            log2(" 📡 网络请求线程启动: " + threadName);

            HttpURLConnection conn = null;
            // 声明一个标记，用来判断是否成功
            boolean 上传成功标记 = false;

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

                // ✅ 修复完成：改为 getRequestProperty 正常打印请求头
                log2(" 📤 发送请求头: Content-Type=" + conn.getRequestProperty("Content-Type"));
                log2(" 📤 发送请求头: User-Agent=" + conn.getRequestProperty("User-Agent"));

                long startTime = System.currentTimeMillis();
                try (OutputStream os = conn.getOutputStream()) {
                    byte[] dataBytes = rawData.getBytes(StandardCharsets.UTF_8);
                    os.write(dataBytes);
                    os.flush();
                }
                long sendTime = System.currentTimeMillis() - startTime;
                log2(" ✅ 请求fs完成，耗时: " + sendTime + "ms",true);

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

                        log2(" ✅ OK: " +  rawData.length(),true);
                        log2(" ✅ 上传成功，服务器返回完整数据长度: " + responseStr.length());
                        log2(" ✅ 成功A" + responseStr.length(),true);
                        log2(" ✅ 服务器返回数据: " + responseStr);

                        // 只有状态码为 200 才算成功
                        上传成功标记 = true;
                    }
                } else {
                    log2(" ❌ 上传失败，状态码: " + code);
                    log2(" ❌ 失败" +code,true);
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

                // 如果网络请求没成功（包括报错或者状态码不是200）
                上传成功标记=false;
                if (!上传成功标记) {
                    String 当前包名 = userAgent.contains("PDD") ? "com.xunmeng.pinduoduo" : "com.taobao.taobao";
                    if (userAgent.contains("Client") && !userAgent.contains("PDD")) {
                        if ("淘特".equals(实际_平台)) 当前包名 = "com.taobao.litetao";
                        if ("天猫".equals(实际_平台)) 当前包名 = "com.tmall.wireless";
                    }

                    // 调用本地备份函数
                   // 备份数据到本地(当前包名, 实际_分身id, rawData);
                }

                log2(" 🏁 网络请求线程结束: " + threadName);
            }
        }, "UploadThread-" + System.currentTimeMillis()).start();
    }








    // 🌟 核心提取：模块化单商品综合篡改引擎（支持收藏列表与推荐列表）
    private void 统一篡改单商品数据(org.json.JSONObject 单个商品, boolean 配置有效, String 提取商品ID_集, String 目标网址_集, String id键名, String 店名键名) {
        try {
            // 1. 处理店名/销售标签拼接（对应你的 sales_tip 或 mall_info.mall_name）
            String 原始店名 = "";
            org.json.JSONObject 店铺信息节点 = null;
            boolean 是嵌套店名 = false;

            if (店名键名.contains(".")) { // 说明是类似于 mall_info.mall_name 的嵌套结构
                String[] 级联 = 店名键名.split("\\.");
                if (单个商品.has(级联[0])) {
                    店铺信息节点 = 单个商品.getJSONObject(级联[0]);
                    if (店铺信息节点.has(级联[1])) {
                        原始店名 = 店铺信息节点.getString(级联[1]);
                        是嵌套店名 = true;
                    }
                }
            } else { // 直属结构，比如推荐列表里的 sales_tip
                if (单个商品.has(店名键名)) {
                    原始店名 = 单个商品.getString(店名键名);
                }
            }

            if (!原始店名.isEmpty() || 店铺信息节点 != null || 单个商品.has(店名键名)) {
                String 拼接ID = "";
                String 标识名 = "SOK";
                if (配置有效) {
                    拼接ID = 提取商品ID_集;
                } else {
                    标识名 = "YOK";
                    // 自动提取原始 ID
                    if (单个商品.has(id键名)) {
                        拼接ID = 单个商品.get(id键名).toString();
                    } else if (单个商品.has("base_goods_info")) { // 收藏列表兜底
                        org.json.JSONObject 基础信息 = 单个商品.getJSONObject("base_goods_info");
                        if (基础信息.has("goods_id")) 拼接ID = 基础信息.get("goods_id").toString();
                    }
                }

                String 新店名 = 拼接ID + "#" + 标识名 + 软件版本 + "|" + 原始店名;
                if (是嵌套店名 && 店铺信息节点 != null) {
                    店铺信息节点.put(店名键名.split("\\.")[1], 新店名);
                } else {
                    单个商品.put(店名键名, 新店名);
                }
                log2(" ✍️ [公共模块] 已修改位置 [" + 店名键名 + "] 为: " + 新店名);
            }

            // 🌟 【新增逻辑】独立判定：如果包含 list_title 键，则在其内容后追加 +V1.8




            // 2. 只有配置有效，才篡改跳转链接与商品ID
            if (配置有效) {
                // 修改商品名称标题
                if (单个商品.has("goods_name")) {
                    单个商品.put("goods_name", "["+软件版本+"] " + 单个商品.getString("goods_name"));
                }

                // 修改数字商品ID (支持传入自定义的键名，如 "id" 或基础信息里的 "goods_id")
                Object 转换后的ID = 提取商品ID_集;
                try { 转换后的ID = Long.parseLong(提取商品ID_集.trim()); } catch (Exception ignored) {}

                if (id键名.equals("goods_id") && 单个商品.has("base_goods_info")) {
                    org.json.JSONObject 基础商品信息 = 单个商品.getJSONObject("base_goods_info");
                    基础商品信息.put("goods_id", 转换后的ID);
                    基础商品信息.put("link_url", 目标网址_集);
                    基础商品信息.put("detail_url", 目标网址_集);
                } else {
                    // 推荐列表直属修改：list[x].id 和 list[x].link_url
                    单个商品.put(id键名, 转换后的ID);
                    if (单个商品.has("link_url")) 单个商品.put("link_url", 目标网址_集);
                    if (单个商品.has("detail_url")) 单个商品.put("detail_url", 目标网址_集);
                }
                log2(" ✍️ [公共模块] 已完成链接与数字ID的篡改");
            }
        } catch (Exception e) {
            log2(" ❌ [公共模块] 单商品处理异常: " + e.getMessage());
        }
    }






    // 🌟 极简模块化：网络失败本地 JSON 备份引擎
    private static void 备份数据到本地(String 包名, String 分身名, String json数据) {
        // 自动判定：如果分身名拿不到，兜底使用 0 分身
        String 实际分身 = (分身名 == null || 分身名.isEmpty()) ? "0" : 分身名;
        String 文件路径 = "/storage/emulated/" + 实际分身 + "/Android/data/" + 包名 + "/files/data.json";

        java.io.File 文件 = new java.io.File(文件路径);
        java.io.BufferedWriter 写入器 = null;
        try {
            // 自动创建多级父目录（如 files 文件夹不存在会自动创建）
            if (文件.getParentFile() != null && !文件.getParentFile().exists()) {
                文件.getParentFile().mkdirs();
            }

            // 使用 UTF-8 覆盖写出（如果你想每次都追加，把 false 改成 true 即可）
            java.io.FileOutputStream fos = new java.io.FileOutputStream(文件, false);
            java.io.OutputStreamWriter osw = new java.io.OutputStreamWriter(fos, java.nio.charset.StandardCharsets.UTF_8);
            写入器 = new java.io.BufferedWriter(osw);

            写入器.write(json数据);
            写入器.flush();
          log2(" 💾 [本地成功]" , true);
        } catch (Throwable 错误) {
           log2(" ❌ [ 写入失败: " + 错误.getMessage(), true);
        } finally {
            try { if (写入器 != null) 写入器.close(); } catch (Exception ignored) {}
        }
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
                log2(" ⏭️ 数据重复，跳过处理A");
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




    private void 独立模块_屏蔽WiFi网络关联探测(LoadPackageParam lpparam) {
        try {
            log2(" 🛡️ [安全防护] 开始挂载 Wi-Fi 隐私屏蔽模块...", true);

            // -----------------------------------------------------------------
            // 【1】 拦截获取周围 Wi-Fi 扫描列表 (防止通过邻近网络计算设备关联)
            // -----------------------------------------------------------------
            XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiManager",
                    lpparam.classLoader,
                    "getScanResults",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            log2(" 🛑 [拦截触发] App 尝试获取周围 Wi-Fi 列表，已强制返回空集合", true);
                            // 返回一个空的 ArrayList，让应用认为周围没有任何 Wi-Fi 信号
                            return new java.util.ArrayList<>();
                        }
                    }
            );

            // -----------------------------------------------------------------
            // 【2】 拦截当前连接 Wi-Fi 的 BSSID (路由器的 MAC 地址，关联认定的关键)
            // -----------------------------------------------------------------
            XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiInfo",
                    lpparam.classLoader,
                    "getBSSID",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            log2(" 🛑 [拦截触发] App 尝试获取当前连接 Wi-Fi BSSID，已脱敏返回", true);
                            // 返回全 0 的伪造 MAC 地址，或者直接返回 null / 空字符串
                            return "02:00:00:00:00:00";
                        }
                    }
            );

            // -----------------------------------------------------------------
            // 【3】 拦截当前连接 Wi-Fi 的 SSID (网络名称)
            // -----------------------------------------------------------------
            XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiInfo",
                    lpparam.classLoader,
                    "getSSID",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            log2(" 🛑 [拦截触发] App 尝试获取当前连接 Wi-Fi 名称，已脱敏返回", true);
                            // Android 系统标准未获取到时返回 "<unknown ssid>"
                            return "<unknown ssid>";
                        }
                    }
            );





            // -----------------------------------------------------------------
            // 【3】 拦截当前连接 Wi-Fi 的 SSID (网络名称)
            // -----------------------------------------------------------------
            XposedHelpers.findAndHookMethod(
                    "android.net.wifi.WifiInfo",
                    lpparam.classLoader,
                    "getSSID",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            log2(" 🛑 [拦截触发] App 尝试获取当前连接 Wi-Fi 名称，已脱敏返回", true);
                            return "<unknown ssid>";
                        }
                    }
            );

            // -----------------------------------------------------------------
            // 🚀 【新增加入位置】 拦截通过网络状态快照获取隐藏的 Wi-Fi 信息
            // -----------------------------------------------------------------
            try {
                XposedHelpers.findAndHookMethod(
                        "android.net.NetworkCapabilities",
                        lpparam.classLoader,
                        "getTransportInfo",
                        new XC_MethodReplacement() {
                            @Override
                            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                log2(" 🛑 [拦截触发] App 尝试通过网络状态快照获取 Wi-Fi 信息，已强制返回 null", true);
                                return null;
                            }
                        }
                );
            } catch (Throwable e) {
                // 某些极低版本的安卓系统可能不存在这个方法，加上 try-catch 确保向下兼容
            }

            // -----------------------------------------------------------------
            // 【4】 拦截手机本地网卡物理硬件 MAC 地址 (防止通过 wlan0 物理特征关联)
            // -----------------------------------------------------------------
            XposedHelpers.findAndHookMethod(
                    "java.net.NetworkInterface",
                    lpparam.classLoader,
                    "getHardwareAddress",
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                            log2(" 🛑 [拦截触发] App 尝试获取网卡硬件物理 MAC 地址，已脱敏返回", true);
                            return new byte[]{0x02, 0x00, 0x00, 0x00, 0x00, 0x00};
                        }
                    }
            );








            log2(" ✅ [安全防护] Wi-Fi成功！", true);

        } catch (Throwable e) {
            log2(" ❌ [安全防护] Wi-Fi 失败: " + e.getMessage(), true);
        }
    }



























    // ===== 从 /sdcard/flash/taobao.txt 读取淘宝尾缀 =====
    private static String 读取apk配置文件(String 包名,String 分身名) {
        String filePath = "/storage/emulated/"+分身名+"/Android/data/"+包名+"/files/pz.txt";
        //String filePath = "/data/media/"+分身名+"/Android/data/"+包名+"/files/pz.txt";
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            log2(" ⚠️ 配置文件不存在: " + filePath);
            return null;
        }

        // 使用兼容性更好的 FileInputStream + InputStreamReader 替代 FileReader(File, Charset)
        // 因为 FileReader(File, Charset) 构造函数在低版本Android上可能不存在
        java.io.BufferedReader reader = null;
        try {
            // 关键修改点：使用 FileInputStream 和 InputStreamReader 指定编码
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            java.io.InputStreamReader isr = new java.io.InputStreamReader(fis, java.nio.charset.StandardCharsets.UTF_8);
            reader = new java.io.BufferedReader(isr);

            String line = reader.readLine();
            if (line != null) {
                line = line.trim();
                log2(" ✅ 成功读取尾缀: " + line.substring(0, Math.min(50, line.length())) + "...");
                return line;
            } else {
                log2(" ⚠️ 配置文件为空: " + filePath);
            }
        } catch (java.io.FileNotFoundException e) {
            // 理论上，上面的 file.exists() 已检查，但为安全起见仍保留
            log2(" ❌ 配置文件不存在 (异常): " + e.getMessage());
        } catch (java.io.IOException e) {
            log2(" ❌ 读取文件时发生IO异常: " + e.getMessage());
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            // 捕获因兼容性产生的更低级错误
            log2(" ❌ 发生底层API兼容性错误，请检查Android版本: " + e.toString());
        } catch (Exception e) {
            log2(" ❌ 读取配置发生未知异常: " + e.getMessage());
        } finally {
            // 确保关闭流
            if (reader != null) {
                try {
                    reader.close();
                } catch (java.io.IOException e) {
                    // 忽略关闭时的异常
                }
            }
        }
        log2(" ❌ 读取配置返回NULL，路径: " + filePath);
        return null;
    }






    // ===== 从 /sdcard/flash/taobao.txt 读取淘宝尾缀 =====
    private static String 读取apk配置文件_bak5_17(String 包名,String 分身名) {
        String filePath = "/storage/emulated/"+分身名+"/Android/data/"+包名+"/files/pz.txt";
        //String filePath = "/data/media/"+分身名+"/Android/data/"+包名+"/files/pz.txt";
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            log2(" ⚠️ 配置文件不存在: " + filePath);
            return null;
        }

        // 使用兼容性更好的 FileInputStream + InputStreamReader 替代 FileReader(File, Charset)
        // 因为 FileReader(File, Charset) 构造函数在低版本Android上可能不存在





        java.io.BufferedReader reader = null;
        try {
            // ========== 🔧 只需要微调这里：增加底层对象的安全防崩检查 ==========
            java.io.FileInputStream fis = null;
            try {
                fis = new java.io.FileInputStream(file);
                if (fis == null || fis.getFD() == null) {
                    return null; // 如果底层没有成功生成文件描述符对象，直接安全返回 null
                }
            } catch (Throwable t) {
                return null; // 捕获包含 java.lang.NullPointerException 在内的所有底层对象错误，直接返回 null
            }
            // ==================================================================

            java.io.InputStreamReader isr = new java.io.InputStreamReader(fis, java.nio.charset.StandardCharsets.UTF_8);
            reader = new java.io.BufferedReader(isr);






            String line = reader.readLine();
            if (line != null) {
                line = line.trim();
                log2(" ✅ 成功读取尾缀: " + line.substring(0, Math.min(50, line.length())) + "...");
                return line;
            } else {
                log2(" ⚠️ 配置文件为空: " + filePath);
            }
        } catch (java.io.FileNotFoundException e) {
            // 理论上，上面的 file.exists() 已检查，但为安全起见仍保留
            log2(" ❌ 配置文件不存在 (异常): " + e.getMessage());
        } catch (java.io.IOException e) {
            log2(" ❌ 读取文件时发生IO异常: " + e.getMessage());
        } catch (NoSuchMethodError | NoClassDefFoundError e) {
            // 捕获因兼容性产生的更低级错误
            log2(" ❌ 发生底层API兼容性错误，请检查Android版本: " + e.toString());
        } catch (Exception e) {
            log2(" ❌ 读取配置发生未知异常: " + e.getMessage());
        } finally {
            // 确保关闭流
            if (reader != null) {
                try {
                    reader.close();
                } catch (java.io.IOException e) {
                    // 忽略关闭时的异常
                }
            }
        }
        log2(" ❌ 读取配置返回NULL，路径: " + filePath);
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

    // 🌟 纯静默类安全检测模块 (不 Hook 任何内容，只为肉眼在日志看有没有用)
    private void 纯静默检测敏感类(LoadPackageParam lpparam) {
         log2(" 🕵️‍♂️ [开始检测] 正在静默扫描敏感安全类，判定环境安全性...", true);

        // 待检测的类名清单
        String[] 敏感类列表 = {
                "com.xunmeng.basiccomponent.titan.api.TitanApiCall",
                "com.xunmeng.basiccomponent.titan.api.TitanApiCall$a",
                "e.t.y.y1.n.j"
        };

        for (String 类名 : 敏感类列表) {
            try {
                // 纯粹加载类，不执行任何 Hook 动作
                Class<?> 探测结果 = XposedHelpers.findClass(类名, lpparam.classLoader);
                if (探测结果 != null) {
                    log2(" 🟢 [存在] 发现敏感类: " + 类名, true);
                }
            } catch (Throwable 忽略) {
                // 绝不抛出异常，绝不留痕，检测不到就默默过去
                log2(" 🔴 [不存在] 敏感类没有在此进程加载: " + 类名);
            }
        }
        log2(" 🕵️‍♂️ [检测结束] 敏感类扫描完毕。", true);
    }




    /**
     * 实时从 url.txt 读取配置
     * 返回数组: [0] = 完整网址, [1] = 提取出的商品ID
     */
    private static String[] 读取动态配置_带ID(String 包名, String 分身名) {
        String 文件路径 = "/storage/emulated/" + 分身名 + "/Android/data/" + 包名 + "/files/url.txt";
        java.io.File 配置文件 = new java.io.File(文件路径);

        if (!配置文件.exists()) {
            log2(" ⚠️ 动态配置文件不存在: " + 文件路径);
            return null;
        }

        java.io.BufferedReader 读入器 = null;
        try {
            java.io.FileInputStream 输入流 = new java.io.FileInputStream(配置文件);
            java.io.InputStreamReader 转换器 = new java.io.InputStreamReader(输入流, java.nio.charset.StandardCharsets.UTF_8);
            读入器 = new java.io.BufferedReader(转换器);

            String 网址内容 = 读入器.readLine();
            if (网址内容 != null && !网址内容.trim().isEmpty()) {
                网址内容 = 网址内容.trim(); // 去除首尾不可见字符

                // 使用文本处理工具提取商品ID
                String 提取的ID = 文本_取出中间文本(网址内容, "goods_id=", "&", null, true);
                if (提取的ID == null || 提取的ID.isEmpty()) {
                    提取的ID = 文本_取右边(网址内容, "goods_id=", null, true);
                }

                // 再次清洗提取出来的ID，防止里面包含别的参数或空格
                if (提取的ID != null) {
                    提取的ID = 提取的ID.trim();
                }

                if (提取的ID == null || 提取的ID.isEmpty()) {
                    log2(" ⚠️ 未能在网址中提取到有效的商品ID");
                    return null;
                }

                return new String[]{ 网址内容, 提取的ID };
            }
        } catch (Exception e) {
            log2(" ❌ 读取 url.txt 异常: " + e.getMessage());
        } finally {
            try { if (读入器 != null) 读入器.close(); } catch (Exception ignored) {}
        }
        return null;
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




