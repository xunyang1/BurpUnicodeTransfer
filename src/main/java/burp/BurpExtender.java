package burp;

import javax.print.attribute.standard.MediaSize;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BurpExtender implements IBurpExtender, IHttpListener{
    public static String NAME = "unicodeTransfer";

    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;

    private PrintWriter stdout;
    private PrintWriter stderr;

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;
        this.helpers = callbacks.getHelpers();

        this.stdout = new PrintWriter(callbacks.getStdout(), true);
        this.stderr = new PrintWriter(callbacks.getStderr(), true);

        callbacks.setExtensionName(NAME);
        callbacks.registerHttpListener(this);

        this.stdout.println(basicInformationOutput());
    }

    /**
     * 基本信息输出
     */
    private static String basicInformationOutput() {
        String str1 = "===================================\n";
        String str2 = String.format("%s 加载成功\n", NAME);
        String str3 = "===================================\n";
        String detail = str1 + str2 + str3;
        return detail;
    }

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {

        //只检测proxy和repeater模块的数据包
        if (toolFlag == 4 || toolFlag == 64){
            //判断是否为响应包
            if (!messageIsRequest){
                byte[] response = messageInfo.getResponse();
                IResponseInfo analyzedResponse = this.helpers.analyzeResponse(response);

                //正则提取body中的unicode
                int bodyOffset = analyzedResponse.getBodyOffset();
                String responseBody = new String(response).substring(bodyOffset);

                List<String> matchList = new ArrayList<>();

                Pattern compile = Pattern.compile("(\\\\u[\\w]{4})+");
                Matcher matcher = compile.matcher(responseBody);
                while (matcher.find()){
                    matchList.add(matcher.group(0));
                }
                //如果没匹配到则直接退出
                if (matchList.size() == 0)
                    return;

                //头部如果存在iso8859-1，更换为utf-8
                List<String> newHeaders = new ArrayList<>();
                List<String> headers = analyzedResponse.getHeaders();
                for (String header : headers) {
                    if (header.startsWith("Content-Type:") && header.contains("iso8859-1"))
                        newHeaders.add(header.replace("iso8859-1", "utf-8"));
                    else
                        newHeaders.add(header);
                }

                //开始解码并替换
                for (String unicode : matchList) {
                    String strForUnicode = unicodeToString(unicode);
                    responseBody = responseBody.replace(unicode, strForUnicode);
                }

                //重新组合响应
                byte[] newResponse = this.helpers.buildHttpMessage(newHeaders, responseBody.getBytes());
                messageInfo.setResponse(newResponse);
            }
        }
    }

    private static String unicodeToString(String str) {
        Pattern pattern = Pattern.compile("(\\\\u(\\w{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            // 将unicode后四位16进制数转换为十进制然后再转换为char中文字符
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }
}
