package blog.service;

import blog.model.Bo.CommentBo;
import blog.model.Vo.CommentVo;
import blog.model.Vo.CommentVoExample;
import com.github.pagehelper.PageInfo;

/**
 * 评论信息处理
 */
public interface CommentService
{
    void insertComment(CommentVo commentVo);

    PageInfo<CommentBo> getComments(Integer cid, int page, int limit);

    PageInfo<CommentVo> getCommentWithPage(CommentVoExample commentVoExample, int page, int limit);

    CommentVo getCommentById(Integer coid);

    void delete(Integer coid, Integer cid);

    void update(CommentVo commentVo);
}
