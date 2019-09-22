package blog.service.impl;

import blog.service.UserService;
import blog.dao.UserVoMapper;
import blog.exception.TipException;
import blog.model.Vo.UserVo;
import blog.model.Vo.UserVoExample;
import blog.utils.TaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserServiceImpl implements UserService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    @Resource
    private UserVoMapper userDao;

    @Override
    public Integer insertUser(UserVo userVo)
    {
        Integer uid = null;
        if (StringUtils.isNotBlank(userVo.getUsername()) && StringUtils.isNotBlank(userVo.getEmail()))
        {
            String encodePwd = TaleUtils.MD5encode(userVo.getUsername() + userVo.getPassword());
            userVo.setPassword(encodePwd);
            userDao.insertSelective(userVo);
        }
        return userVo.getUid();
    }

    @Override
    public UserVo queryUserById(Integer uid)
    {
        UserVo userVo = null;
        if (uid != null) userVo = userDao.selectByPrimaryKey(uid);
        return userVo;
    }

    @Override
    public UserVo login(String userName, String password)
    {
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) throw new TipException("用户名和密码不能为空");
        UserVoExample example = new UserVoExample();
        UserVoExample.Criteria criteria = example.createCriteria();
        criteria.andUsernameEqualTo(userName);
        long count = userDao.countByExample(example);

        if (count < 1) throw new TipException("不存在该用户");
        String pwd = TaleUtils.MD5encode(userName + password);
        criteria.andPasswordEqualTo(pwd);
        List<UserVo> userVos = userDao.selectByExample(example);
        if (userVos.size() != 1) throw new TipException("用户名或密码不正确");
        return userVos.get(0);
    }

    @Override
    public void updateByUid(UserVo userVo)
    {
        if (userVo == null || userVo.getUid() == null) throw new TipException("userVo is null");
        int i = userDao.updateByPrimaryKeySelective(userVo);
        if (i != 1) throw new TipException("update user by uid and return is not one");
    }
}
