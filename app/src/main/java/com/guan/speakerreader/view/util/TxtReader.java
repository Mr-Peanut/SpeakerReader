package com.guan.speakerreader.view.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class TxtReader {

    /*
     * ��δ����ǽ�ȡ��ָ��λ��marked��limit���ȵ��ַ������˴���Щ���⣬�ַ�������󳤶��Ǹ�int�͵ģ����������Ϊint,������������Ǹ�long�ͣ������Ľ�
     */
    public static String readerFromText(String filePath, long marked, int limit) throws Exception {
        if (marked < 0)
            return null;
        int buffLength = 2 * 1024;
        File target = new File(filePath);
        FileInputStream inputStream = new FileInputStream(target);
        InputStreamReader bufferedReader = new InputStreamReader(inputStream, getCodeType(target));
        char[] buff = new char[buffLength];
        int times = 0;
        int left = limit;
        StringBuffer stringBuffer = new StringBuffer();
        bufferedReader.skip(marked);
        while (bufferedReader.read(buff) != -1 && left > 0) {
            if (left < buffLength) {
                stringBuffer.append(buff, 0, left);
                break;
            }
            stringBuffer.append(buff);
            times++;
            left = limit - times * buffLength;
        }
        String result = stringBuffer.toString();
        bufferedReader.close();
        return result;
    }

    public static String getCodeType(File file) throws IOException {
        BufferedInputStream bin = new BufferedInputStream(new FileInputStream(file));
        int p = (bin.read()) << 8 + bin.read();
        String code = null;
        switch (p) {
            case 0xefbb:
                code = "UTF-8";
                break;
            case 0xfffe:
                code = "Unicode";
                break;
            case 0xfeff:
                code = "UTF-16BE";
                break;

            default:
                code = "GBK";
                break;
        }
        bin.close();
        return code;
    }

    public static String getCodeType(String filePath) throws IOException {
        return getCodeType(new File(filePath));
    }

    public static int getTotalWords(String filePath) {
        InputStreamReader reader = null;
        FileInputStream inputStream = null;
        int bufferLength = 1024 * 2;
        int result = 0;
        int readTimes = -1;
        try {
            inputStream = new FileInputStream(new File(filePath));
            reader = new InputStreamReader(inputStream, getCodeType(filePath));
            char[] buffer = new char[bufferLength];
            int temp = 0;
            int marked = 0;
            while ((temp = reader.read(buffer)) != -1) {
                readTimes++;
                marked = temp;
            }
            result = readTimes * bufferLength + marked;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


}
