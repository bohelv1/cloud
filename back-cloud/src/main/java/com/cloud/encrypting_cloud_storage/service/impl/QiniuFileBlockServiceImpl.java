package com.cloud.encrypting_cloud_storage.service.impl;

import com.cloud.encrypting_cloud_storage.models.po.FileBlockPo;
import com.qiniu.http.Response;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;

/**
 * @author leon
 * @Description: 七牛云上传服务
 * @date 2022年04月26日 13:57
 */
@Service("QiniuUploadService")
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class QiniuFileBlockServiceImpl extends BlockServiceImpl implements InitializingBean {
    private final Auth auth;
    private final UploadManager uploadManager;
    @Value("${qiniu.bucket}")
    private String bucket;

    private StringMap putPolicy;


    @Autowired
    public QiniuFileBlockServiceImpl(Auth auth, UploadManager uploadManager) {
        this.auth = auth;
        this.uploadManager = uploadManager;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.putPolicy = new StringMap();
        // 设置返回体的格式
        putPolicy.put("returnBody", "{\"key\":\"$(key)\",\"hash\":\"$(etag)\",\"bucket\":\"$(bucket)\",\"width\":$(imageInfo.width), \"height\":${imageInfo.height}}");
    }

    /**
     * 获取上传凭证
     *
     * @return 上传凭证
     */
    public String getUploadToken() {
        return this.auth.uploadToken(bucket, null, 3600, putPolicy);
    }


    @Override
    public boolean uploadBlock(FileBlockPo fileBlockPo) throws Exception {
        log.debug("-------------------"+fileBlockPo.getFingerprint());
        Response response = uploadManager.put(fileBlockPo.getData(), fileBlockPo.getFingerprint(), getUploadToken());
        int retry = 0;
        while (response.needRetry() && retry < 3) {
            response = uploadManager.put(fileBlockPo.getData(), fileBlockPo.getFingerprint(), getUploadToken());
            retry++;
        }
        return true;
    }

    @Override
    public byte[] downloadBlock(FileBlockPo fileBlockPo) throws Exception {

        return super.downloadBlock(fileBlockPo);
    }
}
