package com.breech.extremity.web.api.user;

import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.core.service.redis.impl.RedisServiceImpl;
import com.breech.extremity.dto.UserDTO;
import com.breech.extremity.dto.UserInfoDTO;
import com.breech.extremity.model.User;
import com.breech.extremity.service.UserService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private String fileDownloadUrl;
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Resource
    private UserService userService;
    @GetMapping("/email")
    public GlobalResult<User> findUserDTOByEmail(@RequestParam("email") String email) {
        User user=userService.findByAccount(email);
        return GlobalResultGenerator.genSuccessResult(user);
    }


    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "没有选择文件");
        }

        // 获取文件名
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件名无效");
        }

        // 创建存储文件的目录（如果没有的话）
        File uploadDir = new File(fileDownloadUrl);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        // 设置文件保存路径
        File serverFile = new File(uploadDir, fileName);
        logger.warn(String.valueOf(serverFile));
        try {
            UserInfoDTO userInfoDTO = new UserInfoDTO();

            file.transferTo(serverFile);
            return ResponseEntity.ok("头像上传成功");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败", e);
        }
    }
}
