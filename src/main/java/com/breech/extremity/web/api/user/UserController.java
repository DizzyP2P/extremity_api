package com.breech.extremity.web.api.user;

import com.breech.extremity.core.response.GlobalResult;
import com.breech.extremity.core.response.GlobalResultGenerator;
import com.breech.extremity.core.service.redis.impl.RedisServiceImpl;
import com.breech.extremity.dto.UserDTO;
import com.breech.extremity.dto.UserInfoDTO;
import com.breech.extremity.mapper.UserRoleMapper;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Value("${resource.image-download-url}")
    private String fileDownloadUrl;
    private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

    @Resource
    private UserService userService;
    @Resource
    private UserRoleMapper userRoleMapper;
    @GetMapping("/email")
    public GlobalResult<User> findUserDTOByEmail(@RequestParam("email") String email) {
        User user=userService.getUserByEmail(email);
        return GlobalResultGenerator.genSuccessResult(user);
    }

    @GetMapping("/id")
    public GlobalResult<UserInfoDTO> findUserDTOById(@RequestParam("id") long id){
        UserInfoDTO user = userService.findUserInfo(id);
        return GlobalResultGenerator.genSuccessResult(user);
    }
    @GetMapping("/role")
    public GlobalResult<Integer> findUserActivated(@RequestParam("email") String email){
        User user=userService.getUserByEmail(email);
        int status = userRoleMapper.selectByPrimaryKey(user.getIdUser()).getActivated();
        return GlobalResultGenerator.genSuccessResult(status);
    }
    @PostMapping("/upload-avatar")
    public GlobalResult<String> uploadAvatar(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("account") String account,
            @RequestParam("name") String name,
            @RequestParam("sex") String sex,
            @RequestParam("phone") String phone,
            @RequestParam("email") String email) {

        try {
            // 获取当前用户
            User existingUser = UserUtils.getCurrentUserByToken();

            // 更新用户信息
            User user = new User();
            if (account != null) user.setAccount(account);
            if (name != null) user.setRealName(name);
            if (phone != null) user.setPhone(phone);
            if (sex != null) user.setSex(sex);
            user.setEmail(email);

            // 如果没有上传文件，仅更新用户信息
            if (file == null || file.isEmpty()) {
                userService.updateUser(user);
                return GlobalResultGenerator.genSuccessResult("用户信息更新成功，无头像上传");
            }

            // 验证文件名有效性
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件名无效");
            }

            // 创建上传目录
            File uploadDir = new File(fileDownloadUrl);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "无法创建上传目录");
            }

            // 删除旧头像文件
            String oldAvatarUrl = existingUser.getAvatarUrl();
            if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
                File oldFile = new File(uploadDir, oldAvatarUrl);
                if (oldFile.exists() && !oldFile.delete()) {
                    logger.error("删除旧头像文件失败: " + oldAvatarUrl);
                }
            }

            // 保存新头像文件
            String newFilename = UUID.randomUUID() + "_" + originalFilename;
            File newFile = new File(uploadDir, newFilename);
            file.transferTo(newFile);

            // 更新用户头像 URL 并保存到数据库
            user.setAvatarUrl(newFilename);
            userService.updateUser(user);

            return GlobalResultGenerator.genSuccessResult("用户信息及头像更新成功");
        } catch (IOException e) {
            logger.error("文件保存失败", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "文件保存失败", e);
        } catch (Exception e) {
            logger.error("更新信息失败", e);
            return GlobalResultGenerator.genErrorResult("更新信息失败");
        }
    }
}
