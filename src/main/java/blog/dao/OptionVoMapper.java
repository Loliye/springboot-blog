package blog.dao;

import java.util.List;

import blog.model.Vo.OptionVo;
import blog.model.Vo.OptionVoExample;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
public interface OptionVoMapper
{
    long countByExample(OptionVoExample example);

    int deleteByExample(OptionVoExample example);

    int deleteByPrimaryKey(String name);

    int insert(OptionVo record);

    int insertSelective(OptionVo record);

    List<OptionVo> selectByExample(OptionVoExample example);

    OptionVo selectByPrimaryKey(String name);

    int updateByExampleSelective(@Param("record") OptionVo record, @Param("example") OptionVoExample example);

    int updateByExample(@Param("record") OptionVo record, @Param("example") OptionVoExample example);

    int updateByPrimaryKeySelective(OptionVo record);

    int updateByPrimaryKey(OptionVo record);

    /**
     * 批量保存
     *
     * @param optionVos list
     * @return 保存的个数
     */
    int insertOptions(List<OptionVo> optionVos);
}