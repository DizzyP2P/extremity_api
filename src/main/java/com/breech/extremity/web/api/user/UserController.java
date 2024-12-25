package com.breech.extremity.web.api.user;

import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.core.service.redis.impl.RedisServiceImpl;
import com.breech.extremity.dto.UserDTO;
import com.breech.extremity.dto.UserInfoDTO;
import com.breech.extremity.model.User;
import com.breech.extremity.service.UserService;
import com.breech.extremity.util.UserUtils;
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

    @Value("${resource.image-download-url}")
    private String fileDownloadUrl;
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Resource
    private UserService userService;
    @GetMapping("/email")
    public GlobalResult<User> findUserDTOByEmail(@RequestParam("email") String email) {
        User user=userService.getUserByEmail(email);
        return GlobalResultGenerator.genSuccessResult(user);
    }


    @PostMapping("/upload-avatar")
    public GlobalResult<String> uploadAvatar(@RequestParam(value = "file", required = false) MultipartFile file,
                                               @RequestParam("account") String account,
                                               @RequestParam("name") String name,
                                               @RequestParam("sex") String sex,
                                               @RequestParam("phone") String phone,
                                               @RequestParam("email") String email) {
        User currentUser = UserUtils.getCurrentUserByToken();

        try {
            if (file == null || file.isEmpty()) {

                User user = new User();
                if(account!=null){
                    user.setAccount(account);
                }
                if(name!=null){
                    user.setRealName(name);
                }
                if(phone!=null){
                    user.setPhone(phone);
                }
                if(sex!=null){
                    user.setSex(sex);
                }

                user.setEmail(email);
                userService.updateUser(user);
                return GlobalResultGenerator.genSuccessResult("ok");
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
            User existingUser = userService.getUserByEmail(email);  // 获取用户信息

            String oldAvatarUrl = existingUser.getAvatarUrl();  // 获取旧头像文件名

            // 如果用户有旧头像文件，则删除它
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                File oldFile = new File(uploadDir, oldAvatarUrl);
                if (oldFile.exists()) {
                    boolean deleted = oldFile.delete();
                    if (!deleted) {
                        logger.error("删除旧头像文件失败: " + oldAvatarUrl);
                    }
                }
            }
            // 设置文件保存路径
            File serverFile = new File(uploadDir, fileName);
            try {
                User user = new User();
                user.setAvatarUrl(fileName);
                if(account!=null){
                    user.setAccount(account);
                }
                if(name!=null){
                    user.setRealName(name);
                }
                if(phone!=null){
                    user.setPhone(phone);
                }
                if(sex!=null){
                    user.setSex(sex);
                }
                user.setEmail(email);
                file.transferTo(serverFile);
                userService.updateUser(user);
                return GlobalResultGenerator.genSuccessResult("ok");
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败", e);
            }
        }catch (Exception e) {
            e.printStackTrace();
            return GlobalResultGenerator.genErrorResult("更新信息失败");
        }
    }
}
