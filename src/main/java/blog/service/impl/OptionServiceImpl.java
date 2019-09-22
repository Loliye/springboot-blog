package blog.service.impl;

import blog.dao.OptionVoMapper;
import blog.model.Vo.OptionVo;
import blog.model.Vo.OptionVoExample;
import blog.service.OptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class OptionServiceImpl implements OptionService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(OptionServiceImpl.class);

    @Resource
    private OptionVoMapper optionDao;

    @Override
    public void insertOption(OptionVo optionVo)
    {
        LOGGER.debug("Entry insertOption method:option={}", optionVo);
        optionDao.insertSelective(optionVo);
        LOGGER.debug("Exit insertOption method.");
    }

    @Override
    public void insertOption(String name, String value)
    {
        LOGGER.debug("Entry insertOption method:name={},value={}", name, value);
        OptionVo optionVo = new OptionVo();
        optionVo.setName(name);
        optionVo.setValue(value);
        if (optionDao.selectByExample(new OptionVoExample()).size() == 0) optionDao.insertSelective(optionVo);
        else optionDao.updateByPrimaryKeySelective(optionVo);
        LOGGER.debug("Exit insertOption method.");

    }

    @Override
    public void saveOptions(Map<String, String> options)
    {
        if (null != options && !options.isEmpty())
        {
            options.forEach(this::insertOption);
        }
    }

    @Override
    public List<OptionVo> getOptions()
    {
        return optionDao.selectByExample(new OptionVoExample());
    }
}
