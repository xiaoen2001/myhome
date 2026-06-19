package com.share.util;

import com.share.common.BusinessException;
import com.share.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.UUID;

@Component
public class FileUpload {
    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${server.base-url:http://localhost:8080}")
    private String baseUrl;

    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + suffix;
        String subDir = "carousels";
        File dir = new File(uploadDir, subDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(500, "无法创建上传目录");
        }
        File dest = new File(dir, newFileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败");
        }
        String imageUrl = baseUrl + "/uploads/carousels/" + newFileName;
        return Result.success(imageUrl);
    }

    /**
     * 将 base64 字符串保存为图片文件
     * @param base64Data 格式如 "data:image/jpeg;base64,xxxxx"
     * @param subDir 子目录，例如 "notes"
     * @return 可访问的图片URL
     */
    public String saveBase64Image(String base64Data, String subDir) {
        if (base64Data == null || !base64Data.startsWith("data:image/")) {
            throw new BusinessException(400, "图片格式不正确");
        }
        // 提取真实的 base64 数据（去掉前缀）
        String[] parts = base64Data.split(",");
        if (parts.length < 2) {
            throw new BusinessException(400, "图片数据无效");
        }
        String base64Image = parts[1];
        // 使用 java.util.Base64 解码
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);

        // 确定文件扩展名
        String extension;
        if (base64Data.contains("image/jpeg")) {
            extension = ".jpg";
        } else if (base64Data.contains("image/png")) {
            extension = ".png";
        } else {
            extension = ".jpg"; // 默认
        }
        String fileName = UUID.randomUUID() + extension;
        File dir = new File(uploadDir, subDir);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException(500, "创建目录失败");
        }
        File dest = new File(dir, fileName);
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(imageBytes);
        } catch (IOException e) {
            throw new BusinessException(500, "保存图片失败: " + e.getMessage());
        }
        return baseUrl + "/uploads/" + subDir + "/" + fileName;
    }
}
