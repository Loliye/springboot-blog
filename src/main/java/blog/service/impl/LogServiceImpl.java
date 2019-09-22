package blog.service.impl;

import blog.constant.WebConst;
import blog.dao.LogVoMapper;
import blog.service.LogService;
import blog.utils.DateKit;
import blog.model.Vo.LogVo;
import blog.model.Vo.LogVoExample;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LogServiceImpl implements LogService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LogServiceImpl.class);

    @Resource
    private LogVoMapper logDao;

    @Override
    public void insertLog(LogVo logVo)
    {
        logDao.insert(logVo);
    }

    @Override
    public void insertLog(String action, String data, String ip, Integer authorId)
    {
        LogVo logVo = new LogVo();
        logVo.setAuthorId(authorId);
        logVo.setData(data);
        logVo.setIp(ip);
        logVo.setAction(action);
        logVo.setCreated(DateKit.getCurrentUnixTime());
        logDao.insert(logVo);
    }

    /**
     * 分页
     *
     * @param page  页数
     * @param limit 最少
     * @return
     */
    @Override
    public List<LogVo> getLogs(int page, int limit)
    {
        LOGGER.debug("Entry getLogs method:page={},limit={}", page, limit);
        if (page <= 0)
            page = 1;
        if (limit < 1 || limit > WebConst.MAX_POSTS)
            limit = 10;
        LogVoExample logVoExample = new LogVoExample();
        logVoExample.setOrderByClause("id desc");
        PageHelper.startPage((page - 1) * limit, limit);
        List<LogVo> logVos = logDao.selectByExample(logVoExample);

        LOGGER.debug("Exit getLogs method.");
        return logVos;
    }
}
