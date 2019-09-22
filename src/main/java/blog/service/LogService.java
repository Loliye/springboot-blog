package blog.service;

import blog.model.Vo.LogVo;

import java.util.List;

public interface LogService
{
    void insertLog(LogVo logVo);

    void insertLog(String action, String data, String ip, Integer authorId);

    List<LogVo> getLogs(int page, int limit);
}
