/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.util.AutoUpdater
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.http.HttpGetter;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class AutoUpdater implements Runnable {
    private static final Base64.Decoder decoder = Base64.getDecoder();
    private static String relV = null;

    @Override
    public void run() {
        update();
    }

    /**
     * 判断插件是否有更新
     */
    public boolean isUpdate() {
        try {
            return !MultiCore.getPlugin().getVersion().endsWith(relV);
        } catch (Exception ignore) {
        }
        return false;
    }

    /**
     * 发送更新消息
     */
    public void infoUpdate() {
        update();
        if (isUpdate()) {
            MultiCore.info(I18n.getTransString("plugin_new_version_info", MultiCore.getPlugin().getVersion(), relV));
        }
    }

    /**
     * 周期性的更新检查
     */
    private void update() {
        try {
            URL url = new URL("https://api.github.com/repos/CaaMoe/MultiLogin/contents/gradle.properties?ref=master");
            JsonObject jo = (JsonObject) new JsonParser().parse(HttpGetter.httpGet(url));
            String pat = jo.get("content").getAsString();
            pat = pat.substring(0, pat.length() - 1);
            String v = new String(decoder.decode(pat), StandardCharsets.UTF_8);
            relV = v.split("=")[1].trim();
        } catch (Exception ignore) {
        }
    }
}
