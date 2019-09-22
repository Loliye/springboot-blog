package blog.service;

import blog.model.Vo.UserVo;


public interface UserService
{
    Integer insertUser(UserVo userVo);

    UserVo queryUserById(Integer uid);

    UserVo login(String userName, String password);

    void updateByUid(UserVo userVo);
}
