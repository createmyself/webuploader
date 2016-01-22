package org.xxz.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * �ϴ�������
 * @author xxzkid
 *
 */
public final class UploaderUtil {

    private static final Log log = LogFactory.getLog(UploaderUtil.class);

    /**
     * springmvc �ļ��ϴ�
     * @param request 
     * @param basePath ����·�� ��/��ͷ
     * @param formFileName ����file��nameֵ
     * @param dir �ϴ�Ŀ���ļ��� ��/��ͷ
     * @param allowedPattern ����ĸ�ʽ
     * @param fileSize �����ϴ��Ĵ�С ��λKB
     * @return 
     */
    public static UploadResult uploader(HttpServletRequest request, String basePath, String formFileName, String dir, List<String> allowedPattern, long fileSize) {
        String successCode = UploadResult.CODE_0;
        String successInfo = UploadResult.DESC_0;

        UploadResult result = new UploadResult();

        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;

        File fileDir = new File(basePath + dir);
        if (!fileDir.exists() && !fileDir.mkdirs()) {
            log.error("upload mkdirs failed.");
            throw new RuntimeException("upload mkdirs failed.");
        }

        /* �ļ�·������ */
        List<String> list = new ArrayList<String>();

        /* ҳ��ؼ����ļ��� */
        List<MultipartFile> multipartFileList = multipartRequest.getFiles(formFileName);
        if (multipartFileList.isEmpty()) {
            result.setCode(successCode);
            // ���һ����·��
            list.add("");
            result.setUrls(list);
            result.setDesc(successInfo);
            return result;
        }

        if (allowedPattern == null || allowedPattern.size() == 0) {
            allowedPattern = new ArrayList<String>();
            allowedPattern.add(".gif");
            allowedPattern.add(".jpg");
            allowedPattern.add(".jpeg");
            allowedPattern.add(".png");
        }

        if (fileSize <= 0) {
            fileSize = 1 * 1024 * 1024;
        } else {
            fileSize *= 1024;
        }

        try {
            for (MultipartFile multipartFile : multipartFileList) {
                String originalFilename = multipartFile.getOriginalFilename();
                if ("".equals(originalFilename)) {
                    result.setCode(successCode);
                    // ���һ����·��
                    list.add(UploadResult.EMPTY_URL);
                    result.setUrls(list);
                    result.setDesc(successInfo);
                    return result;
                }
                /* ��ȡ�ļ��ĺ�׺ */
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                if (!allowedPattern.contains(suffix)) {
                    result.setCode(UploadResult.CODE_1);
                    result.setUrls(null);
                    result.setDesc(UploadResult.DESC_1);
                    return result;
                }
                if (multipartFile.getSize() > fileSize) {
                    result.setCode("2");
                    result.setUrls(null);
                    result.setDesc(String.format(UploadResult.DESC_2, fileSize));
                    return result;
                }
                /* ʹ��ʱ��������ļ����� */
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
                String nowTime = sdf.format(new Date());
                String filename = nowTime + suffix;// �����ļ�����

                /** ƴ���������ļ�����·�����ļ� **/
                String fullFilename = fileDir + "/" + filename;
                File file = new File(fullFilename);
                multipartFile.transferTo(file);
                list.add(dir + "/" + filename);
            }
            result.setCode(successCode);
            result.setUrls(list);
            result.setDesc(successInfo);
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info("��������Ϊ��" + result.toString());
        return result;
    }

    /**
     * �ϴ��󷵻ؽ��
     * @author xxzkid
     */
    public static class UploadResult {
        
        /** @see UploadResult#DESC_0 */
        public static final String CODE_0 = "0";
        /** @see UploadResult#DESC_1 */
        public static final String CODE_1 = "1";
        /** @see UploadResult#DESC_2 */
        public static final String CODE_2 = "2";
        
        /** �ɹ� success */
        public static final String DESC_0 = "�ɹ�";
        /** �ϴ��ļ���ʽ����ȷ upload pattern not true */
        public static final String DESC_1 = "�ϴ��ļ���ʽ����ȷ";
        /** �ϴ��ļ���С������%sKB upload file size gt %s KB */
        public static final String DESC_2 = "�ϴ��ļ���С������%sKB";

        /** ��·�� */
        public static final String EMPTY_URL = "";
        
        private String code; // ������
        private String desc; // ����
        List<String> urls = new ArrayList<String>(); // �ϴ��ļ�·���ļ���

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setUrls(List<String> urls) {
            this.urls = urls;
        }

        public List<String> getUrls() {
            return urls;
        }

        @Override
        public String toString() {
            return "UploadResult [code=" + code + ", desc=" + desc + ", urls=" + urls + "]";
        }
    }

}
