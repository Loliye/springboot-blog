package blog.service.impl;

import blog.service.AttachService;
import blog.dao.AttachVoMapper;
import blog.model.Vo.AttachVo;
import blog.model.Vo.AttachVoExample;
import blog.utils.DateKit;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class AttachServiceImpl implements AttachService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachServiceImpl.class);
    @Resource
    private AttachVoMapper attachDao;

    @Override
    public PageInfo<AttachVo> getAttachs(Integer page, Integer limit)
    {
        PageHelper.startPage(page, limit);
        AttachVoExample attachVoExample = new AttachVoExample();
        attachVoExample.setOrderByClause("id desc");
        List<AttachVo> attachVos = attachDao.selectByExample(attachVoExample);
        return new PageInfo<>(attachVos);
    }

    @Override
    public void save(String fname, String fkey, String ftype, Integer author)
    {
        AttachVo attachVo = new AttachVo();
        attachVo.setFname(fname);
        attachVo.setAuthorId(author);
        attachVo.setCreated(DateKit.getCurrentUnixTime());
        attachVo.setFkey(fkey);
        attachVo.setFtype(ftype);
        attachDao.insert(attachVo);
    }

    @Override
    public AttachVo selectById(Integer id)
    {
        if (id != null) return attachDao.selectByPrimaryKey(id);
        return null;
    }

    @Override
    public void deleteById(Integer id)
    {
        if (id != null) attachDao.deleteByPrimaryKey(id);
    }
}
